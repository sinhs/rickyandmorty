package com.sid.rickmorty.service;

import com.sid.rickmorty.client.RickAndMortyClient;
import com.sid.rickmorty.model.dto.LocationDto;
import com.sid.rickmorty.model.dto.ResidentSummaryDto;
import com.sid.rickmorty.repository.CharacterRepository;
import com.sid.rickmorty.repository.LocationRepository;
import com.sid.rickmorty.repository.LocationResidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final RickAndMortyClient client;
    private final LocationRepository locationRepository;
    private final CharacterRepository characterRepository;
    private final LocationResidentRepository residentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ReentrantLock refreshLock = new ReentrantLock();

    public LocationService(RickAndMortyClient client,
                           LocationRepository locationRepository,
                           CharacterRepository characterRepository,
                           LocationResidentRepository residentRepository,
                           JdbcTemplate jdbcTemplate) {
        this.client = client;
        this.locationRepository = locationRepository;
        this.characterRepository = characterRepository;
        this.residentRepository = residentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LocationDto> listLocations() {
        List<LocationDto> response = new ArrayList<>();
        locationRepository.findAll().forEach(location -> {
            List<ResidentSummaryDto> residents = residentRepository.findCharacterIdsByLocation(location.id())
                    .stream()
                    .map(characterRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(character -> new ResidentSummaryDto(
                            character.id(),
                            character.name(),
                            character.status(),
                            character.species(),
                            character.image()))
                    .toList();
            response.add(new LocationDto(location.id(), location.name(), location.type(), location.dimension(), residents));
        });
        return response;
    }

    @Transactional
    public void refreshFromSource() {
        if (!refreshLock.tryLock()) {
            log.info("refresh already in progress; skipping");
            return;
        }
        int page = 1;
        boolean hasNext = true;
        try {
            while (hasNext) {
                Mono<Map<String, Object>> responseMono = client.fetchLocationsPage(page);
                Map<String, Object> payload = responseMono.blockOptional().orElse(Map.of());
                List<Map<String, Object>> results = (List<Map<String, Object>>) payload.getOrDefault("results", List.of());
                persistLocations(results);
                Map<String, Object> info = (Map<String, Object>) payload.getOrDefault("info", Map.of());
                hasNext = info.getOrDefault("next", null) != null;
                page++;
            }
        } finally {
            refreshLock.unlock();
        }
    }

    private void persistLocations(List<Map<String, Object>> rawLocations) {
        rawLocations.forEach(rawLocation -> {
            Integer locationId = (Integer) rawLocation.get("id");
            if (locationId == null) {
                log.warn("Skipping location with null ID: {}", rawLocation);
                return;
            }
            upsertLocation(locationId,
                    (String) rawLocation.get("name"),
                    (String) rawLocation.get("type"),
                    (String) rawLocation.get("dimension"));
            // remove stale associations so refreshed data mirrors source
            residentRepository.deleteByLocationId(locationId);
            List<String> residentUrls = (List<String>) rawLocation.getOrDefault("residents", List.of());
            List<Integer> residentIds = residentUrls.stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> {
                        try {
                            int lastSlash = url.lastIndexOf('/');
                            if (lastSlash < 0 || lastSlash == url.length() - 1) {
                                log.warn("Invalid resident URL format: {}", url);
                                return null;
                            }
                            String idStr = url.substring(lastSlash + 1);
                            return Integer.valueOf(idStr);
                        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                            log.warn("Failed to parse resident ID from URL: {}", url, e);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .toList();
            if (!residentIds.isEmpty()) {
                ingestResidents(locationId, residentIds);
            }
        });
    }

    private void ingestResidents(Integer locationId, List<Integer> residentIds) {
        List<List<Integer>> batches = chunk(residentIds, 20);
        batches.forEach(batch -> {
            List<Map<String, Object>> characters = client.fetchCharactersBatch(batch).blockOptional().orElse(List.of());
            characters.forEach(this::upsertCharacter);
            batch.forEach(characterId -> upsertLocationResident(locationId, characterId));
        });
    }

    private void upsertLocation(Integer id, String name, String type, String dimension) {
        jdbcTemplate.update("""
                INSERT INTO locations(id, name, type, dimension)
                VALUES(?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    name=excluded.name,
                    type=excluded.type,
                    dimension=excluded.dimension
                """, id, name, type, dimension);
    }

    private void upsertCharacter(Map<String, Object> rawCharacter) {
        Map<String, Object> origin = (Map<String, Object>) rawCharacter.getOrDefault("origin", Map.of());
        Map<String, Object> location = (Map<String, Object>) rawCharacter.getOrDefault("location", Map.of());
        jdbcTemplate.update("""
                INSERT INTO characters(id, name, status, species, gender, origin, location, image)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    name=excluded.name,
                    status=excluded.status,
                    species=excluded.species,
                    gender=excluded.gender,
                    origin=excluded.origin,
                    location=excluded.location,
                    image=excluded.image
                """,
                rawCharacter.get("id"),
                rawCharacter.get("name"),
                rawCharacter.get("status"),
                rawCharacter.get("species"),
                rawCharacter.get("gender"),
                origin.getOrDefault("name", ""),
                location.getOrDefault("name", ""),
                rawCharacter.get("image"));
    }

    private void upsertLocationResident(Integer locationId, Integer characterId) {
        jdbcTemplate.update("""
                INSERT INTO location_residents(location_id, character_id)
                VALUES(?, ?)
                ON CONFLICT(location_id, character_id) DO NOTHING
                """, locationId, characterId);
    }

    private static List<List<Integer>> chunk(List<Integer> source, int size) {
        return source.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    List<List<Integer>> chunks = new ArrayList<>();
                    for (int i = 0; i < list.size(); i += size) {
                        chunks.add(list.subList(i, Math.min(list.size(), i + size)));
                    }
                    return chunks;
                }));
    }
}
