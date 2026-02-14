package com.princelumpy.breakvault.ui.battlecombos.managetags

import AppStyleDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.common.Constants.BATTLE_TAG_CHARACTER_LIMIT
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.ui.common.GenericItemList
import com.princelumpy.breakvault.ui.common.TagDialog

@Composable
fun BattleTagListScreen(
    onNavigateUp: () -> Unit,
    battleTagListViewModel: BattleTagListViewModel = hiltViewModel()
) {
    val uiState by battleTagListViewModel.uiState.collectAsStateWithLifecycle()
    val userInputs = uiState.userInputs
    val dialogState = uiState.dialogState

    BattleTagListContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onAddTagClicked = battleTagListViewModel::onAddTagClicked,
        onEditTagClicked = battleTagListViewModel::onEditTagClicked,
        onDeleteTagClicked = battleTagListViewModel::onDeleteTagClicked,
        onNewTagNameChange = battleTagListViewModel::onNewTagNameChange,
        onTagNameForEditChange = battleTagListViewModel::onTagNameForEditChange,
        onAddTag = battleTagListViewModel::onAddTag,
        onUpdateTag = battleTagListViewModel::onUpdateTag,
        onDeleteTag = battleTagListViewModel::onDeleteTag,
        onAddTagDialogDismiss = battleTagListViewModel::onAddTagDialogDismiss,
        onEditTagDialogDismiss = battleTagListViewModel::onEditTagDialogDismiss,
        onDeleteTagDialogDismiss = battleTagListViewModel::onDeleteTagDialogDismiss
    )
}


// --- STATELESS COMPOSABLE (The "Dumb" UI) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleTagListContent(
    uiState: BattleTagListUiState,
    onNavigateUp: () -> Unit,
    onAddTagClicked: () -> Unit,
    onEditTagClicked: (BattleTag) -> Unit,
    onDeleteTagClicked: (BattleTag) -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onTagNameForEditChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onUpdateTag: () -> Unit,
    onDeleteTag: () -> Unit,
    onAddTagDialogDismiss: () -> Unit,
    onEditTagDialogDismiss: () -> Unit,
    onDeleteTagDialogDismiss: () -> Unit
) {
    // Create convenience variables for cleaner access
    val dialogState = uiState.dialogState
    val userInputs = uiState.userInputs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.battle_tag_list_title)) },
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
            FloatingActionButton(
                onClick = onAddTagClicked,
                modifier = Modifier.imePadding()
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.battle_tag_list_add_fab_description),
                )
            }
        }
    ) { paddingValues ->
        if (uiState.tags.isEmpty()) {
            EmptyTagListState(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )
        } else {
            GenericItemList(
                items = uiState.tags,
                onItemClick = onEditTagClicked,
                onEditClick = onEditTagClicked,
                onDeleteClick = onDeleteTagClicked,
                getItemKey = { it.id },
                getItemName = { it.name },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }

    if (dialogState.showAddDialog) {
        TagDialog(
            title = stringResource(id = R.string.tag_list_add_new_tag_dialog_title),
            labelText = stringResource(id = R.string.tag_list_tag_name_label),
            confirmButtonText = stringResource(id = R.string.common_add),
            tagName = userInputs.newTagName,
            characterLimit = BATTLE_TAG_CHARACTER_LIMIT,
            isError = uiState.newTagNameError != null,
            errorMessage = uiState.newTagNameError,
            onTagNameChange = onNewTagNameChange,
            onConfirm = onAddTag,
            onDismiss = onAddTagDialogDismiss
        )
    }

    dialogState.tagForEditDialog?.let {
        TagDialog(
            title = stringResource(id = R.string.tag_list_edit_tag_name_dialog_title),
            labelText = stringResource(id = R.string.tag_list_new_tag_name_label),
            confirmButtonText = stringResource(id = R.string.common_save),
            tagName = userInputs.tagNameForEdit,
            characterLimit = BATTLE_TAG_CHARACTER_LIMIT,
            isError = uiState.editTagNameError != null,
            errorMessage = uiState.editTagNameError,
            onTagNameChange = onTagNameForEditChange,
            onConfirm = onUpdateTag,
            onDismiss = onEditTagDialogDismiss
        )
    }

    dialogState.tagForDeleteDialog?.let { tagToDelete ->
        DeleteTagDialog(
            tagToDelete = tagToDelete,
            onDeleteTag = onDeleteTag,
            onDeleteTagDialogDismiss = onDeleteTagDialogDismiss
        )
    }
}

@Composable
private fun EmptyTagListState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = AppStyleDefaults.SpacingLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.battle_tag_list_no_tags_message),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// --- DIALOGS ---

@Composable
private fun DeleteTagDialog(
    tagToDelete: BattleTag,
    onDeleteTag: () -> Unit,
    onDeleteTagDialogDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeleteTagDialogDismiss,
        title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.battle_tag_list_delete_confirmation,
                    tagToDelete.name
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDeleteTag,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDeleteTagDialogDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}


// --- PREVIEWS ---

@Preview(showBackground = true, name = "Populated List")
@Composable
private fun BattleTagListContentPreview_Populated() {
    val tags = listOf(
        BattleTag(id = "1", name = "Boxing"),
        BattleTag(id = "2", name = "Kicking"),
        BattleTag(id = "3", name = "Power")
    )
    MaterialTheme {
        BattleTagListContent(
            uiState = BattleTagListUiState(tags = tags),
            onNavigateUp = {},
            onAddTagClicked = {},
            onEditTagClicked = {},
            onDeleteTagClicked = {},
            onNewTagNameChange = {},
            onTagNameForEditChange = {},
            onAddTag = {},
            onUpdateTag = {},
            onDeleteTag = {},
            onAddTagDialogDismiss = {},
            onEditTagDialogDismiss = {},
            onDeleteTagDialogDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty List")
@Composable
private fun BattleTagListContentPreview_Empty() {
    MaterialTheme {
        BattleTagListContent(
            uiState = BattleTagListUiState(tags = emptyList()),
            onNavigateUp = {},
            onAddTagClicked = {},
            onEditTagClicked = {},
            onDeleteTagClicked = {},
            onNewTagNameChange = {},
            onTagNameForEditChange = {},
            onAddTag = {},
            onUpdateTag = {},
            onDeleteTag = {},
            onAddTagDialogDismiss = {},
            onEditTagDialogDismiss = {},
            onDeleteTagDialogDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Add Dialog Open")
@Composable
private fun BattleTagListContentPreview_AddDialog() {
    MaterialTheme {
        BattleTagListContent(
            uiState = BattleTagListUiState(
                dialogState = DialogState(showAddDialog = true),
                userInputs = UserInputs(newTagName = "New Tag")
            ),
            onNavigateUp = {},
            onAddTagClicked = {},
            onEditTagClicked = {},
            onDeleteTagClicked = {},
            onNewTagNameChange = {},
            onTagNameForEditChange = {},
            onAddTag = {},
            onUpdateTag = {},
            onDeleteTag = {},
            onAddTagDialogDismiss = {},
            onEditTagDialogDismiss = {},
            onDeleteTagDialogDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Dialog Open")
@Composable
private fun BattleTagListContentPreview_EditDialog() {
    val tagToEdit = BattleTag(id = "1", name = "Boxing")
    MaterialTheme {
        BattleTagListContent(
            uiState = BattleTagListUiState(
                tags = listOf(tagToEdit),
                dialogState = DialogState(tagForEditDialog = tagToEdit),
                userInputs = UserInputs(tagNameForEdit = "Boxing Edit")
            ),
            onNavigateUp = {},
            onAddTagClicked = {},
            onEditTagClicked = {},
            onDeleteTagClicked = {},
            onNewTagNameChange = {},
            onTagNameForEditChange = {},
            onAddTag = {},
            onUpdateTag = {},
            onDeleteTag = {},
            onAddTagDialogDismiss = {},
            onEditTagDialogDismiss = {},
            onDeleteTagDialogDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Delete Dialog Open")
@Composable
private fun BattleTagListContentPreview_DeleteDialog() {
    val tagToDelete = BattleTag(id = "1", name = "Boxing")
    MaterialTheme {
        BattleTagListContent(
            uiState = BattleTagListUiState(
                tags = listOf(tagToDelete),
                dialogState = DialogState(tagForDeleteDialog = tagToDelete)
            ),
            onNavigateUp = {},
            onAddTagClicked = {},
            onEditTagClicked = {},
            onDeleteTagClicked = {},
            onNewTagNameChange = {},
            onTagNameForEditChange = {},
            onAddTag = {},
            onUpdateTag = {},
            onDeleteTag = {},
            onAddTagDialogDismiss = {},
            onEditTagDialogDismiss = {},
            onDeleteTagDialogDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTagDialogPreview() {
    MaterialTheme {
        TagDialog(
            title = "Add New Tag",
            labelText = "Tag Name",
            confirmButtonText = "Add",
            tagName = "New Tag",
            characterLimit = BATTLE_TAG_CHARACTER_LIMIT,
            isError = false,
            errorMessage = null,
            onTagNameChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Add Dialog With Error")
@Composable
private fun BattleTagListContentPreview_AddDialogError() {
    MaterialTheme {
        BattleTagListContent(
            uiState = BattleTagListUiState(
                dialogState = DialogState(showAddDialog = true),
                userInputs = UserInputs(newTagName = ""), // Empty input to trigger error state
                newTagNameError = "Tag name cannot be empty." // The error message from the ViewModel
            ),
            onNavigateUp = {},
            onAddTagClicked = {},
            onEditTagClicked = {},
            onDeleteTagClicked = {},
            onNewTagNameChange = {},
            onTagNameForEditChange = {},
            onAddTag = {},
            onUpdateTag = {},
            onDeleteTag = {},
            onAddTagDialogDismiss = {},
            onEditTagDialogDismiss = {},
            onDeleteTagDialogDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTagDialogPreview() {
    MaterialTheme {
        TagDialog(
            title = "Edit Tag Name",
            labelText = "New Tag Name",
            confirmButtonText = "Save",
            tagName = "Existing Tag",
            characterLimit = BATTLE_TAG_CHARACTER_LIMIT,
            isError = false,
            errorMessage = null,
            onTagNameChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Dialog With Error")
@Composable
private fun BattleTagListContentPreview_EditDialogError() {
    val tags = listOf(
        BattleTag(id = "1", name = "Boxing"),
        BattleTag(id = "2", name = "Kicking")
    )
    MaterialTheme {
        BattleTagListContent(
            uiState = BattleTagListUiState(
                tags = tags,
                dialogState = DialogState(tagForEditDialog = tags[1]), // Editing "Kicking"
                userInputs = UserInputs(tagNameForEdit = "Boxing"), // Trying to rename it to "Boxing"
                editTagNameError = "Tag 'Boxing' already exists." // The resulting error
            ),
            onNavigateUp = {},
            onAddTagClicked = {},
            onEditTagClicked = {},
            onDeleteTagClicked = {},
            onNewTagNameChange = {},
            onTagNameForEditChange = {},
            onAddTag = {},
            onUpdateTag = {},
            onDeleteTag = {},
            onAddTagDialogDismiss = {},
            onEditTagDialogDismiss = {},
            onDeleteTagDialogDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteTagDialogPreview() {
    MaterialTheme {
        DeleteTagDialog(
            tagToDelete = BattleTag(id = "1", name = "Power Moves"),
            onDeleteTag = {},
            onDeleteTagDialogDismiss = {}
        )
    }
}
