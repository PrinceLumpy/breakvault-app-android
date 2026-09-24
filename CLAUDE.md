# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

BreakVault — single-module Android app (Kotlin, Jetpack Compose, Hilt, Room) for breakdancers to
track moves, practice combos, and battle combos. See `README.md` for the
feature list.

## Build & test commands

Gradle wrapper, single `:app` module. On Windows use `./gradlew` from Git Bash or `gradlew.bat`.

```bash
./gradlew assembleDebug            # build debug APK
./gradlew installDebug             # build + install on connected device/emulator
./gradlew lint                     # Android Lint (report: app/build/reports/lint-results-debug.html)
./gradlew test                     # JVM unit tests (all variants)
./gradlew testDebugUnitTest        # JVM unit tests, debug variant
./gradlew connectedDebugAndroidTest# instrumented/Compose UI tests (needs a device)
./gradlew clean
```

Run a single unit test or class:

```bash
./gradlew testDebugUnitTest --tests "com.princelumpy.breakvault.SomeViewModelTest"
./gradlew testDebugUnitTest --tests "*SomeViewModelTest.methodName"
```

Notes on the build:
- JDK 17 (`sourceCompatibility`/`jvmTarget` = 17); toolchain resolved via foojay in
  `settings.gradle.kts`.
- Gradle configuration cache is **on** (`gradle.properties`) — build-script changes may need
  `--no-configuration-cache` when debugging.
- KSP (not kapt) generates Room and Hilt code.
- Dependency versions live in `gradle/libs.versions.toml`; add libraries there, reference via
  `libs.*` aliases in `app/build.gradle.kts`.
- `app/src/test` holds JVM unit tests; `app/src/androidTest` does not exist yet. Test dependencies
  (JUnit4, MockK, `kotlinx-coroutines-test`, `androidx.arch.core:core-testing`, Compose UI test)
  are declared.

## Architecture

MVVM + repository over Room, everything under `com.princelumpy.breakvault`.

**Layers**
- `data/local/entity` — Room `@Entity` data classes, all also `@Serializable` (they double as the
  export format). Primary keys are `String` UUIDs defaulted in the constructor
  (`UUID.randomUUID().toString()`); entities carry `createdAt`/`modifiedAt` epoch millis.
- `data/local/dao` — three DAOs: `MoveDao`, `PracticeComboDao`, `BattleDao`. Queries
  return `Flow<...>` for reactive reads; `...List()` suspend variants exist for one-shot export.
- `data/local/relation` — `@Relation` classes (e.g. `MoveWithTags`) for the many-to-many tag joins
  via `MoveTagCrossRef` / `BattleComboTagCrossRef`.
- `data/repository` — one repository per domain plus `SettingsRepository` and
  `UserPreferencesRepository` (DataStore Preferences for persisted UI choices such as the battle
  list sort; not part of export or DB reset). Repositories are thin:
  they pass Flows through unchanged and wrap multi-write operations in `withContext(Dispatchers.IO)`
  / DAO `@Transaction`. **Filtering and derivation belong in the ViewModel, not here.**
- `data/service/export/model/AppDataExport.kt` — the full-database JSON snapshot used by
  `SettingsRepository` for import/export to a user-chosen `Uri` (kotlinx.serialization).
- `di/DatabaseModule.kt` — the only Hilt module; provides `AppDB` and each DAO as `@Singleton`.
  Repositories are `@Singleton` with `@Inject constructor`, so they need no `@Provides`.
- `ui/<feature>/{list,addedit,managetags}` — each folder holds a `*Screen.kt` (stateless-ish
  Compose) plus a `@HiltViewModel *ViewModel.kt`.

**ViewModel convention** (see `ui/moves/list/MoveListViewModel.kt` as the reference): a private
`UserInteractions` `MutableStateFlow` holds user-driven state, `combine(...repository flows...,
_userInteractions)` derives a single public `data class ...UiState`, exposed as a `StateFlow` via
`stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)` with `isLoading = true` as the
initial value.

**Database** — `data/local/database/AppDB.kt` is the single `@Database` (currently `version = 6`)
with hand-written `MIGRATION_x_y` objects, `fallbackToDestructiveMigration(true)`, a `Converters`
`@TypeConverters`, and an `AppDbCallback` that calls `prepopulateExampleData()` on create. Example
data is debug-only (guarded by `BuildConfig.DEBUG`). Schemas are exported to `app/schemas`.

**When changing an entity**: bump the `version`, add a `Migration` object, register it in
`addMigrations(...)` inside `getDatabase`, and update `AppDataExport` + the import path in
`SettingsRepository` if the field should round-trip through export.

## Navigation

Two nested `NavHost`s, both in `BreakVaultActivity.kt` / `BreakVaultNavGraph.kt`:
- **Outer** (`AppRoot`) — `Screen.Main` plus all overlay screens (`overlayNavGraph`): tag
  management, settings, combo generator, and every add/edit screen.
- **Inner** (`BottomNavGraph` inside `MainAppScreen`) — only the three `bottomNavItems` tabs, with
  index-based horizontal slide transitions. The drawer and bottom bar live in `MainAppScreen`; a
  screen opens an overlay through the `outerNavController`, and switches tabs through the
  `innerNavController`.

All routes, argument keys, and navigation calls are centralized in `BreakVaultNavigation.kt`:
`Screen` (sealed class), `BreakVaultDestinations`, `BreakVaultDestinationsArgs`, and
`BreakVaultNavigationActions`. Add new destinations there rather than hardcoding route strings; call
sites take an already-`remember`ed `BreakVaultNavigationActions`.

## Conventions

- All user-facing text goes in `res/values/strings.xml` and is read with `stringResource(...)`;
  `Screen` stores a `@StringRes labelResId` rather than a literal.
- Input length caps and similar magic numbers live in `common/Constants.kt`.
- `BreakVaultApplication` is `@HiltAndroidApp`; `BreakVaultActivity` is the single
  `@AndroidEntryPoint` activity.
