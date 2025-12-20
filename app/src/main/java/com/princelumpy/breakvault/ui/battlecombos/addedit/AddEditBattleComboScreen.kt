package com.princelumpy.breakvault.ui.battlecombos.addedit

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.EnergyLevel
import com.princelumpy.breakvault.data.local.entity.TrainingStatus
import com.princelumpy.breakvault.data.local.entity.SavedCombo

// --- STATEFUL COMPOSABLE (The "Smart" one) ---

@Composable
fun AddEditBattleComboScreen(
    onNavigateUp: () -> Unit,
    comboId: String? = null,
    viewModel: AddEditBattleComboViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(comboId) {
        viewModel.loadCombo(comboId)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarMessageShown()
        }
    }

    // This composable connects the ViewModel to the stateless UI
    AddEditBattleComboContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onSaveClick = {
            viewModel.saveCombo {
                keyboardController?.hide()
                onNavigateUp()
            }
        },
        onDescriptionChange = viewModel::onDescriptionChange,
        onShowImportDialog = viewModel::showImportDialog,
        onEnergyChange = viewModel::onEnergyChange,
        onStatusChange = viewModel::onStatusChange,
        onTagSelected = viewModel::onTagSelected,
        onNewTagNameChange = viewModel::onNewTagNameChange,
        onAddBattleTag = viewModel::addBattleTag,
        onImportCombo = viewModel::onImportCombo
    )
}

// --- STATELESS COMPOSABLE (The "Dumb" UI) ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditBattleComboContent(
    uiState: AddEditBattleComboUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onSaveClick: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onShowImportDialog: (Boolean) -> Unit,
    onEnergyChange: (EnergyLevel) -> Unit,
    onStatusChange: (TrainingStatus) -> Unit,
    onTagSelected: (String) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onAddBattleTag: () -> Unit,
    onImportCombo: (SavedCombo) -> Unit,
) {
    // A convenience variable to simplify access
    val userInputs = uiState.userInputs

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (userInputs.isNewCombo) stringResource(id = R.string.add_edit_battle_combo_new_title) else stringResource(
                            id = R.string.add_edit_battle_combo_edit_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSaveClick) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.common_save)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(AppStyleDefaults.SpacingLarge)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
        ) {
            OutlinedTextField(
                value = userInputs.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(id = R.string.add_edit_battle_combo_description_label)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(id = R.string.add_edit_battle_combo_description_placeholder)) }
            )

            if (userInputs.isNewCombo) {
                Button(
                    onClick = { onShowImportDialog(true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(id = R.string.add_edit_battle_combo_import_button))
                }
            }

            Text(
                stringResource(id = R.string.add_edit_battle_combo_energy_label),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnergyChip(
                    label = stringResource(id = R.string.add_edit_battle_combo_energy_low),
                    color = Color(0xFF4CAF50),
                    isSelected = userInputs.selectedEnergy == EnergyLevel.LOW,
                    onClick = { onEnergyChange(EnergyLevel.LOW) }
                )
                EnergyChip(
                    label = stringResource(id = R.string.add_edit_battle_combo_energy_med),
                    color = Color(0xFFFFC107),
                    isSelected = userInputs.selectedEnergy == EnergyLevel.MEDIUM,
                    onClick = { onEnergyChange(EnergyLevel.MEDIUM) }
                )
                EnergyChip(
                    label = stringResource(id = R.string.add_edit_battle_combo_energy_high),
                    color = Color(0xFFF44336),
                    isSelected = userInputs.selectedEnergy == EnergyLevel.HIGH,
                    onClick = { onEnergyChange(EnergyLevel.HIGH) }
                )
            }

            Text(
                stringResource(id = R.string.add_edit_battle_combo_readiness_label),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
            ) {
                FilterChip(
                    selected = userInputs.selectedStatus == TrainingStatus.TRAINING,
                    onClick = { onStatusChange(TrainingStatus.TRAINING) },
                    label = { Text(stringResource(id = R.string.add_edit_battle_combo_training_label)) },
                    leadingIcon = { Text("🔨") }
                )
                FilterChip(
                    selected = userInputs.selectedStatus == TrainingStatus.READY,
                    onClick = { onStatusChange(TrainingStatus.READY) },
                    label = { Text(stringResource(id = R.string.add_edit_battle_combo_ready_label)) },
                    leadingIcon = { Text("🔥") }
                )
            }

            Text(
                stringResource(id = R.string.add_edit_battle_combo_tags_label),
                style = MaterialTheme.typography.titleMedium
            )
            if (uiState.allBattleTags.isEmpty() && userInputs.newTagName.isBlank()) {
                Text(
                    stringResource(id = R.string.add_edit_battle_combo_no_tags_message),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
            ) {
                uiState.allBattleTags.forEach { tag ->
                    FilterChip(
                        selected = userInputs.selectedTags.contains(tag.name),
                        onClick = { onTagSelected(tag.name) },
                        label = { Text(tag.name) },
                        leadingIcon = if (userInputs.selectedTags.contains(tag.name)) {
                            {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                OutlinedTextField(
                    value = userInputs.newTagName,
                    onValueChange = onNewTagNameChange,
                    label = { Text(stringResource(id = R.string.add_edit_battle_combo_new_tag_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddBattleTag() })
                )
                Button(onClick = onAddBattleTag) {
                    Text(stringResource(id = R.string.common_add))
                }
            }
        }
    }

    if (uiState.showImportDialog) {
        AlertDialog(
            onDismissRequest = { onShowImportDialog(false) },
            title = { Text(stringResource(id = R.string.add_edit_battle_combo_import_dialog_title)) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .height(AppStyleDefaults.SpacingExtraLarge * 5)
                        .fillMaxWidth()
                ) {
                    items(uiState.allPracticeCombos, key = { it.id }) { combo ->
                        ListItem(
                            headlineContent = { Text(combo.name) },
                            supportingContent = { Text(combo.moves.joinToString(" -> ")) },
                            modifier = Modifier.clickable { onImportCombo(combo) }
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onShowImportDialog(false) }) {
                    Text(stringResource(id = R.string.common_cancel))
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

// --- PREVIEWS ---

@Preview(showBackground = true, name = "Add New Combo")
@Composable
private fun AddEditBattleComboContent_AddNewPreview() {
    MaterialTheme {
        AddEditBattleComboContent(
            uiState = AddEditBattleComboUiState(
                userInputs = UserInputs(isNewCombo = true), // Key for "Add" mode
                allBattleTags = listOf(
                    BattleTag("1", "Boxing"),
                    BattleTag("2", "Kicking")
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateUp = {},
            onSaveClick = {},
            onDescriptionChange = {},
            onShowImportDialog = {},
            onEnergyChange = {},
            onStatusChange = {},
            onTagSelected = {},
            onNewTagNameChange = {},
            onAddBattleTag = {},
            onImportCombo = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Existing Combo")
@Composable
private fun AddEditBattleComboContent_EditPreview() {
    MaterialTheme {
        AddEditBattleComboContent(
            uiState = AddEditBattleComboUiState(
                userInputs = UserInputs(
                    isNewCombo = false, // Key for "Edit" mode
                    description = "Jab, Cross, Hook",
                    selectedEnergy = EnergyLevel.HIGH,
                    selectedStatus = TrainingStatus.READY,
                    selectedTags = setOf("Boxing", "Power")
                ),
                allBattleTags = listOf(
                    BattleTag("1", "Boxing"),
                    BattleTag("2", "Kicking"),
                    BattleTag("3", "Power")
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateUp = {},
            onSaveClick = {},
            onDescriptionChange = {},
            onShowImportDialog = {},
            onEnergyChange = {},
            onStatusChange = {},
            onTagSelected = {},
            onNewTagNameChange = {},
            onAddBattleTag = {},
            onImportCombo = {}
        )
    }
}

@Preview(showBackground = true, name = "Import Dialog")
@Composable
private fun AddEditBattleComboContent_ShowImportDialogPreview() {
    MaterialTheme {
        AddEditBattleComboContent(
            uiState = AddEditBattleComboUiState(
                userInputs = UserInputs(isNewCombo = true),
                showImportDialog = true, // Key for this preview
                allPracticeCombos = listOf(
                    SavedCombo(id = "1", name = "Classic 1-2", moves = listOf("Jab", "Cross")),
                    SavedCombo(id = "2", name = "Leg Day", moves = listOf("Left Kick", "Right Kick")),
                    SavedCombo(id = "3", name = "Muay Thai Basic", moves = listOf("Jab", "Cross", "Left Kick"))
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateUp = {},
            onSaveClick = {},
            onDescriptionChange = {},
            onShowImportDialog = {},
            onEnergyChange = {},
            onStatusChange = {},
            onTagSelected = {},
            onNewTagNameChange = {},
            onAddBattleTag = {},
            onImportCombo = {}
        )
    }
}
