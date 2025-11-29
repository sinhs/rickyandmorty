package com.sid.rickmorty.model.dto;

/**
 * Minimal resident info that accompanies a location response to keep payloads predictable.
 */
public record ResidentSummaryDto(
        int id,
        String name,
        String status,
        String species,
        String image
) {
}
