# CLAUDE.md — Kaasu Expense Tracker

## Overview

Kaasu is an offline-first Android expense tracker (Kotlin + Jetpack Compose + Room).
Fully offline, SQLite local storage, premium cream-and-gold Material 3 design.
Tamil for "money, cash, coin" — origin of the English word "cash".

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| UI | Jetpack Compose + Material 3 | BOM 2024.05.00 |
| Architecture | MVVM + Repository + Manual DI | — |
| Database | Room (SQLite) | 2.6.1 |
| Navigation | Navigation Compose | 2.7.7 |
| State | StateFlow exclusively | — |
| Charts | Compose Canvas (no third-party) | — |
| Preferences | DataStore | 1.1.1 |
| Build | AGP + Kotlin + KSP | 8.4.0 / 1.9.24 / 1.9.24-1.0.20 |
| Min SDK / Target | 26 (Android 8.0) / 34 | — |

## Key Directories

```
app/src/main/java/com/example/expense/
├── ExpenseApp.kt                    # DI container (lazy singletons)
├── MainActivity.kt                  # Single Activity, edge-to-edge
├── data/
│   ├── db/                          # Room DB, DAOs, Migration objects
│   ├── model/                       # Room entities + pure-Kotlin display models
│   ├── mapper/                      # Entity ↔ Display extension functions
│   └── repository/                  # Interfaces + Impl classes wrapping DAOs
├── ui/
│   ├── navigation/NavGraph.kt       # Routes, 3-tab bottom bar, ViewModel wiring
│   ├── theme/                       # Custom premium palette (Color.kt, Theme.kt)
│   ├── expense/                     # Day-based list, add/edit form, ViewModel, CurrencyPicker
│   ├── category/                    # CategoryListContent + CategoryDialog composables
│   ├── chart/                       # Reports: Canvas-drawn donut + bar charts
│   ├── settings/                    # Global settings (currency + categories)
│   └── export/                      # CSV export via SAF
└── util/                            # Formatters, CsvExporter, PreferencesManager, AppResult
```

## Build & Verify

| Command | Description |
|---------|-------------|
| `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew assembleDebug` | Build debug APK |
| `.\gradlew assembleRelease` | Build release APK (needs keystore.properties) |
| `.\gradlew test` | Run unit tests |
| `.\gradlew lint` | Run lint |
| `bash scripts/install-hooks.sh` | One-time git hook setup (release tagging reminder) |

APK: `app/build/outputs/apk/debug/Kaasu-debug.apk`

## Core Conventions

- **Manual DI only** — `ExpenseApp.kt:17-20` holds singletons, passed via `ViewModelProvider.Factory`
- **StateFlow, no LiveData** — all reactive state uses `StateFlow` + `stateIn(viewModelScope, ...)`
- **Interface-segregated repositories** — `ExpenseRepository.kt` (interface) → `ExpenseRepositoryImpl.kt`
- **Display models for UI** — Room entities never reach composables; mapped via `Mappers.kt:8-34`
- **`data class.copy()` for immutability** — never mutate entity/model properties in place
- **SAF for file I/O** — no storage permissions; `ActivityResultContracts.CreateDocument` for exports
- **No dynamic color** — fixed premium palette at `Theme.kt:15-77`, must never be overridden
- **No third-party chart libraries** — all charts are Compose `Canvas` in `ChartScreen.kt:210-353`
- **Transparent TopAppBar + zero-elevation cards** — `Color.Transparent` app bars, `BorderStroke(1.dp)` cards
- **Theme tokens only** — use `MaterialTheme.colorScheme.*`, never hardcode colors

## Additional Documentation

Check these files when relevant to the task:

| File | Content | When to consult |
|------|---------|----------------|
| `AGENTS.md` | Full project structure, build commands, signing setup | Project setup, CI/CD |
| `CONTRIBUTING.md` | Branching, PR, release workflow, signing | Git operations, releases |
| `.claude/docs/architectural_patterns.md` | 12 patterns extracted from codebase | Architecture changes, new features |
| `README.md` | User-facing features, screenshots | Feature descriptions, download links |
| `backlog/improvements.md` | Prioritized feature plan (local-only, gitignored) | Planning new features |

## Quick Patterns Reference

See `.claude/docs/architectural_patterns.md` for full details on these repeating patterns:

1. Manual DI via Application class
2. Interface-segregated repository layer
3. Room Entity → Display Model → Compose UI
4. StateFlow-only reactive streams
5. Day-based expense navigation
6. Bottom nav + overlay route navigation
7. Compose Canvas custom charts
8. SAF for file operations
9. Category seeding on first launch
10. Database migration with version numbering
11. Zero-elevation card design
12. `data class.copy()` for immutable state
