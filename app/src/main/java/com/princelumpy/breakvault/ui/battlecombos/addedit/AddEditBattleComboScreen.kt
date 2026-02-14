package com.princelumpy.breakvault.ui.battlecombos.addedit

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import com.princelumpy.breakvault.ui.common.TagDialog
import com.princelumpy.breakvault.ui.common.TagSelectionCard
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
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

    AddEditBattleComboScaffold(
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

/**
 * A stateless scaffold that handles the overall layout for the Add/Edit BattleComboList Combo screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditBattleComboScaffold(
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
    onImportCombo: (PracticeCombo) -> Unit,
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
                modifier = Modifier.imePadding(),
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
            AddEditBattleComboFormContent(
                modifier = Modifier.padding(paddingValues),
                userInputs = userInputs,
                dialogsAndMessages = dialogsAndMessages,
                allBattleTags = uiState.allBattleTags,
                onDescriptionChange = onDescriptionChange,
                onShowImportDialog = onShowImportDialog,
                onEnergyChange = onEnergyChange,
                onStatusChange = onStatusChange,
                onTagSelected = onTagSelected,
                onNewTagNameChange = onNewTagNameChange,
                onAddBattleTag = onAddBattleTag
            )
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

/**
 * The main form content composable containing all input fields.
 */
@Composable
private fun AddEditBattleComboFormContent(
    modifier: Modifier = Modifier,
    userInputs: UserInputs,
    dialogsAndMessages: UiDialogsAndMessages,
    allBattleTags: List<BattleTag>,
    onDescriptionChange: (String) -> Unit,
    onShowImportDialog: (Boolean) -> Unit,
    onEnergyChange: (EnergyLevel) -> Unit,
    onStatusChange: (TrainingStatus) -> Unit,
    onTagSelected: (String) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onAddBattleTag: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppStyleDefaults.SpacingLarge)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
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
            allBattleTags = allBattleTags,
            selectedTags = userInputs.selectedTags,
            newTagName = userInputs.newTagName,
            newTagError = dialogsAndMessages.newTagError,
            onTagSelected = onTagSelected,
            onNewTagNameChange = onNewTagNameChange,
            onAddBattleTag = onAddBattleTag
        )
    }
}

/**
 * Description input field with character limit.
 */
@Composable
private fun DescriptionField(
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

/**
 * Button to trigger import dialog.
 */
@Composable
private fun ImportButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text(stringResource(id = R.string.add_edit_battle_combo_import_button))
    }
}

/**
 * Section for selecting energy level.
 */
@Composable
private fun EnergySection(
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

/**
 * Section for selecting training status/readiness.
 */
@Composable
private fun ReadinessSection(
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

/**
 * Section for displaying and adding battle tags.
 */
@Composable
private fun TagsSection(
    allBattleTags: List<BattleTag>,
    selectedTags: Set<String>,
    newTagName: String,
    newTagError: String?,
    onTagSelected: (String) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onAddBattleTag: () -> Unit
) {
    var showAddTagDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(id = R.string.add_edit_battle_combo_tags_label),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = { showAddTagDialog = true }) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(id = R.string.add_edit_battle_combo_new_tag_label),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    TagSelectionCard(
        allTags = allBattleTags,
        selectedTags = selectedTags,
        isLoading = false,
        emptyMessage = stringResource(id = R.string.add_edit_battle_combo_no_tags_message),
        onTagSelected = onTagSelected,
        getTagId = { it.id },
        getTagName = { it.name }
    )

    if (showAddTagDialog) {
        TagDialog(
            title = stringResource(id = R.string.add_edit_battle_combo_new_tag_label),
            labelText = stringResource(id = R.string.add_edit_battle_combo_new_tag_label),
            confirmButtonText = stringResource(id = R.string.common_add),
            tagName = newTagName,
            characterLimit = BATTLE_TAG_CHARACTER_LIMIT,
            isError = newTagError != null,
            errorMessage = newTagError,
            onTagNameChange = onNewTagNameChange,
            onConfirm = {
                onAddBattleTag()
            },
            onDismiss = {
                showAddTagDialog = false
                onNewTagNameChange("") // Clear input on dismiss
            }
        )
    }

    // Close dialog when tag is successfully added (error cleared and input cleared)
    LaunchedEffect(newTagError, newTagName) {
        if (showAddTagDialog && newTagError == null && newTagName.isEmpty()) {
            showAddTagDialog = false
        }
    }
}

/**
 * Dialog for importing practice combos.
 */
@Composable
private fun ImportDialog(
    practiceCombos: List<PracticeCombo>,
    onImportCombo: (PracticeCombo) -> Unit,
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

/**
 * Dialog for confirming combo deletion.
 */
@Composable
private fun DeleteComboDialog(
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

/**
 * Chip for displaying and selecting energy level.
 */
@Composable
private fun EnergyChip(
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


//region Previews

@Preview(showBackground = true, name = "Add BattleComboList Combo - New")
@Composable
private fun AddEditBattleComboFormContentPreview() {
    val dummyTags = listOf(
        BattleTag(name = "Opening"),
        BattleTag(name = "Finisher"),
        BattleTag(name = "Defensive")
    )
    BreakVaultTheme {
        AddEditBattleComboFormContent(
            userInputs = UserInputs(
                description = "Jab -> Cross -> Hook",
                selectedEnergy = EnergyLevel.MEDIUM,
                selectedStatus = TrainingStatus.TRAINING,
                selectedTags = setOf("Opening"),
                isNewCombo = true
            ),
            dialogsAndMessages = UiDialogsAndMessages(),
            allBattleTags = dummyTags,
            onDescriptionChange = {},
            onShowImportDialog = {},
            onEnergyChange = {},
            onStatusChange = {},
            onTagSelected = {},
            onNewTagNameChange = {},
            onAddBattleTag = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit BattleComboList Combo - With Data")
@Composable
private fun AddEditBattleComboFormContentEditPreview() {
    val dummyTags = listOf(
        BattleTag(name = "Opening"),
        BattleTag(name = "Finisher"),
        BattleTag(name = "Defensive")
    )
    BreakVaultTheme {
        AddEditBattleComboFormContent(
            userInputs = UserInputs(
                description = "Uppercut -> Spinning Kick -> Ground Move",
                selectedEnergy = EnergyLevel.HIGH,
                selectedStatus = TrainingStatus.READY,
                selectedTags = setOf("Finisher", "Opening"),
                isNewCombo = false,
                newTagName = "Power"
            ),
            dialogsAndMessages = UiDialogsAndMessages(),
            allBattleTags = dummyTags,
            onDescriptionChange = {},
            onShowImportDialog = {},
            onEnergyChange = {},
            onStatusChange = {},
            onTagSelected = {},
            onNewTagNameChange = {},
            onAddBattleTag = {}
        )
    }
}

@Preview(showBackground = true, name = "Add BattleComboList Combo - With Errors")
@Composable
private fun AddEditBattleComboFormContentWithErrorsPreview() {
    val dummyTags = listOf(
        BattleTag(name = "Opening"),
        BattleTag(name = "Finisher")
    )
    BreakVaultTheme {
        AddEditBattleComboFormContent(
            userInputs = UserInputs(
                description = "",
                selectedEnergy = EnergyLevel.LOW,
                selectedStatus = TrainingStatus.TRAINING,
                selectedTags = emptySet(),
                isNewCombo = true,
                newTagName = "Opening"
            ),
            dialogsAndMessages = UiDialogsAndMessages(
                descriptionError = "Description cannot be empty.",
                newTagError = "Tag 'Opening' already exists."
            ),
            allBattleTags = dummyTags,
            onDescriptionChange = {},
            onShowImportDialog = {},
            onEnergyChange = {},
            onStatusChange = {},
            onTagSelected = {},
            onNewTagNameChange = {},
            onAddBattleTag = {}
        )
    }
}

@Preview(showBackground = true, name = "Delete Combo Dialog")
@Composable
private fun DeleteComboDialogPreview() {
    BreakVaultTheme {
        DeleteComboDialog(
            comboDescription = "Jab -> Cross -> Hook",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Import Dialog - With Combos")
@Composable
private fun ImportDialogPreview() {
    val dummyCombos = listOf(
        PracticeCombo(id = "1", name = "combo1", moves = listOf("Jab", "Cross", "Hook")),
        PracticeCombo(id = "2", name = "Combo 2", moves = listOf("Windmill", "Freeze", "Air Flare"))
    )
    BreakVaultTheme {
        ImportDialog(
            practiceCombos = dummyCombos,
            onImportCombo = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Import Dialog - Empty")
@Composable
private fun ImportDialogEmptyPreview() {
    BreakVaultTheme {
        ImportDialog(
            practiceCombos = emptyList(),
            onImportCombo = {},
            onDismiss = {}
        )
    }
}

//endregion
