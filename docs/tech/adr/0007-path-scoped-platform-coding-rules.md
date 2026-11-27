# 0007 - Path-scoped coding rules per platform, SwiftRouting for iOS navigation

**Status:** Accepted
**Date:** 2026-11-27

## Context

Agents and contributors writing code in `apps/ios`, `apps/android` and `apps/web` had no written conventions; only PR conventions existed (work item `20261127-platform-rules`, decisions D1–D13).

## Decision

- Each platform has two rule files in `.claude/rules/{ios,android,web}/`: code conventions and testing. Each file is scoped with `paths:` frontmatter to its app, so it loads only when working on that app. They are for agents and humans alike.
- iOS and Android rules are modelled on [swift-routing](https://github.com/lowki93/swift-routing)'s `ai-rules/` (conventions, testing), Android translated to Kotlin/Compose. Web rules capture the patterns already in `apps/web`.
- iOS navigation uses the SwiftRouting library. The package is added to the Xcode project with the first screen that navigates.
- The Swift skills `swift-routing`, `swift-testing-expert` and `swift-concurrency` are vendored in `.agents/skills/` and pinned in `skills-lock.json`.
- Rules cover code and tests only, not process (planning, versioning, tickets).

## Rationale

Path scoping keeps each app's rules out of the other apps' context. Vendoring the skills makes them available after a clone instead of on one machine.

## Alternatives Considered

- One root `CLAUDE.md` indexing `ai-rules/*.md` (swift-routing's layout): rejected, every rule would load for every task.
- Linter/formatter enforcement (SwiftLint, ktlint, ESLint/Biome): deferred until a platform gets its first real feature.
- Global (user-level) Swift skills: rejected, contributors would lack them.

## Consequences

- Reviewers can cite a rule file and section.
- Rules are not machine-enforced yet; drift is caught in review.

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
