# Plan: Battle Tag Colors (replace energy + status)

Takes energy levels and the 🔥/🔨 training status out of the battle combo UI. Battle tags get an
optional color, and each battle combo card shows a vertical color strip made of its tags' colors.
The energy-based sort options are replaced by **Newest first** (default), **Name**, and **Group by
color**. The chosen sort is saved to user preferences.

**Decided approach for persisted data:** keep the `energy` and `status` columns and stop using
them. The only schema change is one nullable column, `battle_tags.color`, so the migration is a
single `ALTER TABLE ... ADD COLUMN`. No table rebuild, and no risk of hitting the `CASCADE` foreign
key on `battle_combo_tag_cross_ref`. This matters because `fallbackToDestructiveMigration(true)`
wipes the database if a migration throws.

## Decisions

| Topic | Decision |
| --- | --- |
| Color source | Fixed palette, `enum class TagColor`. Tags either share an exact color or don't, so removing duplicates is reliable. |
| Strip order | Distinct colors sorted by palette position (`TagColor.ordinal`). The same set of colors always stacks the same way on every card. |
| No colored tags | No colored bands. The 8dp slot stays reserved, so titles line up across cards. |
| Sort options | Newest first (default), Name A→Z, Group by color. Used combos still sink to the bottom in every mode. |
| Sort persistence | DataStore Preferences (new dependency). The app has no preference storage yet. |
| Tag popup | One `BattleTagDialog` (name + color) used for add and edit on the manage tags screen, and for the inline "new tag" in add/edit combo. |
| Manage tags cards | Each tag card has the same left strip as combo cards, showing its single color or nothing. |
| Legacy columns | `BattleCombo.energy` / `status` and `BattleEnums.kt` stay. They're marked as unused in KDoc, not `@Deprecated`, which would add warnings to Room's generated code. |

## Step 1: Data layer

1. **New `data/local/entity/TagColor.kt`:**
   ```kotlin
   @Serializable
   enum class TagColor(val argb: Long) {
       RED(0xFFE53935), ORANGE(0xFFFB8C00), YELLOW(0xFFFDD835), GREEN(0xFF43A047),
       TEAL(0xFF00897B), BLUE(0xFF1E88E5), PURPLE(0xFF8E24AA), PINK(0xFFD81B60)
   }
   ```
   Uses `Long` ARGB so entities don't import Compose; the UI converts with `Color(argb)`.
   **Never rename or remove an entry.** It's stored by name in Room and in export JSON, so a
   missing name breaks old backups on import. Reordering changes the strip order but is safe.
2. **`BattleTag.kt`:** add `val color: TagColor? = null`. Room stores enums as TEXT by name, the
   same as `EnergyLevel` today.
3. **`BattleCombo.kt`:** add a comment on `energy` / `status`: "Unused since v6; kept to avoid a
   table rebuild." Leave the defaults as they are.
4. **`AppDB.kt`:**
   - `version = 6`.
   - `MIGRATION_5_6`: `ALTER TABLE battle_tags ADD COLUMN color TEXT DEFAULT NULL`, registered
     in `addMigrations(...)`.
   - `prepopulateExampleData()`: give the example battle tags colors (e.g. Power = RED,
     Technique = BLUE), add a third tag so one example combo shows a split strip, and drop the
     `energy` / `status` arguments.
5. **`BattleDao.kt`:** `getAllBattleCombosWithTags` changes from `ORDER BY energy ASC` to
   `ORDER BY createdAt DESC`. The ViewModel re-sorts anyway; this only keeps the query from
   pointing at a dead column.
6. **`BattleRepository.kt`:** add `updateBattleTag(tag: BattleTag)`, wrapping the existing
   `BattleDao.updateBattleTag`. Callers pass `modifiedAt = now`. Remove `updateTagName` and
   `BattleDao.updateTagName` once nothing calls them; that query never set `modifiedAt`, unlike
   `MoveDao.updateTagName`.
7. **Export/import:** no code change. `BattleTag.color` is serialized automatically. Old backups
   have no `color`, so it defaults to `null`. Their `energy` / `status` values still import
   into the kept columns.

**Checkpoint:** `./gradlew assembleDebug` builds, and an existing install upgrades to v6 without
losing data. Before upgrading, export a backup from a device that has real data.

## Step 2: Sort preference storage

8. **`gradle/libs.versions.toml`:** add `androidx.datastore:datastore-preferences` and reference
   it in `app/build.gradle.kts` as `implementation(libs.androidx.datastore.preferences)`.
9. **New `data/repository/UserPreferencesRepository.kt`:** `@Singleton`, `@Inject constructor(@ApplicationContext context)`,
   with a top-level `private val Context.userPrefs by preferencesDataStore("user_prefs")`. It needs
   no Hilt `@Provides`, so `DatabaseModule` stays the only module.
   - `val battleSortOption: Flow<BattleSortOption>`: reads a string key and maps it with
     `runCatching { BattleSortOption.valueOf(it) }`, falling back to `NEWEST`.
   - `suspend fun setBattleSortOption(option: BattleSortOption)`.
   - Like `TagColor`, never rename `BattleSortOption` entries.
   - Note: Settings → reset database (`clearAllTables`) won't clear this preference. That's
     acceptable.

## Step 3: Battle combo list

10. **`BattleComboListViewModel.kt`:**
    - `enum class BattleSortOption { NEWEST, NAME, COLOR }`.
    - Remove `sortOption` from `UserInteractions`. Add `userPreferencesRepository.battleSortOption`
      to the `combine(...)`. The first emission waits for DataStore, so `isLoading` covers the
      read.
    - `changeSortOption` becomes `viewModelScope.launch { userPreferencesRepository.setBattleSortOption(it) }`.
    - Derive the strip colors here, as CLAUDE.md requires:
      ```kotlin
      fun List<BattleTag>.stripColors(): List<TagColor> =
          mapNotNull { it.color }.distinct().sortedBy { it.ordinal }
      ```
      Put this in `ui/battlecombos/common/TagColorStrip.kt` next to the composable so the tag
      screen can share it.
    - Wrap list items as `data class BattleComboListItem(val comboWithTags: BattleComboWithTags, val stripColors: List<TagColor>)`.
      `filteredAndSortedCombos` becomes `List<BattleComboListItem>`.
    - Comparators. Each starts with `isUsed`:
      - `NEWEST`: `createdAt` descending.
      - `NAME`: `title` case-insensitive, then `createdAt` descending.
      - `COLOR`: `stripColors.firstOrNull()?.ordinal ?: Int.MAX_VALUE` (uncolored last), then the
        full ordinal list (so red+blue sits next to red), then `title`.
11. **New `ui/battlecombos/common/TagColorStrip.kt`:** `@Composable fun TagColorStrip(colors: List<TagColor>, modifier)`.
    - An 8dp-wide `Column` with `fillMaxHeight()`, and one `Box(Modifier.weight(1f).fillMaxWidth().background(Color(c.argb)))`
      per color. Equal weights give 50/50, thirds, and so on automatically.
    - With an empty list it draws an empty 8dp spacer.
12. **`BattleComboListScreen.kt`:**
    - Delete `energyColor`, `statusIcon`, the status `Text`, and the `EnergyLevel` /
      `TrainingStatus` imports.
    - Replace the energy strip `Box` with `TagColorStrip(item.stripColors)`.
    - The sort menu has 3 items and shows a check on the current option. Now that the choice
      persists, users need to see which sort is active.
    - Update the previews to use tags with colors, including a multi-color example.

## Step 4: Shared tag dialog and color picker

13. **`ui/common/AppComponents.kt`, `TagDialog`:** add an optional `extraContent: (@Composable () -> Unit)? = null`
    slot, rendered below the text field. The dialog's `text` becomes a `Column`. Move tag screens
    don't pass it, so they're unchanged.
14. **New `ui/battlecombos/common/TagColorPicker.kt`:** a `FlowRow` of round swatches, with a
    leading "No color" swatch (outlined with a slash) followed by each `TagColor`. The selected
    swatch gets a ring or check.
    - Each swatch's content description is its color name, read from `strings.xml`.
15. **New `ui/battlecombos/common/BattleTagDialog.kt`:** a thin wrapper, `BattleTagDialog(title, confirmButtonText, tagName, tagColor, isError, errorMessage, onTagNameChange, onTagColorChange, onConfirm, onDismiss)`.
    It calls `TagDialog(... characterLimit = BATTLE_TAG_CHARACTER_LIMIT, extraContent = { TagColorPicker(...) })`.
    This is the single component used for all three battle tag popups.

## Step 5: Manage battle tags screen

16. **`BattleTagListViewModel.kt`:**
    - `UserInputs` gains `newTagColor: TagColor?` and `tagColorForEdit: TagColor?`, plus
      `onNewTagColorChange` / `onTagColorForEditChange`.
    - `onEditTagClicked` pre-fills the color. Both dismiss handlers reset it.
    - `onAddTag` inserts `BattleTag(name, color = newTagColor)`.
    - `onUpdateTag`: the "no change" early exit must compare name **and** color. Save with
      `updateBattleTag(tagToEdit.copy(name = newName, color = tagColorForEdit, modifiedAt = now))`.
17. **`BattleTagListScreen.kt`:**
    - Swap both `TagDialog` calls for `BattleTagDialog`.
    - Replace `GenericItemList` with `FlexibleItemList` and a local `BattleTagItem` card.
      `ListItemCard` pads its content, so it can't hold a strip flush with the card edge.
    - `BattleTagItem` uses the same layout as `BattleComboItem`: `Row(Modifier.height(IntrinsicSize.Min))`
      containing `TagColorStrip(listOfNotNull(tag.color))`, the name, then edit and delete
      buttons.
    - `GenericItemList` is left alone because move tags still use it.
    - Update the previews with colored tags and the dialog with a color selected.

## Step 6: Add/edit battle combo screen

18. **`AddEditBattleComboViewModel.kt`:**
    - Remove `selectedEnergy`, `selectedStatus`, `originalEnergy`, `originalStatus`,
      `onEnergyChange`, `onStatusChange`, and their lines in both unsaved-changes checks
      (~lines 332-341).
    - Add `newTagColor: TagColor?` to `UserInputs` and `onNewTagColorChange`. `addBattleTag()`
      passes the color, and color is cleared together with the name.
    - **Fix `createdAt` on edit:** `saveCombo` builds a fresh `BattleCombo(...)` without passing
      either timestamp. Both default to now, and `REPLACE` writes them, so every edit overwrites
      `createdAt` and an edited combo jumps to the top under "Newest first". Keep the loaded combo
      (`private var originalCombo: BattleCombo?`) and save with
      `originalCombo.copy(title, description, isUsed, modifiedAt = now)`. This matches how moves
      and practice combos already work: `createdAt` is written only on insert. It also keeps the
      legacy `energy` / `status` values as they were.
    - Combos edited before this fix already have a wrong `createdAt` (their last edit time). That
      can't be recovered, so they will sort by their last edit time.
    - `toggleUsed` (list VM) intentionally does **not** bump `modifiedAt`. Marking a combo as used
      is a battle-day action, not an edit.
19. **`AddEditBattleComboScreen.kt`:**
    - Delete `EnergySection`, `StatusSection`, `EnergyChip`, their call sites and parameters, and
      the enum imports.
    - The inline new-tag popup becomes `BattleTagDialog`, with the color threaded through the
      tag section's parameters.
    - The existing auto-close `LaunchedEffect` keeps working, since it keys on name and error.
    - Update the previews.

## Step 7: Strings and cleanup

20. **`strings.xml`:**
    - Remove `battle_combo_list_sort_energy_*`, `battle_combo_list_sort_status_*`,
      `add_edit_battle_combo_energy_*`, and the status section strings.
    - Add `battle_combo_list_sort_newest`, `battle_combo_list_sort_name`,
      `battle_combo_list_sort_color`, `tag_color_label`, `tag_color_none`, and one
      `tag_color_<name>` per palette entry.
21. Grep for leftovers: `EnergyLevel`, `TrainingStatus`, `🔥`, `🔨`, `StatusFireFirst`. The only
    remaining hits should be `BattleEnums.kt`, `BattleCombo.kt`, and `Converters.kt` if it
    references them.
22. Update the Database section of `CLAUDE.md` to say `version = 6`.

## Step 8: Tests

`app/src/test` doesn't exist yet; this step creates it.

23. `stripColors()`: no tags → empty; uncolored tags → empty; duplicate colors → one entry;
    mixed input order → palette order.
24. Sort comparators: used combos last in every mode; `COLOR` puts uncolored combos last and
    groups combos by their first color.

## Manual verification

- Upgrade an existing v5 install: combos, tags, and tag links survive, and every tag shows no
  color.
- Combos with 0 colored tags show no bands. 1 color gives a solid strip, 2 give 50/50 (top/bottom
  in palette order), 3+ give even bands. Two tags with the same color give a single band.
- The chosen sort survives an app restart. A fresh install defaults to Newest first.
- Editing a combo doesn't move it under Newest first.
- Adding a tag inline from add/edit combo uses the same name + color popup.
- Export, then import, round-trips tag colors. A pre-v6 backup still imports.
