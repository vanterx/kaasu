# Kaasu — Personal Expense Tracker

"Kaasu" most commonly refers to the Tamil word for money, cash, or coin, often symbolizing wealth in South Indian culture. Historically, it represented small gold, silver, or copper coins and is considered the origin of the English word "cash".

A clean, offline Android expense tracker built with Jetpack Compose and Material 3. Premium light luxury design with warm cream surfaces and gold accents.

## Screenshots

| Expenses | Reports | Export |
|----------|---------|--------|
| Day view with date picker, add/edit/delete | Donut chart with centre total + bar breakdowns | CSV export |

## Features

- **Date-based tracking** — defaults to today's expenses, date picker to jump to any date
- **Track expenses** — amount, description, category, account (Cash, Cheque, Saving, Credit), date
- **Reports** — donut chart with centre total, per-category progress bars (Compose Canvas, no third-party libs)
- **Global settings** — manage categories and currency from a unified settings screen (top-right gear icon)
- **Categories** — 8 defaults seeded on first launch, 12-colour picker, fully customizable
- **CSV Export** — export all expenses via Android SAF file picker (no storage permission)
- **Multi-currency** — 10 currencies, NZD default, persisted with DataStore
- **Fully offline** — Room/SQLite local storage, no internet needed
- **Premium design** — warm cream palette, gold accent, K monogram launcher icon

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository |
| Database | Room (SQLite) |
| Navigation | Navigation Compose (3 bottom tabs + overlay screens) |
| DI | Manual (App container) |
| Architecture | MVVM + Repository (interface/impl + domain models) |
| Charts | Compose Canvas |
| Preferences | DataStore |
| Min SDK | 26 (Android 8.0) |

## Build

Open in Android Studio and sync Gradle, or build from command line:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew assembleDebug
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for release builds, signing setup, and CI details.

## Project Structure

```
app/src/main/java/com/example/expense/
├── ExpenseApp.kt                 # DI container (lazy singletons)
├── MainActivity.kt               # Single Activity
├── data/
│   ├── db/                       # Room DB, DAOs, Migration objects
│   ├── model/                    # Room entities + pure-Kotlin display models
│   ├── mapper/                   # Entity ↔ Display extension functions
│   └── repository/               # Repository interfaces + Impl classes
├── ui/
│   ├── navigation/               # NavGraph + 3-tab bottom bar
│   ├── theme/                    # Material 3 theme (premium palette)
│   ├── expense/                  # Day-based expense list + add/edit
│   ├── chart/                    # Reports: pie/bar charts
│   ├── settings/                 # Global settings (categories + currency)
│   └── export/                   # CSV export
└── util/                         # Formatters, CsvExporter, PreferencesManager, AppResult
```

See [AGENTS.md](AGENTS.md) for the full annotated tree with every file listed.

## Download

Get the latest APK from [Releases](https://github.com/vanterx/kaasu/releases).

Current version: **1.6.0**
