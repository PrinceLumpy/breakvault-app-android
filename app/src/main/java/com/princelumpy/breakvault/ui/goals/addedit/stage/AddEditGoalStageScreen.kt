package com.princelumpy.breakvault.ui.goals.addedit.stage

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.common.Constants.GOAL_STAGE_TITLE_CHARACTER_LIMIT
import com.princelumpy.breakvault.common.Constants.GOAL_STAGE_UNIT_CHARACTER_LIMIT
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
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
        val userInputs = currentUiState.userInputs
        val dialogState = currentUiState.dialogState
        val uiState = currentUiState.uiState

        Scaffold(
            topBar = {
                AddEditGoalStageTopBar(
                    isNewStage = currentUiState.isNewStage,
                    onNavigateUp = onNavigateUp,
                    onDeleteClick = { addEditGoalStageViewModel.showDeleteDialog(true) }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { addEditGoalStageViewModel.saveStage { onNavigateUp() } }
                ) {
                    Icon(
                        Icons.Filled.Save,
                        contentDescription = stringResource(id = R.string.add_edit_goal_stage_save_description)
                    )
                }
            }
        ) { innerPadding ->
            if (!uiState.isInitialLoadDone) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AddEditGoalStageContent(
                    modifier = Modifier
                        .padding(innerPadding)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { focusManager.clearFocus() }
                        ),
                    name = userInputs.name,
                    nameError = uiState.nameError,
                    onNameChange = { addEditGoalStageViewModel.onNameChange(it) },
                    currentCount = userInputs.currentCount,
                    currentCountError = uiState.currentCountError,
                    onCurrentCountChange = { addEditGoalStageViewModel.onCurrentCountChange(it) },
                    targetCount = userInputs.targetCount,
                    targetError = uiState.targetError,
                    onTargetCountChange = { addEditGoalStageViewModel.onTargetCountChange(it) },
                    unit = userInputs.unit,
                    unitError = uiState.unitError,
                    onUnitChange = { addEditGoalStageViewModel.onUnitChange(it) },
                    focusRequester = focusRequester,
                    onSave = { addEditGoalStageViewModel.saveStage { onNavigateUp() } }
                )
            }
        }

        if (dialogState.showDeleteDialog) {
            DeleteGoalStageDialog(
                onDismiss = { addEditGoalStageViewModel.showDeleteDialog(false) },
                onConfirmDelete = { addEditGoalStageViewModel.deleteStage { onNavigateUp() } }
            )
        }
    }
}

/**
 * A stateless top bar for the Add/Edit Goal Stage screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditGoalStageTopBar(
    isNewStage: Boolean,
    onNavigateUp: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(stringResource(id = if (isNewStage) R.string.add_edit_goal_stage_add_title else R.string.add_edit_goal_stage_edit_title))
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back_button_description)
                )
            }
        },
        actions = {
            if (!isNewStage) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.add_edit_goal_stage_delete_description),
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
private fun AddEditGoalStageContent(
    name: String,
    nameError: String?,
    onNameChange: (String) -> Unit,
    currentCount: String,
    currentCountError: String?,
    onCurrentCountChange: (String) -> Unit,
    targetCount: String,
    targetError: String?,
    onTargetCountChange: (String) -> Unit,
    unit: String,
    unitError: String?,
    onUnitChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppStyleDefaults.SpacingLarge)
    ) {
        // Stage Name
        OutlinedTextField(
            value = name,
            onValueChange = {
                if (it.length <= GOAL_STAGE_TITLE_CHARACTER_LIMIT) {
                    onNameChange(it)
                }
            },
            label = { Text(stringResource(id = R.string.add_edit_goal_stage_name_label)) },
            isError = nameError != null,
            supportingText = {
                nameError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

        // Current Count and Target Count Row
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = currentCount,
                onValueChange = onCurrentCountChange,
                label = { Text(stringResource(id = R.string.add_edit_goal_stage_current_label)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                isError = currentCountError != null,
                supportingText = {
                    currentCountError?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(AppStyleDefaults.SpacingMedium))

            OutlinedTextField(
                value = targetCount,
                onValueChange = onTargetCountChange,
                label = { Text(stringResource(id = R.string.add_edit_goal_stage_target_label)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                isError = targetError != null,
                supportingText = {
                    targetError?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

        // Unit Field
        OutlinedTextField(
            value = unit,
            onValueChange = {
                if (it.length <= GOAL_STAGE_UNIT_CHARACTER_LIMIT) {
                    onUnitChange(it)
                }
            },
            isError = unitError != null,
            supportingText = {
                unitError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            label = { Text(stringResource(id = R.string.add_edit_goal_stage_unit_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSave()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A stateless dialog for confirming stage deletion.
 */
@Composable
private fun DeleteGoalStageDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.add_edit_goal_stage_delete_dialog_title)) },
        text = { Text(stringResource(id = R.string.add_edit_goal_stage_delete_dialog_text)) },
        confirmButton = {
            TextButton(
                onClick = onConfirmDelete,
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

@Preview(showBackground = true, name = "Add New Stage Top Bar")
@Composable
private fun AddEditGoalStageTopBar_AddNewPreview() {
    BreakVaultTheme {
        AddEditGoalStageTopBar(
            isNewStage = true,
            onNavigateUp = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Stage Top Bar")
@Composable
private fun AddEditGoalStageTopBar_EditPreview() {
    BreakVaultTheme {
        AddEditGoalStageTopBar(
            isNewStage = false,
            onNavigateUp = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Add/Edit Stage Content")
@Composable
private fun AddEditGoalStageContent_Preview() {
    BreakVaultTheme {
        AddEditGoalStageContent(
            name = "First Set",
            nameError = null,
            onNameChange = {},
            currentCount = "45",
            currentCountError = null,
            onCurrentCountChange = {},
            targetCount = "100",
            targetError = null,
            onTargetCountChange = {},
            unit = "reps",
            unitError = null,
            onUnitChange = {},
            focusRequester = remember { FocusRequester() },
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Stage Content with Errors")
@Composable
private fun AddEditGoalStageContent_ErrorPreview() {
    BreakVaultTheme {
        AddEditGoalStageContent(
            name = "",
            nameError = "Stage name cannot be empty.",
            onNameChange = {},
            currentCount = "-5",
            currentCountError = "Current count must be 0 or greater.",
            onCurrentCountChange = {},
            targetCount = "abc",
            targetError = "Target count must be a number.",
            onTargetCountChange = {},
            unit = "A very long unit name that exceeds the limit",
            unitError = "Unit cannot exceed 10 characters.",
            onUnitChange = {},
            focusRequester = remember { FocusRequester() },
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Delete Stage Dialog")
@Composable
private fun DeleteGoalStageDialogPreview() {
    BreakVaultTheme {
        DeleteGoalStageDialog(
            onDismiss = {},
            onConfirmDelete = {}
        )
    }
}

//endregion