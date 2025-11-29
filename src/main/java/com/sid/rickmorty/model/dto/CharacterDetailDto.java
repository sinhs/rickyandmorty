package com.sid.rickmorty.model.dto;

import java.util.List;

/**
 * Exposes the character profile along with associated notes so product and interviewer can discuss persistence decisions.
 */
public record CharacterDetailDto(
        int id,
        String name,
        String status,
        String species,
        String gender,
        String origin,
        String currentLocation,
        String image,
        List<CharacterNoteDto> notes
) {
}
