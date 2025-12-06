package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.data.GoalStage
import com.princelumpy.breakvault.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalScreen(
    navController: NavController,
    goalId: String,
    goalViewModel: GoalViewModel = viewModel()
) {
    val goal by goalViewModel.getGoalById(goalId).observeAsState()
    val stages by goalViewModel.getStagesForGoal(goalId).observeAsState(emptyList())
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArchiveDialog by remember { mutableStateOf(false) }
    
    // State for adding progress
    var stageToAddTo by remember { mutableStateOf<GoalStage?>(null) }
    var showAddProgressDialog by remember { mutableStateOf(false) }

    LaunchedEffect(goal) {
        goal?.let {
            if (!initialized) {
                title = it.title
                description = it.description
                initialized = true
            }
        }
    }

    // Calculate total progress
    val totalProgress = if (stages.isNotEmpty()) {
        stages.sumOf { it.currentCount.toDouble() / it.targetCount } / stages.size
    } else {
        0.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Goal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showArchiveDialog = true }) {
                        Icon(Icons.Default.Archive, contentDescription = "Archive Goal")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    goal?.let {
                        goalViewModel.updateGoal(it.copy(title = title, description = description))
                        navController.popBackStack()
                    }
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Goal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal Title") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Description field UI removed as requested
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Section
            Text(
                text = "Overall Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { totalProgress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Stages Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stages / Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { 
                    navController.navigate("add_edit_goal_stage/$goalId")
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Add Stage")
                }
            }
            HorizontalDivider()
            
            // Stages List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                if (stages.isEmpty()) {
                    item {
                        Text(
                            text = "No stages yet. Add one to track progress!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    items(stages) { stage ->
                        StageItem(
                            stage = stage,
                            onEdit = {
                                navController.navigate("add_edit_goal_stage/$goalId?stageId=${stage.id}")
                            },
                            onAddProgress = {
                                stageToAddTo = stage
                                showAddProgressDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Delete Confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        goal?.let { goalViewModel.deleteGoal(it) }
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Archive Confirmation
    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("Archive Goal") },
            text = { Text("Are you sure you want to archive this goal?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        goal?.let { goalViewModel.archiveGoal(it) }
                        showArchiveDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Add Progress Dialog
    if (showAddProgressDialog && stageToAddTo != null) {
        AddProgressDialog(
            stage = stageToAddTo!!,
            onDismiss = { showAddProgressDialog = false },
            onConfirm = { amount ->
                goalViewModel.incrementStageProgress(stageToAddTo!!, amount)
                showAddProgressDialog = false
            }
        )
    }
}

@Composable
fun StageItem(
    stage: GoalStage,
    onEdit: () -> Unit,
    onAddProgress: () -> Unit
) {
    val progress = if (stage.targetCount > 0) stage.currentCount.toFloat() / stage.targetCount else 0f
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stage.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${stage.currentCount} / ${stage.targetCount} ${stage.unit}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Actions
            Row {
                IconButton(onClick = onAddProgress) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Progress",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Stage",
                        modifier = Modifier.height(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddProgressDialog(
    stage: GoalStage,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Progress") },
        text = {
            Column {
                Text("Current: ${stage.currentCount} / ${stage.targetCount} ${stage.unit}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() || char == '-' }) {
                             amountStr = it 
                        }
                    },
                    label = { Text("Amount to add (or subtract)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountStr.toIntOrNull()
                    if (amount != null) {
                        onConfirm(amount)
                    }
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
