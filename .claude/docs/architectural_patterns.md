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
  ├── :feature:export    ← ExportScreen, CsvExporter
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

- `GetFilteredExpensesUseCase.execute(...)` — filtering, sorting, grouping (was in `ExpenseViewModel`)
- `SaveExpenseUseCase.save/update(...)` — validation + repository call
- `DeleteExpenseUseCase(id)` — thin wrapper enabling testability

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

Default view is today's expenses. Arrow buttons navigate prev/next day; date picker jumps to any date. Month switching resets the selected day.

---

## 8. Bottom nav + overlay route navigation

**Files:** `app/.../NavGraph.kt`

3 bottom tabs: `Expenses`, `Reports`, `Export`. 3 overlay routes: `add_expense`, `edit_expense`, `settings` — bottom bar hidden for these. Visibility computed by checking `currentDestination.hierarchy`.

---

## 9. Compose Canvas custom charts (no third-party libs)

**Files:** `feature/chart/.../ChartScreen.kt`, `core/ui/.../theme/Color.kt`

- Donut/pie: `Canvas` + `drawArc()` with `Stroke(width)`, center text via native `android.graphics.Paint`
- Bar charts: `Box` with `fillMaxWidth(fraction)` nested inside a background `Box`
- 12-color palette: `ChartColors` in `Color.kt`, indexed by `Category.colorIndex % ChartColors.size`

---

## 10. SAF for file operations (no storage permissions)

**Files:** `feature/export/.../ExportScreen.kt`, `feature/export/.../util/CsvExporter.kt`

CSV export uses `ActivityResultContracts.CreateDocument("text/csv")`. Content written via `context.contentResolver.openOutputStream(uri)`. No `READ/WRITE_EXTERNAL_STORAGE` permissions needed.

---

## 11. Category seeding on first launch

**Files:** `core/data/.../repository/CategoryRepositoryImpl.kt`, `app/.../ExpenseApp.kt`

`seedDefaultCategories()` checks `dao.getCategoryCount() == 0` then inserts 8 fixed categories with `colorIndex` 0–7. Called from `ExpenseApp.onCreate()` in `applicationScope` (IO dispatcher).

---

## 12. Database migration with version numbering

**Files:** `core/database/.../ExpenseDatabase.kt`

```kotlin
@Database(version = 2)
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN account TEXT")
    }
}
```

No `fallbackToDestructiveMigration()` — data preserved across upgrades.

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
