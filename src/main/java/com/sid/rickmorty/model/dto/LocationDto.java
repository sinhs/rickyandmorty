package com.sid.rickmorty.model.dto;

import java.util.List;

/**
 * API-facing DTO representing a location with flattened resident summaries so the UI
 * can render list views without multiple round trips.
 */
public record LocationDto(
        int id,
        String name,
        String type,
        String dimension,
        List<ResidentSummaryDto> residents
) {
}
