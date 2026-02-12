# BreakVault: Input Validation Standard

This document outlines the standard for handling user text inputs. All new and refactored features
must adhere to this standard.

## Core Principle

> **Validate on entry, guard on action.**

This principle is implemented through a three-layer validation strategy:

1. **UI Layer (Screen):** Prevent invalid input at the source.
2. **ViewModel State Update:** Sanitize state by rejecting invalid values.
3. **ViewModel Action:** Defensively guard critical operations.

---

## 1. UI Layer: Input Capping

The first layer validates in the `TextField` composable to provide immediate feedback by preventing
input that exceeds character limits.

**Standard:** In the `onValueChange` lambda, check input length against the character limit
constant. Only call the `ViewModel` function if within the limit.

**Example (`MyScreen.kt`):**

```kotlin
import com.princelumpy.breakvault.common.Constants.BATTLE_TAG_CHARACTER_LIMIT

OutlinedTextField(
    value = uiState.userInputs.newTagName,
    onValueChange = { newText ->
        if (newText.length <= BATTLE_TAG_CHARACTER_LIMIT) {
            viewModel.onNewTagNameChange(newText)
        }
    },
    label = { Text("New Tag Name") },
    isError = uiState.dialogsAndMessages.newTagError != null,
    supportingText = {
        uiState.dialogsAndMessages.newTagError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
)
```

---

## 2. ViewModel: State Sanitization

The second layer ensures the `ViewModel`'s internal state (`StateFlow`) never holds invalid values.

**Standard:** The `ViewModel` function for text changes must validate input length. If invalid,
return without updating state. Clear field-specific errors on valid input.

**Example (`MyViewModel.kt`):**

```kotlin
fun onDescriptionChange(newDescription: String) {
    if (newDescription.length <= BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT) {
        _userInputs.update { it.copy(description = newDescription) }

        if (_dialogsAndMessages.value.descriptionError != null) {
            _dialogsAndMessages.update { it.copy(descriptionError = null) }
        }
    }
}
```

---

## 3. ViewModel: Action Guards

The final layer is a defensive check before critical actions (e.g., database writes). This guards
against unexpected state issues and makes requirements explicit.

**Standard:** Action functions must re-validate all inputs against all business rules (e.g.,
`isBlank`, length). On failure, update UI state with an error and `return` immediately.

**Example (`MyViewModel.kt`):**

```kotlin
fun saveCombo(onSuccess: () -> Unit) {
    val currentInputs = _userInputs.value

    when {
        currentInputs.description.isBlank() -> {
            _dialogsAndMessages.update {
                it.copy(descriptionError = "Description cannot be empty.")
            }
            return
        }
        currentInputs.description.length > BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT -> {
            _dialogsAndMessages.update {
                it.copy(descriptionError = "Description cannot exceed $BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT characters.")
            }
            return
        }
    }

    viewModelScope.launch {
        // ... proceed with save operation
    }
}
```

---

## 4. Error and Message Handling

Centralize transient UI states for consistent error handling.

**Standard:** Use a dedicated `UiDialogsAndMessages` data class for all transient UI states. Use *
*supporting text** as the primary method for displaying field-specific validation errors.

### Structure

```kotlin
data class UiDialogsAndMessages(
    val descriptionError: String? = null,
    val newTagError: String? = null,
    val snackbarMessage: String? = null,  // For non-field errors only
    // ... other transient states
)
```

### Error Handling Logic

**Setting Errors:**

- Set field error strings (e.g., `descriptionError`) **only** in action guards (e.g., `saveCombo`).
- Error messages must be complete, user-friendly sentences.
- Use `snackbarMessage` only for non-field errors (network issues, success messages, etc.).

**Clearing Errors:**

- **Field-specific errors** (e.g., `descriptionError`) are cleared in the corresponding`on...Change`
  function when valid input is provided.
- **`snackbarMessage`** is cleared via a dedicated event handler (e.g., `onSnackbarMessageShown()`)
  that the UI calls after display.

### UI Display

**Field Errors (Primary Method):**

```kotlin
OutlinedTextField(
    value = uiState.userInputs.description,
    onValueChange = { viewModel.onDescriptionChange(it) },
    isError = uiState.dialogsAndMessages.descriptionError != null,
    supportingText = {
        uiState.dialogsAndMessages.descriptionError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
)
```

**Non-Field Errors (Snackbar):**

```kotlin
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(uiState.dialogsAndMessages.snackbarMessage) {
    uiState.dialogsAndMessages.snackbarMessage?.let { message ->
        snackbarHostState.showSnackbar(message)
        viewModel.onSnackbarMessageShown()
    }
}
```

---

## 5. Exception: Validation in Modals and Dialogs

Snackbars may be obscured or disconnected in modal contexts.

**Standard:** For inputs within modals or dialogs, validation errors **must** be displayed *inside
the dialog*.

### Primary Method: TextField Supporting Text

Display contextual error messages directly below the `TextField` using `supportingText` and
`isError = true`.

**Example:**

```kotlin
var text by remember { mutableStateOf("") }
var isError by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf<String?>(null) }

OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Item Name") },
    isError = isError,
    supportingText = {
        if (isError) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
)

Button(onClick = {
    if (text.isBlank()) {
        isError = true
        errorMessage = "Name cannot be empty."
    } else {
        // ... proceed
    }
}) {
    Text("Save")
}
```

### Secondary Method: General Error Area

For non-field-specific errors, use a `Text` composable within the dialog layout (e.g., above action
buttons).

---

## 6. Uniqueness Validation

**Standard:** Uniqueness validation **must** be performed as a defensive guard in the action
function. It **must not** be performed on every keystroke in `on...Change` functions.

### Scenario 1: Check Against Existing UI State List (Preferred)

If the `UiState` contains a complete list, use it as the source of truth. This avoids unnecessary
database queries.

**Example (`addBattleTag` with state list):**

```kotlin
fun addBattleTag() {
    val newTagName = uiState.value.userInputs.newTagName.trim()
    val allTagNames = uiState.value.allBattleTags.map { it.name }

    when {
        newTagName.isBlank() -> {
            _dialogsAndMessages.update {
                it.copy(newTagError = "Tag name cannot be empty.")
            }
            return
        }
        newTagName.length > BATTLE_TAG_CHARACTER_LIMIT -> {
            _dialogsAndMessages.update {
                it.copy(newTagError = "Tag cannot exceed $BATTLE_TAG_CHARACTER_LIMIT characters.")
            }
            return
        }
        allTagNames.any { it.equals(newTagName, ignoreCase = true) } -> {
            _dialogsAndMessages.update {
                it.copy(newTagError = "Tag '$newTagName' already exists.")
            }
            return
        }
    }

    viewModelScope.launch {
        battleRepository.insertBattleTag(BattleTag(name = newTagName))
        _userInputs.update { it.copy(newTagName = "") }
    }
}
```

### Scenario 2: Check Requiring Database Query

If UI state doesn't contain necessary data, perform an asynchronous query within
`viewModelScope.launch`.

**Example:**

```kotlin
fun validateUsername(username: String) {
    viewModelScope.launch {
        if (userRepository.usernameExists(username)) {
            _dialogsAndMessages.update {
                it.copy(usernameError = "Username already exists.")
            }
            return@launch
        }
        // ... proceed
    }
}
```

---

## Summary Checklist

- [ ] **UI Layer:** Cap input length in `TextField.onValueChange`
- [ ] **State Update:** Validate length in `on...Change` functions
- [ ] **Action Guard:** Re-validate all business rules before critical operations
- [ ] **Errors:** Set field error strings in action guards, clear in `on...Change` functions
- [ ] **Display:** Use `supportingText` for field errors, `Snackbar` for non-field errors
- [ ] **Modals:** Display errors inside the dialog using `supportingText`
- [ ] **Uniqueness:** Check in action guards, prefer state lists over queries