package com.sid.rickmorty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Centralized WebClient configuration so we can switch between REST and GraphQL clients quickly.
 * Dialing up buffer limits allows us to ingest large payloads without reconfiguration.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient rickAndMortyWebClient(@Value("${rickmorty.api-base:https://rickandmortyapi.com/api}") String apiBaseUrl) {
        HttpClient httpClient = HttpClient.create();
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(apiBaseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(clientDefaultCodecsConfigurer -> clientDefaultCodecsConfigurer
                                .defaultCodecs()
                                .maxInMemorySize(4 * 1024 * 1024))
                        .build())
                .build();
    }
}
