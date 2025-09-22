package com.princelumpy.breakvault.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.data.Tag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel
import com.princelumpy.breakvault.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMoveScreen(
    navController: NavController,
    moveId: String?,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    var moveName by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }
    val allTags by moveViewModel.allTags.observeAsState(initial = emptyList())
    var selectedTags by remember { mutableStateOf(setOf<Tag>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // For Snackbar messages

    LaunchedEffect(key1 = moveId) {
        if (moveId != null) {
            val moveBeingEdited = moveViewModel.getMoveForEditing(moveId)
            if (moveBeingEdited != null) {
                moveName = moveBeingEdited.move.name
                selectedTags = moveBeingEdited.tags.toSet()
            } else {
                Log.w("AddEditMoveScreen", "Could not find move with ID $moveId for editing.")
            }
        } else {
            moveName = ""
            selectedTags = setOf()
        }
    }

    val saveMoveAction: () -> Unit = {
        if (moveName.isNotBlank()) {
            if (moveId == null) {
                moveViewModel.addMove(moveName, selectedTags.toList())
            } else {
                moveViewModel.updateMoveAndTags(moveId, moveName, selectedTags.toList())
            }
            keyboardController?.hide()
            navController.popBackStack()
        } else {
            Log.w("AddEditMoveScreen", "Validation failed: Move name cannot be blank.")
            scope.launch {
                snackbarHostState.showSnackbar(message = context.getString(R.string.add_edit_move_error_blank_name))
            }
        }
    }

    val addTagAction: () -> Unit = {
        if (newTagName.isNotBlank()) {
            val trimmedTagName = newTagName.trim()
            if (!allTags.any { it.name.equals(trimmedTagName, ignoreCase = true) }) {
                moveViewModel.addTag(trimmedTagName)
                newTagName = "" // Clear input after successful add
            } else {
                Log.w("AddEditMoveScreen", "Validation failed: Tag '$trimmedTagName' already exists.")
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.add_edit_move_error_tag_exists, trimmedTagName)
                    )
                }
                newTagName = "" // Clear input after attempting to add existing tag
            }
            keyboardController?.hide()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (moveId == null) stringResource(id = R.string.add_edit_move_add_new_move_title) else stringResource(id = R.string.add_edit_move_edit_move_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.common_back_button_description))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = saveMoveAction) {
                Icon(Icons.Filled.Done, contentDescription = stringResource(id = R.string.add_edit_move_save_move_fab_description))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = moveName,
                onValueChange = { moveName = it },
                label = { Text(stringResource(id = R.string.add_edit_move_move_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { saveMoveAction() })
            )

            Text(stringResource(id = R.string.add_edit_move_select_tags_label), style = MaterialTheme.typography.titleMedium)
            if (allTags.isEmpty() && newTagName.isBlank()) {
                Text(stringResource(id = R.string.add_edit_move_no_tags_available_message), style = MaterialTheme.typography.bodySmall)
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allTags.forEach { tag ->
                    FilterChip(
                        selected = selectedTags.any { it.id == tag.id },
                        onClick = {
                            selectedTags = if (selectedTags.any { it.id == tag.id }) {
                                selectedTags.filterNot { selectedTag -> selectedTag.id == tag.id }.toSet()
                            } else {
                                selectedTags + tag
                            }
                        },
                        label = { Text(tag.name) },
                        leadingIcon = if (selectedTags.any { it.id == tag.id }) {
                            { Icon(Icons.Filled.Done, stringResource(id = R.string.add_edit_move_selected_chip_description), Modifier.size(FilterChipDefaults.IconSize)) }
                        } else { null }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(stringResource(id = R.string.add_edit_move_add_new_tag_label), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text(stringResource(id = R.string.add_edit_move_new_tag_name_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTagAction() })
                )
                Button(onClick = addTagAction) {
                    Text(stringResource(id = R.string.common_add))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Add Mode")
@Composable
fun AddEditMoveScreenPreview_AddMode() {
    ComboGeneratorTheme {
        AddEditMoveScreen(
            navController = rememberNavController(),
            moveId = null,
            moveViewModel = FakeMoveViewModel() // Use Fake ViewModel
        )
    }
}

@Preview(showBackground = true, name = "Edit Mode")
@Composable
fun AddEditMoveScreenPreview_EditMode() {
    ComboGeneratorTheme {
        AddEditMoveScreen(
            navController = rememberNavController(),
            moveId = "previewEditId", // Use the ID faked in FakeMoveViewModel
            moveViewModel = FakeMoveViewModel() // Use Fake ViewModel
        )
    }
}
