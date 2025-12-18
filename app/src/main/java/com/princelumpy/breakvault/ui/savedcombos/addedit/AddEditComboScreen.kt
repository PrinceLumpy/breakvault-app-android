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
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryEditable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditComboScreen(
    navController: NavController,
    comboId: String?,
    savedComboViewModel: SavedComboViewModel = viewModel()
) {
    val uiState by savedComboViewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = comboId) {
        savedComboViewModel.loadCombo(comboId)
    }

    LaunchedEffect(Unit) {
        if (uiState.isNewCombo) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isNewCombo) stringResource(R.string.create_combo_title) else stringResource(
                            R.string.edit_combo_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back_button_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.comboName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Must add a name before saving")
                        }
                    } else if (uiState.selectedMoves.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("A combo must have at least one move")
                        }
                    } else {
                        savedComboViewModel.saveCombo {
                            focusManager.clearFocus()
                            navController.popBackStack()
                        }
                    }
                },
                containerColor = if (uiState.comboName.isNotBlank() && uiState.selectedMoves.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(R.string.save_combo_fab_description)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppStyleDefaults.SpacingLarge)
                .verticalScroll(rememberScrollState())
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
        ) {
            OutlinedTextField(
                value = uiState.comboName,
                onValueChange = { savedComboViewModel.onComboNameChange(it) },
                label = { Text(stringResource(R.string.combo_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Text(
                stringResource(R.string.select_moves_title),
                style = MaterialTheme.typography.titleMedium
            )

            if (uiState.selectedMoves.isEmpty()) {
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
                    uiState.selectedMoves.forEachIndexed { index, moveName ->
                        ComboMoveItem(
                            moveName = moveName,
                            onRemove = { savedComboViewModel.removeMoveFromCombo(index) }
                        )
                    }
                }
            }

            val filteredMoves = uiState.allMoves.filter {
                it.name.contains(uiState.searchText, ignoreCase = true)
            }

            ExposedDropdownMenuBox(
                expanded = uiState.expanded,
                onExpandedChange = { savedComboViewModel.onExpandedChange(!uiState.expanded) },
                modifier = Modifier.padding(bottom = AppStyleDefaults.SpacingExtraLarge) // Extra padding for FAB
            ) {
                OutlinedTextField(
                    value = uiState.searchText,
                    onValueChange = { savedComboViewModel.onSearchTextChange(it) },
                    label = { Text(stringResource(id = R.string.add_edit_combo_add_move_label)) },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (uiState.searchText.isNotBlank()) {
                                savedComboViewModel.addMoveToCombo(uiState.searchText.trim())
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
                        .menuAnchor(PrimaryEditable),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (filteredMoves.isNotEmpty()) {
                                savedComboViewModel.addMoveToCombo(filteredMoves.first().name)
                            } else if (uiState.searchText.isNotBlank()) {
                                savedComboViewModel.addMoveToCombo(uiState.searchText.trim())
                            }
                        }
                    )
                )

                if (filteredMoves.isNotEmpty() && uiState.expanded) {
                    ExposedDropdownMenu(
                        expanded = uiState.expanded,
                        onDismissRequest = { savedComboViewModel.onExpandedChange(false) }
                    ) {
                        filteredMoves.forEach { move ->
                            DropdownMenuItem(
                                text = { Text(move.name) },
                                onClick = { savedComboViewModel.addMoveToCombo(move.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComboMoveItem(
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
