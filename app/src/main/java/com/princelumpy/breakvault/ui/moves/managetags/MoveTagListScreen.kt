package com.princelumpy.breakvault.ui.moves.managetags

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.ui.common.GenericItemList
import com.princelumpy.breakvault.ui.common.TagDialog
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun MoveTagListScreen(
    onNavigateBack: () -> Unit,
    moveTagListViewModel: MoveTagListViewModel = hiltViewModel()
) {
    val uiState by moveTagListViewModel.uiState.collectAsStateWithLifecycle()
    val userInputs = uiState.userInputs
    val dialogState = uiState.dialogState

    MoveTagListScaffold(
        tags = uiState.tags,
        onNavigateBack = onNavigateBack,
        onAddTagClicked = moveTagListViewModel::onAddTagClicked,
        onEditClick = moveTagListViewModel::onEditTagClicked,
        onDeleteClick = moveTagListViewModel::onDeleteTagClicked
    )

    if (dialogState.showAddDialog) {
        TagDialog(
            title = stringResource(id = R.string.tag_list_add_new_tag_dialog_title),
            labelText = stringResource(id = R.string.tag_list_tag_name_label),
            confirmButtonText = stringResource(id = R.string.common_add),
            tagName = userInputs.newTagName,
            isError = uiState.newTagNameError != null,
            errorMessage = uiState.newTagNameError,
            onTagNameChange = moveTagListViewModel::onNewTagNameChange,
            onDismiss = moveTagListViewModel::onAddTagDialogDismiss,
            onConfirm = moveTagListViewModel::onAddTag
        )
    }

    dialogState.tagForEditDialog?.let {
        TagDialog(
            title = stringResource(id = R.string.tag_list_edit_tag_name_dialog_title),
            labelText = stringResource(id = R.string.tag_list_new_tag_name_label),
            confirmButtonText = stringResource(id = R.string.common_save),
            tagName = userInputs.tagNameForEdit,
            isError = uiState.editTagNameError != null,
            errorMessage = uiState.editTagNameError,
            onTagNameChange = moveTagListViewModel::onTagNameForEditChange,
            onDismiss = moveTagListViewModel::onEditTagDialogDismiss,
            onConfirm = moveTagListViewModel::onUpdateTag
        )
    }

    dialogState.tagForDeleteDialog?.let { tagToDelete ->
        DeleteTagDialog(
            tagName = tagToDelete.name,
            onDismiss = moveTagListViewModel::onDeleteTagDialogDismiss,
            onConfirm = moveTagListViewModel::onDeleteTag
        )
    }
}


/**
 * A stateless scaffold that handles the overall layout for the Move Tag List screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveTagListScaffold(
    tags: List<MoveTag>,
    onNavigateBack: () -> Unit,
    onAddTagClicked: () -> Unit,
    onEditClick: (MoveTag) -> Unit,
    onDeleteClick: (MoveTag) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.tag_list_manage_tags_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                modifier = Modifier.imePadding(),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.tag_list_add_tag_fab_description)
                )
            }
        }
    ) { paddingValues ->
        if (tags.isEmpty()) {
            EmptyTagListState(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )
        } else {
            GenericItemList(
                items = tags,
                onItemClick = onEditClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                getItemKey = { it.id },
                getItemName = { it.name },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * A stateless composable for the empty state of the tag list.
 */
@Composable
private fun EmptyTagListState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = AppStyleDefaults.SpacingLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.tag_list_no_tags_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * A stateless dialog for confirming tag deletion.
 */
@Composable
private fun DeleteTagDialog(
    tagName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.tag_list_delete_confirmation_message,
                    tagName
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

@Preview(showBackground = true)
@Composable
private fun MoveTagListScaffold_WithTags_Preview() {
    BreakVaultTheme {
        MoveTagListScaffold(
            tags = listOf(
                MoveTag(id = "1", name = "Beginner"),
                MoveTag(id = "2", name = "Power"),
                MoveTag(id = "3", name = "Freezes")
            ),
            onNavigateBack = {},
            onAddTagClicked = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveTagListScaffold_NoTags_Preview() {
    BreakVaultTheme {
        MoveTagListScaffold(
            tags = emptyList(),
            onNavigateBack = {},
            onAddTagClicked = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview
@Composable
private fun AddTagDialogPreview() {
    BreakVaultTheme {
        TagDialog(
            title = "Add New Tag",
            labelText = "Tag Name",
            confirmButtonText = "Add",
            tagName = "Flow",
            onTagNameChange = {},
            onDismiss = {},
            onConfirm = {},
            isError = false,
            errorMessage = null
        )
    }
}

@Preview
@Composable
private fun EditTagDialogPreview() {
    BreakVaultTheme {
        TagDialog(
            title = "Edit Tag Name",
            labelText = "New Tag Name",
            confirmButtonText = "Save",
            tagName = "Old Name",
            onTagNameChange = {},
            onDismiss = {},
            onConfirm = {},
            isError = false,
            errorMessage = null
        )
    }
}

@Preview
@Composable
private fun DeleteTagDialogPreview() {
    BreakVaultTheme {
        DeleteTagDialog(
            tagName = "The Tag To Delete",
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyTagListStatePreview() {
    BreakVaultTheme {
        EmptyTagListState(modifier = Modifier.fillMaxSize())
    }
}
//endregion
