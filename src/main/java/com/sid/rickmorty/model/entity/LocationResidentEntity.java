package com.sid.rickmorty.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("location_residents")
public record LocationResidentEntity(
        @Id Long id,
        Integer locationId,
        Integer characterId
) {
    public static LocationResidentEntity of(Integer locationId, Integer characterId) {
        return new LocationResidentEntity(null, locationId, characterId);
    }
}
