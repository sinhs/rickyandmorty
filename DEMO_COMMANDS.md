# Demo Commands & Talk Track

Run these from the repo root (`/Users/sid/Downloads/rick-morty-service`). Each task has CLI commands plus subtitles to narrate during the recording.

## Setup
```bash
mvn spring-boot:run
```
Wait for the app on port 8080.

## Task 1: Data Retrieval (Locations with Residents)
**Commands**
```bash
# Fetch locations with resident summaries
curl http://localhost:8080/api/locations | jq '.[0:2]'  # show first two for brevity
```
**what is the command  doing**
- “I’m using the existing REST API for simplicity and predictable caching; the service already ingests Rick & Morty REST data into SQLite.”
- “This endpoint returns each location with type plus resident name, status, species, and image URL—ready for UI consumption.”
- “GraphQL could offer client-shaped queries later, but REST keeps ingestion and rate limiting straightforward.”

## Task 2: Interaction & Notes (Character Details + Persistent Notes)
**Commands**
```bash
# View character details (includes current notes)
curl http://localhost:8080/api/characters/1 | jq

# Add a persistent note
curl -X POST "http://localhost:8080/api/characters/1/notes" \
  -H "Content-Type: application/json" \
  -d '{"note":"Evaluation: fits interdimensional mission.", "author":"demo"}' | jq

# Re-fetch to show the note persisted
curl http://localhost:8080/api/characters/1 | jq '.notes'
```
**what is the command  doing**
- “Character details come from the local SQLite cache; notes are stored in `character_notes`, so they persist between runs.”
- “I’m posting a note to associate user/LLM insights with this character, then re-reading to verify persistence.”
- “SQLite is chosen for ACID guarantees and easy backup; JSON/local storage would lack transactional safety for concurrent writes.”

## Task 3: Generative Layer (LLM + Heuristic Evaluation)
**Commands**
```bash
# Generate Rick & Morty-style text with built-in heuristic scoring
curl -X POST http://localhost:8080/api/generations \
  -H "Content-Type: application/json" \
  -d '{"characterId":1,"prompt":"Narrate Morty entering a new portal in a noir tone"}' | jq
```
**what is the command  doing**
- “This hits the `/api/generations` endpoint. The service builds a Rick & Morty prompt, calls the configured LLM (mock by default, OpenAI if profile enabled), and stores the prompt/output.”
- “We run lightweight evaluation—factuality, creativity, completeness—returned in the response so we can compare generations.”
- “The same abstraction supports swapping providers; switching to OpenAI is just `SPRING_PROFILES_ACTIVE=openai` with `OPENAI_API_KEY`.”

## Task 4 (Bonus): AI Search & Filtering
**Commands**
```bash
# Fuzzy/semantic-ish search across characters
curl "http://localhost:8080/api/search?q=space rick" | jq '.[0:5]'
```
**what is the command  doing**
- “Search blends lexical and fuzzy scoring to surface relevant characters without needing an external vector DB.”
- “Results return resident summaries so UIs can render name, status, species, and image quickly.”

## Closing Notes
- “All features run locally: REST ingestion into SQLite, character CRUD with notes, LLM generation with heuristic evaluation, and fuzzy search.”
- “If I needed client-shaped queries, I’d add a GraphQL facade on top of the same service layer without changing ingestion or persistence.”
