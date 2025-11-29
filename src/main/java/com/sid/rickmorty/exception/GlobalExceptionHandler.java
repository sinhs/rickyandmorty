package com.sid.rickmorty.exception;

import com.sid.rickmorty.client.RickAndMortyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(Map.of("error", "validation_failed", "details", errors));
    }

    @ExceptionHandler(RickAndMortyClient.ApiClientException.class)
    public ResponseEntity<Map<String, Object>> handleApiClientError(RickAndMortyClient.ApiClientException ex) {
        log.warn("API client error", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "external_api_error", "message", ex.getMessage()));
    }

    @ExceptionHandler(RickAndMortyClient.ApiServerException.class)
    public ResponseEntity<Map<String, Object>> handleApiServerError(RickAndMortyClient.ApiServerException ex) {
        log.error("API server error", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "external_api_unavailable", "message", "External service temporarily unavailable"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "invalid_request", "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "internal_error", "message", "An unexpected error occurred"));
    }
}
