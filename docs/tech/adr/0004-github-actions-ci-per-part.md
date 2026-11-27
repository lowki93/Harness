# 0004 - GitHub Actions CI per part, path-filtered

**Status:** Accepted
**Date:** 2026-11-27

## Context

The skeleton must stay working, and agentic PR follow-up and merge rely on CI proof (decision D7).

## Decision

GitHub Actions checks each part on every PR, and only when its folder changes. A change to `packages/contract/` runs the API and web jobs; a change under `apps/ios/` runs only iOS.

## Rationale

Keeps the skeleton working with fast feedback; mobile builds are slow, so unrelated parts are skipped.

## Consequences

- API and web run tests; iOS and Android are only built (D17).
- CI fails when the contract or generated client is out of date, and names the regeneration command (D14).
- Every job reports its result on the PR.

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
