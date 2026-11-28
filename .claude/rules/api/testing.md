---
paths:
  - "apps/api/**"
---

# API Testing

Rules for tests in `apps/api`. See also the `agentic-code-testing` skill.

## 1) Runner

- Use `bun:test`: `describe`, `test`, `expect`.
- Tests live in `apps/api/tests/`, mirroring `src/`, as `*.test.ts` (example: `src/app.ts` is tested by `tests/app.test.ts`).

## 2) Structure

- One `describe` per route, named `"METHOD /path"` (example: `"GET /health"`).
- Test names are plain sentences describing the behaviour.
- One behaviour per test.

## 3) Calling the API

- Call `app.request(path, init)` in-process. Never start a server or open a port.
- Assert the status first, then parse the body with the route's exported response schema (`Schema.parse(await response.json())`) before comparing values.
- Reuse the schemas exported by the resource module. Do not redefine them in tests.

## 4) Coverage

- Cover the success response of every route.
- Once a route takes input, cover a 4xx response for each validated input (params, query, body).
- Assert the error status and the error body shape.

## 5) Determinism

- Tests are deterministic and never touch the network.
- Fake time, randomness and external services at the boundary.

## 6) Commands

Single source for running checks:

- `bun run --filter @harness/api test`: API tests.
- `bun run --filter @harness/api typecheck`: API type check.
- `bun run generate`, then `git diff packages/`: contract drift check. The diff must be empty after you commit regenerated files.
- `bun run verify` at the root before opening a PR.
