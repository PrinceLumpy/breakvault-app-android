package com.princelumpy.breakvault.ui.moves.list

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.princelumpy.breakvault.ui.common.FlexibleItemList
import com.princelumpy.breakvault.ui.common.TagFilterRow
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveListScreen(
    onNavigateToMoveTagList: () -> Unit = {},
    onNavigateToAddEditMove: (String?) -> Unit = {},
    onNavigateToComboGenerator: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    viewModel: MoveListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MoveListContent(
        uiState = uiState,
        onNavigateToMoveTagList = onNavigateToMoveTagList,
        onNavigateToAddEditMove = onNavigateToAddEditMove,
        onNavigateToComboGenerator = onNavigateToComboGenerator,
        onOpenDrawer = onOpenDrawer,
        onToggleTagFilter = viewModel::toggleTagFilter,
        onClearFilters = viewModel::clearFilters
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveListContent(
    uiState: MoveListUiState,
    onNavigateToMoveTagList: () -> Unit,
    onNavigateToAddEditMove: (String?) -> Unit,
    onNavigateToComboGenerator: () -> Unit,
    onOpenDrawer: () -> Unit,
    onToggleTagFilter: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.move_list_screen_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onOpenDrawer() }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.drawer_content_description)
                        )
                    }
                },
                actions = {
                    // Manage Tags Button
                    IconButton(onClick = onNavigateToMoveTagList) {
                        Icon(
                            Icons.AutoMirrored.Filled.Label,
                            contentDescription = "Manage Battle Tags"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.moveList.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onNavigateToAddEditMove(null) },
                    modifier = Modifier.imePadding(),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.move_list_add_move_button)
                    )
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {
                GenerateComboButton(onClick = onNavigateToComboGenerator)

                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))

                if (uiState.allTags.isNotEmpty()) {
                    TagFilterRow(
                        tags = uiState.allTags,
                        selectedTagNames = uiState.selectedTagNames,
                        onTagSelected = onToggleTagFilter,
                        getTagName = { it.name },
                        onClearFilters = onClearFilters
                    )
                }

                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

                if (uiState.moveList.isEmpty()) {
                    EmptyMovesState(onAddMove = { onNavigateToAddEditMove(null) })
                } else {
                    MovesList(
                        moves = uiState.moveList,
                        onEditClick = onNavigateToAddEditMove,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
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
    modifier: Modifier = Modifier
) {
    FlexibleItemList(
        items = moves,
        getItemKey = { it.move.id },
        modifier = modifier
    ) { moveWithTags ->
        MoveCard(
            moveWithTags = moveWithTags,
            onEditClick = { onEditClick(moveWithTags.move.id) }
        )
    }
}

@Composable
fun MoveCard(
    moveWithTags: MoveWithTags,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall),
        onClick = onEditClick
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
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(id = R.string.move_card_edit_button),
                tint = MaterialTheme.colorScheme.primary
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
            onClearFilters = {},
            onTagSelected = {},
            getTagName = { it.name }
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
            onEditClick = {}
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
            onEditClick = {}
        )
    }
}