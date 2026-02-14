package com.princelumpy.breakvault.ui.combogenerator

import AppStyleDefaults
import androidx.compose.foundation.background
import com.princelumpy.breakvault.data.local.entity.Move
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    onLengthChange: (Float) -> Unit,
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
                    modifier = Modifier.imePadding(),
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
                .padding(horizontal = AppStyleDefaults.SpacingLarge)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
        ) {
            ModeTabRow(
                currentMode = uiState.settings.currentMode,
                onModeChange = onModeChange
            )

            when (uiState.settings.currentMode) {
                GenerationMode.Random -> {
                    RandomModeUI(
                        allMoveTags = uiState.allTags,
                        isLoadingTags = uiState.isLoadingTags,
                        selectedMoveTags = uiState.settings.selectedTags,
                        onTagsChange = onTagsChange,
                        selectedLength = uiState.settings.selectedLength,
                        onLengthChange = { length ->
                            onLengthChange(length)
                        },
                        allowRepeats = uiState.settings.allowRepeats,
                        onAllowRepeatsChange = onAllowRepeatsChange
                    )
                }

                GenerationMode.Structured -> {
                    StructuredModeUI(
                        allMoveTags = uiState.allTags,
                        isLoadingTags = uiState.isLoadingTags,
                        moveTagSequence = uiState.settings.structuredMoveTagSequence,
                        onAddTagToSequence = onAddTagToSequence,
                        onRemoveLastTagFromSequence = onRemoveLastTagFromSequence
                    )
                }
            }

            Button(
                onClick = onGenerateCombo,
                modifier = Modifier.fillMaxWidth(),
                enabled = when (uiState.settings.currentMode) {
                    GenerationMode.Random -> true
                    GenerationMode.Structured -> uiState.settings.structuredMoveTagSequence.isNotEmpty()
                }
            ) {
                Text(stringResource(id = R.string.combo_generator_generate_combo_button))
            }

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
    isLoadingTags: Boolean,
    selectedMoveTags: Set<MoveTag>,
    onTagsChange: (Set<MoveTag>) -> Unit,
    selectedLength: Int,
    onLengthChange: (Float) -> Unit,
    allowRepeats: Boolean,
    onAllowRepeatsChange: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        Text(
            stringResource(id = R.string.combo_generator_select_tags_label),
            style = MaterialTheme.typography.titleMedium
        )

        MoveTagChipGroup(
            allMoveTags = allMoveTags,
            isLoadingTags = isLoadingTags,
            selectedMoveTags = selectedMoveTags,
            onTagsChange = onTagsChange
        )

        RandomModeControls(
            allowRepeats = allowRepeats,
            onAllowRepeatsChange = onAllowRepeatsChange,
            selectedLength = selectedLength,
            onLengthChange = onLengthChange
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoveTagChipGroup(
    allMoveTags: List<MoveTag>,
    isLoadingTags: Boolean,
    selectedMoveTags: Set<MoveTag>,
    onTagsChange: (Set<MoveTag>) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppStyleDefaults.SpacingExtraLarge * 8),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppStyleDefaults.SpacingMedium),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoadingTags -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppStyleDefaults.SpacingExtraLarge)
                    )
                }

                allMoveTags.isEmpty() -> {
                    Text(
                        stringResource(id = R.string.combo_generator_no_tags_message),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
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
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomModeControls(
    allowRepeats: Boolean,
    onAllowRepeatsChange: (Boolean) -> Unit,
    selectedLength: Int,
    onLengthChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
        ) {
            LengthSlider(
                selectedLength = selectedLength,
                onLengthChange = onLengthChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LengthSlider(
    selectedLength: Int,
    onLengthChange: (Float) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(selectedLength.toFloat()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(
                id = R.string.combo_generator_combo_length_label,
                sliderValue.toInt()
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
            },
            onValueChangeFinished = {
                onLengthChange(sliderValue)
            },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuredModeUI(
    allMoveTags: List<MoveTag>,
    isLoadingTags: Boolean,
    moveTagSequence: List<MoveTag>,
    onAddTagToSequence: (MoveTag) -> Unit,
    onRemoveLastTagFromSequence: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTagForDropdown by remember { mutableStateOf<MoveTag?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        Text(
            stringResource(id = R.string.combo_generator_define_structure_label),
            style = MaterialTheme.typography.titleMedium
        )

        TagSelectionComboBox(
            allMoveTags = allMoveTags,
            isLoadingTags = isLoadingTags,
            selectedTag = selectedTagForDropdown,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onTagSelected = {
                selectedTagForDropdown = it
                searchQuery = ""
            }
        )

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

        SequenceControlButtons(
            selectedTag = selectedTagForDropdown,
            onAddTag = { selectedTagForDropdown?.let(onAddTagToSequence) },
            onRemoveLastTag = onRemoveLastTagFromSequence,
            canRemove = moveTagSequence.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

        CurrentSequenceCard(moveTagSequence = moveTagSequence)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionComboBox(
    allMoveTags: List<MoveTag>,
    isLoadingTags: Boolean,
    selectedTag: MoveTag?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTagSelected: (MoveTag) -> Unit
) {
    val filteredTags = remember(allMoveTags, searchQuery) {
        if (searchQuery.isEmpty()) {
            allMoveTags
        } else {
            allMoveTags.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!isLoadingTags) onExpandedChange(it) }
    ) {
        OutlinedTextField(
            value = if (expanded) searchQuery else (selectedTag?.name ?: ""),
            onValueChange = { newValue ->
                onSearchQueryChange(newValue)
                if (!expanded) {
                    onExpandedChange(true)
                }
            },
            enabled = !isLoadingTags,
            label = {
                Text(
                    if (isLoadingTags) "Loading tags..."
                    else stringResource(id = R.string.combo_generator_add_tag_to_sequence_label)
                )
            },
            placeholder = {
                if (expanded) {
                    Text("Search tags...")
                }
            },
            trailingIcon = {
                if (isLoadingTags) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppStyleDefaults.SpacingMedium),
                        strokeWidth = 2.dp
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
                onSearchQueryChange("")
            }
        ) {
            if (filteredTags.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "No tags found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {},
                    enabled = false
                )
            } else {
                filteredTags.forEach { tag ->
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
            isLoadingTags = false,
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
                isLoadingTags = false,
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
                isLoadingTags = false,
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