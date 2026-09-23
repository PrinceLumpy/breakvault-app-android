# Plan: Remove Timer, Goals, and Archived Goals

Removes the timer screen, the goals feature (list / add-edit / stages), and the archived goals
screen, along with every now-unused reference. Roughly 3,900 lines across 13 deleted files plus
8 edited files.

**Decided approach for persisted data:** drop the tables and drop the export fields. `AppDB` is
bumped to version 5 with a migration that `DROP`s `goals` and `goal_stages`, and
`AppDataExport` loses its `goals` / `goalStages` fields. Old export files still import cleanly
because `SettingsViewModel.kt:32` already configures `Json { ignoreUnknownKeys = true }`.

## Scope

### Files deleted (13)

| Area | Files |
| --- | --- |
| Timer UI | `ui/timer/TimerScreen.kt` (325 lines; no ViewModel — state lives in the composable) |
| Goals UI | `ui/goals/list/{GoalsScreen,GoalsViewModel}.kt`, `ui/goals/addedit/{AddEditGoalScreen,AddEditGoalViewModel}.kt`, `ui/goals/addedit/stage/{AddEditGoalStageScreen,AddEditGoalStageViewModel}.kt`, `ui/goals/archived/{ArchivedGoalsScreen,ArchivedGoalsViewModel}.kt` |
| Shared-but-goal-only | `ui/common/GoalStageItem.kt` — already dead code: `GoalsScreen.kt:336` declares its own `GoalStageItem` that shadows it |
| Data layer | `data/local/entity/Goal.kt`, `data/local/entity/GoalStage.kt`, `data/local/relation/GoalWithStages.kt`, `data/local/dao/GoalDao.kt`, `data/repository/GoalRepository.kt` |

### Files edited (8)

`BreakVaultNavigation.kt`, `BreakVaultNavGraph.kt`, `BreakVaultActivity.kt`,
`data/service/export/model/AppDataExport.kt`, `data/repository/SettingsRepository.kt`,
`di/DatabaseModule.kt`, `data/local/database/AppDB.kt`, `common/Constants.kt`
(plus `res/values/strings.xml`, `README.md`, `CLAUDE.md`).

## Step 1 — UI layer: delete screens and unwire navigation

1. Delete `ui/timer/` and `ui/goals/` entirely, plus `ui/common/GoalStageItem.kt`.
2. `BreakVaultNavigation.kt`:
   - Remove `Screen.GoalList`, `Screen.Timer`, `Screen.ArchivedGoals`, `Screen.AddEditGoal`,
     `Screen.AddEditGoalStage`.
   - Remove `Screen.GoalList` and `Screen.Timer` from `bottomNavItems` — the bottom bar goes from
     5 tabs to 3 (Moves, Practice Combos, Battle).
   - Remove `GOAL_ID_ARG` and `STAGE_ID_ARG` from `BreakVaultDestinationsArgs`, and the five
     matching route vals from `BreakVaultDestinations`.
   - Delete these `BreakVaultNavigationActions` methods: `navigateToArchivedGoals`,
     `navigateToAddEditGoal`, `navigateToAddEditGoalStage`,
     `navigateToAddEditGoalStageFromParentGoal`, `navigateFromStageToGoal`.
3. `BreakVaultNavGraph.kt`: remove the 5 screen imports, the `GOALS_LIST_ROUTE` and `TIMER_ROUTE`
   composables in `BottomNavGraph`, and the `ADD_EDIT_GOAL_ROUTE`, `ADD_EDIT_GOAL_STAGE_ROUTE`,
   and `ARCHIVED_GOALS_ROUTE` composables in `overlayNavGraph`.
4. `BreakVaultActivity.kt`: remove the "Archived Goals" `NavigationDrawerItem` (~lines 165-172),
   leaving Practice Tags, Battle Tags, and Settings.

**Checkpoint:** `./gradlew assembleDebug` should now fail *only* in the data layer. That confirms
the UI is fully detached.

## Step 2 — Export format

5. `AppDataExport.kt`: drop the `goals` and `goalStages` fields and their two imports.
6. `SettingsRepository.kt`: remove `val goalDao = db.goalDao()` from both `getAppDataForExport`
   and the import function, the two export field assignments (~lines 37-38), and the two
   `insertAllGoals` / `insertAllGoalStages` calls (~lines 58-59).

No compatibility shim is needed — `ignoreUnknownKeys = true` already tolerates the stale keys in
previously exported JSON.

## Step 3 — Data layer and DB migration

7. `di/DatabaseModule.kt`: remove the `GoalDao` import and its `@Provides` function.
8. `data/local/database/AppDB.kt`:
   - Remove the `Goal` / `GoalStage` / `GoalDao` imports, both entries from the `entities` array,
     and `abstract fun goalDao()`.
   - Bump `version = 4` to `version = 5`.
   - Add `MIGRATION_4_5` running `DROP TABLE IF EXISTS goal_stages` then
     `DROP TABLE IF EXISTS goals` — child table first, since `GoalStage` has a foreign key to
     `Goal`. Register it in `addMigrations(...)`.
   - Leave `MIGRATION_2_3` and `MIGRATION_3_4` untouched. They must still run for users upgrading
     from v2/v3, and they reference these tables while the tables still exist at that point in the
     chain.
   - Check `prepopulateExampleData()` for goal seeding and remove it if present (the sections
     reviewed cover tags, moves, practice combos, and battle combos only, so this is likely a
     no-op).
9. Delete the five data-layer files listed in the scope table.
10. If a `5.json` schema is generated under `app/schemas/`, remove the stale `4.json`. The schema
    directory is currently empty, so there is likely nothing to do.

## Step 4 — Resources and cleanup

11. `res/values/strings.xml`: remove the timer block (~lines 372-385) and `screen_label_timer`
    (~line 42), plus the goal strings. **Verify each `goal` match individually** — 69 lines match
    case-insensitively and some may belong to shared or battle-combo strings rather than the goal
    feature.
12. `common/Constants.kt`: remove `GOAL_TITLE_CHARACTER_LIMIT`, `GOAL_DESCRIPTION_CHARACTER_LIMIT`,
    `GOAL_STAGE_TITLE_CHARACTER_LIMIT`, `GOAL_STAGE_MAX_TARGET_COUNT`, and
    `GOAL_STAGE_UNIT_CHARACTER_LIMIT`.
13. `ui/common/AppComponents.kt`: `AppLinearProgressIndicator` was used only by goal screens.
    Re-grep after the deletions and remove it if orphaned. Keep everything else — `TagSelectionCard`,
    `ListItemCard`, `GenericItemList`, etc. are all still used by moves and combos.
14. Keep the `sh.reorderable` dependency: `AddEditPracticeComboScreen` and its ViewModel still use it.
15. `README.md`: remove the "Goal Setting" and "Training Timer" feature bullets.
16. `CLAUDE.md`: update the feature list, the DB version (4 to 5), the DAO list (four to three),
    and the bottom-nav tab count (five to three).

## Step 5 — Verify

17. `./gradlew clean assembleDebug` — must be clean.
18. `./gradlew lint` — catches leftover unused resources; check for unreferenced string and
    drawable warnings.
19. Final sweep: `grep -rniE "goal|timer" app/src/main` should return nothing outside incidental
    prose.
20. Manual smoke test on a device **upgraded from an existing v4 install** (not a fresh install)
    to actually exercise `MIGRATION_4_5`. Then run a Settings export/import round-trip and confirm
    that both the new format and an older goal-bearing JSON file import without error.

## Risks

- **Goal data loss is irreversible.** Dropping the tables permanently destroys goal data for any
  user who has not exported first.
- **`fallbackToDestructiveMigration(true)` is still enabled.** If `MIGRATION_4_5` throws, Room
  silently wipes the *entire* database instead of surfacing the failure. The upgrade test in
  step 20 is the guard against this.

## Follow-ups (separate from this removal)

These surfaced while reading the code and are unrelated to the goals/timer work:

- `SettingsRepository.resetDatabase()` calls `prepopulateExampleData()` after `clearAllTables()`.
  Since prepopulation is guarded by `if (!BuildConfig.DEBUG) return`, the "Wipe Database" button
  empties the DB in release but reseeds sample data in debug. Removing that one line makes the
  button behave as labelled in both build types, and removes the atomicity concern (Room already
  wraps `clearAllTables()` in its own transaction). Note it also makes the sample data unreachable
  except on first DB creation.
- Snackbar strings in `SettingsViewModel.kt:82` and `:90` are hardcoded English rather than
  `strings.xml` resources, unlike the rest of the screen.
- `importAppData()` wipes before inserting, i.e. restore-from-backup semantics. A merge option
  ("add new items only") is feasible — the DAOs already use `OnConflictStrategy.REPLACE` with
  stable UUID keys — but needs decisions about deletions, cross-device name duplicates, and
  stale tag cross-references.
