package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.SavedCombo
import com.princelumpy.breakvault.data.SavedComboWithMoves
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCombosScreen(
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val savedCombosList by moveViewModel.savedCombos.observeAsState(initial = emptyList())

    var showRenameDialog by remember { mutableStateOf(false) }
    var comboToRename by remember { mutableStateOf<SavedComboWithMoves?>(null) }
    var newComboNameText by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf<SavedComboWithMoves?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.saved_combos_screen_title)) }
            )
        }
    ) { paddingValues ->
        if (savedCombosList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.saved_combos_no_combos_message))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    savedCombosList,
                    key = { savedComboWithMoves -> savedComboWithMoves.savedCombo.id }) { savedComboItem ->
                    SavedComboItem(
                        savedComboWithMoves = savedComboItem,
                        onItemClick = {
                            comboToRename = savedComboItem
                            newComboNameText = savedComboItem.savedCombo.name
                            showRenameDialog = true
                        },
                        onDeleteClick = { showDeleteDialog = savedComboItem }
                    )
                }
            }
        }
    }

    if (showRenameDialog && comboToRename != null) {
        RenameComboDialog(
            currentCombo = comboToRename!!, // Safe due to && condition
            currentName = newComboNameText,
            onNameChange = { newComboNameText = it },
            onDismiss = {
                showRenameDialog = false
                comboToRename = null
                newComboNameText = ""
            },
            onSave = {
                if (newComboNameText.isNotBlank() && newComboNameText != comboToRename!!.savedCombo.name) {
                    moveViewModel.updateSavedComboName(
                        comboToRename!!.savedCombo.id,
                        newComboNameText
                    )
                    // Optional: Snackbar for successful rename
                }
                showRenameDialog = false
                comboToRename = null
                newComboNameText = ""
            }
        )
    }

    showDeleteDialog?.let { comboToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
            text = { Text(stringResource(id = R.string.move_list_delete_confirmation_message, comboToDelete.savedCombo.name)) }, // Reusing string from MoveList, consider specific one for combos
            confirmButton = {
                TextButton(
                    onClick = {
                        moveViewModel.deleteSavedCombo(comboToDelete.savedCombo.id)
                        showDeleteDialog = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                // Consider creating a specific "Combo Deleted" string
                                message = context.getString(R.string.combo_generator_combo_saved_snackbar).replace("saved", "deleted") // Quick hack for now
                            )
                        }
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
fun SavedComboItem(
    savedComboWithMoves: SavedComboWithMoves,
    onItemClick: (SavedComboWithMoves) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(savedComboWithMoves) }, // Make the whole card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = savedComboWithMoves.savedCombo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = savedComboWithMoves.moves.joinToString(separator = " -> ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.saved_combos_delete_combo_description))
            }
        }
    }
}

@Composable
fun RenameComboDialog(
    currentCombo: SavedComboWithMoves,
    currentName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.saved_combos_rename_dialog_title)) },
        text = {
            OutlinedTextField(
                value = currentName,
                onValueChange = onNameChange,
                label = { Text(stringResource(id = R.string.saved_combos_rename_dialog_combo_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = currentName.isNotBlank() && currentName != currentCombo.savedCombo.name
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


@Preview(showBackground = true)
@Composable
fun SavedCombosScreenPreview() {
    ComboGeneratorTheme {
        val fakeViewModel = FakeMoveViewModel()
        SavedCombosScreen(moveViewModel = fakeViewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun SavedComboItemPreview() {
    ComboGeneratorTheme {
        val sampleCombo = SavedComboWithMoves(
            SavedCombo(id = "preview1", name = "Awesome Combo"),
            moves = listOf(
                Move(id = "m1", name = "Jab"),
                Move(id = "m2", name = "Cross"),
                Move(id = "m3", name = "Hook")
            )
        )
        SavedComboItem(
            savedComboWithMoves = sampleCombo,
            onItemClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview
@Composable
fun RenameComboDialogPreview() {
    ComboGeneratorTheme {
        RenameComboDialog(
            currentCombo = SavedComboWithMoves(
                SavedCombo(id = "preview1", name = "Old Name"),
                moves = emptyList()
            ),
            currentName = "Old Name",
            onNameChange = {},
            onDismiss = {},
            onSave = {}
        )
    }
}
