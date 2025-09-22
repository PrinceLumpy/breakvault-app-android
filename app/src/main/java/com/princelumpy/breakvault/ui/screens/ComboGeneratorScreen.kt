package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.Tag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel
import com.princelumpy.breakvault.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComboGeneratorScreen(
    navController: NavController,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val allTags by moveViewModel.allTags.observeAsState(initial = emptyList())
    var selectedGeneratorTags by remember { mutableStateOf(setOf<Tag>()) }
    var generatedComboText by remember { mutableStateOf("") }
    var currentGeneratedMoves by remember { mutableStateOf<List<Move>>(emptyList()) }
    var selectedLength by remember { mutableStateOf<Int?>(null) } // null for Random
    val lengthOptions = listOf(null, 2, 3, 4, 5, 6) // null represents "Random"

    var showLengthWarningDialog by remember { mutableStateOf(false) }
    var warningDialogMessage by remember { mutableStateOf("") }
    var lengthDropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Initialize generatedComboText with string resource
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(id = R.string.combo_generator_select_tags_label), style = MaterialTheme.typography.titleMedium)

            if (allTags.isEmpty()) {
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
                    allTags.forEach { tag ->
                        FilterChip(
                            selected = selectedGeneratorTags.contains(tag),
                            onClick = {
                                selectedGeneratorTags = if (selectedGeneratorTags.contains(tag)) {
                                    selectedGeneratorTags - tag
                                } else {
                                    selectedGeneratorTags + tag
                                }
                            },
                            label = { Text(tag.name) },
                            leadingIcon = if (selectedGeneratorTags.contains(tag)) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = stringResource(id = R.string.add_edit_move_selected_chip_description),
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(stringResource(id = R.string.combo_generator_number_of_moves_label), style = MaterialTheme.typography.titleMedium)
            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = lengthDropdownExpanded,
                    onExpandedChange = { lengthDropdownExpanded = !lengthDropdownExpanded },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    OutlinedTextField(
                        value = selectedLength?.toString() ?: stringResource(id = R.string.combo_generator_random_length_option),
                        onValueChange = {}, // Not directly editable
                        readOnly = true,
                        label = { Text(stringResource(id = R.string.combo_generator_combo_length_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lengthDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(0.6f).menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = lengthDropdownExpanded,
                        onDismissRequest = { lengthDropdownExpanded = false }
                    ) {
                        lengthOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option?.toString() ?: stringResource(id = R.string.combo_generator_random_length_option)) },
                                onClick = {
                                    selectedLength = option
                                    lengthDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (selectedGeneratorTags.isNotEmpty()) {
                        val comboMoves = moveViewModel.generateComboFromTags(selectedGeneratorTags, selectedLength)
                        if (comboMoves.isNotEmpty()) {
                            currentGeneratedMoves = comboMoves
                            generatedComboText = comboMoves.joinToString(separator = "  ->  ") { it.name }

                            if (selectedLength != null && selectedLength!! > comboMoves.size) {
                                warningDialogMessage = context.getString(R.string.combo_generator_length_warning_dialog_message, comboMoves.size)
                                showLengthWarningDialog = true
                            }
                        } else {
                            currentGeneratedMoves = emptyList()
                            generatedComboText = context.getString(R.string.combo_generator_no_moves_found_message)
                        }
                    } else {
                        currentGeneratedMoves = emptyList()
                        generatedComboText = context.getString(R.string.combo_generator_select_at_least_one_tag_message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedGeneratorTags.isNotEmpty()
            ) {
                Text(stringResource(id = R.string.combo_generator_generate_combo_button))
            }

            Spacer(modifier = Modifier.height(4.dp))

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

            if (currentGeneratedMoves.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        moveViewModel.saveCombo("", currentGeneratedMoves)
                        scope.launch {
                            snackbarHostState.showSnackbar(message = context.getString(R.string.combo_generator_combo_saved_snackbar))
                        }
                        // generatedComboText = context.getString(R.string.combo_generator_combo_saved_snackbar) // Optional: Update text field as well
                        // currentGeneratedMoves = emptyList() // Optionally clear after saving
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Save, contentDescription = stringResource(id = R.string.combo_generator_save_combo_icon_description), modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.combo_generator_save_combo_button))
                }
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
