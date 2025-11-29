package com.sid.rickmorty.evaluation;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w{4,}\\b");
    private static final Set<String> RICK_MORTY_KEYWORDS = Set.of(
            "rick", "morty", "dimension", "portal", "alien", "citadel", "galaxy", "universe"
    );

    public EvaluationResult evaluate(String context, String output) {
        Map<EvaluationMetric, Double> scores = new EnumMap<>(EvaluationMetric.class);
        scores.put(EvaluationMetric.FACTUALITY, scoreFactuality(context, output));
        scores.put(EvaluationMetric.CREATIVITY, scoreCreativity(output));
        scores.put(EvaluationMetric.COMPLETENESS, scoreCompleteness(context, output));
        return new EvaluationResult(scores, "heuristic v2", Instant.now());
    }

    private double scoreFactuality(String context, String output) {
        if (output == null || output.isBlank() || context == null || context.isBlank()) {
            return 0.0;
        }

        Set<String> contextWords = extractSignificantWords(context.toLowerCase());
        Set<String> outputWords = extractSignificantWords(output.toLowerCase());
        
        if (contextWords.isEmpty()) {
            return 1.0;
        }

        long matchingWords = outputWords.stream()
                .filter(contextWords::contains)
                .count();

        double factuality = (double) matchingWords / contextWords.size();
        return Math.round(Math.min(1.0, factuality) * 100.0) / 100.0;
    }

    private double scoreCreativity(String output) {
        if (output == null || output.isBlank()) {
            return 0.0;
        }

        String lowerOutput = output.toLowerCase();
        long keywordMatches = RICK_MORTY_KEYWORDS.stream()
                .filter(lowerOutput::contains)
                .count();

        long exclamations = output.chars().filter(ch -> ch == '!').count();
        long questionMarks = output.chars().filter(ch -> ch == '?').count();
        
        double keywordScore = Math.min(1.0, keywordMatches / 3.0);
        double punctuationScore = Math.min(1.0, (exclamations + questionMarks) / 5.0);
        double lengthScore = Math.min(1.0, output.length() / 200.0);
        
        double creativity = (keywordScore * 0.4 + punctuationScore * 0.3 + lengthScore * 0.3);
        return Math.round(creativity * 100.0) / 100.0;
    }

    private double scoreCompleteness(String context, String output) {
        if (context == null || context.isBlank()) {
            return 1.0;
        }
        if (output == null || output.isBlank()) {
            return 0.0;
        }

        Set<String> contextKeyTerms = extractSignificantWords(context.toLowerCase());
        Set<String> outputTerms = extractSignificantWords(output.toLowerCase());
        
        if (contextKeyTerms.isEmpty()) {
            return 1.0;
        }

        long coveredTerms = contextKeyTerms.stream()
                .filter(outputTerms::contains)
                .count();

        double completeness = (double) coveredTerms / contextKeyTerms.size();
        return Math.round(completeness * 100.0) / 100.0;
    }

    private Set<String> extractSignificantWords(String text) {
        return WORD_PATTERN.matcher(text)
                .results()
                .map(match -> match.group().toLowerCase())
                .filter(word -> word.length() >= 4)
                .collect(Collectors.toSet());
    }
}

