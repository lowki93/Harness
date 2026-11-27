# 0009 - Front request layers

**Status:** Accepted
**Date:** 2026-11-27

## Context

The next feature must call the API from every front app, but iOS and Android have no way to make a request. [ADR-0003](0003-skeleton-layout-apps-and-packages.md) deferred generated mobile clients to the first mobile screen that calls the API; that stays true. No screen calls the API in this work item (D6, superseding D2).

## Decision

- **Web** (D3, D11, D19): light reorganization only, no new dependency. `src/requests/health.ts` holds `fetchHealth`; `src/requests/errors.ts` maps `openapi-fetch` results to typed `HttpError(status)`, `TransportError` (rejected fetch) and `DecodingError` (unparsable JSON), with no runtime schema validation. `apiClient` stays in `src/api.ts` as the single entry point; the screen is unchanged.
- **iOS** (D20), modelled on a reference project, hand-written, not generated:
  - `Network/`: `Endpoint` protocol (one enum per resource), `HTTPMethod`, `APIConfiguration(baseURL)`, `HTTPSession` protocol (`URLSession` conforms), `APIClient` / `DefaultAPIClient.send(_:) async throws(APIError) -> Data` returning the raw body.
  - `APIError`: `invalidURL`, `invalidResponse`, `httpStatus(Int)`, `transport` (wraps `URLError`).
  - `AppDependencies` is the composition root and builds the `URLSession` with a 10 s timeout.
  - Pattern for the next feature, documented in `.claude/rules/ios/conventions.md` and `testing.md`: Endpoint, then `NetworkService` (decodes the `XAPIModel` DTO and maps it via `ModelConvertible`), then Repository, then ViewModel. No service or repository ships yet.
  - Tests: `DefaultAPIClientTests` with an actor `HTTPSessionMock` in `HarnessTests/Mock/`.
- **Android** (D21, D22): the same split in Kotlin.
  - `Endpoint` interface (path, method, query), `HttpMethod`, `ApiConfiguration(baseUrl)`, `HttpSession` interface (`suspend execute`) implemented on `HttpURLConnection` with 10 s timeouts and injected `Dispatchers.IO`, `ApiClient` / `DefaultApiClient.send(endpoint): ByteArray`, sealed `ApiError` (`InvalidUrl`, `HttpStatus(code)`, `Transport`), `AppDependencies` composition root. Same rules added under `.claude/rules/android/`.
  - Dependencies: `kotlinx-coroutines-core`; test: `junit`, `kotlinx-coroutines-test`. No `kotlinx-serialization` until the first service. `MockWebServer` dropped: the `HttpSession` seam covers it.
  - Configuration: base URL from `BuildConfig.API_BASE_URL` (debug `http://10.0.2.2:3000`); `INTERNET` permission; a debug-only `network_security_config` allows cleartext to `10.0.2.2`.
  - Tests: JUnit 4 with a hand-written `HttpSessionMock`.
- **Error taxonomy** (D24, superseding D13): mobile clients return the raw body on 2xx; non-2xx is a typed HTTP status error; network error or timeout (10 s on mobile) is a typed transport error. Decoding errors belong to services on mobile (verified with the first service) and to `src/requests/errors.ts` on web. The base URL is configurable (dev: `http://localhost:3000` on the iOS simulator, `http://10.0.2.2:3000` on the Android emulator).
- **No new code calls `/health` on mobile**; screens are unchanged (D6).
- **Verification** (D12, D23): unit tests with a stubbed transport. iOS: new `HarnessTests` target (Swift Testing) in the scheme, `ios.yml` runs `xcodebuild test` on a simulator. Android: `src/test`, `android.yml` adds `./gradlew testDebugUnitTest`.

## Rationale

With no screen calling the API, tests are the only proof. One transport taxonomy everywhere, with decoding in services, lets the next feature declare an endpoint, a DTO and a mapping and nothing else.

## Alternatives Considered

- Front apps call `/health` and display it (D2): superseded by D6, since the front apps need not call health to prepare the next feature.
- Generated mobile clients: still deferred ([ADR-0003](0003-skeleton-layout-apps-and-packages.md)).
- Decoding inside the mobile client (D13): replaced by D24; `send` returns raw data and services decode.
- `MockWebServer` on Android: the `HttpSession` seam covers it.

## Consequences

- Mobile offline cache, retry and a shared mobile design system stay out of scope (D4).
- Mobile CI now runs tests, not only builds.

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
| 2026-11-27 | Added web structure, iOS and Android layer designs, Android config, mobile CI, and the D24 error taxonomy replacing D13 (D19-D24) |
