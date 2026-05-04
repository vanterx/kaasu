# CLAUDE.md — Kaasu Expense Tracker

Kaasu is an offline-first Android expense tracker built with Kotlin, Jetpack Compose, Room, and Material 3. Premium light luxury design — warm cream surfaces, gold accent, K monogram launcher icon.

See [AGENTS.md](AGENTS.md) for build commands, project structure, and architecture overview.
See [CONTRIBUTING.md](CONTRIBUTING.md) for branching, PR, CI, release, and signing workflows.

## Conventions Claude Must Follow

- **No Hilt/Dagger** — manual DI via `ExpenseApp`. Pass new dependencies through the existing factory pattern.
- **No third-party chart libraries** — charts are drawn with Compose `Canvas` in `ui/chart/`.
- **No LiveData** — use `StateFlow`/`Flow` exclusively. Prefer `SharingStarted.WhileSubscribed(5_000)` over `SharingStarted.Lazily` for screen-bound StateFlows.
- **No storage permissions** — file operations go through `ActivityResultContracts` (SAF).
- **No dynamic color** — `dynamicColor` is disabled by design; the premium palette must always apply. Do not re-enable it.
- **Kotlin-first** — avoid `java.util.Calendar`; prefer `java.time.*` (API 26+). Never use `!!`.
- **Immutable state** — use `data class.copy()` for state updates, never mutate in place.
- **No hardcoded signing credentials** — load from `keystore.properties` (see CONTRIBUTING.md).
- **Theme-aware colors only** — use `MaterialTheme.colorScheme.*` tokens, never hardcode `Color.Red`, `Color.Gray`, `Color.Black`.

## Design System

The app uses a custom premium palette — do not revert to the default M3 purple/pink template:

| Token | Light | Dark | Role |
|---|---|---|---|
| `primary` | `#B8965A` | `#D4A96A` | Gold — amounts, accent, FAB |
| `surface` | `#FAF8F4` | `#1C1A17` | Page background |
| `surfaceContainer` | `#F2EDE4` | `#252220` | Card fill |
| `onSurface` | `#1C1A17` | `#E8E0D4` | Primary text |
| `onSurfaceVariant` | `#6B6560` | `#B5ADA5` | Labels, secondary text |
| `outline` | `#C8BFB4` | `#7A736C` | Card borders |
| `error` | `#C0392B` | `#E57373` | Delete, errors |

Cards use `border = BorderStroke(1.dp, colorScheme.outlineVariant)` with zero elevation — not shadow-based elevation.

## Testing

Test directories (`src/test/`, `src/androidTest/`) are empty. For any new ViewModel or Repository code, write JUnit 4 + MockK unit tests. Use `kotlinx-coroutines-test` (`runTest`) for coroutine flows.

```kotlin
@Test
fun `saving expense updates list`() = runTest {
    val repo = mockk<ExpenseRepository>(relaxed = true)
    val vm = ExpenseViewModel(repo, mockk(relaxed = true))
    vm.saveExpense(10.0, "Coffee", null, System.currentTimeMillis())
    coVerify { repo.saveExpense(any()) }
}
```

## Known Technical Debt

- `dateMillis` has no database index — month-range queries do full scans. Add `@Index(value = ["dateMillis"])` to the `Expense` entity if performance degrades.
- Day-boundary midnight calculation is duplicated in `ExpenseViewModel` — extract to `Formatters.kt` when touching that file.
- Release build has `isMinifyEnabled = false` — enable R8 shrinking before a Play Store release.
- `android.app.DatePickerDialog` (legacy View) used instead of M3 `DatePicker` composable in expense screens.
- `"add_expense"` / `"edit_expense"` navigation routes are string literals — should be sealed `Screen` objects for consistency.
- No unit tests exist for any ViewModel or Repository.
