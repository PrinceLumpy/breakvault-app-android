package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.BattleTag
import com.princelumpy.breakvault.viewmodel.BattleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleTagListScreen(
    navController: NavController,
    battleViewModel: BattleViewModel = viewModel()
) {
    val tagsList by battleViewModel.allBattleTags.observeAsState(initial = emptyList())
    var showEditDialog by remember { mutableStateOf<BattleTag?>(null) }
    var showDeleteDialog by remember { mutableStateOf<BattleTag?>(null) }
    var tagNameForEdit by remember { mutableStateOf("") }

    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.battle_tag_list_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.common_back_button_description))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTagDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.battle_tag_list_add_fab_description))
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
                Text(stringResource(id = R.string.battle_tag_list_no_tags_message))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = tagsList,
                    key = { it.id }
                ) { tag ->
                    BattleTagListItem(
                        tag = tag,
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
            title = { Text(stringResource(id = R.string.battle_tag_list_add_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { 
                        if (it.length <= 30) newTagName = it 
                    },
                    label = { Text(stringResource(id = R.string.battle_tag_list_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                         if (newTagName.isNotBlank()) {
                            battleViewModel.addBattleTag(newTagName)
                            showAddTagDialog = false
                            newTagName = ""
                        }
                    })
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            battleViewModel.addBattleTag(newTagName)
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
            title = { Text(stringResource(id = R.string.battle_tag_list_edit_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = tagNameForEdit,
                    onValueChange = { 
                        if (it.length <= 30) tagNameForEdit = it 
                    },
                    label = { Text(stringResource(id = R.string.battle_tag_list_new_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (tagNameForEdit.isNotBlank() && tagNameForEdit != tagToEdit.name) {
                            battleViewModel.updateBattleTag(tagToEdit.copy(name = tagNameForEdit))
                        }
                        showEditDialog = null
                        tagNameForEdit = ""
                    })
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tagNameForEdit.isNotBlank() && tagNameForEdit != tagToEdit.name) {
                            battleViewModel.updateBattleTag(tagToEdit.copy(name = tagNameForEdit))
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
            text = { Text(stringResource(id = R.string.battle_tag_list_delete_confirmation, tagToDelete.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        battleViewModel.deleteBattleTag(tagToDelete)
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
fun BattleTagListItem(
    tag: BattleTag,
    onEditClick: (BattleTag) -> Unit,
    onDeleteClick: (BattleTag) -> Unit
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
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(id = R.string.battle_tag_list_edit_icon_desc))
                }
                IconButton(onClick = { onDeleteClick(tag) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.battle_tag_list_delete_icon_desc), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
