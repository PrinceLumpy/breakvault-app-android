package com.princelumpy.breakvault.ui.goals.addedit

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableLazyListState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.common.Constants.GOAL_DESCRIPTION_CHARACTER_LIMIT
import com.princelumpy.breakvault.common.Constants.GOAL_TITLE_CHARACTER_LIMIT
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.ui.common.AppLinearProgressIndicator
import com.princelumpy.breakvault.ui.common.UnsavedChangesDialog

@Composable
fun AddEditGoalScreen(
    onNavigateUp: () -> Unit,
    onNavigateToAddEditStage: (String, String?) -> Unit,
    addEditGoalViewModel: AddEditGoalViewModel = hiltViewModel()
) {
    val uiState by addEditGoalViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

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
        dialogState.navigateToEditStage?.let { (goalId, stageId) ->
            onNavigateToAddEditStage(goalId, stageId)
            addEditGoalViewModel.onNavigateToEditStageDone()
        }
    }

    if (showDeleteConfirmationDialog) {
        DeleteGoalConfirmationDialog(
            onDismiss = { showDeleteConfirmationDialog = false },
            onConfirmDelete = {
                showDeleteConfirmationDialog = false
                addEditGoalViewModel.deleteGoal { onNavigateUp() }
            }
        )
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
        if (addEditGoalViewModel.hasUnsavedChanges()) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateUp()
        }
    }

    AddEditGoalScaffold(
        snackbarHostState = snackbarHostState,
        isNewGoal = uiState.isNewGoal,
        isLoading = uiState.isLoading,
        title = userInputs.title,
        onTitleChange = { addEditGoalViewModel.onTitleChange(it) },
        titleError = uiState.titleError,
        description = userInputs.description,
        onDescriptionChange = { addEditGoalViewModel.onDescriptionChange(it) },
        descriptionError = uiState.descriptionError,
        stages = uiState.stages,
        onAddStageClick = { addEditGoalViewModel.onAddStageClicked() },
        onEditStageClick = { addEditGoalViewModel.onEditStageClicked(it) },
        onStagesReordered = { addEditGoalViewModel.onStagesReordered(it) },
        onArchiveClick = { addEditGoalViewModel.archiveGoal { onNavigateUp() } },
        onDeleteClick = { showDeleteConfirmationDialog = true },
        hasUnsavedChanges = addEditGoalViewModel.hasUnsavedChanges(),
        onSaveClick = { addEditGoalViewModel.saveGoal(showSnackbar = false) { onNavigateUp() } },
        onNavigateUp = {
            if (addEditGoalViewModel.hasUnsavedChanges()) {
                showUnsavedChangesDialog = true
            } else {
                onNavigateUp()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditGoalScaffold(
    snackbarHostState: SnackbarHostState,
    isNewGoal: Boolean,
    isLoading: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    titleError: String?,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionError: String?,
    stages: List<GoalStage>,
    onAddStageClick: () -> Unit,
    onEditStageClick: (GoalStage) -> Unit,
    onStagesReordered: (List<GoalStage>) -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    hasUnsavedChanges: Boolean,
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
            FloatingActionButton(
                onClick = {
                    focusManager.clearFocus()
                    onSaveClick()
                },
                modifier = Modifier.imePadding(),
                // Updated FAB color: enabled only if title is filled AND there are unsaved changes
                containerColor = if (title.isNotBlank() && hasUnsavedChanges) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
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
                titleError = titleError,
                description = description,
                onDescriptionChange = onDescriptionChange,
                descriptionError = descriptionError,
                stages = stages,
                onAddStageClick = onAddStageClick,
                onEditStageClick = onEditStageClick,
                onStagesReordered = onStagesReordered
            )
        }
    }
}

@Composable
private fun AddEditGoalContent(
    modifier: Modifier = Modifier,
    title: String,
    onTitleChange: (String) -> Unit,
    titleError: String?,
    description: String,
    onDescriptionChange: (String) -> Unit,
    descriptionError: String?,
    stages: List<GoalStage>,
    onAddStageClick: () -> Unit,
    onEditStageClick: (GoalStage) -> Unit,
    onStagesReordered: (List<GoalStage>) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Goal Title
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(id = R.string.add_edit_goal_title_label)) },
            placeholder = { Text(stringResource(id = R.string.add_edit_goal_title_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            isError = titleError != null,
            // Updated to show error text from ViewModel
            supportingText = {
                if (titleError != null) {
                    Text(titleError, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(
                        text = "${title.length}/$GOAL_TITLE_CHARACTER_LIMIT",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

        // Goal Description
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(id = R.string.add_edit_goal_description_label)) },
            placeholder = { Text(stringResource(id = R.string.add_edit_goal_description_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            isError = descriptionError != null,
            // Updated to show error text from ViewModel
            supportingText = {
                if (descriptionError != null) {
                    Text(descriptionError, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(
                        text = "${description.length}/$GOAL_DESCRIPTION_CHARACTER_LIMIT",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

        // Goal Stages Section
        GoalStagesList(
            stages = stages,
            onAddStageClick = onAddStageClick,
            onEditStageClick = onEditStageClick,
            onStagesReordered = onStagesReordered
        )
    }
}

// ... Rest of the file (TopBar, GoalStagesList, EditGoalStageItem, etc.) remains as is ...

/**
 * Top App Bar for Add/Edit Goal screen.
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
        title = {
            Text(
                if (isNewGoal) {
                    stringResource(id = R.string.add_edit_goal_new_goal_title)
                } else {
                    stringResource(id = R.string.add_edit_goal_edit_goal_title)
                }
            )
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
 * A stateless composable to display the list of goal stages with drag-to-reorder functionality.
 */
@Composable
private fun GoalStagesList(
    stages: List<GoalStage>,
    onAddStageClick: () -> Unit,
    onEditStageClick: (GoalStage) -> Unit,
    onStagesReordered: (List<GoalStage>) -> Unit
) {
    var stagesList by remember { mutableStateOf(stages) }
    var isDragging by remember { mutableStateOf(false) }

    // Update the local list when stages prop changes (but not while dragging)
    LaunchedEffect(stages) {
        if (!isDragging) {
            stagesList = stages
        }
    }
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

    if (stagesList.isEmpty()) {
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
        val haptic = LocalHapticFeedback.current
        val lazyListState = rememberLazyListState()

        val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
            stagesList = stagesList.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }

        // Update pending order in ViewModel when dragging ends
        LaunchedEffect(isDragging) {
            if (!isDragging && stagesList.map { it.id } != stages.map { it.id }) {
                onStagesReordered(stagesList)
            }
        }

        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Give it a fixed height since it's in a scrollable parent
        ) {
            items(stagesList, key = { it.id }) { goalStage ->
                ReorderableItem(reorderableLazyListState, key = goalStage.id) { isItemDragging ->
                    LaunchedEffect(isItemDragging) {
                        if (isItemDragging) {
                            isDragging = true
                        } else if (isDragging) {
                            // Small delay to ensure all items have stopped dragging
                            kotlinx.coroutines.delay(50)
                            isDragging = false
                        }
                    }

                    val dragHandleModifier = Modifier.draggableHandle(
                        onDragStarted = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )

                    EditGoalStageItem(
                        stage = goalStage,
                        onClick = { onEditStageClick(goalStage) },
                        isDragging = isItemDragging,
                        dragHandleModifier = dragHandleModifier
                    )
                }
            }
        }
    }
}

@Composable
private fun EditGoalStageItem(
    stage: GoalStage,
    onClick: () -> Unit,
    isDragging: Boolean,
    dragHandleModifier: Modifier
) {
    val stageProgress = if (stage.targetCount > 0) {
        (stage.currentCount.toDouble() / stage.targetCount.toDouble()).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

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
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to reorder",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stage content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() }
            ) {
                Text(
                    text = stage.name.ifBlank { "Untitled Stage" },
                    style = MaterialTheme.typography.bodyLarge
                )

                if (stage.targetCount > 0) {
                    Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                    AppLinearProgressIndicator(
                        progress = { stageProgress.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${stage.currentCount} / ${stage.targetCount} reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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