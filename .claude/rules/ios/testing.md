---
paths:
  - "apps/ios/**"
---

# iOS Testing Rules

Testing conventions for the iOS app in `apps/ios/`. Cite sections as `ios/testing §N`.

## 1) Framework

- Swift Testing: `import Testing`, `@Test`, `#expect(...)`, `#require(...)`.
- No XCTest for new tests.

## 2) Test structure

- One top-level `struct` per domain (example: `HomeViewModelTests`).
- Shared setup goes in the struct `init()`; dependencies are instance properties.
- No helper factories like `makeX()` when `init()` is enough.
- Mark the struct `@MainActor` when the subject is main-actor isolated.

```swift
@MainActor
struct HomeViewModelTests {
  let sut: HomeViewModel

  init() {
    self.sut = HomeViewModel()
  }
}
```

## 3) Group by behaviour

- Group related tests in nested `enum` blocks, one enum per behaviour area.

## 4) Naming

- Preferred: `givenX_whenY_thenZ`.
- Short `state_return_result` is accepted for simple cases (`listIsEmpty_return_placeholder`).
- lowerCamelCase; describe behaviour, not implementation.

## 5) Assertions and scope

- One behaviour per test; never combine unrelated scenarios.
- Prefix with `expected` only for reference values set up by the test (`expectedUser`).
- Never use `expected` for values observed from the system under test (`foundUser`, `receivedEvent`).

## 6) Mocks

- Reusable mocks and test models live in `HarnessTests/Mock/`.
- Keep mocks minimal and reuse existing ones before adding new ones.

## 7) Stability

- Deterministic: no randomness, no timing or sleeps.
- Prefer verifying state over logs.

## 8) Commands

Single source for build and test commands; other docs point here.

No test target exists yet (ADR 0003: build only until the first real screen). Build:

```bash
xcodebuild -project apps/ios/Harness.xcodeproj -scheme Harness -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```

Once a `HarnessTests` target exists, run tests with an available iPhone simulator (list them with `xcrun simctl list devices available`):

```bash
xcodebuild test -project apps/ios/Harness.xcodeproj -scheme Harness -destination 'platform=iOS Simulator,name=<an available iPhone>' CODE_SIGNING_ALLOWED=NO
```
