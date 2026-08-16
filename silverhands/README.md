# SilverHands Backend

AI-powered digital livelihood platform connecting **senior citizens, homemakers and skilled local service providers** with customers looking for local services, homemade products and traditional skills.

This is the Spring Boot backend for the SilverHands MVP. It serves the existing React + TypeScript frontend.

## Tech Stack

- Java 25, Spring Boot 4.1, Maven
- Spring Security (Google OAuth2 login, session-based)
- Spring Data JPA / Hibernate, PostgreSQL (Neon)
- WebSocket (STOMP) for real-time chat
- OpenRouter AI integration (server-side only — the API key never reaches the frontend)

## Running

```bash
./mvnw spring-boot:run
```

The app runs on **http://localhost:8000**.

Swagger UI: http://localhost:8000/swagger-ui/index.html
Health check: http://localhost:8000/actuator/health

Database and OAuth/AI credentials are configured in `src/main/resources/application.yml`.

## Architecture

```
controller (rest/)  →  service  →  repository  →  JPA/Hibernate  →  PostgreSQL
```

```
io/bootify/silverhands/
├── config/          # security (OAuth2, role enrichment), WebSocket, AI, Swagger
├── rest/            # REST controllers + STOMP chat controller
├── service/         # business logic (catalog, chat, ai, user)
├── repos/           # Spring Data repositories
├── domain/          # JPA entities (user, catalog, chat)
├── model/dto/       # request/response DTOs
└── util/exception/  # NotFound handling
```

## Database — five tables

```
users          id, google_id, email, name, phone, role (CUSTOMER | PROVIDER), timestamps
services       id, provider_id → users, name, description, category,
               price_per_hour, area, available_from, available_to, timestamps
products       id, provider_id → users, name, description, price, category,
               area, image_url, timestamps
conversations  id, customer_id → users, provider_id → users, timestamps
               (unique: one conversation per customer–provider pair)
messages       id, conversation_id → conversations, sender_id → users, message, created_at
```

A single `users` table holds both roles — there are no separate customer/provider profile tables.

## Authentication Flow

```
Login → select role → Continue with Google → OAuth2 → find/create user
      → POST /api/me/role (CUSTOMER or PROVIDER) → frontend dashboard
```

- Session-based (JSESSIONID cookie); the frontend must send credentials with requests.
- The DB role is injected as a granted authority (`ROLE_CUSTOMER` / `ROLE_PROVIDER`) on every request.
- Role is chosen once via `POST /api/me/role` and cannot be changed afterwards (409).

## REST API

All endpoints are under `/api` and require an authenticated session unless noted. Errors return clean JSON — no stack traces or SQL details.

### Auth / profile

| Method | Path | Who | Description |
|---|---|---|---|
| GET | `/api/me` | any | current user (id, name, email, role, `roleSelectionRequired`) |
| POST | `/api/me/role` | any | body `{"role": "CUSTOMER" \| "PROVIDER"}`, one-time |
| PUT | `/api/me` | any | update own profile (name, phone, profileImageUrl) |

### Services (provider offerings, e.g. cooking ₹300/hour)

| Method | Path | Who | Description |
|---|---|---|---|
| GET | `/api/services?search=&category=&area=` | any | search services |
| GET | `/api/services?mine=true` | provider | own services |
| GET | `/api/services/{id}` | any | service detail |
| POST | `/api/services` | provider | create |
| PUT | `/api/services/{id}` | provider (owner) | update |
| DELETE | `/api/services/{id}` | provider (owner) | delete |

Service fields: `name`*, `description`, `category`, `pricePerHour`*, `area`*, `availableFrom`, `availableTo` (required*).

### Products (homemade items for sale)

Same CRUD pattern at `/api/products` with the same `search/category/area/mine` filters.
Product fields: `name`*, `description`, `price`*, `category`, `area`*, `imageUrl`.

**Ownership is enforced server-side**: the authenticated user must be the record's provider for PUT/DELETE — a `providerId` sent by the frontend is never trusted. Violations return 403.

### Discovery

| Method | Path | Who | Description |
|---|---|---|---|
| GET | `/api/providers?name=` | any | search providers |
| GET | `/api/providers/{id}` | any | provider detail |
| GET | `/api/customers?name=` | **provider only** | search customers |
| GET | `/api/customers/{id}` | **provider only** | customer detail |

### AI assistant

| Method | Path | Description |
|---|---|---|
| POST | `/api/ai/chat` | body: `message`*, `inputLanguage`*, `outputLanguage`*, `inputType`* — returns `reply`, `recommendedServices[]`, `model`, `usedAi` |

## Real-time Chat (WebSocket / STOMP)

1. Start or reuse a conversation: `POST /api/conversations` with `{"otherUserId": "<uuid>"}` — returns the existing pair conversation or creates one.
2. Connect STOMP to `ws://localhost:8000/ws/chat` (send the session cookie with the handshake).
3. Subscribe: `/topic/conversations/{conversationId}`
4. Send: `/app/chat.send` with body:

```json
{ "conversationId": "<uuid>", "message": "hello" }
```

5. Incoming messages on the topic:

```json
{
  "id": "uuid", "conversationId": "uuid",
  "senderUserId": "uuid", "senderName": "Lakshmi",
  "message": "hello", "createdAt": "2026-08-16T13:54:59Z"
}
```

Related REST endpoints:

| Method | Path | Description |
|---|---|---|
| GET | `/api/conversations` | my conversations (as customer or provider) |
| GET | `/api/conversations/{id}` | one conversation (participants only) |
| GET | `/api/conversations/{id}/messages` | message history (participants only) |

The sender identity is always taken from the authenticated WebSocket session, never from the payload. Only conversation participants can read or send messages.

## Build / Package

```bash
./mvnw clean package
java -jar ./target/silverhands-0.0.1-SNAPSHOT.jar
```

## Notes

- `ddl-auto: update` — schema changes apply automatically on boot.
- Secrets (DB password, Google client secret, OpenRouter key) currently live in `application.yml` and should be moved to environment variables before any deployment.
- The entity class `domain/catalog/Service.java` clashes by name with Spring's `@Service` annotation, so `ServiceService` and `AiRecommendationService` use the fully-qualified `@org.springframework.stereotype.Service`.
