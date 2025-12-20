package com.princelumpy.breakvault.ui.savedcombos.addedit

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBar
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun AddEditComboScreen(
    onNavigateUp: () -> Unit,
    comboId: String?,
    addEditComboViewModel: AddEditComboViewModel = hiltViewModel()
) {
    val uiState by addEditComboViewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = comboId) {
        addEditComboViewModel.loadCombo(comboId)
    }

    LaunchedEffect(uiState.isNewCombo) {
        if (uiState.isNewCombo) {
            focusRequester.requestFocus()
        }
    }

    AddEditComboScaffold(
        uiState = uiState,
        focusRequester = focusRequester,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onComboNameChange = { addEditComboViewModel.onComboNameChange(it) },
        onRemoveMove = { addEditComboViewModel.removeMoveFromCombo(it) },
        onSearchTextChange = { addEditComboViewModel.onSearchTextChange(it) },
        onExpandedChange = { addEditComboViewModel.onExpandedChange(it) },
        onAddMove = { addEditComboViewModel.addMoveToCombo(it) },
        onSaveCombo = {
            addEditComboViewModel.saveCombo {
                focusManager.clearFocus()
                onNavigateUp()
            }
        }
    )
}

/**
 * A stateless scaffold that handles the overall layout for the Add/Edit Combo screen.
 */
@Composable
private fun AddEditComboScaffold(
    uiState: AddEditComboUiState,
    focusRequester: FocusRequester,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onComboNameChange: (String) -> Unit,
    onRemoveMove: (Int) -> Unit,
    onSearchTextChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onAddMove: (String) -> Unit,
    onSaveCombo: () -> Unit,
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
                onNavigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveCombo,
                containerColor = if (userInputs.comboName.isNotBlank() && userInputs.selectedMoves.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(R.string.save_combo_fab_description)
                )
            }
        }
    ) { paddingValues ->
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
            onSearchTextChange = onSearchTextChange,
            onExpandedChange = onExpandedChange,
            onAddMove = onAddMove
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
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                if (isNewCombo) stringResource(R.string.create_combo_title) else stringResource(R.string.edit_combo_title)
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back_button_description)
                )
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
    onSearchTextChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onAddMove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val userInputs = uiState.userInputs

    Column(
        modifier = modifier
            .padding(horizontal = AppStyleDefaults.SpacingLarge)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
    ) {
        OutlinedTextField(
            value = userInputs.comboName,
            onValueChange = onComboNameChange,
            label = { Text(stringResource(R.string.combo_name_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        SelectedMovesList(
            selectedMoves = userInputs.selectedMoves,
            onRemoveMove = onRemoveMove
        )

        AddMoveDropdown(
            searchText = userInputs.searchText,
            allMoves = uiState.allMoves,
            dropdownExpanded = uiState.dialogState.dropdownExpanded,
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
    onRemoveMove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.select_moves_title),
            style = MaterialTheme.typography.titleMedium
        )

        if (selectedMoves.isEmpty()) {
            Text(
                text = stringResource(id = R.string.add_edit_combo_no_moves_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                selectedMoves.forEachIndexed { index, moveName ->
                    ComboMoveItem(
                        moveName = moveName,
                        onRemove = { onRemoveMove(index) }
                    )
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

//region Previews

@Composable
private fun ComboMoveItem(
    moveName: String,
    onRemove: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDefaults.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = moveName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.add_edit_combo_remove_move_description),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditComboTopBar_NewPreview() {
    BreakVaultTheme {
        AddEditComboTopBar(isNewCombo = true, onNavigateUp = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditComboTopBar_EditPreview() {
    BreakVaultTheme {
        AddEditComboTopBar(isNewCombo = false, onNavigateUp = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedMovesList_WithMoves_Preview() {
    BreakVaultTheme {
        SelectedMovesList(
            selectedMoves = listOf("Windmill", "Flare", "Airflare"),
            onRemoveMove = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectedMovesList_NoMoves_Preview() {
    BreakVaultTheme {
        SelectedMovesList(selectedMoves = emptyList(), onRemoveMove = {})
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
        ComboMoveItem(moveName = "Windmill", onRemove = {})
    }
}

//endregion
