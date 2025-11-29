package com.sid.rickmorty.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("characters")
public record CharacterEntity(
        @Id Integer id,
        String name,
        String status,
        String species,
        String gender,
        String origin,
        String location,
        String image
) {
}
