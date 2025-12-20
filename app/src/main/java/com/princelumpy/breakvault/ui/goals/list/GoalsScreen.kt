package com.princelumpy.breakvault.ui.goals.list

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.local.relation.GoalWithStages

// STATEFUL COMPOSABLE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateToAddEditGoal: (String?) -> Unit,
    onNavigateToArchivedGoals: () -> Unit,
    goalsViewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by goalsViewModel.uiState.collectAsStateWithLifecycle()

    GoalsContent(
        uiState = uiState,
        onNavigateToAddEditGoal = onNavigateToAddEditGoal,
        onNavigateToArchivedGoals = onNavigateToArchivedGoals,
        onGoalArchiveClicked = goalsViewModel::onGoalArchiveClicked,
        onConfirmGoalArchive = goalsViewModel::onConfirmGoalArchive,
        onCancelGoalArchive = goalsViewModel::onCancelGoalArchive
    )
}

// STATELESS COMPOSABLE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsContent(
    uiState: GoalsScreenUiState,
    onNavigateToAddEditGoal: (String?) -> Unit,
    onNavigateToArchivedGoals: () -> Unit,
    onGoalArchiveClicked: (GoalWithStages) -> Unit,
    onConfirmGoalArchive: () -> Unit,
    onCancelGoalArchive: () -> Unit
) {
    uiState.dialogState.goalToArchive?.let { goalToArchive ->
        ArchiveGoalDialog(
            goalTitle = goalToArchive.goal.title,
            onConfirm = onConfirmGoalArchive,
            onDismiss = onCancelGoalArchive
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals") },
                actions = {
                    IconButton(onClick = onNavigateToArchivedGoals) {
                        Icon(
                            imageVector = Icons.Filled.Archive,
                            contentDescription = "Archived Goals"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                FloatingActionButton(onClick = { onNavigateToAddEditGoal(null) }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.goals_screen_add_goal_description)
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState()
            uiState.goals.isEmpty() -> EmptyGoalsState(onCreateGoal = { onNavigateToAddEditGoal(null) })
            else -> GoalsList(
                goals = uiState.goals,
                onEditClick = onNavigateToAddEditGoal,
                onArchiveClick = onGoalArchiveClicked,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun ArchiveGoalDialog(
    goalTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.goals_screen_archive_goal_dialog_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.goals_screen_archive_goal_dialog_message,
                    goalTitle
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.common_archive))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyGoalsState(onCreateGoal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppStyleDefaults.SpacingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.goals_screen_no_goals_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(id = R.string.goals_screen_no_goals_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
        )
        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))
        Button(onClick = onCreateGoal) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
            Text(stringResource(id = R.string.goals_screen_create_goal_button))
        }
    }
}

@Composable
fun GoalsList(
    goals: List<GoalWithStages>,
    onEditClick: (String?) -> Unit,
    onArchiveClick: (GoalWithStages) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
        contentPadding = AppStyleDefaults.LazyListPadding
    ) {
        items(goals, key = { it.goal.id }) { goalWithStages ->
            GoalCard(
                goalWithStages = goalWithStages,
                onEditClick = { onEditClick(goalWithStages.goal.id) },
                onArchiveClick = { onArchiveClick(goalWithStages) }
            )
        }
    }
}

@Composable
fun GoalCard(
    goalWithStages: GoalWithStages,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onArchiveClick: (() -> Unit)? = null,
    onUnarchiveClick: (() -> Unit)? = null
) {
    val progress = calculateGoalProgress(goalWithStages.stages)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(AppStyleDefaults.SpacingLarge)) {
            GoalCardHeader(
                title = goalWithStages.goal.title,
                onEditClick = onEditClick,
                onArchiveClick = onArchiveClick,
                onUnarchiveClick = onUnarchiveClick,
                onDeleteClick = onDeleteClick
            )

            if (goalWithStages.goal.description.isNotBlank()) {
                GoalDescription(description = goalWithStages.goal.description)
            }

            if (goalWithStages.stages.isNotEmpty()) {
                GoalProgress(progress = progress)
            }
        }
    }
}

@Composable
fun GoalCardHeader(
    title: String,
    onEditClick: (() -> Unit)?,
    onArchiveClick: (() -> Unit)?,
    onUnarchiveClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.ifBlank { stringResource(id = R.string.goals_screen_untitled_goal) },
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        GoalCardActions(
            onEditClick = onEditClick,
            onArchiveClick = onArchiveClick,
            onUnarchiveClick = onUnarchiveClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
fun GoalCardActions(
    onEditClick: (() -> Unit)?,
    onArchiveClick: (() -> Unit)?,
    onUnarchiveClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?
) {
    Row {
        onEditClick?.let {
            IconButton(onClick = it) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(id = R.string.goals_screen_edit_goal_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        onArchiveClick?.let {
            IconButton(onClick = it) {
                Icon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = stringResource(id = R.string.archived_goals_archive_goal_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        onUnarchiveClick?.let {
            IconButton(onClick = it) {
                Icon(
                    imageVector = Icons.Filled.Unarchive,
                    contentDescription = stringResource(id = R.string.archived_goals_unarchive_goal_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        onDeleteClick?.let {
            IconButton(onClick = it) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.goals_screen_delete_goal_description),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun GoalDescription(description: String) {
    Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun GoalProgress(progress: Double) {
    Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))
    LinearProgressIndicator(
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = AppStyleDefaults.SpacingSmall),
        textAlign = TextAlign.End
    )
}

// HELPER FUNCTIONS
fun calculateGoalProgress(stages: List<GoalStage>): Double {
    if (stages.isEmpty()) return 0.0
    val totalProgress = stages.sumOf {
        it.currentCount.toDouble().coerceAtMost(it.targetCount.toDouble()) / it.targetCount
    }
    return totalProgress / stages.size
}

// PREVIEWS
@Preview(showBackground = true)
@Composable
fun PreviewLoadingState() {
    MaterialTheme {
        LoadingState()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyGoalsState() {
    MaterialTheme {
        EmptyGoalsState(onCreateGoal = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGoalCard() {
    MaterialTheme {
        GoalCard(
            goalWithStages = GoalWithStages(
                goal = Goal(
                    id = "1",
                    title = "Master Muay Thai",
                    description = "Complete all fundamental techniques",
                    isArchived = false,
                    createdAt = 0L,
                    lastUpdated = 0L
                ),
                stages = listOf(
                    GoalStage(
                        id = "1",
                        goalId = "1",
                        name = "Basic Kicks",
                        targetCount = 100,
                        currentCount = 50,
                        createdAt = 0L,
                        lastUpdated = 0L
                    ),
                    GoalStage(
                        id = "2",
                        goalId = "1",
                        name = "Advanced Combos",
                        targetCount = 50,
                        currentCount = 10,
                        createdAt = 0L,
                        lastUpdated = 0L
                    )
                )
            ),
            onEditClick = {},
            onArchiveClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGoalCardNoProgress() {
    MaterialTheme {
        GoalCard(
            goalWithStages = GoalWithStages(
                goal = Goal(
                    id = "1",
                    title = "Learn Boxing",
                    description = "",
                    isArchived = false,
                    createdAt = 0L,
                    lastUpdated = 0L
                ),
                stages = emptyList()
            ),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewArchiveGoalDialog() {
    MaterialTheme {
        ArchiveGoalDialog(
            goalTitle = "Master Muay Thai",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGoalProgress() {
    MaterialTheme {
        Column(modifier = Modifier.padding(AppStyleDefaults.SpacingLarge)) {
            GoalProgress(progress = 0.65)
        }
    }
}