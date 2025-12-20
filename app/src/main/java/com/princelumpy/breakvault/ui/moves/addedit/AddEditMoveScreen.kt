package com.princelumpy.breakvault.ui.moves.addedit


import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMoveScreen(
    onNavigateUp: () -> Unit,
    moveId: String?,
    moveViewModel: AddEditMoveViewModel = hiltViewModel()
) {
    // UPDATED: Use collectAsStateWithLifecycle for better lifecycle management.
    val uiState by moveViewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Create convenience variables for cleaner access.
    val userInputs = uiState.userInputs
    val dialogState = uiState.dialogState

    LaunchedEffect(key1 = moveId) {
        moveViewModel.loadMove(moveId)
    }

    LaunchedEffect(dialogState.snackbarMessage) {
        dialogState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            moveViewModel.onSnackbarMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isNewMove) stringResource(id = R.string.add_edit_move_add_new_move_title) else stringResource(
                            id = R.string.add_edit_move_edit_move_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                moveViewModel.saveMove {
                    focusManager.clearFocus()
                    onNavigateUp()
                }
            }) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.add_edit_move_save_move_fab_description)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(AppStyleDefaults.SpacingLarge)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
        ) {
            OutlinedTextField(
                // UPDATED: Access state from userInputs
                value = userInputs.moveName,
                onValueChange = { moveViewModel.onMoveNameChange(it) },
                label = { Text(stringResource(id = R.string.add_edit_move_move_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { moveViewModel.saveMove { onNavigateUp() } })
            )

            Text(
                stringResource(id = R.string.add_edit_move_select_tags_label),
                style = MaterialTheme.typography.titleMedium
            )
            // UPDATED: Access state from userInputs
            if (uiState.allTags.isEmpty() && userInputs.newTagName.isBlank()) {
                Text(
                    stringResource(id = R.string.add_edit_move_no_tags_available_message),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
            ) {
                uiState.allTags.forEach { tag ->
                    // UPDATED: Check for tag ID in userInputs.selectedTags
                    val isSelected = userInputs.selectedTags.contains(tag.id)
                    FilterChip(
                        selected = isSelected,
                        // UPDATED: Pass the tag's ID instead of the object
                        onClick = { moveViewModel.onTagSelected(tag.id) },
                        label = { Text(tag.name) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Filled.Done,
                                    stringResource(id = R.string.add_edit_move_selected_chip_description),
                                    Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))

            Text(
                stringResource(id = R.string.add_edit_move_add_new_tag_label),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                OutlinedTextField(
                    // UPDATED: Access state from userInputs
                    value = userInputs.newTagName,
                    onValueChange = { moveViewModel.onNewTagNameChange(it) },
                    label = { Text(stringResource(id = R.string.add_edit_move_new_tag_name_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { moveViewModel.addTag() })
                )
                Button(onClick = { moveViewModel.addTag() }) {
                    Text(stringResource(id = R.string.common_add))
                }
            }
        }
    }
}

// Previews remain the same as they don't depend on internal ViewModel logic.
@Preview(showBackground = true, name = "Add Mode")
@Composable
fun AddEditMoveScreenPreview_AddMode() {
    ComboGeneratorTheme {
        AddEditMoveScreen(
            onNavigateUp = {},
            moveId = null
        )
    }
}

@Preview(showBackground = true, name = "Edit Mode")
@Composable
fun AddEditMoveScreenPreview_EditMode() {
    ComboGeneratorTheme {
        AddEditMoveScreen(
            onNavigateUp = {},
            moveId = "previewEditId"
        )
    }
}
