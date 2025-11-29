package com.sid.rickmorty.search;

import com.sid.rickmorty.model.dto.ResidentSummaryDto;
import com.sid.rickmorty.repository.CharacterRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.StreamSupport;

@Service
public class SearchService {

    private final CharacterRepository characterRepository;

    public SearchService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    public List<ResidentSummaryDto> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalized = query.toLowerCase(Locale.US).trim();
        String[] queryTerms = normalized.split("\\s+");
        Set<String> queryTokens = tokenize(normalized);

        return StreamSupport.stream(characterRepository.findAll().spliterator(), false)
                .map(character -> {
                    double score = calculateRelevanceScore(character, normalized, queryTerms, queryTokens);
                    return new SearchResult(character, score);
                })
                .filter(result -> result.score > 0)
                .sorted(Comparator.comparingDouble((SearchResult r) -> r.score).reversed())
                .limit(20)
                .map(result -> new ResidentSummaryDto(
                        result.character.id(),
                        result.character.name(),
                        result.character.status(),
                        result.character.species(),
                        result.character.image()))
                .toList();
    }

    private double calculateRelevanceScore(
            com.sid.rickmorty.model.entity.CharacterEntity character,
            String normalizedQuery,
            String[] queryTerms,
            Set<String> queryTokens) {
        
        String nameLower = safeLower(character.name());
        String speciesLower = safeLower(character.species());
        String statusLower = safeLower(character.status());
        String originLower = safeLower(character.origin());
        String corpus = (nameLower + " " + speciesLower + " " + statusLower + " " + originLower).trim();

        double lexical = lexicalScore(nameLower, speciesLower, statusLower, normalizedQuery, queryTerms);
        double tokenOverlap = similarity(queryTokens, tokenize(corpus));
        double fuzzy = fuzzyNameScore(nameLower, normalizedQuery);

        // Weighted blend to approximate semantic/fuzzy relevance without external embeddings
        return lexical + (tokenOverlap * 40) + (fuzzy * 30);
    }

    private double lexicalScore(String nameLower, String speciesLower, String statusLower,
                                String normalizedQuery, String[] queryTerms) {
        double score = 0;

        if (nameLower.equals(normalizedQuery)) {
            score += 100;
        } else if (nameLower.startsWith(normalizedQuery)) {
            score += 50;
        } else if (nameLower.contains(normalizedQuery)) {
            score += 30;
        }

        for (String term : queryTerms) {
            if (term.isBlank()) {
                continue;
            }
            if (nameLower.contains(term)) {
                score += 20;
            }
            if (speciesLower.contains(term)) {
                score += 15;
            }
            if (statusLower.contains(term)) {
                score += 10;
            }
        }

        return score;
    }

    private Set<String> tokenize(String text) {
        return java.util.Arrays.stream(text.split("[^a-z0-9]+"))
                .map(String::trim)
                .filter(token -> token.length() > 1)
                .collect(java.util.stream.Collectors.toSet());
    }

    private double similarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        long intersection = a.stream().filter(b::contains).count();
        long union = a.size() + b.size() - intersection;
        return union == 0 ? 0 : (double) intersection / union;
    }

    private double fuzzyNameScore(String nameLower, String normalizedQuery) {
        if (nameLower.isBlank() || normalizedQuery.isBlank()) {
            return 0;
        }
        int distance = levenshteinDistance(nameLower, normalizedQuery);
        int maxLen = Math.max(nameLower.length(), normalizedQuery.length());
        double similarity = maxLen == 0 ? 0 : 1.0 - ((double) distance / maxLen);
        return Math.max(0, similarity) * 100;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.US);
    }

    // Lightweight Levenshtein to keep fuzzy scoring dependency-free
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    private record SearchResult(
            com.sid.rickmorty.model.entity.CharacterEntity character,
            double score
    ) {}
}
