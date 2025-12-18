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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.Screen
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveTagListScreen(
    navController: NavController,
    moveTagListViewModel: MoveTagListViewModel = viewModel()
) {
    val uiState by moveTagListViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.tag_list_manage_tags_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { moveTagListViewModel.onAddTagClicked() }) {
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
            if (uiState.tags.isEmpty() && !uiState.showAddDialog) {
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
                            navController.navigate(Screen.MovesByTag.withArgs(it.id, it.name))
                        },
                        onEditClick = { moveTagListViewModel.onEditTagClicked(it) },
                        onDeleteClick = { moveTagListViewModel.onDeleteTagClicked(it) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = { moveTagListViewModel.onAddTagDialogDismiss() },
            title = { Text(stringResource(id = R.string.tag_list_add_new_tag_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = uiState.newTagName,
                    onValueChange = { moveTagListViewModel.onNewTagNameChange(it) },
                    label = { Text(stringResource(id = R.string.tag_list_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { moveTagListViewModel.onAddTag() },
                    enabled = uiState.newTagName.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { moveTagListViewModel.onAddTagDialogDismiss() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    uiState.showEditDialog?.let { tagToEdit ->
        AlertDialog(
            onDismissRequest = { moveTagListViewModel.onEditTagDialogDismiss() },
            title = { Text(stringResource(id = R.string.tag_list_edit_tag_name_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = uiState.tagNameForEdit,
                    onValueChange = { moveTagListViewModel.onTagNameForEditChange(it) },
                    label = { Text(stringResource(id = R.string.tag_list_new_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { moveTagListViewModel.onUpdateTag() },
                    enabled = uiState.tagNameForEdit.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { moveTagListViewModel.onEditTagDialogDismiss() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    uiState.showDeleteDialog?.let { tagToDelete ->
        AlertDialog(
            onDismissRequest = { moveTagListViewModel.onDeleteTagDialogDismiss() },
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
                    onClick = { moveTagListViewModel.onDeleteTag() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { moveTagListViewModel.onDeleteTagDialogDismiss() }) {
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

@Preview(showBackground = true)
@Composable
fun MoveTagListScreenPreview() {
    ComboGeneratorTheme {
        MoveTagListScreen(
            navController = rememberNavController(),
            moveTagListViewModel = viewModel()
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
