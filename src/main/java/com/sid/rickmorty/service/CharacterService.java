package com.sid.rickmorty.service;

import com.sid.rickmorty.model.dto.CharacterDetailDto;
import com.sid.rickmorty.model.dto.CharacterNoteDto;
import com.sid.rickmorty.model.dto.NoteRequest;
import com.sid.rickmorty.model.entity.CharacterNoteEntity;
import com.sid.rickmorty.repository.CharacterNoteRepository;
import com.sid.rickmorty.repository.CharacterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final CharacterNoteRepository noteRepository;

    public CharacterService(CharacterRepository characterRepository, CharacterNoteRepository noteRepository) {
        this.characterRepository = characterRepository;
        this.noteRepository = noteRepository;
    }

    public Optional<CharacterDetailDto> getCharacter(int id) {
        return characterRepository.findById(id).map(character -> new CharacterDetailDto(
                character.id(),
                character.name(),
                character.status(),
                character.species(),
                character.gender(),
                character.origin(),
                character.location(),
                character.image(),
                noteRepository.findByCharacterId(id).stream()
                        .map(this::toDto)
                        .toList()
        ));
    }

    @Transactional
    public CharacterNoteDto addNote(int characterId, NoteRequest request) {
        CharacterNoteEntity entity = CharacterNoteEntity.of(characterId, request.note(), request.author(), Instant.now().toString());
        return toDto(noteRepository.save(entity));
    }

    private CharacterNoteDto toDto(CharacterNoteEntity entity) {
        return new CharacterNoteDto(entity.id(), entity.characterId(), entity.note(), entity.author(), parseInstant(entity.createdAt()));
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
        }
        try {
            long epochMillis = Long.parseLong(value.trim());
            return Instant.ofEpochMilli(epochMillis);
        } catch (NumberFormatException ex) {
            return Instant.EPOCH;
        }
    }
}
