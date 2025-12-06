# Database & Data Schema (Data Agent)

## Database Configuration
- **Library:** Room.
- **Database Class:** `AppDB` (Abstract class extending `RoomDatabase`).
- **Callback:** `AppDbCallback` prepopulates default moveListTags on creation.
- **Migration:** `fallbackToDestructiveMigration(true)` is enabled for development iteration.

## Entities
### Practice Mode
- **`Move`**: `id` (UUID), `name`.
- **`Tag`**: `id` (UUID), `name`.
- **`MoveTagCrossRef`**: Many-to-Many link between Moves and Tags.
- **`SavedCombo`**: `id`, `name`, `moves` (List<String> via TypeConverter), `createdAt`.

### Battle Mode
- **`BattleCombo`**: `id` (UUID), `description`, `energy` (Enum), `status` (Enum), `isUsed` (Boolean).
- **`BattleTag`**: `id` (UUID), `name`.
- **`BattleComboTagCrossRef`**: Many-to-Many link between BattleCombos and BattleTags.

## Type Converters (`Converters.kt`)
- `List<String>` <-> JSON String (for `SavedCombo.moves`).
- `EnergyLevel` <-> String (Enum name).
- `TrainingStatus` <-> String (Enum name).

## Enums (`BattleEnums.kt`)
- Must be annotated with `@Serializable` for Import/Export compatibility.
- `EnergyLevel`: LOW, MEDIUM, HIGH, NONE.
- `TrainingStatus`: READY, TRAINING.

## Import/Export (`AppDataExport`)
- A `@Serializable` data class containing lists of all entities.
- JSON handling is lenient (`ignoreUnknownKeys = true`, `encodeDefaults = true`).

## Rules
- All write operations must be `suspend` functions executed on `Dispatchers.IO`.
- Read operations often expose `LiveData` for UI observation.
- CrossRefs must be managed explicitly when adding/deleting relationships.
