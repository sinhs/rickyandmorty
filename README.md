# Rick & Morty AI Challenge (Java/Spring Boot)

A Spring Boot service that integrates with the Rick & Morty API to provide location and character data with AI-powered generation and evaluation capabilities.

## Features

- **Data Ingestion**: Scheduled refresh of locations and characters from the Rick & Morty API
- **Character Management**: View character details and add persistent notes
- **AI Generation**: Generate Rick & Morty-style narrations using configurable LLM providers
- **Evaluation**: Heuristic-based evaluation of generated content (factuality, creativity, completeness)
- **Search**: Relevance-based search across characters with scoring
- **Resilience**: Retry logic and error handling for external API calls

## Architecture Decisions

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed explanations of:
- REST vs GraphQL API choice
- SQLite persistence strategy
- Data ingestion approach

## Running Locally

### Prerequisites
- Java 21
- Maven 3.8+

### Setup
```bash
mvn spring-boot:run
```

The service will automatically:
- Ingest location and character data on startup
- Refresh data every 30 minutes (configurable)

To run offline without hitting the public API, disable ingestion and use the bundled SQLite snapshot:
```bash
RICKMORTY_INGESTION_ENABLED=false mvn spring-boot:run
```

### API Endpoints

- `GET /api/locations` - List all locations with residents
- `POST /api/locations/refresh` - Manually trigger data refresh
- `GET /api/characters/{id}` - Get character details with notes
- `POST /api/characters/{id}/notes` - Add a note to a character
- `GET /api/search?q={query}` - Search characters by name, species, or status
- `POST /api/generations` - Generate AI content with evaluation

### LLM Configuration

Default: Mock provider (for testing)

To use OpenAI:
```bash
export OPENAI_API_KEY=your-key-here
SPRING_PROFILES_ACTIVE=openai mvn spring-boot:run
```

## Technology Stack

- Spring Boot 3.3.3
- SQLite (embedded database)
- WebFlux (reactive HTTP client)
- Spring JDBC (data access)
