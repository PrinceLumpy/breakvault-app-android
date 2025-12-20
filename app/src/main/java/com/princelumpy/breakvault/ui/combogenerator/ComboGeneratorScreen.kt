package com.princelumpy.breakvault.ui.combogenerator

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.MoveTag

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComboGeneratorScreen(
    onNavigateUp: () -> Unit,
    comboGeneratorViewModel: ComboGeneratorViewModel = hiltViewModel()
) {
    val uiState by comboGeneratorViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Create convenience variables for cleaner access
    val settings = uiState.settings
    val generatedCombo = uiState.generatedCombo
    val dialogAndMessages = uiState.dialogAndMessages

    LaunchedEffect(dialogAndMessages.snackbarMessage) {
        dialogAndMessages.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            comboGeneratorViewModel.onSnackbarShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.combo_generator_title)) },
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
            // UPDATED: Access generatedCombo state
            if (generatedCombo.moves.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { comboGeneratorViewModel.saveCombo() },
                    icon = {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = stringResource(id = R.string.combo_generator_save_combo_icon_description)
                        )
                    },
                    text = { Text(stringResource(id = R.string.combo_generator_save_combo_button)) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(AppStyleDefaults.SpacingLarge)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
        ) {
            // UPDATED: Access settings state
            TabRow(selectedTabIndex = settings.currentMode.ordinal) {
                GenerationMode.entries.forEach { mode ->
                    Tab(
                        selected = settings.currentMode == mode,
                        onClick = { comboGeneratorViewModel.onModeChange(mode) },
                        text = { Text(mode.name) }
                    )
                }
            }

            // UPDATED: Access settings state
            when (settings.currentMode) {
                GenerationMode.Random -> {
                    RandomModeUI(
                        allMoveTags = uiState.allTags,
                        selectedMoveTags = settings.selectedTags,
                        onTagsChange = { comboGeneratorViewModel.onTagsChange(it) },
                        selectedLength = settings.selectedLength,
                        onLengthChange = { comboGeneratorViewModel.onLengthChange(it) },
                        lengthDropdownExpanded = dialogAndMessages.lengthDropdownExpanded,
                        onDropdownExpand = { comboGeneratorViewModel.onDropdownExpand(it) },
                        lengthOptions = listOf(null) + (3..8).toList(),
                        allowRepeats = settings.allowRepeats,
                        onAllowRepeatsChange = { comboGeneratorViewModel.onAllowRepeatsChange(it) }
                    )
                }

                GenerationMode.Structured -> {
                    StructuredModeUI(
                        allMoveTags = uiState.allTags,
                        moveTagSequence = settings.structuredMoveTagSequence,
                        onAddTagToSequence = { comboGeneratorViewModel.onAddTagToSequence(it) },
                        onRemoveLastTagFromSequence = { comboGeneratorViewModel.onRemoveLastTagFromSequence() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

            Button(
                onClick = { comboGeneratorViewModel.generateCombo() },
                modifier = Modifier.fillMaxWidth(),
                // UPDATED: Access settings state
                enabled = (settings.currentMode == GenerationMode.Random && settings.selectedTags.isNotEmpty()) || (settings.currentMode == GenerationMode.Structured && settings.structuredMoveTagSequence.isNotEmpty())
            ) {
                Text(stringResource(id = R.string.combo_generator_generate_combo_button))
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

            Text(
                stringResource(id = R.string.combo_generator_generated_combo_label),
                style = MaterialTheme.typography.titleMedium
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = AppStyleDefaults.SpacingExtraLarge * 2)
            ) {
                Text(
                    // UPDATED: Access generatedCombo state
                    text = generatedCombo.text,
                    modifier = Modifier.padding(AppStyleDefaults.SpacingLarge),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    // UPDATED: Access dialogAndMessages state
    if (dialogAndMessages.showLengthWarningDialog) {
        AlertDialog(
            onDismissRequest = { comboGeneratorViewModel.onDismissLengthWarning() },
            title = { Text(stringResource(id = R.string.combo_generator_length_warning_dialog_title)) },
            text = { Text(dialogAndMessages.warningDialogMessage) },
            confirmButton = {
                TextButton(onClick = { comboGeneratorViewModel.onDismissLengthWarning() }) {
                    Text(stringResource(id = R.string.common_ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RandomModeUI(
    allMoveTags: List<MoveTag>,
    selectedMoveTags: Set<MoveTag>,
    onTagsChange: (Set<MoveTag>) -> Unit,
    selectedLength: Int?,
    onLengthChange: (Int?) -> Unit,
    lengthDropdownExpanded: Boolean,
    onDropdownExpand: (Boolean) -> Unit,
    lengthOptions: List<Int?>,
    allowRepeats: Boolean,
    onAllowRepeatsChange: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
    ) {
        Text(
            stringResource(id = R.string.combo_generator_select_tags_label),
            style = MaterialTheme.typography.titleMedium
        )

        if (allMoveTags.isEmpty()) {
            Text(
                stringResource(id = R.string.combo_generator_no_tags_message),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
            ) {
                allMoveTags.forEach { tag ->
                    FilterChip(
                        selected = selectedMoveTags.contains(tag),
                        onClick = { onTagsChange(if (selectedMoveTags.contains(tag)) selectedMoveTags - tag else selectedMoveTags + tag) },
                        label = { Text(tag.name) },
                        leadingIcon = if (selectedMoveTags.contains(tag)) {
                            {
                                Icon(
                                    Icons.Filled.Done,
                                    stringResource(id = R.string.add_edit_move_selected_chip_description),
                                    Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                Text(
                    text = stringResource(id = R.string.combo_generator_allow_repeats_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = allowRepeats,
                    onCheckedChange = onAllowRepeatsChange
                )
            }

            ExposedDropdownMenuBox(
                expanded = lengthDropdownExpanded,
                onExpandedChange = onDropdownExpand,
                modifier = Modifier.width(AppStyleDefaults.SpacingExtraLarge * 4)
            ) {
                OutlinedTextField(
                    value = selectedLength?.toString()
                        ?: stringResource(id = R.string.combo_generator_random_length_option),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.combo_generator_combo_length_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lengthDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = lengthDropdownExpanded,
                    onDismissRequest = { onDropdownExpand(false) }
                ) {
                    lengthOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option?.toString()
                                        ?: stringResource(id = R.string.combo_generator_random_length_option)
                                )
                            },
                            onClick = {
                                onLengthChange(option)
                                onDropdownExpand(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuredModeUI(
    allMoveTags: List<MoveTag>,
    moveTagSequence: List<MoveTag>,
    onAddTagToSequence: (MoveTag) -> Unit,
    onRemoveLastTagFromSequence: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTagForDropdown by remember { mutableStateOf<MoveTag?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        Text(
            stringResource(id = R.string.combo_generator_define_structure_label),
            style = MaterialTheme.typography.titleMedium
        )

        // Dropdown to select a tag
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedTagForDropdown?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.combo_generator_add_tag_to_sequence_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                allMoveTags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.name) },
                        onClick = {
                            selectedTagForDropdown = tag
                            expanded = false
                        }
                    )
                }
            }
        }

        // Buttons to add or remove tags
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    selectedTagForDropdown?.let {
                        onAddTagToSequence(it)
                    }
                },
                enabled = selectedTagForDropdown != null,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(id = R.string.combo_generator_add_tag_button))
            }
            Button(
                onClick = onRemoveLastTagFromSequence,
                enabled = moveTagSequence.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(id = R.string.combo_generator_remove_last_tag_button))
            }
        }

        // Display the current sequence
        Text(
            stringResource(id = R.string.combo_generator_current_sequence_label),
            style = MaterialTheme.typography.titleSmall
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (moveTagSequence.isEmpty()) {
                    stringResource(id = R.string.combo_generator_sequence_empty_message)
                } else {
                    moveTagSequence.joinToString(" -> ") { it.name }
                },
                modifier = Modifier.padding(AppStyleDefaults.SpacingLarge)
            )
        }
    }
}
