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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.EnergyLevel
import com.princelumpy.breakvault.data.local.entity.TrainingStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditBattleComboScreen(
    onNavigateUp: () -> Unit,
    comboId: String? = null,
    addEditBattleComboViewModel: AddEditBattleComboViewModel = hiltViewModel()
) {
    val uiState by addEditBattleComboViewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(comboId) {
        addEditBattleComboViewModel.loadCombo(comboId)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            addEditBattleComboViewModel.onSnackbarMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isNewCombo) stringResource(id = R.string.add_edit_battle_combo_new_title) else stringResource(
                            id = R.string.add_edit_battle_combo_edit_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                addEditBattleComboViewModel.saveCombo {
                    keyboardController?.hide()
                    onNavigateUp()
                }
            }) {
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
                value = uiState.description,
                onValueChange = { addEditBattleComboViewModel.onDescriptionChange(it) },
                label = { Text(stringResource(id = R.string.add_edit_battle_combo_description_label)) },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text(stringResource(id = R.string.add_edit_battle_combo_description_placeholder)) }
            )

            if (uiState.isNewCombo) {
                Button(
                    onClick = { addEditBattleComboViewModel.showImportDialog(true) },
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
                    isSelected = uiState.selectedEnergy == EnergyLevel.LOW,
                    onClick = { addEditBattleComboViewModel.onEnergyChange(EnergyLevel.LOW) }
                )
                EnergyChip(
                    label = stringResource(id = R.string.add_edit_battle_combo_energy_med),
                    color = Color(0xFFFFC107),
                    isSelected = uiState.selectedEnergy == EnergyLevel.MEDIUM,
                    onClick = { addEditBattleComboViewModel.onEnergyChange(EnergyLevel.MEDIUM) }
                )
                EnergyChip(
                    label = stringResource(id = R.string.add_edit_battle_combo_energy_high),
                    color = Color(0xFFF44336),
                    isSelected = uiState.selectedEnergy == EnergyLevel.HIGH,
                    onClick = { addEditBattleComboViewModel.onEnergyChange(EnergyLevel.HIGH) }
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
                    selected = uiState.selectedStatus == TrainingStatus.TRAINING,
                    onClick = { addEditBattleComboViewModel.onStatusChange(TrainingStatus.TRAINING) },
                    label = { Text(stringResource(id = R.string.add_edit_battle_combo_training_label)) },
                    leadingIcon = { Text("🔨") }
                )
                FilterChip(
                    selected = uiState.selectedStatus == TrainingStatus.READY,
                    onClick = { addEditBattleComboViewModel.onStatusChange(TrainingStatus.READY) },
                    label = { Text(stringResource(id = R.string.add_edit_battle_combo_ready_label)) },
                    leadingIcon = { Text("🔥") }
                )
            }

            Text(
                stringResource(id = R.string.add_edit_battle_combo_tags_label),
                style = MaterialTheme.typography.titleMedium
            )
            if (uiState.allBattleTags.isEmpty() && uiState.newTagName.isBlank()) {
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
                        selected = uiState.selectedTags.contains(tag.name),
                        onClick = { addEditBattleComboViewModel.onTagSelected(tag.name) },
                        label = { Text(tag.name) },
                        leadingIcon = if (uiState.selectedTags.contains(tag.name)) {
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
                    value = uiState.newTagName,
                    onValueChange = { addEditBattleComboViewModel.onNewTagNameChange(it) },
                    label = { Text(stringResource(id = R.string.add_edit_battle_combo_new_tag_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addEditBattleComboViewModel.addBattleTag() })
                )
                Button(onClick = { addEditBattleComboViewModel.addBattleTag() }) {
                    Text(stringResource(id = R.string.common_add))
                }
            }
        }
    }

    if (uiState.showImportDialog) {
        AlertDialog(
            onDismissRequest = { addEditBattleComboViewModel.showImportDialog(false) },
            title = { Text(stringResource(id = R.string.add_edit_battle_combo_import_dialog_title)) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .height(AppStyleDefaults.SpacingExtraLarge * 5)
                        .fillMaxWidth()
                ) {
                    // 3. Use the list from the single uiState
                    items(uiState.allPracticeCombos) { combo ->
                        ListItem(
                            headlineContent = { Text(combo.name) },
                            supportingContent = { Text(combo.moves.joinToString(" -> ")) },
                            modifier = Modifier.clickable { addEditBattleComboViewModel.onImportCombo(combo) }
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { addEditBattleComboViewModel.showImportDialog(false) }) {
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
