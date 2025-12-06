package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.Screen
import com.princelumpy.breakvault.data.MoveListTag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagListScreen(
    navController: NavController,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val tagsList by moveViewModel.allTags.observeAsState(initial = emptyList())
    var showEditDialog by remember { mutableStateOf<MoveListTag?>(null) }
    var showDeleteDialog by remember { mutableStateOf<MoveListTag?>(null) }
    var tagNameForEdit by remember { mutableStateOf("") }

    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.tag_list_manage_tags_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.common_back_button_description))
                    }
                }
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
                    items = tagsList,
                    key = { it.id }
                ) { tag ->
                    TagListItem(
                        moveListTag = tag,
                        onItemClick = {
                            navController.navigate(Screen.MovesByTag.withArgs(it.id, it.name))
                        },
                        onEditClick = {
                            tagNameForEdit = it.name
                            showEditDialog = it
                        },
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }
            }
        }
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false; newTagName = "" },
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
                            newTagName = ""
                        }
                    },
                    enabled = newTagName.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false; newTagName = "" }) {
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
                TextButton(onClick = { showEditDialog = null; tagNameForEdit = "" }) {
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
    moveListTag: MoveListTag,
    onItemClick: (MoveListTag) -> Unit,
    onEditClick: (MoveListTag) -> Unit,
    onDeleteClick: (MoveListTag) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(moveListTag) },
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
                text = moveListTag.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = { onEditClick(moveListTag) }) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(id = R.string.tag_list_edit_tag_description))
                }
                IconButton(onClick = { onDeleteClick(moveListTag) }) {
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
        TagListScreen(navController = rememberNavController(), moveViewModel = FakeMoveViewModel())
    }
}

@Preview(showBackground = true)
@Composable
fun TagListItemPreview() {
    ComboGeneratorTheme {
        TagListItem(
            moveListTag = MoveListTag("1", "Beginner"),
            onItemClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
