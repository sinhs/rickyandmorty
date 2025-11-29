# Architecture Decisions

## API Choice: REST over GraphQL

We chose REST endpoints over GraphQL for the following reasons:

**REST Advantages:**
- **Simplicity**: The Rick & Morty API provides well-structured REST endpoints that are straightforward to consume
- **Caching**: HTTP caching mechanisms work naturally with REST, which is important for our ingestion strategy
- **Rate Limiting**: Easier to implement and reason about rate limiting with REST endpoints
- **Developer Ergonomics**: Spring's WebClient provides excellent support for REST with reactive programming
- **Batch Operations**: The API supports batch character fetching via comma-separated IDs (`/character/1,2,3`), which is efficient for our use case

**GraphQL Trade-offs:**
- While GraphQL would allow more flexible queries, the Rick & Morty API's REST endpoints already provide the data we need
- The overhead of GraphQL query parsing and execution isn't justified for our relatively simple data requirements
- REST endpoints are more predictable for scheduled ingestion jobs

## Persistence: SQLite

We chose SQLite over JSON files or other persistence mechanisms:

**SQLite Advantages:**
- **ACID Compliance**: Ensures data consistency during concurrent operations
- **Query Capabilities**: SQL queries are more powerful than JSON filtering, especially for search operations
- **Performance**: Indexed lookups are faster than scanning JSON files
- **Relationships**: Foreign key relationships between locations, characters, and notes are naturally expressed
- **Portability**: Single file database is easy to backup and deploy
- **Spring Integration**: Spring JDBC provides excellent support with minimal configuration

**Alternatives Considered:**
- **JSON Files**: Would require manual parsing and lack transactional guarantees
- **In-Memory**: Not suitable for persistent notes and generation history
- **PostgreSQL/MySQL**: Overkill for this use case; SQLite provides sufficient performance

## Data Ingestion Strategy

We implement a scheduled refresh pattern rather than on-demand fetching:

- **Reduces External API Load**: Batch ingestion happens periodically (every 30 minutes)
- **Faster Response Times**: All read operations hit local database
- **Resilience**: Service continues to work even if external API is temporarily unavailable
- **Cost Efficiency**: Fewer API calls reduce potential rate limiting issues

The trade-off is that data may be up to 30 minutes stale, which is acceptable for this use case.

## Runtime & API Flow

### Startup / Boot
- `RickMortyServiceApplication` boots Spring Boot and auto-configures Web, WebFlux, JDBC, validation, and SQLite.
- `SqliteDialectProvider` registers the ANSI dialect so Spring Data JDBC can talk to SQLite.
- `schema.sql` auto-creates tables on startup.
- `WebClientConfig` wires a reusable `WebClient` pointing at `https://rickandmortyapi.com/api` (buffer raised for large payloads).
- `DataIngestionRunner` runs once at boot (and on schedule) if `rickmorty.ingestion.enabled=true` (default) to hydrate the DB.

### Data Ingestion Flow
1. `DataIngestionRunner.run()` calls `LocationService.refreshFromSource()`.
2. `LocationService` pages through `/location?page=n` via `RickAndMortyClient.fetchLocationsPage`.
3. Each page is persisted:
   - Upsert location rows.
   - Clear `location_residents` for that location to avoid stale links.
   - Parse resident IDs from URLs, batch fetch characters via `/character/{ids}`, upsert characters, and re-insert resident links.
4. A `ReentrantLock` ensures only one refresh at a time; failures log and continue to next page.

### Request Handling Flow
- Controllers are thin and delegate to services:
  - `LocationController` (`/api/locations`, `/api/locations/refresh`) → `LocationService`.
  - `CharacterController` (`/api/characters/{id}`, `/{id}/notes`) → `CharacterService`.
  - `SearchController` (`/api/search?q=`) → `SearchService`.
  - `GenerationController` (`/api/generations`) → `GenerationService`.
- `GlobalExceptionHandler` normalizes validation errors, external API failures, and unexpected exceptions into JSON responses.

### Location Read Flow
1. `GET /api/locations` → `LocationService.listLocations`.
2. For each location row, load resident character IDs, resolve characters, and return `LocationDto` with `ResidentSummaryDto` list (flattened for UI).

### Character & Notes Flow
1. `GET /api/characters/{id}` → `CharacterService.getCharacter`.
2. Fetch character row + ordered notes (`character_notes`) → `CharacterDetailDto`.
3. `POST /api/characters/{id}/notes` validates `NoteRequest`, ensures character exists, then saves a note and returns `CharacterNoteDto`.

### Search Flow (AI-ish scoring)
1. `GET /api/search?q=` → `SearchService.search`.
2. Scores each character with blended signals: lexical matches, token overlap, and Levenshtein-based fuzzy similarity. Returns top 20 `ResidentSummaryDto`.

### Generation Flow
1. `POST /api/generations` with `GenerationRequest` → `GenerationController`.
2. Controller builds a lightweight context string from the subject character (name/species/origin) or falls back to “Unknown subject”.
3. `GenerationService` builds a Rick & Morty prompt, calls `GenerativeClient.generateText`.
   - Default profile uses `MockGenerativeClient`.
   - `openai` profile uses `OpenAiGenerativeClient` against `/chat/completions` (requires `OPENAI_API_KEY`).
4. `EvaluationService` heuristically scores factuality, creativity, completeness; `GenerationService` persists prompt/output/evaluation in `generations`.
5. Returns `GenerationResponse` (prompt, output, scores).

### Configuration & Profiles
- Base config in `application.yml`:
  - SQLite file at `data/rickmorty.db`.
  - Ingestion enabled by default; disable with `RICKMORTY_INGESTION_ENABLED=false` to run offline on the bundled DB.
  - LLM provider: mock by default; `SPRING_PROFILES_ACTIVE=openai` switches to OpenAI and reads `OPENAI_API_KEY`.

## Additional Technical Architecture Choices

- **API surface evolution (REST now, GraphQL optional later):** The current REST controllers are thin and map directly to service methods. If we later need client-driven queries (e.g., fetch characters and notes in one call with selective fields), we can add a GraphQL facade on top of the same service layer because `WebClient` and DTOs are already transport-agnostic. We avoided a GraphQL-first design to keep ingestion and caching straightforward and to lean on HTTP-level caching/rate-limiting.
- **HTTP stack (WebFlux vs MVC/RestTemplate):** We use `WebClient` (reactive) for outbound calls to tolerate the high-latency external API without blocking servlet threads. Controllers stay imperative (`@RestController`) to keep request handling simple; only the client is reactive, which is enough to free connection pools during ingestion bursts.
- **Ingestion consistency (local lock vs distributed coordination):** A `ReentrantLock` gates refresh to prevent double-ingestion on the same node. We did not add distributed locks because the service is packaged as a single-node demo; if horizontally scaled, we would add database- or Redis-backed locks.
- **LLM provider abstraction (pluggable client):** `GenerativeClient` is a small SPI with mock and OpenAI implementations chosen via Spring profiles. This keeps the service testable offline and lets us swap providers without touching controllers or services.
- **Search implementation (heuristics vs full-text index):** We blended lexical scoring and fuzzy matching in memory to avoid SQLite-specific FTS extensions and keep portability. If search latency grows, we can add SQLite FTS5 or move to an external index without changing the API contract.
- **Error handling (centralized advice):** `GlobalExceptionHandler` normalizes validation errors, upstream failures, and unexpected exceptions so clients receive consistent JSON envelopes instead of per-controller ad hoc handling.

