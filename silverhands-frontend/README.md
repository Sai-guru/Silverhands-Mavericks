# SilverHands Frontend

React + TypeScript + Vite SPA for SilverHands — a marketplace connecting customers with local
service providers (and their products), with AI-assisted discovery and real-time chat.

Talks to the Silverhands Spring Boot backend (Google OAuth login, sessions via cookies).

## Getting started

```bash
pnpm install       # or npm install (from this directory)
pnpm dev           # vite dev server with HMR
pnpm build         # type-check + production build to dist/
pnpm preview       # serve the production build
pnpm lint          # oxlint
```

The backend URL defaults to `http://localhost:8000`. Override it with a `.env` file:

```
VITE_API_URL=http://localhost:8000
```

## Tech

- **React 19** (React Compiler enabled) + **TypeScript**
- **Vite 8** dev server / bundler
- **react-router-dom v7** for routing
- **@stomp/stompjs** for the live-chat WebSocket (STOMP)
- **oxlint** for linting

## How it works

### Auth & roles

Login is "Continue with Google" (`/oauth2/authorization/google` on the backend). The user first
picks a role on the login page; after the OAuth callback (`/auth/callback`) the frontend confirms
it via `POST /api/me/role` with `CUSTOMER` or `PROVIDER`. `GET /api/me` drives session state
(`roleSelectionRequired` etc.), and everything lives behind role-guarded routes
(`/customer/**`, `/provider/**`).

### Pages per role

**Customer** (read-only discovery + chat):
- `/customer/providers` — search providers (`GET /api/providers?name=…`)
- `/customer/services` — search services (`GET /api/services?search=…&area=…&category=…`)
- `/customer/products` — search products (`GET /api/products?search=…`)
- 💬 Chat buttons on any provider row start a conversation with that provider

**Provider** (full CRUD on their own listings):
- `/provider/services` — `GET /api/services?mine=true`, create/edit/delete
  (fields: name, description, category, pricePerHour, area, availableFrom, availableTo)
- `/provider/products` — `GET /api/products?mine=true`, create/edit/delete
  (fields: name, description, price, category, area, imageUrl)
- Ownership is enforced server-side (another provider's id returns 403)

**Both roles:**
- `…/ai` — AI chat (`POST /api/ai/chat` with `message`, `inputLanguage`, `outputLanguage`,
  `inputType`; API key stays server-side and replies are grounded in real service data)
- `…/chat` — live chat over STOMP (below)

### Live chat (STOMP over WebSocket)

- Connect to the backend's `/ws/chat` endpoint (broker URL derived from `VITE_API_URL`,
  `http(s) → ws(s)`) — the authenticated session is carried by the cookie handshake.
- Starting a chat: `POST /api/conversations` with `{"otherUserId": "<uuid>"}` — the backend
  returns the existing pair conversation or creates one.
- History: `GET /api/conversations/{id}/messages`.
- Sending: publish to `/app/chat.send` with body `{"conversationId": "<uuid>", "message": "text"}`
  (the field is `message`, not `content` — the backend DTO validates `@NotBlank message`).
- Receiving: subscribe to `/topic/conversations/{id}`.

## Project structure

```
src/
├── api/            # fetch wrapper, config (API_URL, Google login URL), shared types
├── auth/           # AuthContext (session/role), RequireRole guard, role → home path
├── components/     # Layout (sidebar/topbar), ResourceCrud (generic table + form)
├── pages/          # Login, AuthCallback, Home, AiChat, Chat, ResourcePage
└── resources.ts    # per-role resource registry (API path, fields, search filters)
```

`resources.ts` is the one place to look (or edit) when the backend contract changes: each entry
declares the API path, list params (`mine=true`), writable fields with types, and whether the
listing gets a search/filter bar. `ResourceCrud` renders any registry entry as a table with
create/edit/delete + chat actions, so new endpoints usually need no new page code.

## Notes

- API responses are handled tolerantly: both plain JSON arrays and Spring `Page` objects are
  accepted by the list helper.
- If the dev server needs the backend to allow it, make sure CORS/credentials include the Vite
  origin (default `http://localhost:5173`) on the backend side.
