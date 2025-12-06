package com.princelumpy.breakvault.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.MoveListTag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel
import kotlinx.coroutines.launch

enum class GenerationMode {
    Random, Structured
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComboGeneratorScreen(
    navController: NavController,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val allTags by moveViewModel.allTags.observeAsState(initial = emptyList())
    var selectedGeneratorMoveListTags by remember { mutableStateOf(setOf<MoveListTag>()) }
    var generatedComboText by remember { mutableStateOf("") }
    var currentGeneratedMoves by remember { mutableStateOf<List<Move>>(emptyList()) }
    
    // Revert to Dropdown Logic: null = Random, 1-6 = Fixed
    var selectedLength by remember { mutableStateOf<Int?>(null) }
    val lengthOptions = listOf(null) + (1..6).toList()

    var allowRepeats by remember { mutableStateOf(false) }

    var showLengthWarningDialog by remember { mutableStateOf(false) }
    var warningDialogMessage by remember { mutableStateOf("") }
    
    // Dropdown expansion state
    var lengthDropdownExpanded by remember { mutableStateOf(false) }

    var currentMode by remember { mutableStateOf(GenerationMode.Random) }
    var structuredMoveListTagSequence by remember { mutableStateOf<List<MoveListTag>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        generatedComboText = context.getString(R.string.combo_generator_initial_text)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.combo_generator_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.common_back_button_description))
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentGeneratedMoves.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        moveViewModel.saveCombo("", currentGeneratedMoves.map { it.name })
                        scope.launch {
                            snackbarHostState.showSnackbar(message = context.getString(R.string.combo_generator_combo_saved_snackbar))
                        }
                    },
                    icon = { Icon(Icons.Filled.Save, contentDescription = stringResource(id = R.string.combo_generator_save_combo_icon_description)) },
                    text = { Text(stringResource(id = R.string.combo_generator_save_combo_button)) }
                )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selectedTabIndex = currentMode.ordinal) {
                GenerationMode.entries.forEach { mode ->
                    Tab(
                        selected = currentMode == mode,
                        onClick = { currentMode = mode },
                        text = { Text(mode.name) }
                    )
                }
            }

            when (currentMode) {
                GenerationMode.Random -> {
                    RandomModeUI(
                        allMoveListTags = allTags,
                        selectedMoveListTags = selectedGeneratorMoveListTags,
                        onTagsChange = { selectedGeneratorMoveListTags = it },
                        selectedLength = selectedLength,
                        onLengthChange = { selectedLength = it },
                        lengthDropdownExpanded = lengthDropdownExpanded,
                        onDropdownExpand = { lengthDropdownExpanded = it },
                        lengthOptions = lengthOptions,
                        allowRepeats = allowRepeats,
                        onAllowRepeatsChange = { allowRepeats = it }
                    )
                }
                GenerationMode.Structured -> {
                    StructuredModeUI(allTags, structuredMoveListTagSequence, onSequenceChange = { structuredMoveListTagSequence = it })
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val comboMoves = when (currentMode) {
                        GenerationMode.Random -> {
                            // If no moveListTags selected, use ALL moveListTags
                            val tagsToUse = selectedGeneratorMoveListTags.ifEmpty { allTags.toSet() }
                            
                            if (tagsToUse.isNotEmpty()) {
                                moveViewModel.generateComboFromTags(tagsToUse, selectedLength, allowRepeats)
                            } else {
                                generatedComboText = context.getString(R.string.combo_generator_no_tags_message)
                                emptyList()
                            }
                        }
                        GenerationMode.Structured -> {
                            if (structuredMoveListTagSequence.isNotEmpty()) {
                                moveViewModel.generateStructuredCombo(structuredMoveListTagSequence)
                            } else {
                                generatedComboText = context.getString(R.string.combo_generator_select_at_least_one_tag_message)
                                emptyList()
                            }
                        }
                    }

                    if (comboMoves.isNotEmpty()) {
                        currentGeneratedMoves = comboMoves
                        generatedComboText = comboMoves.joinToString(separator = "  ->  ") { it.name }
                        
                        // Warning logic
                        if (!allowRepeats && currentMode == GenerationMode.Random && selectedLength != null && selectedLength!! > comboMoves.size) {
                            // User asked for more unique moves than available
                            warningDialogMessage = context.getString(R.string.combo_generator_length_warning_dialog_message, comboMoves.size)
                            showLengthWarningDialog = true
                        }
                    } else if ((currentMode == GenerationMode.Random && allTags.isNotEmpty()) || (currentMode == GenerationMode.Structured && structuredMoveListTagSequence.isNotEmpty())) {
                        currentGeneratedMoves = emptyList()
                        generatedComboText = context.getString(R.string.combo_generator_no_moves_found_message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // Enable if: (Random AND we have at least some moveListTags in the system) OR (Structured AND sequence is not empty)
                enabled = (currentMode == GenerationMode.Random && allTags.isNotEmpty()) || (currentMode == GenerationMode.Structured && structuredMoveListTagSequence.isNotEmpty())
            ) {
                Text(stringResource(id = R.string.combo_generator_generate_combo_button))
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(stringResource(id = R.string.combo_generator_generated_combo_label), style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 100.dp)
            ) {
                Text(
                    text = generatedComboText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    if (showLengthWarningDialog) {
        AlertDialog(
            onDismissRequest = { showLengthWarningDialog = false },
            title = { Text(stringResource(id = R.string.combo_generator_length_warning_dialog_title)) },
            text = { Text(warningDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showLengthWarningDialog = false }) {
                    Text(stringResource(id = R.string.common_ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RandomModeUI(
    allMoveListTags: List<MoveListTag>,
    selectedMoveListTags: Set<MoveListTag>,
    onTagsChange: (Set<MoveListTag>) -> Unit,
    selectedLength: Int?,
    onLengthChange: (Int?) -> Unit,
    lengthDropdownExpanded: Boolean,
    onDropdownExpand: (Boolean) -> Unit,
    lengthOptions: List<Int?>,
    allowRepeats: Boolean,
    onAllowRepeatsChange: (Boolean) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(id = R.string.combo_generator_select_tags_label), style = MaterialTheme.typography.titleMedium)

        if (allMoveListTags.isEmpty()) {
            Text(
                stringResource(id = R.string.combo_generator_no_tags_message),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allMoveListTags.forEach { tag ->
                    FilterChip(
                        selected = selectedMoveListTags.contains(tag),
                        onClick = { onTagsChange(if (selectedMoveListTags.contains(tag)) selectedMoveListTags - tag else selectedMoveListTags + tag) },
                        label = { Text(tag.name) },
                        leadingIcon = if (selectedMoveListTags.contains(tag)) {
                            { Icon(Icons.Filled.Done, stringResource(id = R.string.add_edit_move_selected_chip_description), Modifier.size(FilterChipDefaults.IconSize)) }
                        } else { null }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(1.25.dp))

        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Allow Repeats Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            // Dropdown (Takes up space but limited width naturally)
            ExposedDropdownMenuBox(
                expanded = lengthDropdownExpanded,
                onExpandedChange = onDropdownExpand,
                modifier = Modifier.width(180.dp)
            ) {
                OutlinedTextField(
                    value = selectedLength?.toString() ?: stringResource(id = R.string.combo_generator_random_length_option),
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
                            text = { Text(option?.toString() ?: stringResource(id = R.string.combo_generator_random_length_option)) },
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
    allMoveListTags: List<MoveListTag>,
    moveListTagSequence: List<MoveListTag>,
    onSequenceChange: (List<MoveListTag>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMoveListTag by remember { mutableStateOf<MoveListTag?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(id = R.string.combo_generator_define_structure_label), style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedMoveListTag?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.combo_generator_add_tag_to_sequence_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                allMoveListTags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.name) },
                        onClick = {
                            selectedMoveListTag = tag
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Increased limit to 10
            Button(onClick = { onSequenceChange(moveListTagSequence + selectedMoveListTag!!) }, enabled = selectedMoveListTag != null && moveListTagSequence.size < 10) {
                Text(stringResource(id = R.string.combo_generator_add_to_sequence_button))
            }

            if (moveListTagSequence.isNotEmpty()) {
                FilledTonalButton(onClick = { onSequenceChange(moveListTagSequence.dropLast(1)) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(id = R.string.combo_generator_undo_button_description)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.combo_generator_undo_button))
                }
            }
        }

        if (moveListTagSequence.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(id = R.string.combo_generator_current_sequence_label), style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = moveListTagSequence.joinToString(" -> ") { it.name },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ComboGeneratorScreenPreview() {
    ComboGeneratorTheme {
        val previewViewModel = FakeMoveViewModel()
        ComboGeneratorScreen(
            navController = rememberNavController(),
            moveViewModel = previewViewModel
        )
    }
}
