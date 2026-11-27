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

## 6) Skills to load

- Navigation or routing: `swift-routing`
- Concurrency: `swift-concurrency`
- Tests: `swift-testing-expert` and `.claude/rules/ios/testing.md`
