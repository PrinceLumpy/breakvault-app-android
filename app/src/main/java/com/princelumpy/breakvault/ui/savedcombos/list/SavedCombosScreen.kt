package com.princelumpy.breakvault.ui.savedcombos.list

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCombosScreen(
    onNavigateToAddEditCombo: (String?) -> Unit,
    savedComboListViewModel: SavedComboListViewModel = hiltViewModel()
) {
    val uiState by savedComboListViewModel.uiState.collectAsState()

    SavedCombosContent(
        uiState = uiState,
        onNavigateToAddEditCombo = onNavigateToAddEditCombo,
        onShowDeleteDialog = savedComboListViewModel::onShowDeleteDialog,
        onCancelDelete = savedComboListViewModel::onCancelDelete,
        onConfirmDelete = savedComboListViewModel::onConfirmDelete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCombosContent(
    uiState: SavedCombosUiState,
    onNavigateToAddEditCombo: (String?) -> Unit,
    onShowDeleteDialog: (SavedCombo) -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.saved_combos_screen_title)) })
        },
        floatingActionButton = {
            if (uiState.savedCombos.isNotEmpty()) {
                FloatingActionButton(onClick = { onNavigateToAddEditCombo(null) }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.create_combo_fab_description)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (uiState.savedCombos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.saved_combos_no_combos_message),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(id = R.string.saved_combos_empty_state_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onNavigateToAddEditCombo(null) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(id = R.string.create_combo_button_text))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    uiState.savedCombos,
                    key = { it.id }
                ) { savedCombo ->
                    SavedComboItem(
                        savedCombo = savedCombo,
                        onEditClick = {
                            onNavigateToAddEditCombo(savedCombo.id)
                        },
                        onDeleteClick = { onShowDeleteDialog(savedCombo) }
                    )
                }
            }
        }
    }

    uiState.comboToDelete?.let { combo ->
        AlertDialog(
            onDismissRequest = { onCancelDelete() },
            title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
            text = {
                Text(
                    stringResource(
                        id = R.string.saved_combos_delete_confirmation_message,
                        combo.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onConfirmDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onCancelDelete() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun SavedComboItem(
    savedCombo: SavedCombo,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = savedCombo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = savedCombo.moves.joinToString(separator = " -> "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.edit_combo_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.saved_combos_delete_combo_description),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedCombosScreenPreview() {
    val previewUiState = SavedCombosUiState(
        savedCombos = listOf(
            SavedCombo(id = "1", name = "Windmill Freeze", moves = listOf("Windmill", "Baby Freeze")),
            SavedCombo(id = "2", name = "Flare Swipe", moves = listOf("Flare", "Swipe", "Elbow Freeze")),
        )
    )

    ComboGeneratorTheme {
        SavedCombosContent(
            uiState = previewUiState,
            onNavigateToAddEditCombo = {},
            onShowDeleteDialog = {},
            onCancelDelete = {},
            onConfirmDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SavedComboItemPreview() {
    ComboGeneratorTheme {
        val sampleCombo = SavedCombo(
            id = "preview1",
            name = "Awesome Combo",
            moves = listOf("Jab", "Cross", "Hook")
        )
        SavedComboItem(
            savedCombo = sampleCombo,
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
