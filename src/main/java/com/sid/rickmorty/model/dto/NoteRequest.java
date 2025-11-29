package com.sid.rickmorty.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming payload for note creation – stored separately from entity
 * to highlight validation responsibility.
 */
public record NoteRequest(
        @NotBlank(message = "note text is required") String note,
        String author
) {
}
