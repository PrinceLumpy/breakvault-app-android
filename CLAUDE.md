# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

BreakVault — single-module Android app (Kotlin, Jetpack Compose, Hilt, Room) for breakdancers to
track moves, experiment with combo ideas, and prepare battle combos. See `README.md` for the
feature list.

**Naming:** the second tab is called **"Lab"** in the UI, but the code still says "practice combo"
everywhere (`PracticeCombo*` classes, `ui/practicecombos`, `practice_combos` table, `practice_*`
string keys). User-facing text should say "Lab"; don't rename the code identifiers unless asked,
because the table name is persisted and part of the export format.

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
- Existing tests cover pure helpers pulled out of ViewModels as `internal` top-level functions
  (e.g. `filterMoves`, `nextGeneratedComboName`, `sortedFor`/`stripColors`). Prefer that pattern
  over testing a ViewModel end to end.

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
- `ui/common` — composables shared across features:
  - `TagDialog`: name field plus an `extraContent` slot.
  - `TagSelectionCard`, `TagFilterRow`, `FlexibleItemList`.
  - The tag-color pieces: `TagColorPicker`, `TagColorStrip` + `stripColors()`, `ColoredTagItem`.
  - Put anything used by more than one feature here, not under a feature's `common/` folder.

**ViewModel convention** (see `ui/moves/list/MoveListViewModel.kt` as the reference): a private
`UserInteractions` `MutableStateFlow` holds user-driven state, `combine(...repository flows...,
_userInteractions)` derives a single public `data class ...UiState`, exposed as a `StateFlow` via
`stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)` with `isLoading = true` as the
initial value.

**ViewModel messages** — the ViewModel has no `Context`, so it can't read `strings.xml` itself.
Instead it emits a `UiMessage(@StringRes resId, args)` (see `ui/combogenerator`), and the screen
turns it into text with `stringResource`/`LocalResources`. Some older ViewModels (settings, move
tags, add/edit move) still put hardcoded strings in their snackbar state. Use `UiMessage` for new
code.

**Tag colors** — `MoveTag` and `BattleTag` both have an optional `color: TagColor?`. `TagColor` is
a fixed enum palette stored by name, so never rename or remove an entry without migrating the data.
Wherever tags are shown, the colors appear the same way:
- a color picker in the tag dialog
- a color strip on each tag card
- a strip on each move or battle combo card, one band per distinct tag color in palette order

**Database** — `data/local/database/AppDB.kt` is the single `@Database` (currently `version = 7`)
with hand-written `MIGRATION_x_y` objects, `fallbackToDestructiveMigration(true)`, a `Converters`
`@TypeConverters`, and an `AppDbCallback` that calls `prepopulateExampleData()` on create. Example
data is debug-only (guarded by `BuildConfig.DEBUG`). Room is set to export schemas to
`app/schemas`, but that folder isn't committed yet, so there are no migration tests.

**When changing an entity**:
1. Bump the `version`.
2. Add a `Migration` object and register it in `addMigrations(...)` inside `getDatabase`.
3. If the field should round-trip through export, update `AppDataExport` and the import path in
   `SettingsRepository`.

Import decodes with `ignoreUnknownKeys = true`, so removing a field doesn't break old export
files.

Migration gotchas:
- Because of `fallbackToDestructiveMigration`, a broken migration silently wipes user data instead
  of crashing. Test upgrades from the previous version on a device that has real data.
- `minSdk` is 26, whose SQLite has no `DROP COLUMN`. Removing a column means rebuilding the table:
  create a new table, copy the rows, drop the old one, rename. If other tables have `CASCADE`
  foreign keys to it, back up and restore those rows. `MIGRATION_6_7` is the example.
- Room checks the rebuilt table against the entity. Write the `CREATE TABLE` exactly the way Room
  generates it, and leave out `DEFAULT` clauses the entity doesn't declare.

## Navigation

Two nested `NavHost`s, both in `BreakVaultActivity.kt` / `BreakVaultNavGraph.kt`:
- **Outer** (`AppRoot`) — `Screen.Main` plus all overlay screens (`overlayNavGraph`): tag
  management, settings, combo generator, and every add/edit screen.
- **Inner** (`BottomNavGraph` inside `MainAppScreen`) — only the three `bottomNavItems` tabs
  (Moves, Lab, Battle), with
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
