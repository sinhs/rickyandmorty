package com.sid.rickmorty.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Captures what the user wants the LLM to generate so we can score it later.
 */
public record GenerationRequest(
        @NotBlank String subjectType,
        @NotNull Integer subjectId,
        @NotBlank String instruction
) {
}
