package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditComboScreen(
    navController: NavController,
    comboId: String?,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    var comboName by remember { mutableStateOf("") }
    val allMoves by moveViewModel.allMoves.observeAsState(initial = emptyList())
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Use a generic MutableList
    val selectedMoves = remember { mutableStateListOf<String>() }
    val initialLoadDone = remember { mutableStateOf(false) }

    // Search state
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val isEditing = comboId != null

    LaunchedEffect(key1 = comboId) {
        if (isEditing && !initialLoadDone.value) {
            val comboToEdit = moveViewModel.getSavedComboForEditing(comboId!!)
            if (comboToEdit != null) {
                comboName = comboToEdit.name
                selectedMoves.clear()
                selectedMoves.addAll(comboToEdit.moves)
                initialLoadDone.value = true
            }
        }
    }
    
    // Auto-focus name field ONLY if creating new combo
    LaunchedEffect(Unit) {
        if (!isEditing) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) stringResource(R.string.edit_combo_title) else stringResource(R.string.create_combo_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back_button_description))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (comboName.length <= 30) {
                        if (isEditing) {
                            moveViewModel.updateSavedCombo(comboId!!, comboName, selectedMoves.toList())
                        } else {
                            moveViewModel.saveCombo(comboName, selectedMoves.toList())
                        }
                        focusManager.clearFocus()
                        navController.popBackStack()
                    }
                },
                containerColor = if (comboName.isNotBlank() && selectedMoves.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(Icons.Filled.Done, contentDescription = stringResource(R.string.save_combo_fab_description))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = comboName,
                onValueChange = { 
                    if (it.length <= 30) {
                        comboName = it 
                    }
                },
                label = { Text(stringResource(R.string.combo_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Text(stringResource(R.string.select_moves_title), style = MaterialTheme.typography.titleMedium)

            // List of Moves
            if (selectedMoves.isEmpty()) {
                Text(
                    text = "No moves added yet. Add one below!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedMoves.forEachIndexed { index, moveName ->
                        ComboMoveItem(
                            moveName = moveName,
                            onRemove = { selectedMoves.removeAt(index) }
                        )
                    }
                }
            }

            // Add Move Section (At the bottom)
            val filteredMoves = allMoves.filter {
                it.name.contains(searchText, ignoreCase = true)
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(bottom = 80.dp) // Extra padding for FAB
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        if (it.length <= 50) {
                            searchText = it
                            expanded = true
                        }
                    },
                    label = { Text("Add move to combo") },
                    trailingIcon = {
                        IconButton(onClick = {
                            // Plus button: Always add string as is
                            if (searchText.isNotBlank()) {
                                selectedMoves.add(searchText.trim())
                                searchText = ""
                                expanded = false
                            }
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add custom move")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // Enter key logic
                            if (filteredMoves.isNotEmpty()) {
                                // Select first option
                                selectedMoves.add(filteredMoves.first().name)
                                searchText = ""
                                expanded = false
                            } else if (searchText.isNotBlank()) {
                                // Add as is
                                selectedMoves.add(searchText.trim())
                                searchText = ""
                                expanded = false
                            }
                        }
                    )
                )

                if (filteredMoves.isNotEmpty() && expanded) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredMoves.forEach { move ->
                            DropdownMenuItem(
                                text = { Text(move.name) },
                                onClick = {
                                    selectedMoves.add(move.name)
                                    searchText = ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComboMoveItem(
    moveName: String,
    onRemove: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Increased padding since we removed drag handle
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = moveName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove move",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
