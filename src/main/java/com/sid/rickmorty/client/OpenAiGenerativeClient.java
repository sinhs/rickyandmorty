package com.sid.rickmorty.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Real integration path for OpenAI. We keep it lean to stay within the two-hour scope.
 */
@Component
@Profile("openai")
public class OpenAiGenerativeClient implements GenerativeClient {

    private final WebClient webClient;
    private final String model;
    private final String apiKey;

    public OpenAiGenerativeClient(
            WebClient.Builder webClientBuilder,
            @Value("${llm.openai.api-key}") String apiKey,
            @Value("${llm.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${llm.openai.model:gpt-4o-mini}") String model) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.model = model;
    }

    @Override
    public String generateText(String prompt, String context) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is missing; set llm.openai.api-key or OPENAI_API_KEY");
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of("role", "system", "content", "You are a Rick and Morty narrator."),
                        Map.of("role", "user", "content", prompt + "\nContext:" + context)
                }
        );
        Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .blockOptional()
                .orElse(Map.of());

        Map<String, Object> firstChoice = (Map<String, Object>) ((java.util.List<Object>) response.getOrDefault("choices", java.util.List.of()))
                .stream()
                .findFirst()
                .orElse(Map.of());
        Map<String, Object> message = (Map<String, Object>) firstChoice.getOrDefault("message", Map.of());
        Object content = message.getOrDefault("content", "");
        return content == null ? "" : content.toString();
    }
}
