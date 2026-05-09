# Architectural Patterns — Kaasu

Patterns extracted from the codebase. These repeat across multiple files and define how the app works.

---

## 1. Manual DI via DataModule + Application class

All singleton dependencies are created in `DataModule` (`:core:data`), which is instantiated lazily in `ExpenseApp`. `MainActivity` passes the module to `NavGraph`, which wires ViewModel factories.

**Files:** `core/data/.../DataModule.kt`, `app/.../ExpenseApp.kt`, `app/.../NavGraph.kt`

```
ExpenseApp (Application)
  └── dataModule: DataModule (lazy)
        ├── expenseRepository: ExpenseRepository   (ExpenseRepositoryImpl, internal)
        ├── categoryRepository: CategoryRepository (CategoryRepositoryImpl, internal)
        ├── preferencesManager: PreferencesManager
        ├── getFilteredExpensesUseCase: GetFilteredExpensesUseCase
        ├── saveExpenseUseCase: SaveExpenseUseCase
        └── deleteExpenseUseCase: DeleteExpenseUseCase
              ↓
        MainActivity → NavGraph(dataModule)
              ↓
        NavGraph creates ViewModels via ViewModelProvider.Factory(use cases)
```

Repository impls are `internal` to `:core:data`. Feature modules only see the interfaces from `:core:domain`. Only `:app` depends on `:core:data`.

---

## 2. Module dependency graph

```
:app
  ├── :feature:expense   ← ExpenseListScreen, AddEditExpenseScreen, ExpenseViewModel
  ├── :feature:chart     ← ChartScreen, ChartViewModel
  ├── :feature:category  ← CategoryScreen, CategoryViewModel
  ├── :feature:settings  ← SettingsScreen
  ├── :feature:export    ← ExportScreen, ExportViewModel, CsvExporter
  └── :core:data         (for DI wiring only)

Each :feature:* depends on:
  ├── :core:domain       ← repository interfaces, domain models, use cases
  └── :core:ui           ← theme, Formatters, CurrencyPickerDialog

:core:data depends on:
  ├── :core:domain       (re-exported via api())
  └── :core:database     ← Room DB, DAOs, entities (internal)

:core:database depends on:
  └── :core:domain       (for CategoryTotal, AccountTotal in DAO queries)

:core:common — AppResult (pure Kotlin, no Android)
```

**Rule:** Feature modules must never import `:core:data` or `:core:database` directly.

---

## 3. Interface-segregated repository layer with domain models

Repository interfaces live in `:core:domain` and reference only domain models (no Room annotations). Implementations in `:core:data` are `internal` and map between Room entities and domain models.

**Files:** `core/domain/.../repository/ExpenseRepository.kt`, `core/data/.../repository/ExpenseRepositoryImpl.kt`

```kotlin
// :core:domain — visible to all modules
interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseItem>>
    suspend fun saveExpense(amount, description, categoryId, dateMillis, account): Long
    // ...
}

// :core:data — internal, only :app can wire it
internal class ExpenseRepositoryImpl(private val dao: ExpenseDao) : ExpenseRepository {
    override fun getAllExpenses() = dao.getAllExpenses().map { it.toDomain() }
    // ...
}
```

---

## 4. Thin use cases in :core:domain

Business logic that was in ViewModels now lives in use case classes in `:core:domain`. They are pure Kotlin (no Android), injected by `DataModule`, and passed to ViewModel factories.

**Files:** `core/domain/.../usecase/GetFilteredExpensesUseCase.kt`, `SaveExpenseUseCase.kt`, `DeleteExpenseUseCase.kt`

- `GetFilteredExpensesUseCase.execute(...)` — filtering, sorting, grouping (extracted from `ExpenseViewModel`)
- `SaveExpenseUseCase.save/update(...)` — validation (`amount > 0`) + repository call
- `DeleteExpenseUseCase(id)` — thin wrapper enabling testability

Pure Kotlin means these are unit-testable without an emulator: `.\gradlew :core:domain:test`.

---

## 5. Room Entity → Domain Model → Compose UI pipeline

Room-annotated entities (`ExpenseEntity`, `CategoryEntity`) never leave `:core:database`. Mappers in `:core:data` convert them to domain models. UI only sees domain types.

**Files:** `core/database/.../entity/`, `core/data/.../mapper/Mappers.kt`, `core/domain/.../model/`

```
ExpenseWithCategoryEntity (Room relation)    core/database/entity/
        │
        ▼ .toDomain()                        core/data/mapper/Mappers.kt (internal)
ExpenseItem (pure Kotlin)                    core/domain/model/ExpenseItem.kt
        │
        ▼ via DailyGroup StateFlow
Compose UI (ExpenseCard)                     feature/expense/.../ExpenseListScreen.kt
```

---

## 6. StateFlow-only reactive streams

No `LiveData`. No `SharedFlow`. All observable state is `StateFlow<T>`.

**Files:** `feature/expense/.../ExpenseViewModel.kt`, `feature/chart/.../ChartViewModel.kt`

```kotlin
private val _foo = MutableStateFlow(init)
val foo: StateFlow<T> = _foo.asStateFlow()

val derived: StateFlow<U> = _foo
    .combine(other) { a, b -> compute(a, b) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

All `stateIn` calls use `WhileSubscribed(5_000)` — upstream stops 5 s after last subscriber leaves.

---

## 7. Day-based expense navigation

**Files:** `feature/expense/.../ExpenseViewModel.kt`, `feature/expense/.../ExpenseListScreen.kt`

Default view is today's expenses. Arrow buttons navigate prev/next day; M3 `DatePicker` composable jumps to any date. Month switching resets the selected day.

`dayStartMillis(millis)` in `core/ui/.../Formatters.kt` is the shared helper for truncating any timestamp to midnight — used by ViewModels and composables alike.

---

## 8. All navigation routes as sealed Screen objects

**Files:** `app/.../NavGraph.kt`

All routes — bottom tabs and overlay screens — are declared as `sealed class Screen` objects. No raw string literals anywhere in navigation calls.

```kotlin
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Expenses   : Screen("expenses",    "Expenses", ...)
    data object Charts     : Screen("charts",      "Reports",  ...)
    data object Export     : Screen("export",      "Export",   ...)
    data object AddExpense : Screen("add_expense", "",         ...)
    data object EditExpense: Screen("edit_expense","",         ...)
    data object Settings   : Screen("settings",   "",         ...)
}
```

Bottom bar visibility computed by checking `currentDestination.hierarchy` against `bottomNavScreens`.

---

## 9. Compose Canvas custom charts (no third-party libs)

**Files:** `feature/chart/.../ChartScreen.kt`, `core/ui/.../theme/Color.kt`

- Donut/pie: `Canvas` + `drawArc()` with `Stroke(width)`, center text via native `android.graphics.Paint`
- Bar charts: `Box` with `fillMaxWidth(fraction)` nested inside a background `Box`
- 12-color palette: `ChartColors` in `Color.kt`, indexed by `Category.colorIndex % ChartColors.size`

---

## 10. SAF for file operations (no storage permissions)

**Files:** `feature/export/.../ExportScreen.kt`, `feature/export/.../ExportViewModel.kt`, `feature/export/.../util/CsvExporter.kt`

CSV export uses `ActivityResultContracts.CreateDocument("text/csv")`. Export work runs in `ExportViewModel.viewModelScope` via `withContext(Dispatchers.IO)` — no raw `CoroutineScope`. No `READ/WRITE_EXTERNAL_STORAGE` permissions needed.

---

## 11. Category seeding on first launch

**Files:** `core/data/.../repository/CategoryRepositoryImpl.kt`, `app/.../ExpenseApp.kt`

`seedDefaultCategories()` checks `dao.getCategoryCount() == 0` then inserts 8 fixed categories with `colorIndex` 0–7. Called from `ExpenseApp.onCreate()` in `applicationScope` (IO dispatcher).

---

## 12. Database migration with version numbering

**Files:** `core/database/.../ExpenseDatabase.kt`

```kotlin
@Database(version = 3)
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db) { db.execSQL("ALTER TABLE expenses ADD COLUMN account TEXT") }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_dateMillis ON expenses (dateMillis)")
    }
}
```

No `fallbackToDestructiveMigration()` — data preserved across upgrades. Current version: **3**.

---

## 13. Zero-elevation card design

All cards: `BorderStroke(1.dp, colorScheme.outlineVariant)` + `CardDefaults.cardElevation(defaultElevation = 0.dp)`. Container color: `surfaceContainerLow`. No shadow elevation anywhere.

---

## 14. `data class.copy()` for immutable state updates

All entity/display model mutations use `copy()`. Never mutate properties in-place.

---

## 15. Gradle convention plugins (build-logic)

**Files:** `build-logic/src/main/kotlin/`

Three precompiled script plugins avoid duplicating build config across modules:
- `kaasu.android.library` — AGP library + Kotlin, minSdk 26, Java 17
- `kaasu.android.compose` — extends library with Compose BOM + compiler options
- `kaasu.android.feature` — extends compose + auto-adds `:core:domain` and `:core:ui` deps

All module versions centralised in `gradle/libs.versions.toml`.

---

## 16. java.time.* for all date arithmetic

**Files:** `core/data/.../ExpenseRepositoryImpl.kt`, `core/domain/.../GetFilteredExpensesUseCase.kt`, `core/ui/.../Formatters.kt`, `feature/expense/.../ExpenseViewModel.kt`, `feature/chart/.../ChartViewModel.kt`

`java.util.Calendar` is banned. Use `java.time.*` (available from API 26, matching minSdk):

| Old pattern | Replacement |
|------------|-------------|
| `Calendar.getInstance().get(MONTH/YEAR)` | `YearMonth.now().monthValue - 1` / `.year` |
| Month range calculation | `YearMonth.of(y, m+1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()` |
| Midnight truncation | `LocalDate.ofInstant(..., zone).atStartOfDay(zone).toInstant().toEpochMilli()` |
| `isToday()`, `isSameDay()` | `LocalDate.ofInstant(...) == LocalDate.now()` |

The shared `dayStartMillis(millis: Long): Long` helper in `Formatters.kt` is the single place for midnight truncation.

---

## 17. M3 DatePicker composable (not imperative dialog)

**Files:** `feature/expense/.../ExpenseListScreen.kt`, `feature/expense/.../AddEditExpenseScreen.kt`

`android.app.DatePickerDialog` is banned. Use the Material 3 `DatePicker` composable, which is rendered directly in the Compose tree with a state variable:

```kotlin
var showPicker by remember { mutableStateOf(false) }
if (showPicker) {
    val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
    DatePickerDialog(
        onDismissRequest = { showPicker = false },
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onDateSelected(it) }
                showPicker = false
            }) { Text("OK") }
        }
    ) { DatePicker(state = state) }
}
```

Requires `@OptIn(ExperimentalMaterial3Api::class)` on the enclosing composable function.

---

## 18. Unit tests in :core:domain (pure JVM)

**Files:** `core/domain/src/test/java/.../usecase/`

Use cases are pure Kotlin — tested with JUnit 4 + MockK + kotlinx-coroutines-test on the JVM. No emulator, no Android runtime.

```kotlin
@Test
fun `save fails when amount is zero`() = runTest {
    val result = useCase.save(0.0, "Coffee", null, 0L, null)
    assertTrue(result.isFailure)
}
```

Run with: `.\gradlew :core:domain:test` (≈3 s locally). Runs automatically in CI before every `assembleDebug`.

**Convention:** every new use case ships with a matching test file. Every bug fix in `:core:domain` adds a regression test.
