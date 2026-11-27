# 0001 - Harness is a sandbox to test the agentic workflow

**Status:** Accepted
**Date:** 2026-11-27

## Context

The repository holds only agentic tooling: no product code, no fixed business domain. The question was what product the monorepo serves and for whom (decision D1, work item `20261127-monorepo-setup`).

## Decision

Harness is a sandbox project to test the agentic workflow and save development time. Features come ad hoc, as experiments. There is no fixed business domain.

## Rationale

The owner wants a playground to try things, not a defined product.

## Consequences

- Features are separate work items, each an experiment; the skeleton stays free of business logic.
- Agents must be able to build and run every part without help.

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
