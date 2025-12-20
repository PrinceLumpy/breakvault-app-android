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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
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
                        Icons.Default.Save,
                        contentDescription = stringResource(id = R.string.add_edit_goal_stage_save_description)
                    )
                }
            }
        ) { innerPadding ->
            AddEditGoalStageContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { focusManager.clearFocus() }
                    ),
                name = userInputs.name,
                isNameError = userInputs.isNameError,
                onNameChange = { addEditGoalStageViewModel.onNameChange(it) },
                targetCount = userInputs.targetCount,
                isTargetError = userInputs.isTargetError,
                onTargetCountChange = { addEditGoalStageViewModel.onTargetCountChange(it) },
                unit = userInputs.unit,
                onUnitChange = { addEditGoalStageViewModel.onUnitChange(it) },
                focusRequester = focusRequester,
                onSave = { addEditGoalStageViewModel.saveStage { onNavigateUp() } }
            )
        }

        if (currentUiState.dialogState.showDeleteDialog) {
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
                        Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.add_edit_goal_stage_delete_description),
                        tint = MaterialTheme.colorScheme.error
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
    isNameError: Boolean,
    onNameChange: (String) -> Unit,
    targetCount: String,
    isTargetError: Boolean,
    onTargetCountChange: (String) -> Unit,
    unit: String,
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
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(id = R.string.add_edit_goal_stage_name_label)) },
            isError = isNameError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        if (isNameError) {
            Text(
                stringResource(id = R.string.add_edit_goal_stage_name_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = targetCount,
                onValueChange = onTargetCountChange,
                label = { Text(stringResource(id = R.string.add_edit_goal_stage_target_label)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                isError = isTargetError,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(AppStyleDefaults.SpacingLarge))

            OutlinedTextField(
                value = unit,
                onValueChange = onUnitChange,
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
                modifier = Modifier.weight(1f)
            )
        }

        if (isTargetError) {
            Text(
                stringResource(id = R.string.add_edit_goal_stage_target_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
@Composable
private fun AddEditGoalStageContent_Preview() {
    BreakVaultTheme {
        AddEditGoalStageContent(
            name = "First Set",
            isNameError = false,
            onNameChange = {},
            targetCount = "100",
            isTargetError = false,
            onTargetCountChange = {},
            unit = "reps",
            onUnitChange = {},
            focusRequester = remember { FocusRequester() },
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditGoalStageContent_ErrorPreview() {
    BreakVaultTheme {
        AddEditGoalStageContent(
            name = "",
            isNameError = true,
            onNameChange = {},
            targetCount = "abc",
            isTargetError = true,
            onTargetCountChange = {},
            unit = "reps",
            onUnitChange = {},
            focusRequester = remember { FocusRequester() },
            onSave = {}
        )
    }
}

@Preview
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
