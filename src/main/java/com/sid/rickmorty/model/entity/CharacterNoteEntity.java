package com.sid.rickmorty.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("character_notes")
public record CharacterNoteEntity(
        @Id Long id,
        Integer characterId,
        String note,
        String author,
        String createdAt
) {
    public static CharacterNoteEntity of(Integer characterId, String note, String author, String createdAtIso) {
        return new CharacterNoteEntity(null, characterId, note, author, createdAtIso);
    }
}
