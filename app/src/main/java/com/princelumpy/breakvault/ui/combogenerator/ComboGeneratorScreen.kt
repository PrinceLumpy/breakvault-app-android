package com.princelumpy.breakvault.ui.combogenerator

import AppStyleDefaults
import com.princelumpy.breakvault.data.local.entity.Move
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.MoveTag

// STATEFUL COMPOSABLE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComboGeneratorScreen(
    onNavigateUp: () -> Unit,
    comboGeneratorViewModel: ComboGeneratorViewModel = hiltViewModel()
) {
    val uiState by comboGeneratorViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.dialogAndMessages.snackbarMessage) {
        uiState.dialogAndMessages.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            comboGeneratorViewModel.onSnackbarShown()
        }
    }

    ComboGeneratorContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onModeChange = comboGeneratorViewModel::onModeChange,
        onTagsChange = comboGeneratorViewModel::onTagsChange,
        onLengthChange = comboGeneratorViewModel::onLengthChange,
        onAllowRepeatsChange = comboGeneratorViewModel::onAllowRepeatsChange,
        onAddTagToSequence = comboGeneratorViewModel::onAddTagToSequence,
        onRemoveLastTagFromSequence = comboGeneratorViewModel::onRemoveLastTagFromSequence,
        onGenerateCombo = comboGeneratorViewModel::generateCombo,
        onSaveCombo = comboGeneratorViewModel::saveCombo,
    )
}

// STATELESS COMPOSABLE
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComboGeneratorContent(
    uiState: ComboGeneratorUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onModeChange: (GenerationMode) -> Unit,
    onTagsChange: (Set<MoveTag>) -> Unit,
    onLengthChange: (String) -> Unit,
    onAllowRepeatsChange: (Boolean) -> Unit,
    onAddTagToSequence: (MoveTag) -> Unit,
    onRemoveLastTagFromSequence: () -> Unit,
    onGenerateCombo: () -> Unit,
    onSaveCombo: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.combo_generator_title)) },
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
            if (uiState.generatedCombo.moves.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onSaveCombo,
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
            ModeTabRow(
                currentMode = uiState.settings.currentMode,
                onModeChange = onModeChange
            )

            when (uiState.settings.currentMode) {
                GenerationMode.Random -> {
                    RandomModeUI(
                        allMoveTags = uiState.allTags,
                        selectedMoveTags = uiState.settings.selectedTags,
                        onTagsChange = onTagsChange,
                        selectedLength = uiState.settings.selectedLength,
                        lengthError = uiState.dialogAndMessages.lengthError,
                        onLengthChange = { textValue ->
                            onLengthChange(textValue)
                        },
                        allowRepeats = uiState.settings.allowRepeats,
                        onAllowRepeatsChange = onAllowRepeatsChange
                    )
                }

                GenerationMode.Structured -> {
                    StructuredModeUI(
                        allMoveTags = uiState.allTags,
                        moveTagSequence = uiState.settings.structuredMoveTagSequence,
                        onAddTagToSequence = onAddTagToSequence,
                        onRemoveLastTagFromSequence = onRemoveLastTagFromSequence
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

            Button(
                onClick = onGenerateCombo,
                modifier = Modifier.fillMaxWidth(),
                enabled = when (uiState.settings.currentMode) {
                    GenerationMode.Random -> uiState.settings.selectedTags.isNotEmpty()
                    GenerationMode.Structured -> uiState.settings.structuredMoveTagSequence.isNotEmpty()
                }
            ) {
                Text(stringResource(id = R.string.combo_generator_generate_combo_button))
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

            GeneratedComboCard(comboText = uiState.generatedCombo.text)
        }
    }
}

@Composable
fun ModeTabRow(
    currentMode: GenerationMode,
    onModeChange: (GenerationMode) -> Unit
) {
    SecondaryTabRow(selectedTabIndex = currentMode.ordinal) {
        GenerationMode.entries.forEach { mode ->
            Tab(
                selected = currentMode == mode,
                onClick = { onModeChange(mode) },
                text = { Text(mode.name) }
            )
        }
    }
}

@Composable
fun GeneratedComboCard(comboText: String) {
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
            text = comboText,
            modifier = Modifier.padding(AppStyleDefaults.SpacingLarge),
            style = MaterialTheme.typography.bodyLarge
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
    lengthError: String?,
    onLengthChange: (String) -> Unit,
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
            MoveTagChipGroup(
                allMoveTags = allMoveTags,
                selectedMoveTags = selectedMoveTags,
                onTagsChange = onTagsChange
            )
        }

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

        RandomModeControls(
            allowRepeats = allowRepeats,
            onAllowRepeatsChange = onAllowRepeatsChange,
            selectedLength = selectedLength,
            lengthError = lengthError,
            onLengthChange = onLengthChange
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoveTagChipGroup(
    allMoveTags: List<MoveTag>,
    selectedMoveTags: Set<MoveTag>,
    onTagsChange: (Set<MoveTag>) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
    ) {
        allMoveTags.forEach { tag ->
            FilterChip(
                selected = selectedMoveTags.contains(tag),
                onClick = {
                    onTagsChange(
                        if (selectedMoveTags.contains(tag)) selectedMoveTags - tag
                        else selectedMoveTags + tag
                    )
                },
                label = { Text(tag.name) },
                leadingIcon = if (selectedMoveTags.contains(tag)) {
                    {
                        Icon(
                            Icons.Filled.Done,
                            stringResource(id = R.string.add_edit_move_selected_chip_description),
                            Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomModeControls(
    allowRepeats: Boolean,
    onAllowRepeatsChange: (Boolean) -> Unit,
    selectedLength: Int?,
    lengthError: String?,
    onLengthChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
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

        LengthInput(
            selectedLength = selectedLength,
            lengthError = lengthError,
            onLengthChange = onLengthChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LengthInput(
    selectedLength: Int?,
    lengthError: String?,
    onLengthChange: (String) -> Unit
) {
    OutlinedTextField(
        value = selectedLength?.toString() ?: "",
        onValueChange = { newText ->
            if (newText.all { it.isDigit() } && newText.length <= 2) {
                onLengthChange(newText)
            }
        },
        label = { Text(stringResource(id = R.string.combo_generator_combo_length_label)) },
        placeholder = { Text(stringResource(id = R.string.combo_generator_combo_length_option)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = lengthError != null,
        supportingText = {
            lengthError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = Modifier.width(AppStyleDefaults.SpacingExtraLarge * 8)
    )
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

        TagSelectionDropdown(
            allMoveTags = allMoveTags,
            selectedTag = selectedTagForDropdown,
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onTagSelected = { selectedTagForDropdown = it }
        )

        SequenceControlButtons(
            selectedTag = selectedTagForDropdown,
            onAddTag = { selectedTagForDropdown?.let(onAddTagToSequence) },
            onRemoveLastTag = onRemoveLastTagFromSequence,
            canRemove = moveTagSequence.isNotEmpty()
        )

        CurrentSequenceCard(moveTagSequence = moveTagSequence)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionDropdown(
    allMoveTags: List<MoveTag>,
    selectedTag: MoveTag?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTagSelected: (MoveTag) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedTag?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.combo_generator_add_tag_to_sequence_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            allMoveTags.forEach { tag ->
                DropdownMenuItem(
                    text = { Text(tag.name) },
                    onClick = {
                        onTagSelected(tag)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
fun SequenceControlButtons(
    selectedTag: MoveTag?,
    onAddTag: () -> Unit,
    onRemoveLastTag: () -> Unit,
    canRemove: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onAddTag,
            enabled = selectedTag != null,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(id = R.string.combo_generator_add_tag_button))
        }
        Button(
            onClick = onRemoveLastTag,
            enabled = canRemove,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(id = R.string.combo_generator_remove_last_tag_button))
        }
    }
}

@Composable
fun CurrentSequenceCard(moveTagSequence: List<MoveTag>) {
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

// PREVIEWS
@Preview(showBackground = true)
@Composable
fun PreviewModeTabRow() {
    MaterialTheme {
        ModeTabRow(
            currentMode = GenerationMode.Random,
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGeneratedComboCard() {
    MaterialTheme {
        GeneratedComboCard(comboText = "Jab -> Cross -> Hook -> Uppercut")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMoveTagChipGroup() {
    MaterialTheme {
        MoveTagChipGroup(
            allMoveTags = listOf(
                MoveTag("1", "Jab"),
                MoveTag("2", "Cross"),
                MoveTag("3", "Hook")
            ),
            selectedMoveTags = setOf(MoveTag("1", "Jab")),
            onTagsChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRandomModeControls() {
    MaterialTheme {
        RandomModeControls(
            allowRepeats = true,
            onAllowRepeatsChange = {},
            selectedLength = 5,
            lengthError = null,
            onLengthChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCurrentSequenceCard() {
    MaterialTheme {
        CurrentSequenceCard(
            moveTagSequence = listOf(
                MoveTag("1", "Jab"),
                MoveTag("2", "Cross"),
                MoveTag("3", "Hook")
            )
        )
    }
}

@Preview(showBackground = true, name = "Combo Generator Screen (Random)")
@Composable
fun PreviewComboGeneratorContent_RandomMode() {
    MaterialTheme {
        ComboGeneratorContent(
            uiState = ComboGeneratorUiState(
                settings = GenerationSettings(
                    currentMode = GenerationMode.Random,
                    selectedTags = setOf(MoveTag("1", "Jab")),
                    selectedLength = 4
                ),
                allTags = listOf(
                    MoveTag("1", "Jab"),
                    MoveTag("2", "Cross"),
                    MoveTag("3", "Hook")
                ),
                generatedCombo = GeneratedComboState(
                    moves = listOf(
                        Move(id = "1", name = "Jab"),
                        Move(id = "2", name = "Cross"),
                        Move(id = "1", name = "Jab"),
                        Move(id = "3", name = "Hook")
                    ),
                    text = "Jab -> Cross -> Jab -> Hook",
                ),
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateUp = {},
            onModeChange = {},
            onTagsChange = {},
            onLengthChange = {},
            onAllowRepeatsChange = {},
            onAddTagToSequence = {},
            onRemoveLastTagFromSequence = {},
            onGenerateCombo = {},
            onSaveCombo = {},
        )
    }
}

@Preview(showBackground = true, name = "Combo Generator Screen (Structured)")
@Composable
fun PreviewComboGeneratorContent_StructuredMode() {
    MaterialTheme {
        ComboGeneratorContent(
            uiState = ComboGeneratorUiState(
                settings = GenerationSettings(
                    currentMode = GenerationMode.Structured,
                    structuredMoveTagSequence = listOf( // Provide a sequence of tags for structured mode
                        MoveTag("1", "Jab"),
                        MoveTag("2", "Hook")
                    )
                ),
                allTags = listOf(
                    MoveTag("1", "Jab"),
                    MoveTag("2", "Cross"),
                    MoveTag("3", "Hook")
                ),
                generatedCombo = GeneratedComboState(
                    listOf(
                        Move(id = "1", name = "Jab"),
                        Move(id = "2", name = "Cross")
                    ),
                    text = "Jab -> Cross"
                )
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigateUp = {},
            onModeChange = {},
            onTagsChange = {},
            onLengthChange = {},
            onAllowRepeatsChange = {},
            onAddTagToSequence = {},
            onRemoveLastTagFromSequence = {},
            onGenerateCombo = {},
            onSaveCombo = {},
        )
    }
}