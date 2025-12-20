package com.princelumpy.breakvault.ui.moves.managetags

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveTagListScreen(
    onNavigateToMovesByTag: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    moveTagListViewModel: MoveTagListViewModel = hiltViewModel()
) {
    // UPDATED: Use collectAsStateWithLifecycle for better lifecycle management.
    val uiState by moveTagListViewModel.uiState.collectAsStateWithLifecycle()

    MoveTagListContent(
        uiState = uiState,
        onNavigateToMovesByTag = onNavigateToMovesByTag,
        onNavigateBack = onNavigateBack,
        onAddTagClicked = moveTagListViewModel::onAddTagClicked,
        onAddTagDialogDismiss = moveTagListViewModel::onAddTagDialogDismiss,
        onNewTagNameChange = moveTagListViewModel::onNewTagNameChange,
        onAddTag = moveTagListViewModel::onAddTag,
        onEditTagClicked = moveTagListViewModel::onEditTagClicked,
        onEditTagDialogDismiss = moveTagListViewModel::onEditTagDialogDismiss,
        onTagNameForEditChange = moveTagListViewModel::onTagNameForEditChange,
        onUpdateTag = moveTagListViewModel::onUpdateTag,
        onDeleteTagClicked = moveTagListViewModel::onDeleteTagClicked,
        onDeleteTagDialogDismiss = moveTagListViewModel::onDeleteTagDialogDismiss,
        onDeleteTag = moveTagListViewModel::onDeleteTag
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveTagListContent(
    uiState: MoveTagListUiState,
    onNavigateToMovesByTag: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    onAddTagClicked: () -> Unit,
    onAddTagDialogDismiss: () -> Unit,
    onNewTagNameChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onEditTagClicked: (MoveTag) -> Unit,
    onEditTagDialogDismiss: () -> Unit,
    onTagNameForEditChange: (String) -> Unit,
    onUpdateTag: () -> Unit,
    onDeleteTagClicked: (MoveTag) -> Unit,
    onDeleteTagDialogDismiss: () -> Unit,
    onDeleteTag: () -> Unit
) {
    // Create convenience variables for cleaner access
    val userInputs = uiState.userInputs
    val dialogState = uiState.dialogState

    Scaffold(
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
            FloatingActionButton(onClick = onAddTagClicked) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.tag_list_add_tag_fab_description)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(AppStyleDefaults.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // UPDATED: Check dialogState for visibility
            if (uiState.tags.isEmpty() && !dialogState.showAddDialog) {
                Text(stringResource(id = R.string.tag_list_no_tags_message))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                items(
                    items = uiState.tags,
                    key = { it.id }
                ) { tag ->
                    TagListItem(
                        moveTag = tag,
                        onItemClick = {
                            onNavigateToMovesByTag(it.id, it.name)
                        },
                        onEditClick = { onEditTagClicked(it) },
                        onDeleteClick = { onDeleteTagClicked(it) }
                    )
                }
            }
        }
    }

    // UPDATED: Use dialogState.showAddDialog
    if (dialogState.showAddDialog) {
        AlertDialog(
            onDismissRequest = onAddTagDialogDismiss,
            title = { Text(stringResource(id = R.string.tag_list_add_new_tag_dialog_title)) },
            text = {
                OutlinedTextField(
                    // UPDATED: Use userInputs.newTagName
                    value = userInputs.newTagName,
                    onValueChange = onNewTagNameChange,
                    label = { Text(stringResource(id = R.string.tag_list_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onAddTag,
                    // UPDATED: Use userInputs.newTagName
                    enabled = userInputs.newTagName.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_add))
                }
            },
            dismissButton = {
                TextButton(onClick = onAddTagDialogDismiss) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    // UPDATED: Use dialogState.tagForEditDialog
    dialogState.tagForEditDialog?.let { tagToEdit ->
        AlertDialog(
            onDismissRequest = onEditTagDialogDismiss,
            title = { Text(stringResource(id = R.string.tag_list_edit_tag_name_dialog_title)) },
            text = {
                OutlinedTextField(
                    // UPDATED: Use userInputs.tagNameForEdit
                    value = userInputs.tagNameForEdit,
                    onValueChange = onTagNameForEditChange,
                    label = { Text(stringResource(id = R.string.tag_list_new_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onUpdateTag,
                    // UPDATED: Use userInputs.tagNameForEdit
                    enabled = userInputs.tagNameForEdit.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onEditTagDialogDismiss) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    // UPDATED: Use dialogState.tagForDeleteDialog
    dialogState.tagForDeleteDialog?.let { tagToDelete ->
        AlertDialog(
            onDismissRequest = onDeleteTagDialogDismiss,
            title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
            text = {
                Text(
                    stringResource(
                        id = R.string.tag_list_delete_confirmation_message,
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
}

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
                        contentDescription = stringResource(id = R.string.tag_list_edit_tag_description)
                    )
                }
                IconButton(onClick = { onDeleteClick(moveTag) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.tag_list_delete_tag_description),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Preview can be updated to use the new UiState structure for better accuracy
@Preview(showBackground = true)
@Composable
fun MoveTagListScreenPreview() {
    ComboGeneratorTheme {
        MoveTagListContent(
            uiState = MoveTagListUiState(
                tags = listOf(
                    MoveTag(id = "1", name = "Beginner"),
                    MoveTag(id = "2", name = "Power"),
                    MoveTag(id = "3", name = "Freezes")
                )
            ),
            onNavigateToMovesByTag = { _, _ -> },
            onNavigateBack = {},
            onAddTagClicked = {},
            onAddTagDialogDismiss = {},
            onNewTagNameChange = {},
            onAddTag = {},
            onEditTagClicked = {},
            onEditTagDialogDismiss = {},
            onTagNameForEditChange = {},
            onUpdateTag = {},
            onDeleteTagClicked = {},
            onDeleteTagDialogDismiss = {},
            onDeleteTag = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TagListItemPreview() {
    ComboGeneratorTheme {
        TagListItem(
            moveTag = MoveTag(id = "1", name = "Beginner"),
            onItemClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
