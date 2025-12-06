package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.data.Goal
import com.princelumpy.breakvault.data.GoalStage
import com.princelumpy.breakvault.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    navController: NavController,
    goalViewModel: GoalViewModel = viewModel()
) {
    val goals by goalViewModel.activeGoals.observeAsState(initial = emptyList())
    
    Scaffold(
        floatingActionButton = {
            if (goals.isNotEmpty()) {
                FloatingActionButton(onClick = { 
                    val newGoalId = goalViewModel.createGoal(title = "", description = "")
                    navController.navigate("edit_goal/$newGoalId")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (goals.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No active goals.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start tracking your progress by creating your first goal!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val newGoalId = goalViewModel.createGoal(title = "", description = "")
                            navController.navigate("edit_goal/$newGoalId")
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Create Goal")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(goals) { goal ->
                        if (goal.title.isNotBlank() || goal.description.isNotBlank()) {
                             GoalCardWrapper(
                                goal = goal,
                                goalViewModel = goalViewModel,
                                onEditClick = { navController.navigate("edit_goal/${goal.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalCardWrapper(
    goal: Goal,
    goalViewModel: GoalViewModel,
    onEditClick: () -> Unit
) {
    // Observe stages for this specific goal
    val stages by goalViewModel.getStagesForGoal(goal.id).observeAsState(initial = emptyList())
    
    GoalCard(
        goal = goal,
        stages = stages,
        onEditClick = onEditClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalCard(
    goal: Goal,
    stages: List<GoalStage>,
    onEditClick: () -> Unit
) {
    // Calculate progress
    val progress = if (stages.isNotEmpty()) {
        stages.sumOf { it.currentCount.toDouble() / it.targetCount } / stages.size
    } else {
        0.0
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Tapping currently does nothing/expands, or go to edit if desired */ }, 
                onLongClick = onEditClick 
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if(goal.title.isBlank()) "Untitled Goal" else goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Removed DropdownMenu and IconButton (three dots)
            }
            
            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Show progress bar if stages exist
            if (stages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
