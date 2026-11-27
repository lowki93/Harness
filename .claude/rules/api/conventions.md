---
paths:
  - "apps/api/**"
---

# API Conventions

Rules for `apps/api`, the Hono API. The OpenAPI contract is generated from this code (ADR 0005).

## 1) Stack

- Hono through `OpenAPIHono` from `@hono/zod-openapi`. Import `z` and `createRoute` from the same package.
- Bun is the runtime and package manager (ADR 0006). Dependency versions are pinned exactly.
- Formatting follows the existing code: no semicolons, double quotes, 2-space indent, trailing commas in multi-line literals.
- TypeScript is strict. Do not weaken the flags in `tsconfig.base.json`: `strict`, `noUncheckedIndexedAccess`, `exactOptionalPropertyTypes`, `noImplicitOverride`, `verbatimModuleSyntax`, `isolatedModules`.
- Read env vars with bracket access (`process.env["PORT"]`), as `noUncheckedIndexedAccess` requires.

## 2) Resource modules

- One module per resource in `src/`, named after it (example: `src/health.ts`).
- A module exports its Zod schemas and its routes built with `createRoute`.
- Schemas are named with `.openapi("Name")`, in PascalCase (example: `HealthResponse`), and exported so tests reuse them.
- Routes are named `<resource>Route` (example: `healthRoute`).
- Every response status a route can return is declared in `responses`, with a description and a schema.

## 3) Route registration

- Register every API route in `src/app.ts` with `app.openapi(route, handler)`.
- Return typed status responses: `context.json(body, 200)`. Use `as const` on literal values the schema narrows.
- Never use plain `app.get`, `app.post`, etc. for API routes: they escape the contract. Plain methods are for tooling only (example: `/docs`).

## 4) Contract is code-first

- After any route or schema change, run `bun run generate`, then commit the changes in `packages/contract` and `packages/web-client`.
- CI fails when generated files drift from the code. Never edit generated files by hand.

## 5) Validation at trust boundaries

- Validate everything crossing a trust boundary with Zod.
- Request params, query, headers and body are validated through the route definition (`request` in `createRoute`), not inside the handler.
- Environment variables are parsed with Zod at startup (example: `PORT` in `src/index.ts`, with a default and range).
- Never read raw `process.env` or `request` values outside a parsed schema.

## 6) Skills to load

- Always: `agentic-typescript-engineer`, `agentic-code-testing`.
- `agentic-clean-architecture` for business logic (domain, use cases, validation, state transitions).
- `agentic-write-instrumented-code` when adding logging or tracing.
