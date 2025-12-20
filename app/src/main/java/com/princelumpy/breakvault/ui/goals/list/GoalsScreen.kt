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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.relation.GoalWithStages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateToAddEditGoal: (String?) -> Unit,
    onNavigateToArchivedGoals: () -> Unit,
    goalsViewModel: GoalsViewModel = hiltViewModel()
) {
    // UPDATED: Use collectAsStateWithLifecycle
    val uiState by goalsViewModel.uiState.collectAsStateWithLifecycle()
    // Create a convenience variable for the dialog state
    val dialogState = uiState.dialogState

    // UPDATED: Access the goal from the nested dialogState object
    dialogState.goalToArchive?.let { goalWithStagesToArchive ->
        AlertDialog(
            onDismissRequest = { goalsViewModel.onCancelGoalArchive() },
            title = { Text(stringResource(id = R.string.goals_screen_archive_goal_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        id = R.string.goals_screen_archive_goal_dialog_message,
                        goalWithStagesToArchive.goal.title
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { goalsViewModel.onConfirmGoalArchive() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.common_archive))
                }
            },
            dismissButton = {
                TextButton(onClick = { goalsViewModel.onCancelGoalArchive() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
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
            // Floating action button is now always visible unless loading
            if (!uiState.isLoading) {
                FloatingActionButton(onClick = {
                    onNavigateToAddEditGoal(null)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.goals_screen_add_goal_description)
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.goals.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
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
                    Button(
                        onClick = {
                            onNavigateToAddEditGoal(null)
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
                        Text(stringResource(id = R.string.goals_screen_create_goal_button))
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
                    contentPadding = AppStyleDefaults.LazyListPadding
                ) {
                    items(uiState.goals, key = { it.goal.id }) { goalWithStages ->
                        GoalCard(
                            goalWithStages = goalWithStages,
                            onEditClick = { onNavigateToAddEditGoal(goalWithStages.goal.id) },
                            onArchiveClick = { goalsViewModel.onGoalArchiveClicked(goalWithStages) }
                        )
                    }
                }
            }
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
    val progress = if (goalWithStages.stages.isNotEmpty()) {
        val totalProgress = goalWithStages.stages.sumOf {
            it.currentCount.toDouble().coerceAtMost(it.targetCount.toDouble()) / it.targetCount
        }
        totalProgress / goalWithStages.stages.size
    } else {
        0.0
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(AppStyleDefaults.SpacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goalWithStages.goal.title.ifBlank { stringResource(id = R.string.goals_screen_untitled_goal) },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
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

            if (goalWithStages.stages.isNotEmpty()) {
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
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
