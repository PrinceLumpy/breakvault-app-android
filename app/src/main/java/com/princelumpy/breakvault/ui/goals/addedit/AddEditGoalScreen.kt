package com.princelumpy.breakvault.ui.goals.addedit

import AppStyleDefaults
import GoalInputDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.ui.components.GoalStageItem
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun AddEditGoalScreen(
    onNavigateUp: () -> Unit,
    onNavigateToAddEditStage: (String, String?) -> Unit,
    addEditGoalViewModel: AddEditGoalViewModel = hiltViewModel()
) {
    val uiState by addEditGoalViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val userInputs = uiState.userInputs
    val dialogState = uiState.dialogState

    LaunchedEffect(dialogState.snackbarMessage) {
        dialogState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            addEditGoalViewModel.onSnackbarMessageShown()
        }
    }

    LaunchedEffect(dialogState.navigateToAddStageWithGoalId) {
        dialogState.navigateToAddStageWithGoalId?.let { goalIdForStage ->
            onNavigateToAddEditStage(goalIdForStage, null)
            addEditGoalViewModel.onNavigateToAddStageDone()
        }
    }

    LaunchedEffect(dialogState.navigateToEditStage) {
        dialogState.navigateToEditStage?.let { stage ->
            uiState.goalId?.let {
                onNavigateToAddEditStage(it, stage.id)
            }
            addEditGoalViewModel.onNavigateToEditStageDone()
        }
    }

    if (showDeleteConfirmationDialog) {
        DeleteGoalConfirmationDialog(
            onDismiss = { showDeleteConfirmationDialog = false },
            onConfirmDelete = {
                addEditGoalViewModel.deleteGoal { onNavigateUp() }
                showDeleteConfirmationDialog = false
            }
        )
    }

    dialogState.addingRepsToStage?.let { stage ->
        AddRepsDialog(
            stageUnit = stage.unit,
            onDismiss = { addEditGoalViewModel.onAddRepsDismissed() },
            onConfirm = { reps ->
                addEditGoalViewModel.addRepsToStage(stage, reps)
            }
        )
    }

    AddEditGoalScaffold(
        snackbarHostState = snackbarHostState,
        isNewGoal = uiState.isNewGoal,
        isLoading = uiState.isLoading,
        title = userInputs.title,
        onTitleChange = { addEditGoalViewModel.onTitleChange(it) },
        description = userInputs.description,
        onDescriptionChange = { addEditGoalViewModel.onDescriptionChange(it) },
        stages = uiState.stages,
        onAddStageClick = { addEditGoalViewModel.onAddStageClicked() },
        onEditStageClick = { addEditGoalViewModel.onEditStageClicked(it) },
        onAddRepsClick = { addEditGoalViewModel.onAddRepsClicked(it) },
        onArchiveClick = { addEditGoalViewModel.archiveGoal { onNavigateUp() } },
        onDeleteClick = { showDeleteConfirmationDialog = true },
        onSaveClick = { addEditGoalViewModel.saveGoal { onNavigateUp() } },
        onNavigateUp = onNavigateUp
    )
}

/**
 * A stateless scaffold for the Add/Edit Goal screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditGoalScaffold(
    snackbarHostState: SnackbarHostState,
    isNewGoal: Boolean,
    isLoading: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    stages: List<GoalStage>,
    onAddStageClick: () -> Unit,
    onEditStageClick: (GoalStage) -> Unit,
    onAddRepsClick: (GoalStage) -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AddEditGoalTopBar(
                isNewGoal = isNewGoal,
                onNavigateUp = onNavigateUp,
                onArchiveClick = onArchiveClick,
                onDeleteClick = onDeleteClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                focusManager.clearFocus()
                onSaveClick()
            }) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.save_goal_content_description),
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AddEditGoalContent(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(AppStyleDefaults.SpacingLarge),
                title = title,
                onTitleChange = onTitleChange,
                description = description,
                onDescriptionChange = onDescriptionChange,
                stages = stages,
                onAddStageClick = onAddStageClick,
                onEditStageClick = onEditStageClick,
                onAddRepsClick = onAddRepsClick
            )
        }
    }
}

/**
 * The main, stateless content of the screen containing the input form and stages list.
 */
@Composable
private fun AddEditGoalContent(
    modifier: Modifier = Modifier,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    stages: List<GoalStage>,
    onAddStageClick: () -> Unit,
    onEditStageClick: (GoalStage) -> Unit,
    onAddRepsClick: (GoalStage) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(id = R.string.add_edit_goal_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(id = R.string.add_edit_goal_description_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(GoalInputDefaults.DESCRIPTION_FIELD_HEIGHT)
        )

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingExtraLarge))

        GoalStagesList(
            stages = stages,
            onAddStageClick = onAddStageClick,
            onEditStageClick = onEditStageClick,
            onAddRepsClick = onAddRepsClick
        )
    }
}

/**
 * A stateless TopAppBar for the Add/Edit Goal screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditGoalTopBar(
    isNewGoal: Boolean,
    onNavigateUp: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(id = if (isNewGoal) R.string.add_edit_goal_create_title else R.string.add_edit_goal_edit_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.common_back_button_description)
                )
            }
        },
        actions = {
            if (!isNewGoal) {
                IconButton(onClick = onArchiveClick) {
                    Icon(
                        Icons.Filled.Archive,
                        contentDescription = stringResource(id = R.string.add_edit_goal_archive_description)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.add_edit_goal_delete_description)
                    )
                }
            }
        }
    )
}

/**
 * A stateless composable to display the list of goal stages.
 */
@Composable
private fun GoalStagesList(
    stages: List<GoalStage>,
    onAddStageClick: () -> Unit,
    onEditStageClick: (GoalStage) -> Unit,
    onAddRepsClick: (GoalStage) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.add_edit_goal_stages_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Button(onClick = onAddStageClick) {
            Icon(
                Icons.Default.Add,
                contentDescription = null // Decorative
            )
            Spacer(modifier = Modifier.padding(start = AppStyleDefaults.SpacingSmall))
            Text(stringResource(id = R.string.add_edit_goal_add_stage_button))
        }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingLarge))

    if (stages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDefaults.SpacingLarge),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.add_edit_goal_no_stages_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column {
            stages.forEach { goalStage ->
                GoalStageItem(
                    goalStage = goalStage,
                    onEditClick = { onEditStageClick(goalStage) },
                    onAddRepsClick = { onAddRepsClick(goalStage) }
                )
            }
        }
    }
}

/**
 * A stateless dialog for adding reps to a goal stage.
 */
@Composable
private fun AddRepsDialog(
    stageUnit: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var reps by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_edit_goal_add_reps_dialog_title)) },
        text = {
            OutlinedTextField(
                value = reps,
                onValueChange = { reps = it },
                label = { Text(stageUnit.replaceFirstChar { it.uppercase() }) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { reps.toIntOrNull()?.let { onConfirm(it) } },
                enabled = reps.toIntOrNull() != null
            ) {
                Text(stringResource(id = R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}

/**
 * A stateless dialog for confirming goal deletion.
 */
@Composable
private fun DeleteGoalConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.add_edit_goal_delete_dialog_title)) },
        text = { Text(stringResource(id = R.string.add_edit_goal_delete_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
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
private fun AddEditGoalContent_Preview() {
    BreakVaultTheme {
        AddEditGoalContent(
            modifier = Modifier.padding(AppStyleDefaults.SpacingLarge),
            title = "Master the Planche",
            onTitleChange = {},
            description = "Hold a full planche for 30 seconds.",
            onDescriptionChange = {},
            stages = listOf(
                GoalStage("1", "1", "Tuck Planche", 30, 60, "seconds"),
                GoalStage("2", "1", "Straddle Planche", 5, 30, "seconds")
            ),
            onAddStageClick = {},
            onEditStageClick = {},
            onAddRepsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditGoalTopBar_NewGoalPreview() {
    BreakVaultTheme {
        AddEditGoalTopBar(
            isNewGoal = true,
            onNavigateUp = {},
            onArchiveClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditGoalTopBar_EditGoalPreview() {
    BreakVaultTheme {
        AddEditGoalTopBar(
            isNewGoal = false,
            onNavigateUp = {},
            onArchiveClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalStagesList_WithStagesPreview() {
    BreakVaultTheme {
        GoalStagesList(
            stages = listOf(
                GoalStage("1", "1", "First Stage", 10, 20, "reps"),
                GoalStage("2", "1", "Second Stage", 5, 10, "sets")
            ),
            onAddStageClick = {},
            onEditStageClick = {},
            onAddRepsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalStagesList_NoStagesPreview() {
    BreakVaultTheme {
        GoalStagesList(
            stages = emptyList(),
            onAddStageClick = {},
            onEditStageClick = {},
            onAddRepsClick = {}
        )
    }
}

@Preview
@Composable
private fun AddRepsDialogPreview() {
    BreakVaultTheme {
        AddRepsDialog(
            stageUnit = "seconds",
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview
@Composable
private fun DeleteGoalConfirmationDialogPreview() {
    BreakVaultTheme {
        DeleteGoalConfirmationDialog(
            onDismiss = {},
            onConfirmDelete = {}
        )
    }
}

//endregion
