package com.princelumpy.breakvault.ui.battlecombos.addedit

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.common.Constants.BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT
import com.princelumpy.breakvault.common.Constants.BATTLE_TAG_CHARACTER_LIMIT
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.EnergyLevel
import com.princelumpy.breakvault.data.local.entity.TrainingStatus
import com.princelumpy.breakvault.data.local.entity.SavedCombo


// STATEFUL COMPOSABLE
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

    LaunchedEffect(uiState.dialogsAndMessages.snackbarMessage) {
        uiState.dialogsAndMessages.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onSnackbarMessageShown()
        }
    }

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
        onImportCombo = viewModel::onImportCombo,
        onDeleteComboClick = viewModel::onDeleteComboClick,
        onConfirmComboDelete = {
            viewModel.onConfirmComboDelete {
                keyboardController?.hide()
                onNavigateUp()
            }
        },
        onCancelComboDelete = viewModel::onCancelComboDelete
    )
}

// STATELESS COMPOSABLE
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
    onDeleteComboClick: () -> Unit,
    onConfirmComboDelete: () -> Unit,
    onCancelComboDelete: () -> Unit
) {
    val userInputs = uiState.userInputs
    val dialogsAndMessages = uiState.dialogsAndMessages

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (userInputs.isNewCombo)
                            stringResource(id = R.string.add_edit_battle_combo_new_title)
                        else
                            stringResource(id = R.string.add_edit_battle_combo_edit_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                },
                actions = {
                    if (!userInputs.isNewCombo) {
                        IconButton(onClick = onDeleteComboClick) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete combo",
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveClick,
                containerColor = if (uiState.userInputs.description.isNotBlank())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.common_save)
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(AppStyleDefaults.SpacingLarge)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
            ) {
                DescriptionField(
                    description = userInputs.description,
                    descriptionError = dialogsAndMessages.descriptionError,
                    onDescriptionChange = onDescriptionChange
                )

                if (userInputs.isNewCombo) {
                    ImportButton(onClick = { onShowImportDialog(true) })
                }

                EnergySection(
                    selectedEnergy = userInputs.selectedEnergy,
                    onEnergyChange = onEnergyChange
                )

                ReadinessSection(
                    selectedStatus = userInputs.selectedStatus,
                    onStatusChange = onStatusChange
                )

                TagsSection(
                    allBattleTags = uiState.allBattleTags,
                    selectedTags = userInputs.selectedTags,
                    newTagName = userInputs.newTagName,
                    newTagError = dialogsAndMessages.newTagError,
                    onTagSelected = onTagSelected,
                    onNewTagNameChange = onNewTagNameChange,
                    onAddBattleTag = onAddBattleTag
                )
            }
        }
    }

    if (dialogsAndMessages.showImportDialog) {
        ImportDialog(
            practiceCombos = uiState.allPracticeCombos,
            onImportCombo = onImportCombo,
            onDismiss = { onShowImportDialog(false) }
        )
    }

    if (dialogsAndMessages.showDeleteDialog) {
        DeleteComboDialog(
            comboDescription = userInputs.description,
            onConfirm = onConfirmComboDelete,
            onDismiss = onCancelComboDelete
        )
    }
}

// LAYER 1: Input Capping with Supporting Text Error Display
@Composable
fun DescriptionField(
    description: String,
    descriptionError: String?,
    onDescriptionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = description,
        onValueChange = { newText ->
            if (newText.length <= BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT) {
                onDescriptionChange(newText)
            }
        },
        label = { Text(stringResource(id = R.string.add_edit_battle_combo_description_label)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(id = R.string.add_edit_battle_combo_description_placeholder)) },
        isError = descriptionError != null,
        supportingText = {
            if (descriptionError != null) {
                Text(descriptionError, color = MaterialTheme.colorScheme.error)
            } else {
                Text(
                    text = "${description.length} / $BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    )
}

@Composable
fun ImportButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text(stringResource(id = R.string.add_edit_battle_combo_import_button))
    }
}

@Composable
fun EnergySection(
    selectedEnergy: EnergyLevel,
    onEnergyChange: (EnergyLevel) -> Unit
) {
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
            isSelected = selectedEnergy == EnergyLevel.LOW,
            onClick = { onEnergyChange(EnergyLevel.LOW) }
        )
        EnergyChip(
            label = stringResource(id = R.string.add_edit_battle_combo_energy_med),
            color = Color(0xFFFFC107),
            isSelected = selectedEnergy == EnergyLevel.MEDIUM,
            onClick = { onEnergyChange(EnergyLevel.MEDIUM) }
        )
        EnergyChip(
            label = stringResource(id = R.string.add_edit_battle_combo_energy_high),
            color = Color(0xFFF44336),
            isSelected = selectedEnergy == EnergyLevel.HIGH,
            onClick = { onEnergyChange(EnergyLevel.HIGH) }
        )
    }
}

@Composable
fun ReadinessSection(
    selectedStatus: TrainingStatus,
    onStatusChange: (TrainingStatus) -> Unit
) {
    Text(
        stringResource(id = R.string.add_edit_battle_combo_readiness_label),
        style = MaterialTheme.typography.titleMedium
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
    ) {
        FilterChip(
            selected = selectedStatus == TrainingStatus.TRAINING,
            onClick = { onStatusChange(TrainingStatus.TRAINING) },
            label = { Text(stringResource(id = R.string.add_edit_battle_combo_training_label)) },
            leadingIcon = { Text("🔨") }
        )
        FilterChip(
            selected = selectedStatus == TrainingStatus.READY,
            onClick = { onStatusChange(TrainingStatus.READY) },
            label = { Text(stringResource(id = R.string.add_edit_battle_combo_ready_label)) },
            leadingIcon = { Text("🔥") }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsSection(
    allBattleTags: List<BattleTag>,
    selectedTags: Set<String>,
    newTagName: String,
    newTagError: String?,
    onTagSelected: (String) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onAddBattleTag: () -> Unit
) {
    Text(
        stringResource(id = R.string.add_edit_battle_combo_tags_label),
        style = MaterialTheme.typography.titleMedium
    )

    if (allBattleTags.isEmpty() && newTagName.isBlank()) {
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
        allBattleTags.forEach { tag ->
            FilterChip(
                selected = selectedTags.contains(tag.name),
                onClick = { onTagSelected(tag.name) },
                label = { Text(tag.name) },
                leadingIcon = if (selectedTags.contains(tag.name)) {
                    {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }

    NewTagInput(
        newTagName = newTagName,
        newTagError = newTagError,
        onNewTagNameChange = onNewTagNameChange,
        onAddBattleTag = onAddBattleTag
    )
}

// LAYER 1: Input Capping with Supporting Text Error Display
@Composable
fun NewTagInput(
    newTagName: String,
    newTagError: String?,
    onNewTagNameChange: (String) -> Unit,
    onAddBattleTag: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        OutlinedTextField(
            value = newTagName,
            onValueChange = { newText ->
                if (newText.length <= BATTLE_TAG_CHARACTER_LIMIT) {
                    onNewTagNameChange(newText)
                }
            },
            label = { Text(stringResource(id = R.string.add_edit_battle_combo_new_tag_label)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            isError = newTagError != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAddBattleTag() }),
            supportingText = {
                if (newTagError != null) {
                    Text(newTagError, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(
                        text = "${newTagName.length} / $BATTLE_TAG_CHARACTER_LIMIT",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        )
        Button(onClick = onAddBattleTag) {
            Text(stringResource(id = R.string.common_add))
        }
    }
}

@Composable
fun ImportDialog(
    practiceCombos: List<SavedCombo>,
    onImportCombo: (SavedCombo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.add_edit_battle_combo_import_dialog_title)) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .height(AppStyleDefaults.SpacingExtraLarge * 5)
                    .fillMaxWidth()
            ) {
                if (practiceCombos.isEmpty()) {
                    item {
                        Text(
                            stringResource(id = R.string.add_edit_battle_combo_no_practice_combos_message),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(AppStyleDefaults.SpacingLarge)
                        )
                    }
                } else {
                    items(practiceCombos) { combo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppStyleDefaults.SpacingSmall)
                                .clickable { onImportCombo(combo) }
                        ) {
                            Text(
                                text = combo.moves.joinToString(" -> "),
                                modifier = Modifier.padding(AppStyleDefaults.SpacingMedium),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_close))
            }
        }
    )
}

@Composable
fun DeleteComboDialog(
    comboDescription: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
        text = {
            Text("Are you sure you want to delete this combo?\n\n$comboDescription")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
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
            selectedContainerColor = color.copy(alpha = 0.3f),
            selectedLabelColor = color
        )
    )
}

// PREVIEWS
@Preview(showBackground = true)
@Composable
fun PreviewDescriptionField() {
    DescriptionField(
        description = "Sample combo description",
        descriptionError = null,
        onDescriptionChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewDescriptionFieldWithError() {
    DescriptionField(
        description = "",
        descriptionError = "Description cannot be empty.",
        onDescriptionChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewNewTagInput() {
    NewTagInput(
        newTagName = "New Tag",
        newTagError = null,
        onNewTagNameChange = {},
        onAddBattleTag = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewNewTagInputWithError() {
    NewTagInput(
        newTagName = "Power",
        newTagError = "Tag 'Power' already exists.",
        onNewTagNameChange = {},
        onAddBattleTag = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewDeleteComboDialog() {
    DeleteComboDialog(
        comboDescription = "Jab -> Cross -> Hook",
        onConfirm = {},
        onDismiss = {}
    )
}
