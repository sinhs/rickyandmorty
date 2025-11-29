package com.sid.rickmorty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RickAndMortyClient {

    private static final Logger log = LoggerFactory.getLogger(RickAndMortyClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};
    
    private final WebClient webClient;
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(1);

    public RickAndMortyClient(WebClient rickAndMortyWebClient) {
        this.webClient = rickAndMortyWebClient;
    }

    public Mono<Map<String, Object>> fetchLocationsPage(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/location").queryParam("page", page).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(MAP_TYPE)
                .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                        .filter(throwable -> !isClientError(throwable))
                        .doBeforeRetry(retrySignal -> 
                            log.warn("Retrying locations page {} after failure", page, retrySignal.failure())))
                .doOnError(error -> log.error("Failed to fetch locations page {} after retries", page, error));
    }

    public Mono<Map<String, Object>> fetchCharacter(int characterId) {
        return webClient.get()
                .uri("/character/{id}", characterId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(MAP_TYPE)
                .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                        .filter(throwable -> !isClientError(throwable))
                        .doBeforeRetry(retrySignal -> 
                            log.warn("Retrying character {} after failure", characterId, retrySignal.failure())))
                .doOnError(error -> log.error("Failed to fetch character {} after retries", characterId, error));
    }

    public Mono<List<Map<String, Object>>> fetchCharactersBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Mono.just(List.of());
        }
        
        String idsParam = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        
        return webClient.get()
                .uri("/character/{ids}", idsParam)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(Object.class)
                .map(this::parseBatchResponse)
                .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                        .filter(throwable -> !isClientError(throwable))
                        .doBeforeRetry(retrySignal -> 
                            log.warn("Retrying batch fetch for {} after failure", ids, retrySignal.failure())))
                .doOnError(error -> log.error("Failed to fetch characters batch {} after retries", ids, error));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseBatchResponse(Object body) {
        if (body instanceof List<?> list) {
            return list.stream()
                    .map(element -> (Map<String, Object>) element)
                    .toList();
        }
        return List.of((Map<String, Object>) body);
    }

    private Mono<? extends Throwable> handleError(org.springframework.web.reactive.function.client.ClientResponse response) {
        HttpStatusCode status = response.statusCode();
        log.error("API error: {} {}", status.value(), status);
        return response.createException()
                .flatMap(ex -> {
                    if (status.is4xxClientError()) {
                        return Mono.error(new ApiClientException("Client error: " + status, ex));
                    }
                    return Mono.error(new ApiServerException("Server error: " + status, ex));
                });
    }

    private boolean isClientError(Throwable throwable) {
        return throwable instanceof ApiClientException;
    }

    public static class ApiClientException extends RuntimeException {
        public ApiClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ApiServerException extends RuntimeException {
        public ApiServerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
