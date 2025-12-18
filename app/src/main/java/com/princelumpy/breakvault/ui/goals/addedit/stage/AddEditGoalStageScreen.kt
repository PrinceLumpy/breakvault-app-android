package com.princelumpy.breakvault.ui.goals.addedit.stage

import AppStyleDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalStageScreen(
    navController: NavController,
    goalId: String,
    stageId: String? = null,
    goalStageViewModel: GoalStageViewModel = viewModel()
) {
    val uiState by goalStageViewModel.uiState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(goalId, stageId) {
        goalStageViewModel.loadStage(goalId, stageId)
    }

    LaunchedEffect(uiState?.isNewStage) {
        if (uiState?.isNewStage == true) {
            focusRequester.requestFocus()
        }
    }

    uiState?.let { currentUiState ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = if (currentUiState.isNewStage) R.string.add_edit_goal_stage_add_title else R.string.add_edit_goal_stage_edit_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.common_back_button_description)
                            )
                        }
                    },
                    actions = {
                        if (!currentUiState.isNewStage) {
                            IconButton(onClick = { goalStageViewModel.showDeleteDialog(true) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.add_edit_goal_stage_delete_description)
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        goalStageViewModel.saveStage { navController.popBackStack() }
                    }
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = stringResource(id = R.string.add_edit_goal_stage_save_description)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppStyleDefaults.SpacingLarge)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() }
            ) {
                OutlinedTextField(
                    value = currentUiState.name,
                    onValueChange = { goalStageViewModel.onNameChange(it) },
                    label = { Text(stringResource(id = R.string.add_edit_goal_stage_name_label)) },
                    isError = currentUiState.isNameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                if (currentUiState.isNameError) {
                    Text(
                        stringResource(id = R.string.add_edit_goal_stage_name_error),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentUiState.targetCount,
                        onValueChange = { goalStageViewModel.onTargetCountChange(it) },
                        label = { Text(stringResource(id = R.string.add_edit_goal_stage_target_label)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        isError = currentUiState.isTargetError,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(AppStyleDefaults.SpacingLarge))

                    OutlinedTextField(
                        value = currentUiState.unit,
                        onValueChange = { goalStageViewModel.onUnitChange(it) },
                        label = { Text(stringResource(id = R.string.add_edit_goal_stage_unit_label)) },
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

                if (currentUiState.isTargetError) {
                    Text(
                        stringResource(id = R.string.add_edit_goal_stage_target_error),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        if (currentUiState.showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { goalStageViewModel.showDeleteDialog(false) },
                title = { Text(stringResource(id = R.string.add_edit_goal_stage_delete_dialog_title)) },
                text = { Text(stringResource(id = R.string.add_edit_goal_stage_delete_dialog_text)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            goalStageViewModel.deleteStage { navController.popBackStack() }
                        }
                    ) {
                        Text(
                            stringResource(id = R.string.common_delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { goalStageViewModel.showDeleteDialog(false) }) {
                        Text(stringResource(id = R.string.common_cancel))
                    }
                }
            )
        }
    }
}
