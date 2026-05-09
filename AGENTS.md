# AGENTS.md

## Project: Kaasu — Personal Expense Tracker (Android)

Kotlin + Jetpack Compose + Room + Navigation Compose (Material 3). Fully offline, local SQLite storage. Premium light luxury design — warm cream surfaces, gold accent.

## Build & Verify

| Command | Description |
|---------|-------------|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK (requires keystore.properties) |
| `./gradlew test` | Run unit tests |
| `./gradlew lint` | Run lint checks |
| `./gradlew connectedAndroidTest` | Run instrumentation tests (requires emulator/device) |

### Build from command line

```powershell
# Set JAVA_HOME (Android Studio's bundled JDK)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/Kaasu-debug.apk`.

Or use Android Studio's built-in Terminal tab — it sets JAVA_HOME automatically.

### First-time setup

Install the project git hooks after cloning:

```bash
bash scripts/install-hooks.sh
```

This installs a pre-commit hook that reminds about release tagging when `versionName` changes or 10+ commits accumulate since the last tag.

## Module Structure

Multi-module Gradle project. Each module has its own `build.gradle.kts`. Versions centralised in `gradle/libs.versions.toml`. Convention plugins in `build-logic/`.

```
:app                    # Shell: ExpenseApp, MainActivity, NavGraph (DI wiring only)
:core:common            # AppResult — pure Kotlin, no Android
:core:domain            # Repository interfaces, domain models (ExpenseItem, Category, etc.), use cases
:core:database          # Room DB, DAOs, entities (internal — only :core:data sees this)
:core:data              # Repository impls, Mappers, PreferencesManager, DataModule
:core:ui                # Theme (Color, Theme), Formatters, CurrencyPickerDialog
:feature:expense        # ExpenseListScreen, AddEditExpenseScreen, ExpenseViewModel
:feature:chart          # ChartScreen, ChartViewModel
:feature:category       # CategoryScreen, CategoryViewModel
:feature:settings       # SettingsScreen
:feature:export         # ExportScreen, ExportViewModel, CsvExporter
build-logic/            # Convention plugins: kaasu.android.library/compose/feature
gradle/
└── libs.versions.toml  # All dependency versions
.claude/
└── docs/
    └── architectural_patterns.md    # 18 extracted codebase patterns
scripts/
├── install-hooks.sh                 # One-time hook installation
└── hooks/pre-commit                 # Release tagging reminder hook
```

## Architecture

- **Multi-module MVVM**: UI (`:feature:*`) → ViewModel → Use Case (`:core:domain`) → Repository interface (`:core:domain`) → RepositoryImpl (`:core:data`) → Room DAO (`:core:database`) → SQLite
- **Manual DI via DataModule**: `DataModule` (`:core:data`) creates all dependencies; `ExpenseApp` holds it lazily; `NavGraph` wires ViewModel factories
- **Domain isolation**: Feature modules depend only on `:core:domain` interfaces and `:core:ui`. Only `:app` imports `:core:data`
- **Thin use cases**: `GetFilteredExpensesUseCase`, `SaveExpenseUseCase`, `DeleteExpenseUseCase` in `:core:domain` — pure Kotlin, unit-testable without Android
- **StateFlow**: All reactive state is `StateFlow` with `WhileSubscribed(5_000)`
- **Navigation**: `NavHost` with 3 bottom-bar tabs (Expenses, Reports, Export) + 3 overlay screens
- **Min SDK**: 26 (Android 8), **Target SDK**: 34

## Key Conventions

- Kotlin 1.9.24, Compose BOM 2024.05.00, Room 2.6.1, AGP 8.4.0
- **No Hilt/Dagger** — manual DI keeps the app simple
- **No third-party chart libraries** — charts drawn with Compose `Canvas`
- **No LiveData** — `StateFlow` exclusively
- **No dynamic color** — the premium palette is always applied (do not re-enable dynamic color)
- **No `java.util.Calendar`** — use `java.time.*` (`YearMonth`, `LocalDate`, `ZoneId`)
- **No `android.app.DatePickerDialog`** — use M3 `DatePicker` composable
- **No raw `CoroutineScope`** in screens — all async work in `viewModelScope`
- CSV export uses `ActivityResultContracts.CreateDocument` (no storage permissions needed)
- Default categories seeded on first launch via `CategoryRepository.seedDefaultCategories()`
- Signing credentials loaded from `keystore.properties` (git-ignored); never hardcoded
- R8 minification enabled for release builds

## Testing

- Unit tests live in `core/domain/src/test/` — pure JVM, no emulator needed
- Run: `.\gradlew :core:domain:test` (≈3 s) or `.\gradlew test` for all modules
- CI runs `./gradlew test` before every `assembleDebug` on push and PR
- Test stack: JUnit 4 + MockK + kotlinx-coroutines-test
- **Convention**: every new use case ships with a test file; every bug fix adds a regression test

See [CLAUDE.md](CLAUDE.md) for additional conventions, design system tokens, and module map.
See [.claude/docs/architectural_patterns.md](.claude/docs/architectural_patterns.md) for 18 extracted patterns.

## Signing (CI)

Four GitHub Actions secrets required:
- `KEYSTORE_BASE64` — base64-encoded PKCS12 keystore
- `KEYSTORE_STORE_PASSWORD` — keystore password
- `KEYSTORE_KEY_ALIAS` — key alias (`kaasu`)
- `KEYSTORE_KEY_PASSWORD` — key password

See [CONTRIBUTING.md](CONTRIBUTING.md#6-signing) for local setup.
