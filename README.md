# CineGold

A movie catalogue with trailers and user reviews.

- **Server/**: Spring Boot 3.5 REST API on MongoDB (Java 17+)
- **Client/**: React 19 + Vite SPA that **keeps working when the API is down**, using a bundled offline catalogue

## Quick start

### Full stack with Docker
```bash
docker compose up --build
# web: http://localhost:8080   api: http://localhost:8000
```

### Local development
```bash
# API (needs MongoDB; put MONGODB_URI etc. in Server/.env, see Server/.env.example)
cd Server && ./mvnw spring-boot:run

# Frontend (proxies /api to localhost:8000)
cd Client && npm install && npm run dev   # http://localhost:5173
```
The frontend works without the API. It switches to offline mode and shows a banner.

## API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/v1/movies?page=0&size=50` | Paged (`size` ≤ 100) |
| GET | `/api/v1/movies/{imdbId}` | `imdbId` must match `^tt\d{7,10}$` |
| POST | `/api/v1/reviews` | `{"imdbId":"tt…","reviewBody":"…"}`, 2–2000 chars, rate limited |
| GET | `/actuator/health` | Liveness/readiness probes under `/actuator/health/*` |

Errors use RFC 9457 `application/problem+json`. Every other path returns 403.

## Configuration (environment variables)

| Variable | Default | Purpose |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017` | Connection string **including credentials** |
| `MONGODB_DATABASE` | `movie-api-db` | |
| `CORS_ALLOWED_ORIGINS` | localhost dev ports | Comma-separated exact origins of the frontend |
| `FORWARD_HEADERS_STRATEGY` | `none` | Set `framework` only behind a trusted proxy |
| `RATE_LIMIT_READS_PER_MINUTE` / `RATE_LIMIT_WRITES_PER_MINUTE` | 120 / 10 | Per client IP |
| `SEED_DATA` | `true` | Seed the catalogue if the collection is empty |
| `VITE_API_URL` (client, build time) | empty = same origin | API base URL |
| `API_ORIGIN` (client container) | empty | Added to the CSP `connect-src` |

## Offline mode (frontend)

- Every API call goes through a circuit breaker. If a call times out (6 s), fails on the network, returns 5xx, or returns non-JSON, the app serves the **bundled catalogue** (`Client/src/data/movies.json`). It waits 30 s before probing the API again.
- Reviews written offline are stored in the browser (`localStorage`), shown as *Pending sync*, and posted automatically once the API is reachable.
- Movie artwork comes from the public TMDB image CDN, and trailers come from `youtube-nocookie.com`. No API keys are shipped to the browser.

## Security hardening summary

- The hardcoded MongoDB Atlas credentials are removed. Config now comes only from the environment, and `.env` files are git-ignored.
- CORS no longer allows `*`. Only configured origins are accepted.
- Spring Security uses deny-by-default rules, a stateless setup, and no default user. It sends CSP, HSTS, X-Frame-Options, Referrer-Policy, and Permissions-Policy headers.
- Input validation covers IMDb id format, review length, page size limits, unknown JSON fields (rejected), and control-character stripping. This blocks NoSQL operator injection such as `{"$ne":…}`.
- Reviews cannot be created for movies that don't exist, so no orphaned documents are left behind.
- DTOs are used instead of entities, so internal ObjectIds are never exposed.
- Error responses never include stack traces or exception messages.
- Per-IP rate limiting is applied, with a much stricter limit on writes.
- Actuator exposes only `health`/`info`, without details.
- Containers run as non-root users. The API uses a JRE-only image, a read-only filesystem in compose, and `no-new-privileges`. MongoDB is not published to the host.
- The frontend has a strict CSP (via nginx). It validates every URL and id before it reaches `src` or an iframe, uses a sandboxed `youtube-nocookie` embed, and sends requests with `credentials: 'omit'`. User text is rendered as text, never as HTML.
- Unmaintained or vulnerable dependencies were removed (`react-material-ui-carousel`, `react-player`, MUI, axios, font-awesome, the self-referencing `client` package). `npm audit` reports 0 vulnerabilities.

## Tests
```bash
cd Server && ./mvnw test   # security/validation tests + integration test on embedded MongoDB
cd Client && npm run lint && npm run build
```
