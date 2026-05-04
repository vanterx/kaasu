# CLAUDE.md — Kaasu Expense Tracker

Kaasu is an offline-first Android expense tracker built with Kotlin, Jetpack Compose, Room, and Material 3.

See [AGENTS.md](AGENTS.md) for build commands, project structure, and architecture overview.

## Conventions Claude Must Follow

- **No Hilt/Dagger** — manual DI via `ExpenseApp`. Pass new dependencies through the existing factory pattern.
- **No third-party chart libraries** — charts are drawn with Compose `Canvas` in `ui/chart/`.
- **No LiveData** — use `StateFlow`/`Flow` exclusively. Prefer `SharingStarted.WhileSubscribed(5_000)` over `SharingStarted.Lazily` for screen-bound StateFlows.
- **No storage permissions** — file operations go through `ActivityResultContracts` (SAF).
- **Kotlin-first** — avoid `java.util.Calendar`; prefer `java.time.*` (API 26+). Never use `!!`.
- **Immutable state** — use `data class.copy()` for state updates, never mutate in place.
- **No hardcoded signing credentials** — load from `keystore.properties` (see Signing section below).

## Testing

Test directories (`src/test/`, `src/androidTest/`) are empty. For any new ViewModel or Repository code, write JUnit 4 + MockK unit tests. Use `kotlinx-coroutines-test` (`runTest`) for coroutine flows.

```kotlin
// ViewModel test pattern
@Test
fun `saving expense updates list`() = runTest {
    val repo = mockk<ExpenseRepository>(relaxed = true)
    val vm = ExpenseViewModel(repo, mockk(relaxed = true))
    vm.saveExpense(10.0, "Coffee", null, System.currentTimeMillis())
    coVerify { repo.saveExpense(any()) }
}
```

## Signing

Signing credentials live in `keystore.properties` at the repo root (git-ignored). Never hardcode passwords in `build.gradle.kts`.

`keystore.properties` format:
```properties
storeFile=kaasu.keystore
storePassword=<password>
keyAlias=kaasu
keyPassword=<password>
```

For CI, write `keystore.properties` from GitHub Secrets before the build step.

## Known Issues / Technical Debt

- `dateMillis` column has no database index — month-range queries do full scans. Add `@Index(value = ["dateMillis"])` to the `Expense` entity if performance becomes a concern.
- Day-boundary midnight calculation is duplicated three times in `ExpenseViewModel` — extract to a `Formatters.kt` helper when touching that file.
- Release build has `isMinifyEnabled = false` — enable R8 shrinking before a production release.
