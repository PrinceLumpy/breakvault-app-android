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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.ui.goals.list.GoalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedGoalsScreen(
    onNavigateUp: () -> Unit,
    archivedGoalsViewModel: ArchivedGoalsViewModel = hiltViewModel()
) {
    // UPDATED: Use collectAsStateWithLifecycle for better lifecycle management
    val uiState by archivedGoalsViewModel.uiState.collectAsStateWithLifecycle()
    // Create a convenience variable for the dialog state
    val dialogState = uiState.dialogState

    // UPDATED: Access the goal from the nested dialogState object
    dialogState.goalToUnarchive?.let { goalWithStages ->
        AlertDialog(
            onDismissRequest = { archivedGoalsViewModel.onCancelGoalUnarchive() },
            title = { Text(stringResource(id = R.string.archived_goals_unarchive_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        id = R.string.archived_goals_unarchive_dialog_message,
                        goalWithStages.goal.title
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { archivedGoalsViewModel.onConfirmGoalUnarchive() }) {
                    Text(stringResource(id = R.string.common_unarchive))
                }
            },
            dismissButton = {
                TextButton(onClick = { archivedGoalsViewModel.onCancelGoalUnarchive() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    // UPDATED: Access the goal from the nested dialogState object
    dialogState.goalToDelete?.let { goalWithStages ->
        AlertDialog(
            onDismissRequest = { archivedGoalsViewModel.onCancelGoalDelete() },
            title = { Text(stringResource(id = R.string.archived_goals_delete_dialog_title)) },
            text = {
                Text(
                    stringResource(
                        id = R.string.archived_goals_delete_dialog_message,
                        goalWithStages.goal.title
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { archivedGoalsViewModel.onConfirmGoalDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { archivedGoalsViewModel.onCancelGoalDelete() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(id = R.string.archived_goals_title)) },
            navigationIcon = {
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.common_back_button_description)
                    )
                }
            }
        )
    }) { paddingValues ->
        if (uiState.archivedGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.archived_goals_no_goals))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(AppStyleDefaults.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                items(uiState.archivedGoals) { goalWithStages ->
                    GoalCard(
                        goalWithStages = goalWithStages,
                        onUnarchiveClick = {
                            archivedGoalsViewModel.onGoalUnarchiveClicked(
                                goalWithStages
                            )
                        },
                        onDeleteClick = { archivedGoalsViewModel.onGoalDeleteClicked(goalWithStages) }
                    )
                }
            }
        }
    }
}
