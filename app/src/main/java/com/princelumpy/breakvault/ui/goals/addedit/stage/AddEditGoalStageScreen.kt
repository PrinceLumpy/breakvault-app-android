package com.princelumpy.breakvault.ui.goals.addedit.stage

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalStageScreen(
    onNavigateUp: () -> Unit,
    goalId: String,
    stageId: String? = null,
    addEditGoalStageViewModel: AddEditGoalStageViewModel = hiltViewModel()
) {
    val uiState by addEditGoalStageViewModel.uiState.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(goalId, stageId) {
        addEditGoalStageViewModel.loadStage(goalId, stageId)
    }

    LaunchedEffect(uiState?.isNewStage) {
        if (uiState?.isNewStage == true) {
            focusRequester.requestFocus()
        }
    }

    uiState?.let { currentUiState ->
        // Create convenience variables for cleaner access
        val userInputs = currentUiState.userInputs
        val dialogState = currentUiState.dialogState

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = if (currentUiState.isNewStage) R.string.add_edit_goal_stage_add_title else R.string.add_edit_goal_stage_edit_title)) },
                    navigationIcon = {
                        IconButton(onClick = { onNavigateUp() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.common_back_button_description)
                            )
                        }
                    },
                    actions = {
                        if (!currentUiState.isNewStage) {
                            IconButton(onClick = { addEditGoalStageViewModel.showDeleteDialog(true) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.add_edit_goal_stage_delete_description),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        addEditGoalStageViewModel.saveStage { onNavigateUp() }
                    }
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = stringResource(id = R.string.add_edit_goal_stage_save_description)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppStyleDefaults.SpacingLarge)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() }
            ) {
                OutlinedTextField(
                    value = userInputs.name,
                    onValueChange = { addEditGoalStageViewModel.onNameChange(it) },
                    label = { Text(stringResource(id = R.string.add_edit_goal_stage_name_label)) },
                    isError = userInputs.isNameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                if (userInputs.isNameError) {
                    Text(
                        stringResource(id = R.string.add_edit_goal_stage_name_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = userInputs.targetCount,
                        onValueChange = { addEditGoalStageViewModel.onTargetCountChange(it) },
                        label = { Text(stringResource(id = R.string.add_edit_goal_stage_target_label)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        isError = userInputs.isTargetError,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(AppStyleDefaults.SpacingLarge))

                    OutlinedTextField(
                        value = userInputs.unit,
                        onValueChange = { addEditGoalStageViewModel.onUnitChange(it) },
                        label = { Text(stringResource(id = R.string.add_edit_goal_stage_unit_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                addEditGoalStageViewModel.saveStage { onNavigateUp() }
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (userInputs.isTargetError) {
                    Text(
                        stringResource(id = R.string.add_edit_goal_stage_target_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (dialogState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { addEditGoalStageViewModel.showDeleteDialog(false) },
                title = { Text(stringResource(id = R.string.add_edit_goal_stage_delete_dialog_title)) },
                text = { Text(stringResource(id = R.string.add_edit_goal_stage_delete_dialog_text)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            addEditGoalStageViewModel.deleteStage { onNavigateUp() }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(id = R.string.common_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { addEditGoalStageViewModel.showDeleteDialog(false) }) {
                        Text(stringResource(id = R.string.common_cancel))
                    }
                }
            )
        }
    }
}
