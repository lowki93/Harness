# 0008 - API database foundation via Kysely

**Status:** Accepted
**Date:** 2026-11-27

## Context

[ADR-0003](0003-skeleton-layout-apps-and-packages.md) left the database out of the skeleton and said it comes with the first feature that stores data. The next feature will store data; building persistence inside it would mix foundation and product work (D1). Hono stays the API framework ([ADR-0005](0005-contract-is-code-first.md)).

## Decision

The API gains a database foundation, shipped on its own, with no business schema (D1, D5):

- **Kysely** is the typed query builder; every query goes through one Kysely instance shared by the API.
- **Migrations**: `bun run --filter @harness/api migrate` applies pending migrations in order; rerunning with none pending is a no-op; a failing migration stops with a non-zero exit and later ones are not applied (D10).
- **Config**: database settings are Zod-parsed like `PORT`. Missing or invalid config makes the API refuse to start with a clear message; an unreachable database at startup does not stop the API (D9).
- **`GET /health` checks the database** (D7, D8): `200 {status: ok, database: ok}` when reachable; `503 {status: degraded, database: unreachable}` when not, with no internal error detail. The contract and generated web client are regenerated.
- Only the migration mechanism and the connection check ship; the first real schema comes with the first feature that stores data (D5).

**Open:** the database engine is not settled here. [ADR-0003](0003-skeleton-layout-apps-and-packages.md) names PostgreSQL as the chosen database; the tech grill in `agentic-to-plan` confirms or changes the engine, and this ADR is updated in place then.

## Rationale

Plumbing first keeps the next feature small. Failing fast on bad config while staying up on transient outages separates misconfiguration from a database blip, and a 503 lets monitoring see the outage without leaking internals.

## Alternatives Considered

- Business schema now: rejected, no feature stores data yet (D5).
- Front apps calling `/health` to prove the chain (D2): superseded by [0009](0009-front-request-layers.md) (D6); only the API proves it end to end.

## Consequences

- Web shows "API unreachable" when only the database is down; accepted until a front feature must tell them apart (D11).
- Database deployment and hosting stay out of scope.

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
