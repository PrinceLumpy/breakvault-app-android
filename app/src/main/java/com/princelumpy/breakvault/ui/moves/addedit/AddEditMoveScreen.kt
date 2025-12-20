package com.princelumpy.breakvault.ui.moves.addedit


import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun AddEditMoveScreen(
    onNavigateUp: () -> Unit,
    moveId: String?,
    moveViewModel: AddEditMoveViewModel = hiltViewModel()
) {
    val uiState by moveViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = moveId) {
        moveViewModel.loadMove(moveId)
    }

    LaunchedEffect(uiState.dialogState.snackbarMessage) {
        uiState.dialogState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            moveViewModel.onSnackbarMessageShown()
        }
    }

    AddEditMoveScaffold(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateUp = onNavigateUp,
        onMoveNameChange = { moveViewModel.onMoveNameChange(it) },
        onTagSelected = { moveViewModel.onTagSelected(it) },
        onNewTagNameChange = { moveViewModel.onNewTagNameChange(it) },
        onAddTag = { moveViewModel.addTag() },
        onSaveMove = {
            moveViewModel.saveMove {
                focusManager.clearFocus()
                onNavigateUp()
            }
        }
    )
}

/**
 * A stateless scaffold that handles the overall layout for the Add/Edit Move screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMoveScaffold(
    uiState: AddEditMoveUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateUp: () -> Unit,
    onMoveNameChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onSaveMove: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AddEditMoveTopBar(
                isNewMove = uiState.isNewMove,
                onNavigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSaveMove) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.add_edit_move_save_move_fab_description)
                )
            }
        }
    ) { paddingValues ->
        AddEditMoveContent(
            modifier = Modifier
                .padding(paddingValues)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
            userInputs = uiState.userInputs,
            allTags = uiState.allTags,
            onMoveNameChange = onMoveNameChange,
            onTagSelected = onTagSelected,
            onNewTagNameChange = onNewTagNameChange,
            onAddTag = onAddTag,
            onSaveMove = onSaveMove
        )
    }
}

/**
 * A stateless top bar for the Add/Edit Move screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMoveTopBar(
    isNewMove: Boolean,
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                if (isNewMove) stringResource(id = R.string.add_edit_move_add_new_move_title)
                else stringResource(id = R.string.add_edit_move_edit_move_title)
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
}

/**
 * The main, stateless content of the screen containing the input form.
 */
@Composable
private fun AddEditMoveContent(
    modifier: Modifier = Modifier,
    userInputs: UserInputs,
    allTags: List<MoveTag>,
    onMoveNameChange: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onSaveMove: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(AppStyleDefaults.SpacingLarge)
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
    ) {
        OutlinedTextField(
            value = userInputs.moveName,
            onValueChange = onMoveNameChange,
            label = { Text(stringResource(id = R.string.add_edit_move_move_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSaveMove() })
        )

        MoveTagsSection(
            allTags = allTags,
            selectedTags = userInputs.selectedTags,
            onTagSelected = onTagSelected
        )

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))

        AddNewTagSection(
            newTagName = userInputs.newTagName,
            onNewTagNameChange = onNewTagNameChange,
            onAddTag = onAddTag
        )
    }
}

/**
 * A stateless section for displaying and selecting move tags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoveTagsSection(
    allTags: List<MoveTag>,
    selectedTags: Set<String>,
    onTagSelected: (String) -> Unit
) {
    Text(
        stringResource(id = R.string.add_edit_move_select_tags_label),
        style = MaterialTheme.typography.titleMedium
    )
    if (allTags.isEmpty()) {
        Text(
            stringResource(id = R.string.add_edit_move_no_tags_available_message),
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
        ) {
            allTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag.id)
                FilterChip(
                    selected = isSelected,
                    onClick = { onTagSelected(tag.id) },
                    label = { Text(tag.name) },
                    leadingIcon = if (isSelected) {
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
}

/**
 * A stateless section for adding a new tag.
 */
@Composable
private fun AddNewTagSection(
    newTagName: String,
    onNewTagNameChange: (String) -> Unit,
    onAddTag: () -> Unit
) {
    Text(
        stringResource(id = R.string.add_edit_move_add_new_tag_label),
        style = MaterialTheme.typography.titleMedium
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        OutlinedTextField(
            value = newTagName,
            onValueChange = onNewTagNameChange,
            label = { Text(stringResource(id = R.string.add_edit_move_new_tag_name_label)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAddTag() })
        )
        Button(onClick = onAddTag) {
            Text(stringResource(id = R.string.common_add))
        }
    }
}


//region Previews

@Preview(showBackground = true, name = "Add/Edit Content")
@Composable
private fun AddEditMoveContentPreview() {
    val dummyTags = listOf(
        MoveTag(id = "1", name = "Freezes"),
        MoveTag(id = "2", name = "Power"),
        MoveTag(id = "3", name = "Footwork")
    )
    BreakVaultTheme {
        AddEditMoveContent(
            userInputs = UserInputs(
                moveName = "Windmill",
                selectedTags = setOf("2")
            ),
            allTags = dummyTags,
            onMoveNameChange = {},
            onTagSelected = {},
            onNewTagNameChange = {},
            onAddTag = {},
            onSaveMove = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditMoveTopBar_AddPreview() {
    BreakVaultTheme {
        AddEditMoveTopBar(isNewMove = true, onNavigateUp = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditMoveTopBar_EditPreview() {
    BreakVaultTheme {
        AddEditMoveTopBar(isNewMove = false, onNavigateUp = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveTagsSection_WithTags_Preview() {
    val dummyTags = listOf(
        MoveTag(id = "1", name = "Freezes"),
        MoveTag(id = "2", name = "Power")
    )
    BreakVaultTheme {
        MoveTagsSection(allTags = dummyTags, selectedTags = setOf("1"), onTagSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveTagsSection_NoTags_Preview() {
    BreakVaultTheme {
        MoveTagsSection(allTags = emptyList(), selectedTags = emptySet(), onTagSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AddNewTagSectionPreview() {
    BreakVaultTheme {
        AddNewTagSection(newTagName = "New Style", onNewTagNameChange = {}, onAddTag = {})
    }
}

//endregion
