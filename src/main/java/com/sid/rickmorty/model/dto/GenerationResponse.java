package com.sid.rickmorty.model.dto;

import com.sid.rickmorty.evaluation.EvaluationResult;

/**
 * Returns both the generated text and evaluation metadata to help demo the quality scaffold.
 */
public record GenerationResponse(
        int subjectId,
        String subjectType,
        String prompt,
        String output,
        EvaluationResult evaluation
) {
}
