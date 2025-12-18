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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.BattleTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleTagListScreen(
    navController: NavController,
    battleTagListViewModel: BattleTagListViewModel = viewModel()
) {
    val uiState by battleTagListViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.battle_tag_list_title)) },
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
            FloatingActionButton(onClick = { battleTagListViewModel.onAddTagClicked() }) {
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
            if (uiState.tags.isEmpty() && !uiState.showAddDialog) {
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
                        onEditClick = { battleTagListViewModel.onEditTagClicked(it) },
                        onDeleteClick = { battleTagListViewModel.onDeleteTagClicked(it) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = { battleTagListViewModel.onAddTagDialogDismiss() },
            title = { Text(stringResource(id = R.string.battle_tag_list_add_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = uiState.newTagName,
                    onValueChange = { battleTagListViewModel.onNewTagNameChange(it) },
                    label = { Text(stringResource(id = R.string.battle_tag_list_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { battleTagListViewModel.onAddTag() })
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { battleTagListViewModel.onAddTag() },
                    enabled = uiState.newTagName.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { battleTagListViewModel.onAddTagDialogDismiss() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    uiState.showEditDialog?.let { tagToEdit ->
        AlertDialog(
            onDismissRequest = { battleTagListViewModel.onEditTagDialogDismiss() },
            title = { Text(stringResource(id = R.string.battle_tag_list_edit_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = uiState.tagNameForEdit,
                    onValueChange = { battleTagListViewModel.onTagNameForEditChange(it) },
                    label = { Text(stringResource(id = R.string.battle_tag_list_new_tag_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { battleTagListViewModel.onUpdateTag() })
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { battleTagListViewModel.onUpdateTag() },
                    enabled = uiState.tagNameForEdit.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { battleTagListViewModel.onEditTagDialogDismiss() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }

    uiState.showDeleteDialog?.let { tagToDelete ->
        AlertDialog(
            onDismissRequest = { battleTagListViewModel.onDeleteTagDialogDismiss() },
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
                    onClick = { battleTagListViewModel.onDeleteTag() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { battleTagListViewModel.onDeleteTagDialogDismiss() }) {
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
                IconButton(onClick = { onEditClick(tag) }) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.battle_tag_list_edit_icon_desc)
                    )
                }
                IconButton(onClick = { onDeleteClick(tag) }) {
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
