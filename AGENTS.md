# AGENTS.md

## Project: Kaasu — Personal Expense Tracker (Android)

Kotlin + Jetpack Compose + Room + Navigation Compose (Material 3). Fully offline, local SQLite storage.

## Build & Verify

| Command | Description |
|---------|-------------|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK |
| `./gradlew test` | Run unit tests |
| `./gradlew lint` | Run lint checks |
| `./gradlew connectedAndroidTest` | Run instrumentation tests (requires emulator/device) |

### Build from command line

```powershell
# Set JAVA_HOME (Android Studio's bundled JDK)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

Or use Android Studio's built-in Terminal tab — it sets JAVA_HOME automatically.

## Project Structure

```
app/src/main/java/com/example/expense/
├── ExpenseApp.kt                    # Application class (DI container)
├── MainActivity.kt                  # Single Activity entry point
├── data/
│   ├── db/
│   │   ├── ExpenseDatabase.kt       # Room DB singleton
│   │   ├── ExpenseDao.kt            # Expense CRUD + reporting queries
│   │   └── CategoryDao.kt           # Category CRUD
│   ├── model/
│   │   ├── Expense.kt               # Room entity (id, amount, description, categoryId, date)
│   │   ├── Category.kt              # Room entity (id, name, colorIndex)
│   │   └── ExpenseWithCategory.kt   # Relation mapping
│   └── repository/
│       ├── ExpenseRepository.kt
│       └── CategoryRepository.kt
├── ui/
│   ├── navigation/NavGraph.kt       # All routes + bottom nav bar (4 tabs)
│   ├── theme/{Color.kt, Theme.kt}   # Material 3 theme
│   ├── expense/                     # Expense list, add/edit, ViewModel
│   ├── category/                    # Category management, ViewModel
│   ├── chart/                       # Pie/bar charts via Compose Canvas
│   └── export/                      # CSV export via SAF
└── util/
    ├── CsvExporter.kt               # CSV generation
    └── Formatters.kt                # Currency & date formatting
```

## Architecture

- **MVVM + Repository**: UI → ViewModel → Repository → Room DAO → SQLite
- **Manual DI**: `ExpenseApp` holds singleton instances, passed via `ViewModelProvider.Factory`
- **StateFlow**: All data flows are `StateFlow` collected as Compose state
- **Navigation**: `NavHost` with 4 bottom-bar destinations + 2 overlay screens (add/edit expense)
- **Min SDK**: 26 (Android 8), **Target SDK**: 34

## Key Conventions

- Kotlin 1.9.24, Compose BOM 2024.05.00, Room 2.6.1, AGP 8.4.0
- Room entities use `@Entity` with explicit table names
- No Hilt/Dagger — manual DI keeps the app simple
- Charts drawn with Compose `Canvas` (no third-party chart library)
- CSV export uses `ActivityResultContracts.CreateDocument` (no storage permissions needed)
- Default categories are seeded on first launch via `CategoryRepository.seedDefaultCategories()`
