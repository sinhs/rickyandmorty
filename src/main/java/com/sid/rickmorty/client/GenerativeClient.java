package com.sid.rickmorty.client;

/**
 * Abstraction over whichever LLM is available. Keeps interview conversation focused on strategy, not vendor.
 */
public interface GenerativeClient {

    String generateText(String prompt, String context);
}
