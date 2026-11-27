# 0009 - Front request layers

**Status:** Accepted
**Date:** 2026-11-27

## Context

The next feature must call the API from every front app, but iOS and Android have no way to make a request. [ADR-0003](0003-skeleton-layout-apps-and-packages.md) deferred generated mobile clients to the first mobile screen that calls the API; that stays true. No screen calls the API in this work item (D6, superseding D2).

## Decision

- **Web** (D3, D11): light reorganization only. One request module per resource, outside components; `apiClient` remains the single entry point; no new dependency. `fetchHealth` moves to the new module; the screen is unchanged.
- **iOS and Android** (D6, D13): a hand-written request layer, not generated, with the same behaviour on both and web's error taxonomy:
  - 2xx with valid JSON returns the decoded value;
  - non-2xx is a typed HTTP error carrying the status code;
  - invalid or non-conforming JSON is a typed decoding error;
  - network error or timeout (10 s on mobile) is a typed transport error;
  - the base URL is configurable (dev: `http://localhost:3000` on the iOS simulator, `http://10.0.2.2:3000` on the Android emulator).
- **No new code calls `/health` on mobile**; screens are unchanged (D6).
- **Verification** (D12): unit tests with a stubbed transport. Create the `HarnessTests` target (iOS) and `src/test` (Android); iOS and Android CI run them.

## Rationale

With no screen calling the API, tests are the only proof. One error taxonomy everywhere lets the next feature declare an endpoint and a response type and nothing else.

## Alternatives Considered

- Front apps call `/health` and display it (D2): superseded by D6, since the front apps need not call health to prepare the next feature.
- Generated mobile clients: still deferred ([ADR-0003](0003-skeleton-layout-apps-and-packages.md)).

## Consequences

- Mobile offline cache, retry and a shared mobile design system stay out of scope (D4).
- Mobile CI now runs tests, not only builds.

## History

| Date | Change |
|---|---|
| 2026-11-27 | Created |
