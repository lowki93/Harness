# 0005 - Contract is code-first

**Status:** Accepted
**Date:** 2026-11-27

## Context

[0002](0002-monorepo-around-a-shared-openapi-contract.md) fixes a shared OpenAPI contract but leaves open whether it is written first (spec-first) or generated from the API (code-first). Decision D18.

## Decision

Code-first: Hono routes with Zod schemas (`@hono/zod-openapi`). The OpenAPI file is generated from the API, committed in `packages/contract/`, and CI checks it is up to date. Clients consume the committed file.

## Rationale

Zod is written once, native to Hono, with no extra spec tooling; the committed file stays the shared interface.

## Alternatives Considered

- Hand-written YAML with generated Zod and a conformance check: rejected, more tooling.

## Consequences

- The API code is the source of truth; the contract file is a generated artifact.
- A stale contract is *contract drift*, caught by CI (see [0004](0004-github-actions-ci-per-part.md)).

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
