---
paths:
  - "apps/android/**"
---

# Android Conventions

Kotlin and Jetpack Compose coding rules for `apps/android`. Cite sections by number (for example "conventions 3").

## 1) Naming

- PascalCase for types and `@Composable` functions (`HomeScreen`, `UserRow`).
- lowerCamelCase for properties, functions and parameters.
- `UPPER_SNAKE_CASE` for `const val` and top-level constants.

## 2) Visibility

- `private` by default, `internal` when another file in the module needs it.
- `public` only for what a module boundary requires.
- Exception: classes the Android framework instantiates from the manifest (`Activity`, `Application`, `Service`, `BroadcastReceiver`) stay public.
- Kotlin classes are final by default: keep them. Use `open` or `abstract` only with a stated reason.

## 3) Threading and coroutines

- Touch UI state on the main thread only; launch UI work with `viewModelScope` or `Dispatchers.Main`.
- Run I/O on `Dispatchers.IO`, but inject the dispatcher instead of hard-coding it, so tests can replace it.

```kotlin
internal class UserRepository(private val io: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun load(): User = withContext(io) { /* ... */ }
}
```

## 4) UI state

- Model UI state as an immutable `data class` (`val` properties only).
- Expose it from the `ViewModel` as `StateFlow`; keep the mutable flow `private`.

## 5) Compose

- Composables are stateless: hoist state to the caller and pass values and callbacks in.
- `Modifier` is the first optional parameter, defaulting to `Modifier`.

```kotlin
@Composable
internal fun UserRow(name: String, onClick: () -> Unit, modifier: Modifier = Modifier) { /* ... */ }
```

## 6) Documentation

- KDoc (`/** */`) on non-obvious types and functions: say why and the contract, not what the name already says.
- Public API always gets KDoc.

## 7) Dependencies

- Declare every dependency and plugin in `gradle/libs.versions.toml` and reference it through the version catalog. No inline versions in build files.

## 8) Networking

Request layers follow ADR 0009: `Endpoint` → `NetworkService` → `Repository` → `ViewModel`. Nothing calls the API yet; the next feature adds its resource following these rules.

### Folder layout

Under `app/src/main/java/com/harness/app/`:

- `network/`: `Endpoint`, `HttpMethod`, `ApiConfiguration`, `HttpSession`, `UrlConnectionHttpSession`, `ApiClient`, `ApiError` (shared infrastructure).
- `network/<resource>/`: `<Resource>Endpoint` sealed class or enum, `<Resource>NetworkService`, `<Resource>ApiModel` DTOs.
- `repository/`: `<Domain>Repository` and `Default<Domain>Repository`.
- `model/`: domain models.

### Endpoint

- One `sealed class` (or `enum`) per resource implementing `Endpoint`, one case per call.
- `path`, `method` and `queryItems` are `when`-based; `method` and `queryItems` default to `GET` and an empty list.

### ApiClient

- `suspend fun send(endpoint: Endpoint): ByteArray` returns the raw body on 2xx.
- No decoding in the client. Failures are a typed `sealed class ApiError : Exception`: `InvalidUrl`, `HttpStatus(code)`, `Transport(cause)` (any non-cancellation `Exception`, `IOException` and timeouts included). `CancellationException` is always rethrown, never wrapped in `Transport`.
- `UrlConnectionHttpSession` is the only `HttpURLConnection` user: 10 s connect and read timeouts, reads `errorStream` on non-2xx, disconnects in `finally`, runs on the injected dispatcher.

### NetworkService

- `interface <Resource>NetworkService` plus a `Default<Resource>NetworkService` class with constructor `(apiClient: ApiClient, decoder: ...)`. The decoding library is chosen with the first service (no serialization dependency yet).
- One method per call: pick the endpoint, `send`, decode the `<X>ApiModel`, map to the domain model with a `toModel()` extension on the DTO.
- Returns domain types only, never DTOs. Decoding errors propagate to the caller.

### DTO and domain

- `<X>ApiModel` is an immutable `data class` that mirrors the JSON exactly.
- Domain models are immutable `data class`es and own their shape.
- The mapping lives next to the DTO (`internal fun <X>ApiModel.toModel(): <X>`).

### Repository

- `interface <Domain>Repository` plus `Default<Domain>Repository`.
- Aggregates one or more network and storage services behind one facade for view models, and owns business rules (dedupe, limits, caching).
- View models talk only to repositories, never to services or the `ApiClient`, and expose `StateFlow` UI state (conventions 4).

### Dependency injection

- An interface for every layer; dependencies are constructor parameters typed by interface.
- No DI framework. `AppDependencies` is the composition root with `make…()` factories returning interface types.
- It injects the dispatcher (`Dispatchers.IO`, conventions 3) and builds the decoder injected into services.
- No other layer reads `AppDependencies`.
- The base URL comes from `BuildConfig.API_BASE_URL` per build type: debug `http://10.0.2.2:3000` (cleartext allowed to 10.0.2.2 only by the debug `network_security_config`); release is a placeholder until an API is deployed.

## 9) Skills to load

- None project-specific yet.
- Writing or changing tests: `.claude/rules/android/testing.md`.
