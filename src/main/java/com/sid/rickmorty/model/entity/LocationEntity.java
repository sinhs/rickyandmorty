package com.sid.rickmorty.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * SQLite representation of a location fetched from the public API.
 */
@Table("locations")
public record LocationEntity(
        @Id Integer id,
        String name,
        String type,
        String dimension
) {
}
