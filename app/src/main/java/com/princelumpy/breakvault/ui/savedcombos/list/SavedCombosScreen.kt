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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.Screen
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCombosScreen(
    navController: NavController,
    savedComboListViewModel: SavedComboListViewModel = hiltViewModel()
) {
    val uiState by savedComboListViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.saved_combos_screen_title)) })
        },
        floatingActionButton = {
            if (uiState.savedCombos.isNotEmpty()) {
                FloatingActionButton(onClick = { navController.navigate(Screen.AddEditCombo.route) }) {
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
                    text = "Create and save your favorite combos to access them quickly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate(Screen.AddEditCombo.route) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Create Combo")
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
                            navController.navigate(Screen.AddEditCombo.withOptionalArgs(mapOf("comboId" to savedCombo.id)))
                        },
                        onDeleteClick = { savedComboListViewModel.onShowDeleteDialog(savedCombo) }
                    )
                }
            }
        }
    }

    uiState.comboToDelete?.let { combo ->
        AlertDialog(
            onDismissRequest = { savedComboListViewModel.onCancelDelete() },
            title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
            text = {
                Text(
                    stringResource(
                        id = R.string.move_list_delete_confirmation_message,
                        combo.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { savedComboListViewModel.onConfirmDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { savedComboListViewModel.onCancelDelete() }) {
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
                        contentDescription = "Edit Combo",
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
            SavedCombo(id = "1", name = "Combo 1", moves = listOf("Move 1", "Move 2")),
            SavedCombo(id = "2", name = "Combo 2", moves = listOf("Move 3", "Move 4")),
        )
    )

    val fakeViewModel = object : SavedComboListViewModel(savedComboRepository = TODO()) {
        override val uiState: StateFlow<SavedCombosUiState> = MutableStateFlow(previewUiState)
        override fun onShowDeleteDialog(savedCombo: SavedCombo) {}
        override fun onCancelDelete() {}
        override fun onConfirmDelete() {}
    }

    ComboGeneratorTheme {
        SavedCombosScreen(
            navController = rememberNavController(),
            savedComboListViewModel = fakeViewModel
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
