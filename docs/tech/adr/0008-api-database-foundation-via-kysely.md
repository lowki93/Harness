# 0008 - API database foundation via Kysely

**Status:** Accepted
**Date:** 2026-11-27

## Context

[ADR-0003](0003-skeleton-layout-apps-and-packages.md) left the database out of the skeleton and said it comes with the first feature that stores data. The next feature will store data; building persistence inside it would mix foundation and product work (D1). Hono stays the API framework ([ADR-0005](0005-contract-is-code-first.md)).

## Decision

The API gains a database foundation, shipped on its own, with no business schema (D1, D5):

- **Engine** (D14): PostgreSQL ([ADR-0003](0003-skeleton-layout-apps-and-packages.md)) through **Kysely** with `PostgresDialect` and the `pg` driver; every query goes through one Kysely instance shared by the API. Local PostgreSQL runs from a root `compose.yaml` (`docker compose up -d`).
- **Migrations** (D10, D17): Kysely `Migrator` with `FileMigrationProvider` on `apps/api/migrations`, run by a `migrate` script. `bun run --filter @harness/api migrate` applies pending migrations in order; rerunning with none pending is a no-op; a failing migration stops with a non-zero exit and later ones are not applied. Zero migrations ship. No tests of Kysely's own behaviour; API CI gets a Postgres service and runs `migrate` twice as a smoke test of wiring and config.
- **Config** (D9, D15): `DATABASE_URL` is required and Zod-parsed like `PORT`, read from the environment (Bun loads `.env`); the dev value is documented in the README, with no `.env.example`. Missing or invalid config makes the API refuse to start with a clear message; an unreachable database at startup does not stop the API (D9).
- **`GET /health` checks the database** (D7, D8): `200 {status: ok, database: ok}` when reachable; `503 {status: degraded, database: unreachable}` when not, with no internal error detail. The contract and generated web client are regenerated.
- **Test seam** (D16): `createApp({ checkDatabase })`. `checkDatabase` runs `select 1` in production and is stubbed in tests, so `bun run test` and CI unit tests stay in-process with no database.
- **Failure handling** (D18): the `pg` connection and query timeouts are 2 s each (`connectionTimeoutMillis`, `query_timeout`) so `/health` stays responsive; the real error is logged server-side with `console.error` and never returned in the response.
- Only the migration mechanism and the connection check ship; the first real schema comes with the first feature that stores data (D5).

## Rationale

Plumbing first keeps the next feature small. Failing fast on bad config while staying up on transient outages separates misconfiguration from a database blip, and a 503 lets monitoring see the outage without leaking internals.

## Alternatives Considered

- `Bun.sql` Kysely dialect: rejected as community-maintained and unstable; `pg` is the stable dialect on Bun (D14).
- Business schema now: rejected, no feature stores data yet (D5).
- Front apps calling `/health` to prove the chain (D2): superseded by [0009](0009-front-request-layers.md) (D6); only the API proves it end to end.

## Consequences

- Web shows "API unreachable" when only the database is down; accepted until a front feature must tell them apart (D11).
- Database deployment and hosting stay out of scope.

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
| 2026-11-27 | Settled the engine (PostgreSQL, `pg`, compose), config, test seam, migrator and CI smoke test, timeout and logging (D14-D18); removed the open engine question |
