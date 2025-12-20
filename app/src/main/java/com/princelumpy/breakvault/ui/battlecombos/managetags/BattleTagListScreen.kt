package com.princelumpy.breakvault.ui.battlecombos.managetags

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.BattleTag

// --- STATEFUL COMPOSABLE (The "Smart" one) ---

@Composable
fun BattleTagListScreen(
    onNavigateUp: () -> Unit,
    battleTagListViewModel: BattleTagListViewModel = hiltViewModel()
) {
    val uiState by battleTagListViewModel.uiState.collectAsStateWithLifecycle()

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
            FloatingActionButton(onClick = onAddTagClicked) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.battle_tag_list_add_fab_description)
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
            if (uiState.tags.isEmpty() && !dialogState.showAddDialog) {
                Text(stringResource(id = R.string.battle_tag_list_no_tags_message))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                items(
                    items = uiState.tags,
                    key = { it.id }
                ) { tag ->
                    BattleTagListItem(
                        tag = tag,
                        onEditClick = { onEditTagClicked(tag) },
                        onDeleteClick = { onDeleteTagClicked(tag) }
                    )
                }
            }
        }
    }

    if (dialogState.showAddDialog) {
        AlertDialog(
            onDismissRequest = onAddTagDialogDismiss,
            title = { Text(stringResource(id = R.string.battle_tag_list_add_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = userInputs.newTagName,
                    onValueChange = onNewTagNameChange,
                    label = { Text(stringResource(id = R.string.battle_tag_list_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddTag() })
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onAddTag,
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

    dialogState.tagForEditDialog?.let {
        AlertDialog(
            onDismissRequest = onEditTagDialogDismiss,
            title = { Text(stringResource(id = R.string.battle_tag_list_edit_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = userInputs.tagNameForEdit,
                    onValueChange = onTagNameForEditChange,
                    label = { Text(stringResource(id = R.string.battle_tag_list_new_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onUpdateTag() })
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onUpdateTag,
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

    dialogState.tagForDeleteDialog?.let { tagToDelete ->
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
}

@Composable
fun BattleTagListItem(
    tag: BattleTag,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = tag.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.battle_tag_list_edit_icon_desc)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.battle_tag_list_delete_icon_desc),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
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
