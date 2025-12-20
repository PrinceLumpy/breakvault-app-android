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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

// STATEFUL COMPOSABLE
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

    MoveListContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateToAddEditMove = onNavigateToAddEditMove,
        onNavigateToComboGenerator = onNavigateToComboGenerator,
        onToggleTagFilter = viewModel::toggleTagFilter,
        onDeleteMoveClick = viewModel::onDeleteMoveClick,
        onConfirmMoveDelete = viewModel::onConfirmMoveDelete,
        onCancelMoveDelete = viewModel::onCancelMoveDelete
    )
}

// STATELESS COMPOSABLE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveListContent(
    uiState: MoveListUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateToAddEditMove: (String?) -> Unit,
    onNavigateToComboGenerator: () -> Unit,
    onToggleTagFilter: (String) -> Unit,
    onDeleteMoveClick: (MoveWithTags) -> Unit,
    onConfirmMoveDelete: () -> Unit,
    onCancelMoveDelete: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
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
            GenerateComboButton(onClick = onNavigateToComboGenerator)

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

            if (uiState.allTags.isNotEmpty()) {
                TagFilterRow(
                    tags = uiState.allTags,
                    selectedTagNames = uiState.selectedTagNames,
                    onToggleTag = onToggleTagFilter
                )
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

            if (uiState.moveList.isEmpty()) {
                EmptyMovesState(onAddMove = { onNavigateToAddEditMove(null) })
            } else {
                MovesList(
                    moves = uiState.moveList,
                    onEditClick = onNavigateToAddEditMove,
                    onDeleteClick = onDeleteMoveClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    uiState.moveToDelete?.let { moveToDelete ->
        DeleteMoveDialog(
            moveName = moveToDelete.move.name,
            onConfirm = onConfirmMoveDelete,
            onDismiss = onCancelMoveDelete
        )
    }
}

@Composable
fun GenerateComboButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppStyleDefaults.SpacingLarge)
    ) {
        Text(stringResource(id = R.string.move_list_generate_combo_button))
    }
}

@Composable
fun TagFilterRow(
    tags: List<MoveTag>,
    selectedTagNames: Set<String>,
    onToggleTag: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AppStyleDefaults.SpacingLarge),
        horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
    ) {
        items(tags) { tag ->
            FilterChip(
                selected = selectedTagNames.contains(tag.name),
                onClick = { onToggleTag(tag.name) },
                label = { Text(tag.name) }
            )
        }
    }
}

@Composable
fun EmptyMovesState(onAddMove: () -> Unit) {
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
        Button(onClick = onAddMove) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
            Text(stringResource(id = R.string.move_list_add_move_button))
        }
    }
}

@Composable
fun MovesList(
    moves: List<MoveWithTags>,
    onEditClick: (String) -> Unit,
    onDeleteClick: (MoveWithTags) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = AppStyleDefaults.SpacingLarge),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        items(moves, key = { it.move.id }) { moveWithTags ->
            MoveCard(
                moveWithTags = moveWithTags,
                onEditClick = { onEditClick(moveWithTags.move.id) },
                onDeleteClick = { onDeleteClick(moveWithTags) }
            )
        }
    }
}

@Composable
fun MoveCard(
    moveWithTags: MoveWithTags,
    onEditClick: () -> Unit,
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
            MoveCardContent(moveWithTags = moveWithTags, modifier = Modifier.weight(1f))
            MoveCardActions(
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
fun MoveCardContent(moveWithTags: MoveWithTags, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
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
}

@Composable
fun MoveCardActions(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.padding(start = AppStyleDefaults.SpacingMedium)
    ) {
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(id = R.string.move_card_edit_button),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.move_card_delete_move_description),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun DeleteMoveDialog(
    moveName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.common_confirm_deletion_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.move_list_delete_confirmation_message,
                    moveName
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

// PREVIEWS
@Preview(showBackground = true)
@Composable
fun PreviewGenerateComboButton() {
    BreakVaultTheme {
        GenerateComboButton(onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTagFilterRow() {
    BreakVaultTheme {
        TagFilterRow(
            tags = listOf(
                MoveTag(id = "1", name = "Punch"),
                MoveTag(id = "2", name = "Kick"),
                MoveTag(id = "3", name = "Defense")
            ),
            selectedTagNames = setOf("Punch"),
            onToggleTag = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyMovesState() {
    BreakVaultTheme {
        EmptyMovesState(onAddMove = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMoveCard() {
    BreakVaultTheme {
        MoveCard(
            moveWithTags = MoveWithTags(
                move = Move(id = "1", name = "Jab"),
                moveTags = listOf(
                    MoveTag(id = "t1", name = "Fast"),
                    MoveTag(id = "t2", name = "Setup")
                )
            ),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMoveCardNoTags() {
    BreakVaultTheme {
        MoveCard(
            moveWithTags = MoveWithTags(
                move = Move(id = "1", name = "Cross"),
                moveTags = emptyList()
            ),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDeleteMoveDialog() {
    BreakVaultTheme {
        DeleteMoveDialog(
            moveName = "Jab",
            onConfirm = {},
            onDismiss = {}
        )
    }
}