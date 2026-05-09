# Contributing to Kaasu

## Setup

After cloning, install the project git hooks:

```bash
bash scripts/install-hooks.sh
```

This installs a pre-commit hook that reminds you to tag a release when:
- `versionName` is bumped in `app/build.gradle.kts` — prints the exact tag command to run after merging
- 10+ commits have accumulated since the last release tag — suggests tagging

The hook is advisory only (never blocks a commit).

---

## Workflows

### 1. Branching

Always work on a branch off `main`. Name branches by type:

```
feat/short-description        # new feature
fix/short-description         # bug fix
refactor/short-description    # refactor, no behaviour change
review/short-description      # design or code review changes
chore/short-description       # build, config, tooling
```

```powershell
git checkout main
git pull origin main
git checkout -b feat/my-feature
```

---

### 1b. Module boundaries

The project uses a multi-module structure. Follow this rule when deciding where code lives:

| Layer | Module | Can import |
|-------|--------|-----------|
| Domain models & interfaces | `:core:domain` | `:core:common` only |
| Room DB, DAOs, entities | `:core:database` | `:core:domain` |
| Repository impls, mappers | `:core:data` | `:core:domain` + `:core:database` |
| Theme, formatters, shared UI | `:core:ui` | — |
| Feature screens & ViewModels | `:feature:*` | `:core:domain` + `:core:ui` |
| DI wiring, NavGraph | `:app` | all modules |

**Hard rule:** Feature modules must never import `:core:data` or `:core:database`. If a feature needs data, it goes through a `:core:domain` interface. Only `:app` wires the implementation via `DataModule`.

---

### 2. Commits

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>: <short description>

<optional body>
```

| Type | When to use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code change with no behaviour change |
| `chore` | Build, config, tooling, version bump |
| `docs` | Documentation only |
| `test` | Adding or fixing tests |
| `perf` | Performance improvement |
| `ci` | CI/CD workflow changes |

Examples:
```
feat: add recurring expense support
fix: show currency code instead of hardcoded dollar sign
chore: bump versionName to 1.2.0
ci: decode keystore with printf to avoid CRLF issues
```

---

### 3. Pull Requests

1. Push your branch and open a PR against `main`:
   ```powershell
   git push -u origin feat/my-feature
   gh pr create --title "feat: ..." --body "..."
   ```

2. The **CI Build** workflow runs automatically on every PR — it must pass before merging.

3. Merge using **squash merge** to keep `main` history clean:
   ```powershell
   gh pr merge <number> --squash --delete-branch
   ```

4. Pull `main` locally after merge:
   ```powershell
   git checkout main && git pull origin main
   ```

---

### 4. Testing

Unit tests live in `:core:domain` — pure Kotlin, no emulator required.

```powershell
# Run domain use-case tests only (~3 s)
.\gradlew :core:domain:test

# Run all unit tests across all modules
.\gradlew test
```

**Test stack:** JUnit 4 + MockK + kotlinx-coroutines-test (declared in `gradle/libs.versions.toml`).

**Convention:** every new use case class in `:core:domain` must ship with a matching test file in `core/domain/src/test/`. Every bug fix in the domain layer adds a regression test. CI blocks the PR if any test fails.

Adding tests for Android ViewModels and Compose screens (Robolectric or instrumented) is tracked as tech debt item #8 in `backlog/improvements.md`.

---

### 5. CI Build

**Trigger:** push to `main` or any pull request targeting `main`.

**Skipped for:** commits that only change `.md` files or `docs/`.

**Steps:**
1. Checkout code
2. Set up JDK 17 (Temurin)
3. Restore Gradle cache
4. Decode keystore from `KEYSTORE_BASE64` secret
5. Write `keystore.properties` from secrets
6. `./gradlew test` — unit tests (fails build if any test fails; results uploaded as artifact)
7. `./gradlew assembleDebug`
8. Upload `Kaasu-debug.apk` as a build artifact

A green CI run is required before merging any PR.

---

### 6. Release

**Trigger:** pushing a `v*` tag to `main`.

```powershell
git tag v1.2.0
git push origin v1.2.0
```

**Steps:**
1. Same setup as CI (JDK, Gradle, keystore)
2. `./gradlew assembleRelease` — builds a signed release APK
3. Creates a GitHub Release at the tag with:
   - `Kaasu-release.apk` attached
   - Auto-generated release notes from merged PRs

**Before tagging a release:**
- Bump `versionName` in [app/build.gradle.kts](app/build.gradle.kts)
- Ensure all intended PRs are merged to `main`
- Confirm CI is green on `main`

---

### 7. Signing

Release and debug builds are both signed with the shared keystore. Credentials are never stored in the repo — they live in:

- **Locally:** `keystore.properties` (git-ignored) — create this file manually, see format below
- **CI/CD:** four GitHub Actions secrets (`KEYSTORE_BASE64`, `KEYSTORE_STORE_PASSWORD`, `KEYSTORE_KEY_ALIAS`, `KEYSTORE_KEY_PASSWORD`)

`keystore.properties` format:
```properties
storeFile=kaasu.keystore
storePassword=<password>
keyAlias=kaasu
keyPassword=<password>
```

> The keystore file (`app/kaasu.keystore`) is git-ignored. To build locally on a new machine, obtain the keystore file and `keystore.properties` from a team member.
