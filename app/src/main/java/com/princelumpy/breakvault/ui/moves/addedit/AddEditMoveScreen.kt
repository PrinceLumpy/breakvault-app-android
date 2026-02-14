package com.princelumpy.breakvault.ui.moves.addedit


import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.ui.common.TagSelectionCard
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

// Constants for character limits (LAYER 1)
private const val MOVE_NAME_CHARACTER_LIMIT = 100
private const val MOVE_TAG_CHARACTER_LIMIT = 30

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

    LaunchedEffect(uiState.dialogsAndMessages.snackbarMessage) {
        uiState.dialogsAndMessages.snackbarMessage?.let {
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
        },
        onDeleteMoveClick = { moveViewModel.onDeleteMoveClick() },
        onConfirmMoveDelete = {
            moveViewModel.onConfirmMoveDelete {
                focusManager.clearFocus()
                onNavigateUp()
            }
        },
        onCancelMoveDelete = { moveViewModel.onCancelMoveDelete() }
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
    onSaveMove: () -> Unit,
    onDeleteMoveClick: () -> Unit,
    onConfirmMoveDelete: () -> Unit,
    onCancelMoveDelete: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AddEditMoveTopBar(
                isNewMove = uiState.isNewMove,
                onNavigateUp = onNavigateUp,
                onDeleteClick = onDeleteMoveClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSaveMove,
                modifier = Modifier.imePadding(),
                containerColor = if (uiState.userInputs.moveName.isNotBlank())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = stringResource(id = R.string.add_edit_move_save_move_fab_description)
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
            AddEditMoveContent(
                modifier = Modifier
                    .padding(paddingValues)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() },
                userInputs = uiState.userInputs,
                dialogsAndMessages = uiState.dialogsAndMessages,
                allTags = uiState.allTags,
                onMoveNameChange = onMoveNameChange,
                onTagSelected = onTagSelected,
                onNewTagNameChange = onNewTagNameChange,
                onAddTag = onAddTag,
                onSaveMove = onSaveMove
            )
        }
    }

    if (uiState.dialogsAndMessages.showDeleteDialog) {
        DeleteMoveDialog(
            moveName = uiState.userInputs.moveName,
            onConfirm = onConfirmMoveDelete,
            onDismiss = onCancelMoveDelete
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
    onNavigateUp: () -> Unit,
    onDeleteClick: () -> Unit
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
        },
        actions = {
            if (!isNewMove) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.move_card_delete_move_description),
                    )
                }
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
    dialogsAndMessages: UiDialogsAndMessages,
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
        // LAYER 1: Input Capping with Supporting Text Error Display
        OutlinedTextField(
            value = userInputs.moveName,
            onValueChange = { newText ->
                if (newText.length <= MOVE_NAME_CHARACTER_LIMIT) {
                    onMoveNameChange(newText)
                }
            },
            label = { Text(stringResource(id = R.string.add_edit_move_move_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = dialogsAndMessages.moveNameError != null,
            supportingText = {
                dialogsAndMessages.moveNameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSaveMove() })
        )

        Text(
            stringResource(id = R.string.add_edit_move_select_tags_label),
            style = MaterialTheme.typography.titleMedium
        )

        TagSelectionCard(
            allTags = allTags,
            selectedTags = userInputs.selectedTags,
            isLoading = false,
            emptyMessage = stringResource(id = R.string.add_edit_move_no_tags_available_message),
            onTagSelected = onTagSelected,
            getTagId = { it.id },
            getTagName = { it.name }
        )

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))

        AddNewTagSection(
            newTagName = userInputs.newTagName,
            newTagError = dialogsAndMessages.newTagError,
            onNewTagNameChange = onNewTagNameChange,
            onAddTag = onAddTag
        )
    }
}

/**
 * A stateless section for adding a new tag.
 */
@Composable
private fun AddNewTagSection(
    newTagName: String,
    newTagError: String?,
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
        // LAYER 1: Input Capping with Supporting Text Error Display
        OutlinedTextField(
            value = newTagName,
            onValueChange = { newText ->
                if (newText.length <= MOVE_TAG_CHARACTER_LIMIT) {
                    onNewTagNameChange(newText)
                }
            },
            label = { Text(stringResource(id = R.string.add_edit_move_new_tag_name_label)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            isError = newTagError != null,
            supportingText = {
                newTagError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAddTag() })
        )
        Button(onClick = onAddTag) {
            Text(stringResource(id = R.string.common_add))
        }
    }
}

/**
 * Dialog for confirming move deletion.
 */
@Composable
private fun DeleteMoveDialog(
    moveName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.move_list_delete_confirmation_message,
                    moveName
                )
            )
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
            dialogsAndMessages = UiDialogsAndMessages(),
            allTags = dummyTags,
            onMoveNameChange = {},
            onTagSelected = {},
            onNewTagNameChange = {},
            onAddTag = {},
            onSaveMove = {}
        )
    }
}

@Preview(showBackground = true, name = "Add/Edit Content with Errors")
@Composable
private fun AddEditMoveContentWithErrorsPreview() {
    val dummyTags = listOf(
        MoveTag(id = "1", name = "Freezes"),
        MoveTag(id = "2", name = "Power"),
        MoveTag(id = "3", name = "Footwork")
    )
    BreakVaultTheme {
        AddEditMoveContent(
            userInputs = UserInputs(
                moveName = "",
                newTagName = "Power",
                selectedTags = setOf("2")
            ),
            dialogsAndMessages = UiDialogsAndMessages(
                moveNameError = "Move name cannot be empty.",
                newTagError = "Tag 'Power' already exists."
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
        AddEditMoveTopBar(isNewMove = true, onNavigateUp = {}, onDeleteClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AddEditMoveTopBar_EditPreview() {
    BreakVaultTheme {
        AddEditMoveTopBar(isNewMove = false, onNavigateUp = {}, onDeleteClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteMoveDialogPreview() {
    BreakVaultTheme {
        DeleteMoveDialog(
            moveName = "Windmill",
            onConfirm = {},
            onDismiss = {}
        )
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
        TagSelectionCard(
            allTags = dummyTags,
            selectedTags = setOf("1"),
            isLoading = false,
            emptyMessage = "No tags available",
            onTagSelected = {},
            getTagId = { it.id },
            getTagName = { it.name }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveTagsSection_NoTags_Preview() {
    BreakVaultTheme {
        TagSelectionCard(
            allTags = emptyList<MoveTag>(),
            selectedTags = emptySet(),
            isLoading = false,
            emptyMessage = "No tags available",
            onTagSelected = {},
            getTagId = { it.id },
            getTagName = { it.name }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddNewTagSectionPreview() {
    BreakVaultTheme {
        AddNewTagSection(
            newTagName = "New Style",
            newTagError = null,
            onNewTagNameChange = {},
            onAddTag = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddNewTagSectionWithErrorPreview() {
    BreakVaultTheme {
        AddNewTagSection(
            newTagName = "Power",
            newTagError = "Tag 'Power' already exists.",
            onNewTagNameChange = {},
            onAddTag = {}
        )
    }
}

//endregion