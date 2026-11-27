# Harness

A monorepo with an API, a web app and two mobile apps, built around one OpenAPI contract that is generated from the API code.

| Part | Path |
|---|---|
| API (Bun, Hono) | `apps/api` |
| Web (React, Vite) | `apps/web` |
| iOS (SwiftUI) | `apps/ios` |
| Android (Compose) | `apps/android` |
| OpenAPI contract (generated) | `packages/contract` |
| Typed web client (generated types) | `packages/web-client` |

The contract is code-first: `apps/api` produces `packages/contract/openapi.json`, and `packages/web-client` derives its types from it. See [ADR 0005](docs/tech/adr/0005-contract-is-code-first.md) and the [docs](docs/README.md).

## Prerequisites

- All JS parts (API, web, packages): [`bun`](https://bun.sh).
- iOS: macOS with Xcode 16+.
- Android: Android Studio, a JDK 21 (use Android Studio's, e.g. `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` on macOS), and the Android SDK path, set either in `apps/android/local.properties` (`sdk.dir=/path/to/Android/sdk`) or with `ANDROID_HOME`.

## Run, test and build

Install the JS dependencies once, from the root:

```bash
bun install
```

| Part | Run | Test / build |
|---|---|---|
| Everything JS | `bun run dev` (API on :3000, web on :5173, `/api` proxied to the API) | `bun run test`, `bun run typecheck` |
| API | `bun run --filter @harness/api dev` | `bun run --filter @harness/api test` |
| Web | `bun run --filter @harness/web dev` | `bun run --filter @harness/web test`, `... build` |
| web-client | - | `bun run --filter @harness/web-client test` |
| iOS | open `apps/ios/Harness.xcodeproj` in Xcode and run | `xcodebuild -project apps/ios/Harness.xcodeproj -scheme Harness -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build` |
| Android | open `apps/android` in Android Studio and run | `cd apps/android && ./gradlew assembleDebug` |

After any change to the API routes or schemas, regenerate the contract and the client types and commit the result (CI fails otherwise):

```bash
bun run generate
```

Run the JS checks CI relies on (frozen install, typecheck, tests, generate, no diff in `packages/`):

```bash
bun run verify
```

## CI

One GitHub Actions workflow per part, each triggered only by the paths below (pull requests, and pushes to `main`).

| Workflow | Triggering paths |
|---|---|
| `api.yml` | `apps/api/**`, `packages/contract/**`, `packages/web-client/**`, `package.json`, `tsconfig.base.json`, `bun.lock`, the workflow |
| `web.yml` | `apps/web/**`, `packages/web-client/**`, `packages/contract/**`, `package.json`, `tsconfig.base.json`, `bun.lock`, the workflow |
| `ios.yml` | `apps/ios/**`, the workflow |
| `android.yml` | `apps/android/**`, the workflow |

## Contributor setup

### Agentic

The project vendors the [agentic](https://github.com/JohannWilfridCalixte/agent-skills) skills in `.agents/skills/`, linked from `.claude/skills/`: they work after a clone. They need [`bun`](https://bun.sh).

To update them (requires Node.js for `npx`), then commit the result in a PR:

```bash
npx skills@latest update -p -y
```

The project config is in [`.agentic.json`](.agentic.json). To set your own models, run `/agentic-configure --global`.
