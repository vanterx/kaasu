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

## Module Map

```
:app                         # Shell only: ExpenseApp, MainActivity, NavGraph
:core:common                 # AppResult (pure Kotlin, no Android)
:core:domain                 # Repository interfaces, domain models, use cases (pure Kotlin)
:core:database               # Room DB, DAOs, entities (internal to :core:data)
:core:data                   # Repository impls, Mappers, PreferencesManager, DataModule
:core:ui                     # Theme (Color, Theme), Formatters, CurrencyPickerDialog
:feature:expense             # ExpenseListScreen, AddEditExpenseScreen, ExpenseViewModel
:feature:chart               # ChartScreen, ChartViewModel
:feature:category            # CategoryScreen, CategoryViewModel
:feature:settings            # SettingsScreen
:feature:export              # ExportScreen, CsvExporter
```

**Dependency rule:** feature modules only import `:core:domain` and `:core:ui`. Only `:app` imports `:core:data`.

## Build & Verify

| Command | Description |
|---------|-------------|
| `$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; .\gradlew assembleDebug` | Build debug APK |
| `.\gradlew assembleRelease` | Build release APK (needs keystore.properties) |
| `.\gradlew :core:domain:test` | Unit test domain use cases (pure JVM, no emulator) |
| `.\gradlew test` | Run all unit tests |
| `.\gradlew lint` | Run lint |
| `bash scripts/install-hooks.sh` | One-time git hook setup (release tagging reminder) |

APK: `app/build/outputs/apk/debug/Kaasu-debug.apk`

## Core Conventions

- **Manual DI via DataModule** — `DataModule` (`:core:data`) wires all deps; `ExpenseApp` holds it as a lazy property
- **StateFlow, no LiveData** — all reactive state uses `StateFlow` + `stateIn(viewModelScope, WhileSubscribed(5_000))`
- **Domain interfaces in :core:domain** — features depend on interfaces, never on `:core:data` impls
- **Domain models for UI** — Room entities never leave `:core:database`; only `ExpenseItem`, `Category` etc. reach composables
- **Thin use cases** — filtering, sorting, grouping logic lives in `:core:domain/usecase/`, not in ViewModels
- **`data class.copy()` for immutability** — never mutate entity/model properties in place
- **SAF for file I/O** — no storage permissions; `ActivityResultContracts.CreateDocument` for exports
- **No dynamic color** — fixed premium palette in `:core:ui` (`Color.kt`), must never be overridden
- **No third-party chart libraries** — all charts are Compose `Canvas` in `:feature:chart`
- **Transparent TopAppBar + zero-elevation cards** — `Color.Transparent` app bars, `BorderStroke(1.dp)` cards
- **Theme tokens only** — use `MaterialTheme.colorScheme.*`, never hardcode colors
- **Feature module boundary rule** — feature modules must NOT import `:core:data` or `:core:database`

## Additional Documentation

Check these files when relevant to the task:

| File | Content | When to consult |
|------|---------|----------------|
| `AGENTS.md` | Full project structure, build commands, signing setup | Project setup, CI/CD |
| `CONTRIBUTING.md` | Branching, PR, release workflow, signing | Git operations, releases |
| `.claude/docs/architectural_patterns.md` | 15 patterns extracted from codebase | Architecture changes, new features |
| `README.md` | User-facing features, screenshots | Feature descriptions, download links |
| `backlog/improvements.md` | Prioritized feature plan (local-only, gitignored) | Planning new features |

## Quick Patterns Reference

See `.claude/docs/architectural_patterns.md` for full details on these repeating patterns:

1. Manual DI via DataModule + Application class
2. Module dependency graph
3. Interface-segregated repository layer with domain models
4. Thin use cases in :core:domain
5. Room Entity → Domain Model → Compose UI
6. StateFlow-only reactive streams
7. Day-based expense navigation
8. Bottom nav + overlay route navigation
9. Compose Canvas custom charts
10. SAF for file operations
11. Category seeding on first launch
12. Database migration with version numbering
13. Zero-elevation card design
14. `data class.copy()` for immutable state
15. Gradle convention plugins (build-logic)
