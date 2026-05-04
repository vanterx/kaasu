# Architectural Patterns — Kaasu

Patterns extracted from the codebase. These repeat across multiple files and define how the app works.

---

## 1. Manual DI via Application class

Singleton dependencies held in `ExpenseApp` (lazy properties), passed down through constructors.
No Hilt, no Dagger, no service locator.

**Files:** `ExpenseApp.kt:15-20`, `MainActivity.kt:15-21`

```
ExpenseApp (Application)
  ├── database: ExpenseDatabase
  ├── expenseRepository: ExpenseRepository (→ ExpenseRepositoryImpl)
  ├── categoryRepository: CategoryRepository (→ CategoryRepositoryImpl)
  └── preferencesManager: PreferencesManager
        ↓
  MainActivity casts application → ExpenseNavGraph(params)
        ↓
  NavGraph creates ViewModels via ViewModelProvider.Factory(params)
```

Property types are interfaces; implementations are `*Impl` classes created at init time.

---

## 2. Interface-segregated repository layer

Every repository is an interface with a single `*Impl` class wrapping the Room DAO.

**Files:** `ExpenseRepository.kt:10-24`, `ExpenseRepositoryImpl.kt:12`, `CategoryRepository.kt:7-14`, `CategoryRepositoryImpl.kt:10`

```kotlin
interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseWithCategory>>
    suspend fun saveExpense(expense: Expense): Long
    // ...
}

class ExpenseRepositoryImpl(private val dao: ExpenseDao) : ExpenseRepository {
    override fun getAllExpenses() = dao.getAllExpenses()
    // ...
}
```

DAO is the single data source — no remote/cache layers. Repository only delegates.

---

## 3. Room Entity → Display Model → Compose UI pipeline

Room-annotated entities never reach Compose composables. A pure-Kotlin display model sits between
them, built via mapper extension functions in the ViewModel.

**Files:** `Expense.kt:20-27`, `ExpenseDisplay.kt:3-12`, `Mappers.kt:8-34`, `ExpenseViewModel.kt:70-92`

```
ExpenseWithCategory (Room relation)          Expense.kt:20 / ExpenseWithCategory.kt
        │
        ▼ .toDisplay()                        Mappers.kt:8
ExpenseDisplay (pure Kotlin data class)       ExpenseDisplay.kt:3
        │
        ▼ via DailyGroup StateFlow            ExpenseViewModel.kt:70
Compose UI (ExpenseCard)                      ExpenseListScreen.kt:315
```

Key: ViewModel maps entities → display models in the StateFlow pipeline. UI never imports `@Entity` or `@Relation` classes.

---

## 4. StateFlow-only reactive streams

No `LiveData`. No `SharedFlow`. All observable state is `StateFlow<T>`.

**Files:** `ExpenseViewModel.kt:44-114`, `ChartViewModel.kt:27-42`, `CategoryViewModel.kt:17-18`

Pattern for reactive derivation:
```kotlin
private val _foo = MutableStateFlow(init)
val foo: StateFlow<T> = _foo.asStateFlow()

val derived: StateFlow<U> = _foo
    .combine(repository.someFlow()) { a, b -> compute(a, b) }
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
```

Mutable state is private. Derived state uses `combine`/`flatMapLatest`. All `stateIn` uses `Lazily` with `viewModelScope`.

---

## 5. Day-based expense navigation

Expenses default to today (single day view), with date picker for jumping to other dates.
No month-grouped list view in the default screen.

**Files:** `ExpenseViewModel.kt:35-42`, `ExpenseListScreen.kt:80-100,148-156`

```
_selectedDay = today's midnight (default)    ExpenseViewModel.kt:35
        │
DayNavigationHeader (prev/next day arrows)   ExpenseListScreen.kt:220
        │
DatePickerDialog (jump to any date)           ExpenseListScreen.kt:84
```

Day boundaries computed with `Calendar` millis arithmetic. Month boundaries handled transparently
by prev/next day arrows (crossing month boundaries works naturally).

---

## 6. Bottom nav + overlay route navigation

**Files:** `NavGraph.kt:37-47,70-98`

3 bottom tabs: `Expenses`, `Reports`, `Export` → `bottomNavScreens` list.
3 overlay routes: `add_expense`, `edit_expense`, `settings` — bottom bar hidden for these.

Bottom bar visibility: check if `currentDestination.hierarchy` matches any `bottomNavScreens`.
Routes navigate with `popUpTo(findStartDestination)`, `launchSingleTop`, `restoreState`.

---

## 7. Compose Canvas custom charts (no third-party libs)

**Files:** `ChartScreen.kt:210-263,305-353`, `Color.kt:27-31`

- Donut/pie: `Canvas` + `drawArc()` with `Stroke(width)`, center text via native `android.graphics.Paint`
- Bar charts: `Box` with `fillMaxWidth(fraction)` nested inside a background `Box`
- 12-color palette: `ChartColors` list, indexed by `Category.colorIndex % ChartColors.size`

---

## 8. SAF for file operations (no storage permissions)

**Files:** `ExportScreen.kt:49-67`, `CsvExporter.kt:14-29`

CSV export uses `ActivityResultContracts.CreateDocument("text/csv")` — user picks destination.
Content written via `context.contentResolver.openOutputStream(uri)`.
No `READ/WRITE_EXTERNAL_STORAGE` permissions needed.

Same pattern would apply to backup/restore (zip via SAF), import (pick CSV via `OpenDocument`).

---

## 9. Category seeding on first launch

**Files:** `CategoryRepositoryImpl.kt:20-34`, `ExpenseApp.kt:24-28`

`seedDefaultCategories()` checks `dao.getCategoryCount() == 0` then inserts 8 fixed categories
with `colorIndex` 0–7. Called from `ExpenseApp.onCreate()` in `applicationScope` (IO dispatcher).

Categories: Food & Dining, Transport, Shopping, Bills & Utilities, Entertainment, Health, Education, Other.

---

## 10. Database migration with version numbering

**Files:** `ExpenseDatabase.kt:10-12,26-30`

```kotlin
@Database(version = 2)
// ...
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE expenses ADD COLUMN account TEXT")
    }
}
// ... builder.addMigrations(MIGRATION_1_2)
```

Each schema change gets a `Migration` object. Migrations passed via `addMigrations()`.
No `fallbackToDestructiveMigration()` — data is preserved across upgrades.

---

## 11. Zero-elevation card design

**Files:** `ExpenseListScreen.kt:321-331`, `ChartScreen.kt:127-134`, `ExportScreen.kt:119-126`, `CategoryScreen.kt:109-120`

All cards follow: `BorderStroke(1.dp, colorScheme.outlineVariant)` + `CardDefaults.cardElevation(defaultElevation = 0.dp)`.
No shadow-based elevation anywhere. Container color: `surfaceContainerLow`.

---

## 12. `data class.copy()` for immutable state updates

All entity/display model mutations use `copy()`. Never mutate properties in-place.

**Files:** `ExpenseViewModel.kt:162-169`, `CategoryViewModel.kt:20-28`

```kotlin
existingExpense.copy(amount = amount, description = description, ...)
category.copy(name = name, colorIndex = colorIndex)
```
