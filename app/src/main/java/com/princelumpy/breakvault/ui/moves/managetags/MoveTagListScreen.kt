package com.princelumpy.breakvault.ui.moves.managetags

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun MoveTagListScreen(
    onNavigateToMovesByTag: (String, String) -> Unit,
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
        onTagClick = { onNavigateToMovesByTag(it.id, it.name) },
        onEditClick = moveTagListViewModel::onEditTagClicked,
        onDeleteClick = moveTagListViewModel::onDeleteTagClicked
    )

    if (dialogState.showAddDialog) {
        AddTagDialog(
            newTagName = userInputs.newTagName,
            onNewTagNameChange = moveTagListViewModel::onNewTagNameChange,
            onDismiss = moveTagListViewModel::onAddTagDialogDismiss,
            onConfirm = moveTagListViewModel::onAddTag
        )
    }

    dialogState.tagForEditDialog?.let {
        EditTagDialog(
            tagNameForEdit = userInputs.tagNameForEdit,
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
    onTagClick: (MoveTag) -> Unit,
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
            TagList(
                tags = tags,
                onTagClick = onTagClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
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
 * A stateless list of tags.
 */
@Composable
private fun TagList(
    tags: List<MoveTag>,
    onTagClick: (MoveTag) -> Unit,
    onEditClick: (MoveTag) -> Unit,
    onDeleteClick: (MoveTag) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppStyleDefaults.SpacingLarge),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        items(
            items = tags,
            key = { it.id }
        ) { tag ->
            TagListItem(
                moveTag = tag,
                onItemClick = onTagClick,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

/**
 * A stateless item for a single tag in the list.
 */
@Composable
fun TagListItem(
    moveTag: MoveTag,
    onItemClick: (MoveTag) -> Unit,
    onEditClick: (MoveTag) -> Unit,
    onDeleteClick: (MoveTag) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(moveTag) },
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppStyleDefaults.SpacingLarge,
                    vertical = AppStyleDefaults.SpacingMedium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = moveTag.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = { onEditClick(moveTag) }) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.tag_list_edit_tag_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDeleteClick(moveTag) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.tag_list_delete_tag_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


/**
 * A stateless dialog for adding a new tag.
 */
@Composable
private fun AddTagDialog(
    newTagName: String,
    onNewTagNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.tag_list_add_new_tag_dialog_title)) },
        text = {
            OutlinedTextField(
                value = newTagName,
                onValueChange = onNewTagNameChange,
                label = { Text(stringResource(id = R.string.tag_list_tag_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = newTagName.isNotBlank()
            ) {
                Text(stringResource(id = R.string.common_add))
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
 * A stateless dialog for editing a tag name.
 */
@Composable
private fun EditTagDialog(
    tagNameForEdit: String,
    onTagNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.tag_list_edit_tag_name_dialog_title)) },
        text = {
            OutlinedTextField(
                value = tagNameForEdit,
                onValueChange = onTagNameChange,
                label = { Text(stringResource(id = R.string.tag_list_new_tag_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = tagNameForEdit.isNotBlank()
            ) {
                Text(stringResource(id = R.string.common_save))
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
            onTagClick = {},
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
            onTagClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TagListItemPreview() {
    BreakVaultTheme {
        TagListItem(
            moveTag = MoveTag(id = "1", name = "Footwork"),
            onItemClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview
@Composable
private fun AddTagDialogPreview() {
    BreakVaultTheme {
        AddTagDialog(
            newTagName = "Flow",
            onNewTagNameChange = {},
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview
@Composable
private fun EditTagDialogPreview() {
    BreakVaultTheme {
        EditTagDialog(
            tagNameForEdit = "Old Name",
            onTagNameChange = {},
            onDismiss = {},
            onConfirm = {}
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
