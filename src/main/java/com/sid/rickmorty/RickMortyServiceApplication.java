package com.sid.rickmorty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Rick & Morty challenge backend.
 * Keeps the bootstrap minimal so we can focus on data ingestion, persistence, and AI features.
 */
@SpringBootApplication
public class RickMortyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RickMortyServiceApplication.class, args);
    }
}
