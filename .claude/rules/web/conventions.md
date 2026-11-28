---
paths:
  - "apps/web/**"
---

# Web Conventions

Code conventions for the React web app in `apps/web`. They describe what the current code does; new code follows them.

## 1) Skills to load

- `agentic-typescript-engineer` for any TypeScript written in `apps/web`.
- `agentic-code-testing` when writing or reviewing tests (see `testing.md`).

## 2) Components and exports

- Components are React function components written as arrow functions: `export const App = () => { ... }`.
- Use named exports only. No default exports.

## 3) Server state

- Server state goes through TanStack Query: `useQuery` with a stable `queryKey` (example: `["health"]`).
- Query functions live outside the component (example: `fetchHealth` in `src/requests/health.ts`).
- A query function throws on failure so the query reaches its error state. Never return an error value as data.
- Request functions live in `src/requests/`, one module per resource (`health.ts`), never in components.
- The `QueryClient` is created and provided in `main.tsx`; components never create one.

## 4) API access

- Call the API only through the generated `@harness/web-client`, via `apiClient` in `src/api.ts`, from a request function in `src/requests/` (`unwrap(apiClient.GET("/health"))`). `apiClient` stays the single entry point.
- Wrap each call in `unwrap` from `src/requests/errors.ts`: a non-2xx response throws `HttpError(status)`, a rejected fetch throws `TransportError`, an unparsable body throws `DecodingError`. Components only see these typed errors. No runtime schema validation.
- No hand-written `fetch` calls and no hand-written API types: types come from the generated client.
- When the contract changes, regenerate with `bun run generate` (the contract is code-first, see `docs/tech/adr/0005-contract-is-code-first.md`). Never edit `packages/web-client` by hand.

## 5) Rendering states

- Render the three query states explicitly: pending, error, data.
- Every state has visible text (example: "API: checking", "API unreachable", "API: ok").

## 6) Accessibility

- Use semantic elements (`main`, `h1`, `p`) before generic `div`/`span`.
- Live status text carries `role="status"`.
- Anything a test or a user must find has a role or an accessible name.

## 7) Formatting and types

- No semicolons, double quotes, trailing commas in multi-line literals.
- TypeScript is strict as set in `tsconfig.base.json`: `strict`, `noUncheckedIndexedAccess`, `exactOptionalPropertyTypes`, `noImplicitOverride`, `verbatimModuleSyntax`, `isolatedModules`. Code must compile under all of them without `any` or non-null assertions.
- Use `import type` / inline `type` imports for type-only imports (`verbatimModuleSyntax`).
- Throw on missing required DOM or config (example: `Missing #root element`) instead of continuing silently.
