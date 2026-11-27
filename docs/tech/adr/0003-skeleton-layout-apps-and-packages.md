# 0003 - Skeleton layout: apps/ and packages/

**Status:** Accepted
**Date:** 2026-11-27

## Context

The first work item must prove the contract-to-client chain end to end with the smallest surface (decision D4).

## Decision

Skeleton only, no business feature:

| Path | Stack / role |
|---|---|
| `apps/api` | Hono + TypeScript, implements `GET /health` with Zod validation |
| `apps/web` | React + TanStack, calls `/health` through the generated client |
| `apps/ios` | SwiftUI, Hello World |
| `apps/android` | Kotlin + Jetpack Compose, Hello World |
| `packages/contract` | OpenAPI with `GET /health` |
| `packages/web-client` | TypeScript client generated from the contract |

## Rationale

Smallest surface that proves the chain; features come as separate work items.

## Alternatives Considered

- PostgreSQL in the skeleton (D2): rejected for now. A hello world needs no database, and it would bring Docker, migrations and secrets. It stays the chosen database, added with the first feature that stores data.
- Generated iOS and Android clients (D5): not in this work item. They would add two generation pipelines before any mobile screen needs the API. Revisit at the first mobile screen that calls the API.
- Out of scope, each its own work item when needed (D8): deployment, authentication, database, shared design system, App Store and Play Store publishing.

## Consequences

- Mobile apps do not call the API yet.
- Mobile tests are limited to a build until the first real screen (D17).

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
