package com.princelumpy.breakvault.ui.practicecombos.addedit

import android.util.Log
import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.ui.common.UnsavedChangesDialog
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

// Constants for character limits (LAYER 1)
private const val COMBO_NAME_CHARACTER_LIMIT = 30

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun AddEditPracticeComboScreen(
    onNavigateUp: () -> Unit,
    comboId: String?,
    addEditPracticeComboViewModel: AddEditPracticeComboViewModel = hiltViewModel()
) {
    val uiState by addEditPracticeComboViewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = comboId) {
        addEditPracticeComboViewModel.loadCombo(comboId)
    }

    LaunchedEffect(uiState.isNewCombo) {
        if (uiState.isNewCombo) {
            focusRequester.requestFocus()
        }
    }

    if (showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onDismiss = { showUnsavedChangesDialog = false },
            onConfirm = {
                showUnsavedChangesDialog = false
                onNavigateUp()
            }
        )
    }

    // Handle system back button
    BackHandler(enabled = true) {
        if (addEditPracticeComboViewModel.hasUnsavedChanges()) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateUp()
        }
    }

    AddEditPracticeComboScaffold(
        uiState = uiState,
        focusRequester = focusRequester,
        snackbarHostState = snackbarHostState,
        addEditPracticeComboViewModel = addEditPracticeComboViewModel,
        onNavigateUp = {
            if (addEditPracticeComboViewModel.hasUnsavedChanges()) {
                showUnsavedChangesDialog = true
            } else {
                onNavigateUp()
            }
        },
        onComboNameChange = { addEditPracticeComboViewModel.onComboNameChange(it) },
        onRemoveMove = { addEditPracticeComboViewModel.removeMoveFromCombo(it) },
        onMovesReordered = { addEditPracticeComboViewModel.onMovesReordered(it) },
        onSearchTextChange = { addEditPracticeComboViewModel.onSearchTextChange(it) },
        onExpandedChange = { addEditPracticeComboViewModel.onExpandedChange(it) },
        onAddMove = { addEditPracticeComboViewModel.addMoveToCombo(it) },
        onSaveCombo = {
            addEditPracticeComboViewModel.saveCombo {
                focusManager.clearFocus()
                onNavigateUp()
            }
        },
        onDeleteComboClick = { addEditPracticeComboViewModel.onDeleteComboClick() },
        onConfirmComboDelete = {
            addEditPracticeComboViewModel.onCancelComboDelete() // Dismiss dialog first
            addEditPracticeComboViewModel.onConfirmComboDelete {
                focusManager.clearFocus()
                onNavigateUp()
            }
        },
        onCancelComboDelete = { addEditPracticeComboViewModel.onCancelComboDelete() }
    )
}

/**
 * A stateless scaffold that handles the overall layout for the Add/Edit Combo screen.
 */
@Composable
private fun AddEditPracticeComboScaffold(
    uiState: AddEditComboUiState,
    focusRequester: FocusRequester,
    snackbarHostState: SnackbarHostState,
    addEditPracticeComboViewModel: AddEditPracticeComboViewModel,
    onNavigateUp: () -> Unit,
    onComboNameChange: (String) -> Unit,
    onRemoveMove: (Int) -> Unit,
    onMovesReordered: (List<String>) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onAddMove: (String) -> Unit,
    onSaveCombo: () -> Unit,
    onDeleteComboClick: () -> Unit,
    onConfirmComboDelete: () -> Unit,
    onCancelComboDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val userInputs = uiState.userInputs

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AddEditComboTopBar(
                isNewCombo = uiState.isNewCombo,
                onNavigateUp = onNavigateUp,
                onDeleteClick = onDeleteComboClick
            )
        },
        floatingActionButton = {
            val hasUnsavedChanges = addEditPracticeComboViewModel.hasUnsavedChanges()
            val isValid = userInputs.comboName.isNotBlank() && userInputs.selectedMoves.isNotEmpty()
            FloatingActionButton(
                onClick = onSaveCombo,
                modifier = Modifier.imePadding(),
                containerColor = if (isValid && hasUnsavedChanges)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(R.string.save_combo_fab_description)
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AddEditComboContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { focusManager.clearFocus() }
                    ),
                uiState = uiState,
                focusRequester = focusRequester,
                onComboNameChange = onComboNameChange,
                onRemoveMove = onRemoveMove,
                onMovesReordered = onMovesReordered,
                onSearchTextChange = onSearchTextChange,
                onExpandedChange = onExpandedChange,
                onAddMove = onAddMove
            )
        }
    }

    if (uiState.dialogsAndMessages.showDeleteDialog) {
        DeleteComboDialog(
            comboName = userInputs.comboName,
            onConfirm = onConfirmComboDelete,
            onDismiss = onCancelComboDelete
        )
    }
}

/**
 * A stateless top bar for the Add/Edit Combo screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditComboTopBar(
    isNewCombo: Boolean,
    onNavigateUp: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                if (isNewCombo) stringResource(R.string.create_combo_title)
                else stringResource(R.string.edit_combo_title)
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back_button_description)
                )
            }
        },
        actions = {
            if (!isNewCombo) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete combo",
                    )
                }
            }
        }
    )
}

/**
 * The main, stateless content of the screen containing the input form.
 */
@Composable
private fun AddEditComboContent(
    uiState: AddEditComboUiState,
    focusRequester: FocusRequester,
    onComboNameChange: (String) -> Unit,
    onRemoveMove: (Int) -> Unit,
    onMovesReordered: (List<String>) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onAddMove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userInputs = uiState.userInputs
    val dialogsAndMessages = uiState.dialogsAndMessages

    Column(
        modifier = modifier
            .padding(horizontal = AppStyleDefaults.SpacingLarge)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
    ) {
        // LAYER 1: Input Capping with Supporting Text Error Display
        OutlinedTextField(
            value = userInputs.comboName,
            onValueChange = { newText ->
                if (newText.length <= COMBO_NAME_CHARACTER_LIMIT) {
                    onComboNameChange(newText)
                }
            },
            label = { Text(stringResource(R.string.combo_name_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            isError = dialogsAndMessages.comboNameError != null,
            supportingText = {
                dialogsAndMessages.comboNameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        SelectedMovesList(
            selectedMoves = userInputs.selectedMoves,
            movesError = dialogsAndMessages.movesError,
            onRemoveMove = onRemoveMove,
            onMovesReordered = onMovesReordered
        )

        AddMoveDropdown(
            searchText = userInputs.searchText,
            allMoves = uiState.allMoves,
            dropdownExpanded = dialogsAndMessages.dropdownExpanded,
            onSearchTextChange = onSearchTextChange,
            onExpandedChange = onExpandedChange,
            onAddMove = onAddMove
        )
    }
}

/**
 * A stateless section for displaying the list of selected moves.
 */
@Composable
private fun SelectedMovesList(
    selectedMoves: List<String>,
    movesError: String?,
    onRemoveMove: (Int) -> Unit,
    onMovesReordered: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.select_moves_title),
            style = MaterialTheme.typography.titleMedium
        )

        if (selectedMoves.isEmpty()) {
            Text(
                text = movesError ?: stringResource(id = R.string.add_edit_combo_no_moves_message),
                style = MaterialTheme.typography.bodyMedium,
                color = if (movesError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
            )
        } else {
            var movesList by remember { mutableStateOf(selectedMoves) }

            // Update local list when selectedMoves changes from ViewModel
            LaunchedEffect(selectedMoves) {
                movesList = selectedMoves
            }

            val lazyListState = rememberLazyListState()
            val haptic = LocalHapticFeedback.current

            val reorderableLazyListState =
                rememberReorderableLazyListState(lazyListState) { from, to ->
                    movesList = movesList.toMutableList().apply {
                        add(to.index, removeAt(from.index))
                    }
                    Log.d("PracticeComboScreen", "After reorder: movesList = $movesList")
                    // Immediately notify ViewModel of the new order
                    onMovesReordered(movesList)
                }

            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Fixed height since it's in a scrollable parent
            ) {
                itemsIndexed(
                    movesList,
                    key = { index, item -> "$item-$index" }) { index, moveName ->
                    ReorderableItem(
                        reorderableLazyListState,
                        key = "$moveName-$index"
                    ) { isItemDragging ->
                        val dragHandleModifier = Modifier.draggableHandle(
                            onDragStarted = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )

                        ComboMoveItem(
                            moveName = moveName,
                            onRemove = {
                                movesList = movesList.toMutableList().apply {
                                    removeAt(index)
                                }
                                onMovesReordered(movesList)
                            },
                            isDragging = isItemDragging,
                            dragHandleModifier = dragHandleModifier
                        )
                    }
                }
            }
        }
    }
}

/**
 * A stateless dropdown menu for searching and adding moves.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMoveDropdown(
    searchText: String,
    allMoves: List<Move>,
    dropdownExpanded: Boolean,
    onSearchTextChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onAddMove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredMoves = allMoves.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = dropdownExpanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier.padding(bottom = AppStyleDefaults.SpacingExtraLarge) // Extra padding for FAB
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            label = { Text(stringResource(id = R.string.add_edit_combo_add_move_label)) },
            trailingIcon = {
                IconButton(onClick = {
                    if (searchText.isNotBlank()) {
                        onAddMove(searchText.trim())
                    }
                }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.add_edit_combo_add_custom_move_description)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (filteredMoves.isNotEmpty()) {
                        onAddMove(filteredMoves.first().name)
                    } else if (searchText.isNotBlank()) {
                        onAddMove(searchText.trim())
                    }
                }
            )
        )

        if (filteredMoves.isNotEmpty() && dropdownExpanded) {
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                filteredMoves.forEach { move ->
                    DropdownMenuItem(
                        text = { Text(move.name) },
                        onClick = { onAddMove(move.name) }
                    )
                }
            }
        }
    }
}

/**
 * Dialog for confirming combo deletion.
 */
@Composable
private fun DeleteComboDialog(
    comboName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
        text = {
            Text("Are you sure you want to delete the combo \"$comboName\"?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}

//region Previews

@Composable
private fun ComboMoveItem(
    moveName: String,
    onRemove: () -> Unit,
    isDragging: Boolean,
    dragHandleModifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isDragging) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        shape = MaterialTheme.shapes.small,
        tonalElevation = if (isDragging) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDefaults.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle icon with drag gesture
            IconButton(
                onClick = {},
                modifier = dragHandleModifier
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Drag to reorder",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = moveName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.add_edit_combo_remove_move_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditComboTopBar_NewPreview() {
    BreakVaultTheme {
        AddEditComboTopBar(isNewCombo = true, onNavigateUp = {}, onDeleteClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditComboTopBar_EditPreview() {
    BreakVaultTheme {
        AddEditComboTopBar(isNewCombo = false, onNavigateUp = {}, onDeleteClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedMovesList_WithMoves_Preview() {
    BreakVaultTheme {
        SelectedMovesList(
            selectedMoves = listOf("Windmill", "Flare", "Airflare"),
            movesError = null,
            onRemoveMove = {},
            onMovesReordered = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedMovesList_NoMoves_Preview() {
    BreakVaultTheme {
        SelectedMovesList(
            selectedMoves = emptyList(),
            movesError = null,
            onRemoveMove = {},
            onMovesReordered = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedMovesList_ErrorPreview() {
    BreakVaultTheme {
        SelectedMovesList(
            selectedMoves = emptyList(),
            movesError = "Please add at least one move to the combo.",
            onRemoveMove = {},
            onMovesReordered = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddMoveDropdownPreview() {
    BreakVaultTheme {
        AddMoveDropdown(
            searchText = "Wi",
            allMoves = listOf(Move(id = "1", name = "Windmill"), Move(id = "2", name = "Whip")),
            dropdownExpanded = true,
            onSearchTextChange = {},
            onExpandedChange = {},
            onAddMove = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ComboMoveItemPreview() {
    BreakVaultTheme {
        ComboMoveItem(moveName = "Windmill", onRemove = {}, isDragging = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteComboDialogPreview() {
    BreakVaultTheme {
        DeleteComboDialog(
            comboName = "My Combo",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

//endregion