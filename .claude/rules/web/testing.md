---
paths:
  - "apps/web/**"
---

# Web Testing Rules

Testing conventions for `apps/web`: Vitest, Testing Library and the jest-dom matchers loaded in `tests/vitest.setup.ts`.

## 1) Structure

- Tests live in `apps/web/tests/`, mirroring `src/`: `src/App.tsx` is tested by `tests/App.test.tsx`.
- Request functions and their typed errors are tested in `tests/requests/` (`errors.test.ts`), through the request function with `fetch` stubbed.
- One top-level `describe` per component, named after it (`describe("App", ...)`).
- One behaviour per `test`. A test arranges, acts, then asserts.
- Test names are plain sentences describing user-visible behaviour: `shows API unreachable when the fetch rejects`.

## 2) Rendering

- Render through a local helper (example: `renderApp`) that builds a fresh `QueryClient` per test with `defaultOptions: { queries: { retry: false } }` and wraps the component in `QueryClientProvider`.
- Never share a `QueryClient` between tests.
- `tests/vitest.setup.ts` already runs `cleanup()` and `vi.unstubAllGlobals()` after each test; do not repeat them.

## 3) Queries and assertions

- Query by role and accessible name first (`getByRole("status")`), then by text. Use test ids only as a last resort.
- Assert with jest-dom matchers (`toHaveTextContent`, `toBeInTheDocument`).
- Wait for async UI with `waitFor` or `findBy*`. Never use fixed sleeps.

## 4) Network

- Stub the network at the boundary with `vi.stubGlobal("fetch", ...)`. Never mock the `@harness/web-client` or `api.ts` module.
- Cover success, a rejected fetch (`TransportError`), a non-2xx response (`HttpError` with the status) and an unparsable body (`DecodingError`) for every call.
- A stub that needs to be inspected is a `vi.fn`; assert on the request it received (example: URL path `/api/health`).

## 5) Determinism

- No real network, no real timers, no dependence on test order or on the wall clock.
- A test that fails intermittently is fixed or removed, never retried.

## 6) Commands

Single source for running checks:

- `bun run --filter @harness/web test`: web tests.
- `bun run --filter @harness/web typecheck`: web type check.
- `bun run verify` at the repo root before opening a PR.
