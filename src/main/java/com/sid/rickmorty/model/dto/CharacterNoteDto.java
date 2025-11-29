package com.sid.rickmorty.model.dto;

import java.time.Instant;

/**
 * Represents a persisted note so we can reason about authorship and evaluation timeline.
 */
public record CharacterNoteDto(
        long id,
        int characterId,
        String note,
        String author,
        Instant createdAt
) {
}
