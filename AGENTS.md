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
│   ├── theme/
│   │   ├── Color.kt                 # Premium palette tokens
│   │   └── Theme.kt                 # Custom light + dark color schemes
│   ├── expense/                     # Expense list, add/edit, ViewModel, CurrencyPicker
│   ├── category/                    # Category management, ViewModel
│   ├── chart/                       # Pie/bar charts via Compose Canvas
│   └── export/                      # CSV export via SAF
└── util/
    ├── CsvExporter.kt               # CSV generation
    ├── Formatters.kt                # Currency & date formatting
    └── PreferencesManager.kt        # DataStore currency preference
scripts/
├── install-hooks.sh                 # One-time hook installation
└── hooks/pre-commit                 # Release tagging reminder hook
```

## Architecture

- **MVVM + Repository**: UI → ViewModel → Repository → Room DAO → SQLite
- **Manual DI**: `ExpenseApp` holds singleton instances, passed via `ViewModelProvider.Factory`
- **StateFlow**: All data flows are `StateFlow` collected as Compose state
- **Navigation**: `NavHost` with 4 bottom-bar destinations + 2 overlay screens (add/edit expense)
- **Min SDK**: 26 (Android 8), **Target SDK**: 34

## Key Conventions

- Kotlin 1.9.24, Compose BOM 2024.05.00, Room 2.6.1, AGP 8.4.0
- **No Hilt/Dagger** — manual DI keeps the app simple
- **No third-party chart libraries** — charts drawn with Compose `Canvas`
- **No LiveData** — `StateFlow` exclusively
- **No dynamic color** — the premium palette is always applied (do not re-enable dynamic color)
- CSV export uses `ActivityResultContracts.CreateDocument` (no storage permissions needed)
- Default categories seeded on first launch via `CategoryRepository.seedDefaultCategories()`
- Signing credentials loaded from `keystore.properties` (git-ignored); never hardcoded

## Signing (CI)

Four GitHub Actions secrets required:
- `KEYSTORE_BASE64` — base64-encoded PKCS12 keystore
- `KEYSTORE_STORE_PASSWORD` — keystore password
- `KEYSTORE_KEY_ALIAS` — key alias (`kaasu`)
- `KEYSTORE_KEY_PASSWORD` — key password

See [CONTRIBUTING.md](CONTRIBUTING.md#6-signing) for local setup.
