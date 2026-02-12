package com.princelumpy.breakvault.ui.goals.archived

import AppStyleDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.local.relation.GoalWithStages
import com.princelumpy.breakvault.ui.common.AppLinearProgressIndicator
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun ArchivedGoalsScreen(
    onNavigateUp: () -> Unit,
    archivedGoalsViewModel: ArchivedGoalsViewModel = hiltViewModel()
) {
    val uiState by archivedGoalsViewModel.uiState.collectAsStateWithLifecycle()
    val dialogState = uiState.dialogState

    dialogState.goalToUnarchive?.let { goalWithStages ->
        UnarchiveConfirmationDialog(
            goalTitle = goalWithStages.goal.title,
            onDismiss = { archivedGoalsViewModel.onCancelGoalUnarchive() },
            onConfirmUnarchive = { archivedGoalsViewModel.onConfirmGoalUnarchive() }
        )
    }

    dialogState.goalToDelete?.let { goalWithStages ->
        DeleteConfirmationDialog(
            goalTitle = goalWithStages.goal.title,
            onDismiss = { archivedGoalsViewModel.onCancelGoalDelete() },
            onConfirmDelete = { archivedGoalsViewModel.onConfirmGoalDelete() }
        )
    }

    ArchivedGoalsScaffold(
        archivedGoals = uiState.archivedGoals,
        onNavigateUp = onNavigateUp,
        onUnarchiveClick = { goal -> archivedGoalsViewModel.onGoalUnarchiveClicked(goal) },
        onDeleteClick = { goal -> archivedGoalsViewModel.onGoalDeleteClicked(goal) }
    )
}

/**
 * A stateless scaffold that handles the overall layout for the Archived Goals screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchivedGoalsScaffold(
    archivedGoals: List<GoalWithStages>,
    onNavigateUp: () -> Unit,
    onUnarchiveClick: (GoalWithStages) -> Unit,
    onDeleteClick: (GoalWithStages) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.archived_goals_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (archivedGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.archived_goals_no_goals))
            }
        } else {
            ArchivedGoalsList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                archivedGoals = archivedGoals,
                onUnarchiveClick = onUnarchiveClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

/**
 * A stateless list of archived goals.
 */
@Composable
private fun ArchivedGoalsList(
    archivedGoals: List<GoalWithStages>,
    onUnarchiveClick: (GoalWithStages) -> Unit,
    onDeleteClick: (GoalWithStages) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = AppStyleDefaults.LazyListPadding,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        items(archivedGoals, key = { it.goal.id }) { goalWithStages ->
            ExpandableArchivedGoalCard(
                goalWithStages = goalWithStages,
                onUnarchiveClick = { onUnarchiveClick(goalWithStages) },
                onDeleteClick = { onDeleteClick(goalWithStages) }
            )
        }
    }
}

@Composable
private fun ExpandableArchivedGoalCard(
    goalWithStages: GoalWithStages,
    onUnarchiveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val progress = calculateGoalProgress(goalWithStages.stages)
    val hasStages = goalWithStages.stages.isNotEmpty()

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron rotation"
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(AppStyleDefaults.SpacingLarge)) {
            // Goal Header - clickable to expand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hasStages) { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = goalWithStages.goal.title.ifBlank {
                                stringResource(id = R.string.goals_screen_untitled_goal)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasStages) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotationAngle),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (hasStages) {
                        Text(
                            text = "${goalWithStages.stages.size} ${if (goalWithStages.stages.size == 1) "stage" else "stages"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onUnarchiveClick) {
                        Icon(
                            imageVector = Icons.Filled.Unarchive,
                            contentDescription = stringResource(id = R.string.archived_goals_unarchive_goal_description),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(id = R.string.goals_screen_delete_goal_description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Goal Description
            if (goalWithStages.goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                Text(
                    text = goalWithStages.goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Overall Progress
            if (hasStages) {
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))
                AppLinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                Text(
                    text = stringResource(
                        id = R.string.goals_screen_progress_text,
                        (progress * 100).toInt()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Expandable Stages List (read-only)
            AnimatedVisibility(visible = isExpanded && hasStages) {
                Column(
                    modifier = Modifier.padding(top = AppStyleDefaults.SpacingMedium)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingSmall))

                    goalWithStages.stages.forEach { stage ->
                        ArchivedGoalStageItem(stage = stage)
                        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedGoalStageItem(stage: GoalStage) {
    val stageProgress = if (stage.targetCount > 0) {
        (stage.currentCount.toDouble() / stage.targetCount.toDouble()).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(AppStyleDefaults.SpacingMedium)
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

// HELPER FUNCTIONS
private fun calculateGoalProgress(stages: List<GoalStage>): Double {
    if (stages.isEmpty()) return 0.0
    val totalProgress = stages.sumOf {
        it.currentCount.toDouble().coerceAtMost(it.targetCount.toDouble()) / it.targetCount
    }
    return totalProgress / stages.size
}

/**
 * A stateless dialog to confirm unarchiving a goal.
 */
@Composable
private fun UnarchiveConfirmationDialog(
    goalTitle: String,
    onDismiss: () -> Unit,
    onConfirmUnarchive: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.archived_goals_unarchive_dialog_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.archived_goals_unarchive_dialog_message,
                    goalTitle
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmUnarchive) {
                Text(stringResource(id = R.string.common_unarchive))
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
 * A stateless dialog to confirm deleting a goal.
 */
@Composable
private fun DeleteConfirmationDialog(
    goalTitle: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.archived_goals_delete_dialog_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.archived_goals_delete_dialog_message,
                    goalTitle
                )
            )
        },
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

@Preview(showBackground = true, name = "Archived Goals Screen")
@Composable
private fun ArchivedGoalsScaffold_WithGoals_Preview() {
    val dummyGoals = listOf(
        GoalWithStages(
            goal = Goal(
                id = "1",
                title = "Finish novel",
                description = "Complete the first draft.",
                isArchived = true,
                createdAt = 0L,
                lastUpdated = 0L
            ),
            stages = listOf(
                GoalStage(
                    id = "s1",
                    goalId = "1",
                    name = "Chapter 1",
                    currentCount = 5,
                    targetCount = 10,
                    createdAt = 0L,
                    lastUpdated = 0L
                ),
                GoalStage(
                    id = "s2",
                    goalId = "1",
                    name = "Chapter 2",
                    currentCount = 2,
                    targetCount = 12,
                    createdAt = 0L,
                    lastUpdated = 0L
                )
            )
        ),
        GoalWithStages(
            goal = Goal(
                id = "2",
                title = "Learn guitar",
                description = "Master 3 songs.",
                isArchived = true,
                createdAt = 0L,
                lastUpdated = 0L
            ),
            stages = listOf(
                GoalStage(
                    id = "s3",
                    goalId = "2",
                    name = "Stairway to Heaven",
                    currentCount = 20,
                    targetCount = 100,
                    createdAt = 0L,
                    lastUpdated = 0L
                )
            )
        )
    )
    BreakVaultTheme {
        ArchivedGoalsScaffold(
            archivedGoals = dummyGoals,
            onNavigateUp = {},
            onUnarchiveClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty Archived Goals")
@Composable
private fun ArchivedGoalsScaffold_NoGoals_Preview() {
    BreakVaultTheme {
        ArchivedGoalsScaffold(
            archivedGoals = emptyList(),
            onNavigateUp = {},
            onUnarchiveClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Expandable Archived Card")
@Composable
private fun PreviewExpandableArchivedGoalCard() {
    BreakVaultTheme {
        ExpandableArchivedGoalCard(
            goalWithStages = GoalWithStages(
                goal = Goal(
                    id = "1",
                    title = "Old Goal",
                    description = "This was archived",
                    isArchived = true,
                    createdAt = 0L,
                    lastUpdated = 0L
                ),
                stages = listOf(
                    GoalStage(
                        id = "1",
                        goalId = "1",
                        name = "Stage 1",
                        targetCount = 100,
                        currentCount = 75,
                        createdAt = 0L,
                        lastUpdated = 0L
                    ),
                    GoalStage(
                        id = "2",
                        goalId = "1",
                        name = "Stage 2 No Target",
                        targetCount = 0,
                        currentCount = 0,
                        createdAt = 0L,
                        lastUpdated = 0L
                    )
                )
            ),
            onUnarchiveClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Archived Stage Item")
@Composable
private fun PreviewArchivedGoalStageItem() {
    BreakVaultTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ArchivedGoalStageItem(
                stage = GoalStage(
                    id = "1",
                    goalId = "1",
                    name = "Practice Combos",
                    targetCount = 50,
                    currentCount = 35,
                    createdAt = 0L,
                    lastUpdated = 0L
                )
            )
        }
    }
}

@Preview
@Composable
private fun UnarchiveConfirmationDialogPreview() {
    BreakVaultTheme {
        UnarchiveConfirmationDialog(
            goalTitle = "My Old Goal",
            onDismiss = {},
            onConfirmUnarchive = {}
        )
    }
}

@Preview
@Composable
private fun DeleteConfirmationDialogPreview() {
    BreakVaultTheme {
        DeleteConfirmationDialog(
            goalTitle = "A Goal I Hate",
            onDismiss = {},
            onConfirmDelete = {}
        )
    }
}

//endregion