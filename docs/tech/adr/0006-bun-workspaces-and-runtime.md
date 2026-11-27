# 0006 - bun workspaces, runtime and test runner

**Status:** Accepted
**Date:** 2026-11-27

## Context

The monorepo needs a JS package manager and workspace tool for its JS packages (decision D19).

## Decision

bun workspaces. bun is also the API runtime and test runner. Root `bun run dev` starts API and web.

## Rationale

bun is already installed and required by agentic.

## Alternatives Considered

- pnpm: rejected, not installed.
- Turborepo: rejected, overkill for two JS packages.

## Consequences

- One tool for install, run and test on the JS side.
- The API depends on bun as its runtime.

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
