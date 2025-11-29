package com.sid.rickmorty.evaluation;

import java.time.Instant;
import java.util.Map;

/**
 * Stores the heuristic scores so we can persist them and reason about improvements later.
 */
public record EvaluationResult(
        Map<EvaluationMetric, Double> scores,
        String evaluatorNotes,
        Instant evaluatedAt
) {
    public static EvaluationResult empty() {
        return new EvaluationResult(Map.of(), "", Instant.now());
    }
}
