# ADRs

| Entry | Description |
|---|---|
| [0001](0001-harness-is-an-agentic-sandbox.md) | Harness is a sandbox to test the agentic workflow; features are experiments |
| [0002](0002-monorepo-around-a-shared-openapi-contract.md) | Monorepo evolving API and clients around a shared OpenAPI contract |
| [0003](0003-skeleton-layout-apps-and-packages.md) | Skeleton layout: apps/{api,web,ios,android}, packages/{contract,web-client} |
| [0004](0004-github-actions-ci-per-part.md) | GitHub Actions CI per part on every PR, path-filtered |
| [0005](0005-contract-is-code-first.md) | Contract is code-first: Hono + Zod generate the committed OpenAPI file, CI checks drift |
| [0006](0006-bun-workspaces-and-runtime.md) | bun workspaces; bun is also the API runtime and test runner |
| [0007](0007-path-scoped-platform-coding-rules.md) | Path-scoped coding rules per app (iOS, Android, web, API); SwiftRouting for iOS navigation; Swift skills vendored |
| [0008](0008-api-database-foundation-via-kysely.md) | API database foundation: Kysely, migrate command, database check in `/health`; PostgreSQL via `pg` |
| [0009](0009-front-request-layers.md) | Request layers: web request modules, hand-written iOS and Android layers (Endpoint, APIClient, HTTPSession seam) with a shared error taxonomy |
