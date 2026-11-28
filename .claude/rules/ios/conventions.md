---
paths:
  - "apps/ios/**"
---

# iOS Conventions

Swift and SwiftUI coding rules for the iOS app in `apps/ios/` (iOS 18, Swift 6). Cite sections as `ios/conventions §N`.

## 1) Naming

- PascalCase for types (`ContentView`, `HomeRoute`).
- lowerCamelCase for properties, methods and enum cases.
- Name by behaviour, not by implementation detail.

## 2) Access control

- `private` by default; widen to `internal` only when another file needs it.
- No `public` in the app target unless a module boundary requires it.

## 3) Concurrency

- Swift 6 strict concurrency: fix data-race warnings, do not silence them.
- `@MainActor` on view models, routers and other UI-bound types (SwiftUI `View` is already main-actor isolated).
- Types that cross isolation boundaries are `Sendable`.
- `final class` by default; prefer `struct` and `enum` for plain values.

## 4) Doc comments

- `///` on non-obvious types and functions: say what and why, not how.
- Skip them on self-evident members.

## 5) Navigation

All app navigation uses the SwiftRouting library (https://github.com/lowki93/swift-routing).

- Routes are `Route` enums.
- `RoutingView` sits at the root of the navigation hierarchy.
- `Router` performs push and present.
- No raw `NavigationStack` or `NavigationLink(destination:)` for app navigation.
- The SPM package is added with the first screen that navigates. It is not a dependency yet.
- Check the `swift-routing` skill for the exact API (routes, `TabRouter`, deeplinks, `@Routable` macros) before writing navigation code. Do not guess signatures.

## 6) Networking

Request layers follow ADR 0009: `Endpoint` → `NetworkService` → `Repository` → `ViewModel`. Nothing calls the API yet; the next feature adds its resource following these rules.

### Folder layout

- `Network/`: `Endpoint`, `HTTPMethod`, `APIConfiguration`, `HTTPSession`, `APIClient`, `APIError` (shared infrastructure).
- `Network/<Resource>/`: `<Resource>Endpoint` enum, `<Resource>NetworkService`, `<Resource>APIModel` DTOs.
- `ModelConvertible/`: `ModelConvertible` protocol and the array conversion strategy (`.allElements`, `.dropFailingElements`).
- `Repository/`: `<Domain>Repository` and `Default<Domain>Repository`.
- `Model/`: domain models.

### Endpoint

- One internal `enum` per resource conforming to `Endpoint`, one case per call.
- `path`, `method` and `queryItems` are `switch`-based; `method` and `queryItems` default to `.get` and `[]`.

### APIClient

- `send(_ endpoint: some Endpoint) async throws(APIError) -> Data` returns the raw body on 2xx.
- No decoding in the client. Failures are typed `APIError`: `.invalidURL`, `.invalidResponse`, `.httpStatus(Int)`, `.transport(URLError)` (`CancellationError` maps to `.transport(URLError(.cancelled))`).

### NetworkService

- `protocol <Resource>NetworkService: Sendable` plus a `Default<Resource>NetworkService` struct with `init(apiClient:decoder:)`.
- One method per call: pick the endpoint, `send`, decode the `<X>APIModel`, map to the domain model through `ModelConvertible` (`toModel()` for one value, `toDomainModel(.allElements)` or `.dropFailingElements` for arrays).
- Returns domain types only, never DTOs. Decoding errors propagate to the caller.

### DTO and domain

- `<X>APIModel` is `Decodable, Sendable`, internal, and mirrors the JSON exactly.
- Domain models are `Sendable, Hashable` value types and own their shape.
- The mapping lives on the DTO (`extension <X>APIModel: ModelConvertible`).

### Repository

- `protocol <Domain>Repository: Sendable` plus `Default<Domain>Repository`.
- Aggregates one or more network and storage services behind one facade for view models, and owns business rules (dedupe, limits, caching).
- Make it an `actor` when it holds mutable state.
- View models talk only to repositories, never to services or the `APIClient`.

### Dependency injection

- A protocol for every layer; dependencies are protocol-typed properties received through `init`.
- No DI framework. `@MainActor final class AppDependencies` is the composition root with `make…()` factories returning protocol types.
- It owns one shared `URLSession` (10 s request timeout) and builds the `JSONDecoder` (`convertFromSnakeCase`, `iso8601` dates when the API needs them) injected into services.
- No other layer reads `AppDependencies`.
- The base URL comes from `APIConfiguration.current` (`#if DEBUG`).

## 7) Skills to load

- Navigation or routing: `swift-routing`
- Concurrency: `swift-concurrency`
- Tests: `swift-testing-expert` and `.claude/rules/ios/testing.md`
