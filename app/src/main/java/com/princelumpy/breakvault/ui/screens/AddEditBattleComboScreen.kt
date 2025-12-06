package com.princelumpy.breakvault.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.data.BattleCombo
import com.princelumpy.breakvault.data.EnergyLevel
import com.princelumpy.breakvault.data.TrainingStatus
import com.princelumpy.breakvault.viewmodel.BattleViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditBattleComboScreen(
    navController: NavController,
    comboId: String? = null,
    battleViewModel: BattleViewModel = viewModel(),
    moveViewModel: MoveViewModel = viewModel() // For importing practice combos
) {
    // Form State
    var description by remember { mutableStateOf("") }
    var selectedEnergy by remember { mutableStateOf(EnergyLevel.NONE) }
    var selectedStatus by remember { mutableStateOf(TrainingStatus.TRAINING) }
    var isUsedState by remember { mutableStateOf(false) }
    
    // Tags State
    val allBattleTags by battleViewModel.allBattleTags.observeAsState(initial = emptyList())
    var selectedTags by remember { mutableStateOf(setOf<String>()) } // Storing moveListTag NAMES
    var newTagName by remember { mutableStateOf("") }
    var pendingAutoSelectTagName by remember { mutableStateOf<String?>(null) }
    
    // Internal ID for updating
    var loadedComboId by remember { mutableStateOf<String?>(null) }
    
    // Import Dialog State
    var showImportDialog by remember { mutableStateOf(false) }
    val practiceCombos by moveViewModel.savedCombos.observeAsState(initial = emptyList())

    val isEditing = comboId != null

    // UI Helpers
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Load existing data if editing
    LaunchedEffect(comboId) {
        if (isEditing) {
            val comboWithTags = battleViewModel.getBattleComboById(comboId!!)
            if (comboWithTags != null) {
                loadedComboId = comboWithTags.battleCombo.id
                description = comboWithTags.battleCombo.description
                selectedEnergy = comboWithTags.battleCombo.energy
                selectedStatus = comboWithTags.battleCombo.status
                isUsedState = comboWithTags.battleCombo.isUsed
                selectedTags = comboWithTags.tags.map { it.name }.toSet()
            }
        }
    }

    // Auto-select logic for new moveListTags
    LaunchedEffect(allBattleTags, pendingAutoSelectTagName) {
        pendingAutoSelectTagName?.let { tagName ->
            val foundTag = allBattleTags.find { it.name.equals(tagName, ignoreCase = true) }
            if (foundTag != null) {
                selectedTags = selectedTags + foundTag.name
                pendingAutoSelectTagName = null
            }
        }
    }

    val saveAction: () -> Unit = {
        if (description.isNotBlank()) {
            if (isEditing && loadedComboId != null) {
                battleViewModel.updateBattleCombo(
                    BattleCombo(
                        id = loadedComboId!!,
                        description = description,
                        energy = selectedEnergy,
                        status = selectedStatus,
                        isUsed = isUsedState
                    ),
                    selectedTags.toList()
                )
            } else {
                battleViewModel.addBattleCombo(
                    description = description,
                    energy = selectedEnergy,
                    status = selectedStatus,
                    tags = selectedTags.toList()
                )
            }
            keyboardController?.hide()
            navController.popBackStack()
        } else {
            Log.w("AddEditBattleCombo", "Validation failed: Description cannot be blank.")
            scope.launch {
                snackbarHostState.showSnackbar("Description cannot be empty")
            }
        }
    }

    val addMoveListTagAction: () -> Unit = {
        if (newTagName.isNotBlank()) {
            val trimmedTagName = newTagName.trim()
            if (!allBattleTags.any { it.name.equals(trimmedTagName, ignoreCase = true) }) {
                battleViewModel.addBattleTag(trimmedTagName)
                pendingAutoSelectTagName = trimmedTagName
                newTagName = ""
            } else {
                // MoveListTag exists, just select it
                 if (!selectedTags.contains(trimmedTagName)) {
                     selectedTags = selectedTags + trimmedTagName
                 }
                 newTagName = ""
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Battle Combo" else "New Battle Combo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = saveAction) {
                Icon(Icons.Filled.Done, contentDescription = "Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Combo Description") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                placeholder = { Text("e.g. 6-step -> freeze -> power move") }
            )

            // Import Button (Only visible when creating new)
            if (!isEditing) {
                Button(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Import from Practice Mode")
                }
            }

            // Energy Level Selector
            Text("Energy Level", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnergyChip(
                    label = "Low", 
                    color = Color(0xFF4CAF50), 
                    isSelected = selectedEnergy == EnergyLevel.LOW,
                    onClick = { selectedEnergy = EnergyLevel.LOW }
                )
                EnergyChip(
                    label = "Med", 
                    color = Color(0xFFFFC107), 
                    isSelected = selectedEnergy == EnergyLevel.MEDIUM,
                    onClick = { selectedEnergy = EnergyLevel.MEDIUM }
                )
                EnergyChip(
                    label = "High", 
                    color = Color(0xFFF44336), 
                    isSelected = selectedEnergy == EnergyLevel.HIGH,
                    onClick = { selectedEnergy = EnergyLevel.HIGH }
                )
            }

            // Status Selector
            Text("Readiness", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == TrainingStatus.TRAINING,
                    onClick = { selectedStatus = TrainingStatus.TRAINING },
                    label = { Text("Training") },
                    leadingIcon = { Text("🔨") }
                )
                FilterChip(
                    selected = selectedStatus == TrainingStatus.READY,
                    onClick = { selectedStatus = TrainingStatus.READY },
                    label = { Text("Battle Ready") },
                    leadingIcon = { Text("🔥") }
                )
            }

            // Tags Selection (Chip Flow Row)
            Text("Battle Tags", style = MaterialTheme.typography.titleMedium)
            if (allBattleTags.isEmpty() && newTagName.isBlank()) {
                Text("No battle moveListTags available. Add some below!", style = MaterialTheme.typography.bodySmall)
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allBattleTags.forEach { tag ->
                    FilterChip(
                        selected = selectedTags.contains(tag.name),
                        onClick = {
                            selectedTags = if (selectedTags.contains(tag.name)) {
                                selectedTags - tag.name
                            } else {
                                selectedTags + tag.name
                            }
                        },
                        label = { Text(tag.name) },
                        leadingIcon = if (selectedTags.contains(tag.name)) {
                            { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                        } else { null }
                    )
                }
            }

            // Add New MoveListTag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("New Tag Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addMoveListTagAction() })
                )
                Button(onClick = addMoveListTagAction) {
                    Text("Add")
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Select Practice Combo") },
            text = {
                LazyColumn(
                    modifier = Modifier.height(300.dp).fillMaxWidth()
                ) {
                    items(practiceCombos) { combo ->
                        ListItem(
                            headlineContent = { Text(combo.name) },
                            supportingContent = { Text(combo.moves.joinToString(" -> ")) },
                            modifier = Modifier.clickable {
                                description = combo.moves.joinToString(" -> ")
                                showImportDialog = false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EnergyChip(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = Color.Black,
            selectedLeadingIconColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) color else Color.Gray
        ),
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null) }
        } else null
    )
}
