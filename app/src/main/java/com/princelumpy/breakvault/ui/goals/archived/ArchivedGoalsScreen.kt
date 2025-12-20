package com.princelumpy.breakvault.ui.goals.archived

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.local.relation.GoalWithStages
import com.princelumpy.breakvault.ui.goals.list.GoalCard
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
        }) { paddingValues ->
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
        contentPadding = PaddingValues(AppStyleDefaults.SpacingLarge),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        items(archivedGoals) { goalWithStages ->
            GoalCard(
                goalWithStages = goalWithStages,
                onUnarchiveClick = { onUnarchiveClick(goalWithStages) },
                onDeleteClick = { onDeleteClick(goalWithStages) }
            )
        }
    }
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

@Preview(showBackground = true)
@Composable
private fun ArchivedGoalsScaffold_WithGoals_Preview() {
    val dummyGoals = listOf(
        GoalWithStages(
            goal = Goal(
                id = "1",
                title = "Finish novel",
                description = "Complete the first draft.",
                isArchived = true
            ),
            stages = listOf(
                GoalStage(
                    id = "s1",
                    goalId = "1",
                    name = "Chapter 1",
                    currentCount = 5,
                    targetCount = 10,
                    unit = "pages"
                ),
                GoalStage(
                    id = "s2",
                    goalId = "1",
                    name = "Chapter 2",
                    currentCount = 2,
                    targetCount = 12,
                    unit = "pages"
                )
            )
        ),
        GoalWithStages(
            goal = Goal(
                id = "2",
                title = "Learn guitar",
                description = "Master 3 songs.",
                isArchived = true
            ),
            stages = listOf(
                GoalStage(
                    id = "s3",
                    goalId = "2",
                    name = "Stairway to Heaven",
                    currentCount = 20,
                    targetCount = 100,
                    unit = "practice hours"
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

@Preview(showBackground = true)
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
