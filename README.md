# Movie Recommendation API

A Spring Boot REST API that recommends movies using semantic similarity. The application loads movie descriptions from `src/main/resources/movies.json`, creates embeddings through an OpenAI-compatible embedding service, and compares vectors with cosine similarity.

## Features

- Semantic movie search from a natural-language description.
- Recommendations for movies similar to a selected title.
- Top three matches returned for each request.
- Local catalog data loaded from a JSON resource at startup.

## Technology Stack

- Java 21
- Spring Boot 4.1.1
- Spring MVC
- Spring AI 2.0.1
- OpenRouter, using its OpenAI-compatible API
- Maven Wrapper
- Lombok

## Prerequisites

- Java 21 or newer
- An OpenRouter API key
- Network access to the configured embedding service during application startup and API requests

## Configuration

The application reads the API key from the `API_KEY` environment variable. It also supports a local `.env` file because `application.yaml` imports `.env` as an optional properties file.

Create `.env` in the project root, or set the variable in your shell:

```properties
API_KEY=your-openrouter-api-key
```

The configured embedding model is:

```text
nvidia/nemotron-3-embed-1b:free
```

The OpenAI-compatible base URL is `https://openrouter.ai/api/v1`. To use a different compatible provider or model, update `src/main/resources/application.yaml`.

## Running Locally

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080` by default.

During startup, the application reads `movies.json` and generates one embedding per movie description. The embedding provider must therefore be configured before the application starts successfully.

## API Endpoints

### Search by description

```http
GET /movies/search?query={description}
```

Returns the three movies whose descriptions are most semantically similar to the supplied query.

Example:

```bash
curl --get "http://localhost:8080/movies/search" \
  --data-urlencode "query=a science-fiction story about space travel and survival"
```

Example response:

```json
[
  {
    "title": "Interstellar",
    "description": "A former pilot joins a space mission through a mysterious wormhole...",
    "match": 0.87
  }
]
```

The exact similarity values depend on the configured embedding model.

### Find similar movies

```http
GET /movies/{title}/similar
```

Returns the three movies most similar to the movie identified by `{title}`. The title must match a catalog entry exactly, including capitalization and punctuation.

Example:

```bash
curl "http://localhost:8080/movies/The%20Matrix/similar"
```

Both endpoints return a JSON array of `MovieMatch` objects with these fields:

| Field | Type | Description |
| --- | --- | --- |
| `title` | string | Movie title |
| `description` | string | Movie description |
| `match` | number | Cosine similarity score |

## Movie Data

Movie records are stored in `src/main/resources/movies.json` as an array of objects:

```json
[
  {
    "title": "Example Movie",
    "description": "A short description used to calculate semantic similarity."
  }
]
```

To add or replace movies, edit this file and restart the application so new embeddings are generated.

## Project Structure

```text
src/
  main/
    java/com/movie_recommendation/
      MovieRecommendationApplication.java  Application entry point
      controller/MovieController.java      REST endpoints
      service/MovieService.java             Loading, embedding, and similarity logic
      model/                               Request and response domain objects
    resources/
      application.yaml                     Spring and embedding configuration
      movies.json                          Movie catalog
  test/
    java/com/movie_recommendation/
      MovieRecommendationApplicationTests.java
```

## Testing

Run the test suite with the Maven Wrapper:

On Windows:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```bash
./mvnw test
```

The current test suite verifies that the Spring application context loads successfully.

## Build

Create the executable JAR with:

```powershell
.\mvnw.cmd clean package
```

Run the packaged application with:

```powershell
java -jar target/movie-recommendation-0.0.1-SNAPSHOT.jar
```

## Current Limitations

- Movie embeddings are generated in memory at every startup; there is no persistent vector database or embedding cache.
- The catalog is bundled with the application and is not managed through an API.
- Search always returns at most three results.
- Similar-movie lookup requires an exact movie title.
- An invalid title currently results in an `IllegalArgumentException` rather than a dedicated not-found response.
- The embedding provider is required for both initialization and query processing.
