# Kaasu — Personal Expense Tracker

A simple, offline Android expense tracker built with Jetpack Compose and Material 3.

## Screenshots

| Expenses | Charts | Categories | Export |
|----------|--------|------------|--------|
| Monthly list with add/edit/delete | Donut chart + bar breakdowns | Add/edit/delete categories | Export all to CSV |

## Features

- **Track expenses** — amount, description, category, date
- **Monthly view** — navigate months, see totals
- **Charts** — donut chart by category + progress bars (Compose Canvas, no third-party libs)
- **Categories** — 8 defaults seeded on first launch, fully customizable
- **CSV Export** — export all expenses via Android SAF file picker
- **Fully offline** — Room/SQLite local storage, no internet needed

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository |
| Database | Room (SQLite) |
| Navigation | Navigation Compose |
| DI | Manual (App container) |
| Charts | Compose Canvas |
| Min SDK | 26 (Android 8.0) |

## Build

### Android Studio

Open the project, wait for Gradle sync, then **Build > Build APK(s)**.

### Command Line

```powershell
# Set JAVA_HOME to Android Studio's bundled JDK
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew assembleDebug
```

APK: `app\build\outputs\apk\debug\Kaasu-debug.apk`

Or use Android Studio's built-in Terminal tab — it sets JAVA_HOME automatically.

## Project Structure

```
app/src/main/java/com/example/expense/
├── ExpenseApp.kt                 # Application class (DI container)
├── MainActivity.kt               # Single Activity
├── data/
│   ├── db/                       # Room DB, DAOs
│   ├── model/                    # Entities
│   └── repository/               # Repositories
├── ui/
│   ├── navigation/               # NavGraph + bottom bar
│   ├── theme/                    # Material 3 theme
│   ├── expense/                  # List + add/edit
│   ├── category/                 # Category management
│   ├── chart/                    # Pie/bar charts
│   └── export/                   # CSV export
└── util/                         # Currency/date formatting, CSV writer
```

## Download

Get the latest APK from [Releases](https://github.com/vanterx/kaasu/releases).
