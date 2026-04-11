package com.princelumpy.breakvault.ui.goals.addedit

import AppStyleDefaults
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.KeyboardCapitalization
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
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    val userInputs = uiState.userInputs
    val dialogState = uiState.dialogState

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
        onStageMove = { from, to -> addEditGoalViewModel.onStageMove(from, to) },
        onArchiveClick = { addEditGoalViewModel.archiveGoal { onNavigateUp() } },
        onDeleteClick = { showDeleteConfirmationDialog = true },
        hasUnsavedChanges = addEditGoalViewModel.hasUnsavedChanges(),
        onSaveClick = { addEditGoalViewModel.saveGoal { onNavigateUp() } },
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
    onStageMove: (Int, Int) -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    hasUnsavedChanges: Boolean,
    onSaveClick: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
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
                onStageMove = onStageMove
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
    onStageMove: (Int, Int) -> Unit
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
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Sentences,
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
                .fillMaxWidth(),
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
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences,
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
            onStageMove = onStageMove
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
 * A stateless composable to display the list of goal stages.
 */
@Composable
private fun GoalStagesList(
    stages: List<GoalStage>,
    onAddStageClick: () -> Unit,
    onEditStageClick: (GoalStage) -> Unit,
    onStageMove: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
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
        val hapticFeedback = LocalHapticFeedback.current
        val lazyListState = rememberLazyListState()
        val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
            onStageMove(from.index, to.index)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp),
        ) {
            items(stages, key = { it.id }) { goalStage ->
                ReorderableItem(reorderableState, key = goalStage.id) { isDragging ->
                    ReorderableGoalStageItem(
                        stage = goalStage,
                        isDragging = isDragging,
                        onClick = { onEditStageClick(goalStage) },
                        scope = this
                    )
                }
            }
        }
    }
}

@Composable
private fun ReorderableGoalStageItem(
    stage: GoalStage,
    isDragging: Boolean,
    onClick: () -> Unit,
    scope: sh.calvin.reorderable.ReorderableCollectionItemScope,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 2.dp,
        label = "elevation"
    )

    val stageProgress = if (stage.targetCount > 0) {
        (stage.currentCount.toDouble() / stage.targetCount.toDouble()).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.small,
        shadowElevation = elevation,
        tonalElevation = if (isDragging) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(AppStyleDefaults.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle icon
            IconButton(
                onClick = {},
                modifier = with(scope) {
                    Modifier.draggableHandle()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Reorder stage",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(AppStyleDefaults.SpacingSmall))

            // Stage content
            Column(
                modifier = Modifier.weight(1f)
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
            // Edit Icon
            IconButton(
                onClick = onClick
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit stage",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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