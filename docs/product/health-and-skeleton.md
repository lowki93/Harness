# Monorepo skeleton: what it offers

Harness is an agentic sandbox ([ADR-0001](../tech/adr/0001-harness-is-an-agentic-sandbox.md)). The skeleton has no business feature; it proves the contract-to-client chain.

## API

- `GET /health` also checks the database. Database reachable: `200` with `{"status":"ok","database":"ok"}`. Unreachable: `503` with `{"status":"degraded","database":"unreachable"}`, with no internal detail. The contract defines both.
- The API starts even if the database is unreachable; with missing or invalid database config it refuses to start and names the setting.
- `bun run --filter @harness/api migrate` applies pending migrations in order; with none pending it does nothing; a failing migration stops it with a non-zero exit. No business schema ships yet.
- `/docs` shows the OpenAPI contract as Swagger UI.

## Web

- Home page shows "API: ok", fetched through the generated client.
- With the API down, it shows a clear "API unreachable" message, not a blank page. Unchanged by the database check: with only the database down, the 503 also shows "API unreachable".
- API calls live in one request module per resource, outside components, through the single client.

## Mobile

- iOS (SwiftUI) and Android (Compose) show a "Hello World" text. Still no API call from any screen.
- Each has a request layer ready for the next feature: decoded value on 2xx, typed errors for HTTP status, decoding and transport (10 s timeout), configurable base URL. Covered by unit tests, not a screen.

## Local use

- The README gives each part's install, run and test commands.
- One root command starts the API and the web together.

## CI

- Runs on every PR, only for the touched parts: `apps/ios/` runs iOS only; `packages/contract/` runs API and web.
- API tests `/health` and `migrate`; web tests its "ok" and "unreachable" states; iOS and Android build and run the request layer unit tests.
- Fails when the contract or generated client is out of date, naming the regeneration command.

## Out of scope

Deployment, authentication, database deployment or hosting, any business table or route, mobile offline cache or retry, shared design system, store publishing, generated mobile clients.
