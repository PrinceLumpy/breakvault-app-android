package com.princelumpy.breakvault.ui.moves.list

import AppStyleDefaults
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveListScreen(
    onNavigateToAddEditMove: (String?) -> Unit = {},
    onNavigateToComboGenerator: () -> Unit = {},
    onNavigateToTagList: () -> Unit = {},
    viewModel: MoveListViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.userMessage?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearMessage()
        }

    }

    Scaffold(
        floatingActionButton = {
            // Add Move FAB
            // Only show the FAB if there are moves in the list
            if (uiState.moveList.isNotEmpty()) {
                FloatingActionButton(onClick = { onNavigateToAddEditMove(null) }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.move_list_add_move_button)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Generate Combo Button
            Button(
                onClick = { onNavigateToComboGenerator() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppStyleDefaults.SpacingLarge)
            ) {
                Text(stringResource(id = R.string.move_list_generate_combo_button))
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

            // Show list of all tags for filtering
            if (uiState.allTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AppStyleDefaults.SpacingLarge),
                    horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
                ) {
                    items(uiState.allTags) { tag ->
                        FilterChip(
                            selected = uiState.selectedTagNames.contains(tag.name),
                            onClick = { viewModel.toggleTagFilter(tag.name) },
                            label = { Text(tag.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

            // Show message and add move button if no moves
            if (uiState.moveList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.move_list_no_moves_message),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(id = R.string.move_list_no_moves_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
                    )
                    Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))
                    Button(onClick = { onNavigateToAddEditMove(null) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
                        Text(stringResource(id = R.string.move_list_add_move_button))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = AppStyleDefaults.SpacingLarge),
                    verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
                ) {
                    items(uiState.moveList, key = { it.move.id }) { moveWithTags ->
                        MoveCard(
                            moveWithTags = moveWithTags,
                            onEditClick = { moveId ->
                                onNavigateToAddEditMove(moveId)
                            },
                            onDeleteClick = { viewModel.onDeleteMoveClick(moveWithTags) }
                        )
                    }
                }
            }
        }
    }

    uiState.moveToDelete?.let { moveWithTagsToDelete ->
        AlertDialog(
            onDismissRequest = { viewModel.onCancelMoveDelete() },
            title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
            text = {
                Text(
                    stringResource(
                        id = R.string.move_list_delete_confirmation_message,
                        moveWithTagsToDelete.move.name
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onConfirmMoveDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(id = R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelMoveDelete() }) {
                    Text(stringResource(id = R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun MoveCard(
    moveWithTags: MoveWithTags,
    onEditClick: (String) -> Unit,
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
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = moveWithTags.move.name, style = MaterialTheme.typography.titleMedium)
                if (moveWithTags.moveTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                    Text(
                        text = moveWithTags.moveTags.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = AppStyleDefaults.SpacingMedium)
            ) {
                IconButton(onClick = { onEditClick(moveWithTags.move.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.move_card_edit_button),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDeleteClick() }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.move_card_delete_move_description),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoveListScreenPreview() {
    ComboGeneratorTheme {
        MoveListScreen(viewModel = viewModel())
    }
}

@Preview(showBackground = true)
@Composable
fun MoveCardPreview() {
    ComboGeneratorTheme {
        val previewMove = Move(id = "prev1", name = "Preview Jab")
        val previewTags =
            listOf(MoveTag(id = "t1", name = "Fast"), MoveTag(id = "t2", name = "Setup"))
        val moveWithTags = MoveWithTags(move = previewMove, moveTags = previewTags)
        MoveCard(
            moveWithTags = moveWithTags,
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
