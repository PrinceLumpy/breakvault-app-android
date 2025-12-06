package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalStageScreen(
    navController: NavController,
    goalId: String,
    stageId: String? = null,
    goalViewModel: GoalViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var targetCountStr by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("reps") }
    var isNameError by remember { mutableStateOf(false) }
    var isTargetError by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // If editing an existing stage, load its data
    LaunchedEffect(stageId) {
        if (stageId != null && !initialized) {
            val stage = goalViewModel.getStageById(stageId)
            stage?.let {
                name = it.name
                targetCountStr = it.targetCount.toString()
                unit = it.unit
                initialized = true
            }
        }
    }
    
    // Auto-focus name field on launch if new
    LaunchedEffect(Unit) {
        if (stageId == null) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (stageId == null) "Add Goal Stage" else "Edit Goal Stage") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (stageId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Stage")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val target = targetCountStr.toIntOrNull()
                    if (name.isBlank()) {
                        isNameError = true
                    } else if (target == null || target <= 0) {
                        isTargetError = true
                    } else {
                        if (stageId == null) {
                            // Add New
                            goalViewModel.addGoalStage(goalId, name, target, unit)
                        } else {
                            // Update Existing
                            goalViewModel.updateGoalStageById(stageId, name, target, unit)
                        }
                        navController.popBackStack()
                    }
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Stage")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() } // Hide keyboard on tap outside
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    if (it.length <= 30) {
                        name = it
                        isNameError = false
                    }
                },
                label = { Text("Stage Name (e.g. Windmills)") },
                isError = isNameError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            if (isNameError) {
                Text("Name cannot be empty", color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Target Number Input
                OutlinedTextField(
                    value = targetCountStr,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            targetCountStr = ""
                            isTargetError = false
                        } else if (newValue.all { it.isDigit() }) {
                            targetCountStr = newValue
                            isTargetError = false
                        }
                    },
                    label = { Text("Target Count") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    isError = isTargetError,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))

                // Unit Input
                OutlinedTextField(
                    value = unit,
                    onValueChange = { 
                         if (it.length <= 10) {
                             unit = it 
                         }
                    },
                    label = { Text("Unit (e.g. reps)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (isTargetError) {
                Text("Target must be a valid number > 0", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog && stageId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Stage") },
            text = { Text("Are you sure you want to delete this stage?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        goalViewModel.deleteGoalStageById(stageId)
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
}
