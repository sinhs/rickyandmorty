package com.sid.rickmorty.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Offline fallback so demo runs without API keys. Output intentionally tongue-in-cheek for Rick & Morty vibe.
 */
@Component
@Profile("!openai")
public class MockGenerativeClient implements GenerativeClient {

    @Override
    public String generateText(String prompt, String context) {
        return "[mock llm] " + prompt + " | context hash=" + Math.abs(context.hashCode()) + " @" + Instant.now();
    }
}
