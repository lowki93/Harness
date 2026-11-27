# Glossary

| Term | Definition |
|---|---|
| Contract | The OpenAPI description of the API surface, in `packages/contract/`, shared by the API and its clients and served as Swagger UI docs. |
| Part | One independently built and checked unit of the monorepo: an app or a package; CI runs per part. |
| App | A runnable part under `apps/` (api, web, ios, android). |
| Package | A shared, non-runnable part under `packages/` (contract, web-client). |
| Generated client | Code produced from the contract (e.g. `packages/web-client`), never edited by hand. |
| Skeleton | The first work item's minimal set of parts, with no business feature. |
| Contract drift | The committed contract no longer matches what the API generates; CI fails until it is regenerated. |
| Migration | A versioned, ordered change to the database schema, applied by the API's `migrate` command; rerunning with none pending does nothing. |
| Request layer | The code in a front app that calls the API and returns a decoded value or a typed error (HTTP, decoding or transport); screens never call the network directly. |
| Database check | The part of `GET /health` that tests the database connection and reports `database: ok` or `unreachable`. |
| Endpoint | A description of one API call (path, method, query) in a mobile request layer, declared as one enum per resource and sent by the API client. |
| API model (DTO) | The `XAPIModel` type that mirrors a JSON response exactly, decoded by a network service and then mapped to a domain model. |
| Network service | A mobile class that sends an Endpoint through the API client, decodes the raw body into an API model and maps it to a domain model. |
| Repository | A mobile class that exposes domain models to view models by calling network services, hiding where the data comes from. |
