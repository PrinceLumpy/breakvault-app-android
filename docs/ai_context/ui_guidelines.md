# UI/UX Guidelines (UI Agent)

## Tech Stack
- **Framework:** Jetpack Compose.
- **Design System:** Material3 (`androidx.compose.material3`).
- **Icons:** Material Icons Extended.

## Common Patterns
1.  **Screen Structure:**
    - Always use `Scaffold` for top-level screens.
    - Use `TopAppBar` for navigation headers.
    - Use `SnackbarHost` for transient messages (success/error).

2.  **Lists & Layouts:**
    - Use `LazyColumn` for scrollable lists.
    - Use `FlowRow` (from `androidx.compose.foundation.layout`) for displaying chips/moveListTags.
    - Use `Card` or `ListItem` for individual data entries.

3.  **Input Forms:**
    - `OutlinedTextField` for text input.
    - `FilterChip` or `InputChip` for moveListTag selection.
    - `FloatingActionButton` (FAB) for primary creation actions.
    - **Dialogs**: Use `AlertDialog` for confirmations (delete/archive) and simple inline edits (e.g., adding progress).

4.  **State Management:**
    - Observe ViewModel LiveData using `observeAsState()`.
    - Hoist state to the Screen composable level; pass lambdas down to sub-components.
    - Use `LaunchedEffect` for one-time events (e.g., loading data, navigation side-effects).

## Goal Tracking (New)
- **Goals Screen**:
    - Displays a list of active goals in Cards.
    - Each card shows the title and an **overall progress bar** (aggregated from stages).
    - Long-press or tap on a card to navigate to the **Edit Goal** screen.
    - Destructive actions (Delete/Archive) are **not** available directly on the card list.

- **Edit Goal Screen**:
    - Allows editing the Title.
    - Displays "Overall Progress" bar.
    - Lists "Stages / Milestones".
    - **Add Stage**: Navigates to a dedicated `AddEditGoalStageScreen`.
    - **Stage Item**: Shows stage progress (e.g., "5/10 reps").
        - Includes a **"+" button** to quickly add reps via a popup dialog.
        - Includes an **Edit (pencil)** button to navigate to the detail edit screen.
    - **Top Bar Actions**: Contains the **Archive** and **Delete** options for the goal itself.

- **Add/Edit Goal Stage Screen**:
    - Dedicated screen for creating or modifying a stage.
    - Fields: Name, Target Count, Unit.
    - Target Count and Unit are displayed side-by-side in a row.
    - Includes a Delete action in the top bar if editing an existing stage.

## Theming
- Use `MaterialTheme.colorScheme` for colors (e.g., `primary`, `error`, `surface`).
- Use `MaterialTheme.typography` for text styles.
- Avoid hardcoded colors; defined in `ui/theme/Color.kt` if custom are needed.

## Example Snippet
```kotlin
@Composable
fun ExampleScreen(viewModel: MyViewModel = viewModel()) {
    val data by viewModel.data.observeAsState(emptyList())
    Scaffold(
        topBar = { TopAppBar(title = { Text("Title") }) }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(data) { item ->
                Text(text = item.name)
            }
        }
    }
}
```
