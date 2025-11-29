package com.sid.rickmorty.repository;

import com.sid.rickmorty.model.entity.LocationResidentEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationResidentRepository extends CrudRepository<LocationResidentEntity, Long> {

    @Query("SELECT character_id FROM location_residents WHERE location_id = :locationId")
    List<Integer> findCharacterIdsByLocation(@Param("locationId") int locationId);

    @Modifying
    @Query("DELETE FROM location_residents WHERE location_id = :locationId")
    void deleteByLocationId(@Param("locationId") int locationId);
}
