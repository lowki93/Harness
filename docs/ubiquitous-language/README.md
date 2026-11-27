# Glossary

| Term | Definition |
|---|---|
| Contract | The OpenAPI description of the API surface, in `packages/contract/`, shared by the API and its clients and served as Swagger UI docs. |
| Part | One independently built and checked unit of the monorepo: an app or a package; CI runs per part. |
| App | A runnable part under `apps/` (api, web, ios, android). |
| Package | A shared, non-runnable part under `packages/` (contract, web-client). |
| Generated client | Code produced from the contract (e.g. `packages/web-client`), never edited by hand. |
| Skeleton | The first work item's minimal set of parts, with no business feature. |
