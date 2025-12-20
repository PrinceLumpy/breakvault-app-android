package com.princelumpy.breakvault.ui.savedcombos.list

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun SavedComboListScreen(
    onNavigateToAddEditCombo: (String?) -> Unit,
    savedComboListViewModel: SavedComboListViewModel = hiltViewModel()
) {
    val uiState by savedComboListViewModel.uiState.collectAsStateWithLifecycle()
    val dialogState = uiState.dialogState

    SavedComboListScaffold(
        savedCombos = uiState.savedCombos,
        onNavigateToAddEditCombo = onNavigateToAddEditCombo,
        onEditClick = { onNavigateToAddEditCombo(it.id) },
        onDeleteClick = savedComboListViewModel::onShowDeleteDialog
    )

    dialogState.comboToDelete?.let { combo ->
        DeleteComboDialog(
            comboName = combo.name,
            onDismiss = savedComboListViewModel::onCancelDelete,
            onConfirm = savedComboListViewModel::onConfirmDelete
        )
    }
}

/**
 * A stateless scaffold that handles the overall layout for the Saved Combo List screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedComboListScaffold(
    savedCombos: List<SavedCombo>,
    onNavigateToAddEditCombo: (String?) -> Unit,
    onEditClick: (SavedCombo) -> Unit,
    onDeleteClick: (SavedCombo) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.saved_combos_screen_title)) })
        },
        floatingActionButton = {
            if (savedCombos.isNotEmpty()) {
                FloatingActionButton(onClick = { onNavigateToAddEditCombo(null) }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.create_combo_fab_description)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (savedCombos.isEmpty()) {
            EmptyState(
                onNavigateToAddEditCombo = { onNavigateToAddEditCombo(null) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            ComboList(
                savedCombos = savedCombos,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

/**
 * A stateless list of saved combos.
 */
@Composable
private fun ComboList(
    savedCombos: List<SavedCombo>,
    onEditClick: (SavedCombo) -> Unit,
    onDeleteClick: (SavedCombo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = AppStyleDefaults.LazyListPadding,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        items(
            savedCombos,
            key = { it.id }
        ) { savedCombo ->
            SavedComboItem(
                savedCombo = savedCombo,
                onEditClick = { onEditClick(savedCombo) },
                onDeleteClick = { onDeleteClick(savedCombo) }
            )
        }
    }
}

/**
 * A stateless composable for the empty state of the combo list.
 */
@Composable
private fun EmptyState(
    onNavigateToAddEditCombo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AppStyleDefaults.SpacingLarge),
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
            modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
        )
        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))
        Button(onClick = onNavigateToAddEditCombo) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
            Text(stringResource(id = R.string.create_combo_button_text))
        }
    }
}

/**
 * A stateless dialog for confirming combo deletion.
 */
@Composable
private fun DeleteComboDialog(
    comboName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.saved_combos_delete_confirmation_message,
                    comboName
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(id = R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}

/**
 * A stateless item for a single saved combo in the list.
 */
@Composable
fun SavedComboItem(
    savedCombo: SavedCombo,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = AppStyleDefaults.SpacingLarge,
                    vertical = AppStyleDefaults.SpacingMedium
                )
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
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                Text(
                    text = savedCombo.moves.joinToString(separator = " -> "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = AppStyleDefaults.SpacingMedium)
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

// Previews

@Preview(showBackground = true)
@Composable
private fun SavedComboListScaffold_WithCombos_Preview() {
    val previewCombos = listOf(
        SavedCombo(
            id = "1",
            name = "Windmill Freeze",
            moves = listOf("Windmill", "Baby Freeze")
        ),
        SavedCombo(
            id = "2",
            name = "Flare Swipe",
            moves = listOf("Flare", "Swipe", "Elbow Freeze")
        ),
    )

    BreakVaultTheme {
        SavedComboListScaffold(
            savedCombos = previewCombos,
            onNavigateToAddEditCombo = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedComboListScaffold_Empty_Preview() {
    BreakVaultTheme {
        SavedComboListScaffold(
            savedCombos = emptyList(),
            onNavigateToAddEditCombo = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedComboItemPreview() {
    BreakVaultTheme {
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

@Preview
@Composable
private fun DeleteComboDialogPreview() {
    BreakVaultTheme {
        DeleteComboDialog(
            comboName = "My Awesome Combo",
            onDismiss = { },
            onConfirm = { }
        )
    }
}
