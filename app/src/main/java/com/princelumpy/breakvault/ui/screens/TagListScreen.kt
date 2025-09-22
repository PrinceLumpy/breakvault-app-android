package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Ensure this import is present if using the top-level items function; usually not needed for LazyListScope.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.princelumpy.breakvault.data.Tag
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.R
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagListScreen(
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val tagsList by moveViewModel.allTags.observeAsState(initial = emptyList())
    var showEditDialog by remember { mutableStateOf<Tag?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Tag?>(null) }
    var tagNameForEdit by remember { mutableStateOf("") }

    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.tag_list_manage_tags_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTagDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.tag_list_add_tag_fab_description))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (tagsList.isEmpty() && !showAddTagDialog) {
                Text(stringResource(id = R.string.tag_list_no_tags_message))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = tagsList, // Explicitly named 'items' parameter
                    key = { it.id } // 'it' here refers to each Tag in tagsList
                ) { tag -> // 'tag' is the item from tagsList
                    TagListItem(
                        tag = tag ,
                        onEditClick = { clickedTag -> // Renamed 'it' to 'clickedTag' for clarity
                            tagNameForEdit = clickedTag.name
                            showEditDialog = clickedTag
                        },
                        onDeleteClick = { clickedTag -> // Renamed 'it' to 'clickedTag' for clarity
                            showDeleteDialog = clickedTag
                        }
                    )
                }
            }
        }
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTagDialog = false
                newTagName = "" // Reset name on dismiss
            },
            title = { Text(stringResource(id = R.string.tag_list_add_new_tag_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text(stringResource(id = R.string.tag_list_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            moveViewModel.addTag(newTagName)
                            showAddTagDialog = false
                            newTagName = "" // Reset name after adding
                        }
                    },
                    enabled = newTagName.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_add))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddTagDialog = false
                    newTagName = "" // Reset name on cancel
                }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    showEditDialog?.let { tagToEdit ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text(stringResource(id = R.string.tag_list_edit_tag_name_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = tagNameForEdit,
                    onValueChange = { tagNameForEdit = it },
                    label = { Text(stringResource(id = R.string.tag_list_new_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tagNameForEdit.isNotBlank() && tagNameForEdit != tagToEdit.name) {
                            moveViewModel.updateTag(tagToEdit.id, tagNameForEdit)
                        }
                        showEditDialog = null
                        tagNameForEdit = ""
                    },
                    enabled = tagNameForEdit.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditDialog = null
                    tagNameForEdit = ""
                }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    showDeleteDialog?.let { tagToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
            text = { Text(stringResource(id = R.string.tag_list_delete_confirmation_message, tagToDelete.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        moveViewModel.deleteTag(tagToDelete)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun TagListItem(
    tag: Tag,
    onEditClick: (Tag) -> Unit,
    onDeleteClick: (Tag) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = { onEditClick(tag) }) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(id = R.string.tag_list_edit_tag_description))
                }
                IconButton(onClick = { onDeleteClick(tag) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.tag_list_delete_tag_description), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagListScreenPreview() {
    ComboGeneratorTheme {
        rememberNavController() // Added for preview consistency if NavController is used internally or by TopAppBar
        TagListScreen(moveViewModel = FakeMoveViewModel())
    }
}

@Preview(showBackground = true)
@Composable
fun TagListItemPreview() {
    ComboGeneratorTheme {
        TagListItem(tag = Tag("1", "Beginner"), onEditClick = {}, onDeleteClick = {})
    }
}
