---
paths:
  - "apps/android/**"
---

# Android Testing Rules

JUnit 4 testing conventions for `apps/android`. Cite sections by number (for example "testing 3").

## 1) Framework

- JUnit 4, the Android default. No extra plugin.
- Local unit tests live in `apps/android/app/src/test/java/com/harness/app/`, mirroring the production package.

## 2) Test structure

- One test class per domain (example: `RouterTests`).
- Keep shared setup in `@Before` or in property initialisers.
- Prefer properties for dependencies; avoid `makeX()` helpers when the class setup is enough.

## 3) Group tests by behaviour

- Annotate the outer class with `@RunWith(Enclosed::class)`.
- One nested class per behaviour area (the equivalent of nested enums in swift-routing). Nested classes are static, so they set up their own subject.

```kotlin
@RunWith(Enclosed::class)
class RouterTests {

    class CurrentRoute {
        private val router = Router(Configuration.default)

        @Test
        fun givenEmptyPath_whenReadingCurrentRoute_thenReturnsRoot() {
            assertEquals(Route.Root, router.currentRoute)
        }
    }

    class Push {
        // push tests
    }
}
```

## 4) Naming

- `givenX_whenY_thenZ`, in lowerCamelCase.
- No backtick names: they break on older Android tooling.
- Describe behaviour, not implementation.

## 5) Assertions and scope

- One behaviour per test; do not combine unrelated scenarios.
- Use clear expected values.
- Prefix with `expected` only reference values set up by the test (`expectedUser`). Never prefix observed outputs (`foundUser`, `receivedEvent`).

## 6) Mocks

- Reusable mocks and test models live in `apps/android/app/src/test/java/com/harness/app/mock/<layer>/` (`network/` today).
- A mock is a hand-written class implementing the interface, built from a `Result` and recording calls in a list exposed read-only (`HttpSessionMock` records `requests`). No mocking library.
- Keep mocks minimal and reuse existing ones before adding new ones.

### One test class per layer

- `DefaultApiClientTests` with `HttpSessionMock`.
- `<Resource>EndpointTests`: path, method, query per case.
- `<Resource>NetworkServiceTests` with `ApiClientMock` and JSON fixtures.
- `<X>ApiModelTests`: DTO to domain mapping.
- `<Domain>RepositoryTests` with network service mocks.
- ViewModel tests with repository mocks.

### Fixtures

- JSON fixtures live in `apps/android/app/src/test/resources/fixtures/` and mirror real API responses.

## 7) Determinism

- No randomness, no real time, no sleeping.
- Test coroutines with `runTest` and injected dispatchers (conventions 3).
- Prefer state verification over logs.

## 8) Commands

Single source for Android build and test commands; other docs point here.

- Prerequisites (see README): JDK 21 (Android Studio's works) and the Android SDK path via `apps/android/local.properties` (`sdk.dir=...`) or `ANDROID_HOME`.
- Build: `cd apps/android && ./gradlew assembleDebug` (add `assembleRelease` to check the release variant)
- Unit tests: `cd apps/android && ./gradlew testDebugUnitTest`

Tests use JUnit 4, `kotlinx-coroutines-test` and the dependencies declared in the version catalog. CI runs `./gradlew testDebugUnitTest assembleDebug`.
