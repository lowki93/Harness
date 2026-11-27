# 0002 - Monorepo around a shared OpenAPI contract

**Status:** Accepted
**Date:** 2026-11-27

## Context

Experiments span a backend and three clients. A change to the API should ship together with the clients that consume it (decision D3).

## Decision

One monorepo evolves the API and its clients together around a shared OpenAPI contract in `packages/contract/`. The contract is also served as Swagger UI docs. The repo is the one place for conventions, CI and agentic setup.

## Rationale

One PR changes the API and what its clients consume.

## Consequences

- The contract is the shared surface between API and clients; drift between them must be caught (see [0004](0004-github-actions-ci-per-part.md)).
- Whether the contract is written first (spec-first) or generated from the API's Zod schemas (code-first) is not decided here; see [0005](0005-contract-is-code-first.md).

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
| 2026-11-27 | Open spec-first vs code-first line now points to 0005 (decision unchanged) |
