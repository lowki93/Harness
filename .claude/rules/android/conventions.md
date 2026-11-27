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

## 8) Skills to load

- None project-specific yet.
- Writing or changing tests: `.claude/rules/android/testing.md`.
