# Monorepo skeleton: what it offers

Harness is an agentic sandbox ([ADR-0001](../tech/adr/0001-harness-is-an-agentic-sandbox.md)). The skeleton has no business feature; it proves the contract-to-client chain.

## API

- `GET /health` responds `200` with `{"status":"ok"}`, as the contract defines.
- `/docs` shows the OpenAPI contract as Swagger UI.

## Web

- Home page shows "API: ok", fetched through the generated client.
- With the API down, it shows a clear "API unreachable" message, not a blank page.

## Mobile

- iOS (SwiftUI) and Android (Compose) show a "Hello World" text. No API call.

## Local use

- The README gives each part's install, run and test commands.
- One root command starts the API and the web together.

## CI

- Runs on every PR, only for the touched parts: `apps/ios/` runs iOS only; `packages/contract/` runs API and web.
- API tests `/health`; web tests its "ok" and "unreachable" states; iOS and Android are built only.
- Fails when the contract or generated client is out of date, naming the regeneration command.

## Out of scope

Deployment, authentication, database, shared design system, store publishing, generated mobile clients.
