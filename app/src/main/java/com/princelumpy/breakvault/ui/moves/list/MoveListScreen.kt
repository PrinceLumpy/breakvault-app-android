// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.ui.moves.list

import AppStyleDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.TagColor
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import com.princelumpy.breakvault.ui.common.FlexibleItemList
import com.princelumpy.breakvault.ui.common.TagColorStrip
import com.princelumpy.breakvault.ui.common.TagFilterRow
import com.princelumpy.breakvault.ui.common.stripColors
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
        onClearFilters = viewModel::clearFilters,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchOpen = viewModel::onSearchOpen,
        onSearchClose = viewModel::onSearchClose
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
    onClearFilters: () -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onSearchOpen: () -> Unit = {},
    onSearchClose: () -> Unit = {}
) {
    // System back closes search before it leaves the screen.
    BackHandler(enabled = uiState.isSearchActive, onBack = onSearchClose)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (uiState.isSearchActive) {
                        MoveSearchField(
                            query = uiState.searchQuery,
                            onQueryChange = onSearchQueryChange
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.move_list_screen_title),
                        )
                    }
                },
                navigationIcon = {
                    if (uiState.isSearchActive) {
                        IconButton(onClick = onSearchClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.move_list_close_search_description)
                            )
                        }
                    } else {
                        IconButton(onClick = { onOpenDrawer() }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(id = R.string.drawer_content_description)
                            )
                        }
                    }
                },
                actions = {
                    if (!uiState.isSearchActive && uiState.hasAnyMoves) {
                        IconButton(onClick = onSearchOpen) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(id = R.string.move_list_search_placeholder)
                            )
                        }
                    }
                    // Manage Tags Button
                    IconButton(onClick = onNavigateToMoveTagList) {
                        Icon(
                            Icons.AutoMirrored.Filled.Label,
                            contentDescription = stringResource(id = R.string.move_list_manage_tags_button)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.hasAnyMoves) {
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

                if (!uiState.hasAnyMoves) {
                    EmptyMovesState(onAddMove = { onNavigateToAddEditMove(null) })
                } else if (uiState.moveList.isEmpty()) {
                    NoMatchingMovesState()
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

/**
 * Search box shown in place of the top bar title while search is open. Grabs focus (and the
 * keyboard) when it appears.
 */
@Composable
fun MoveSearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = { Text(stringResource(id = R.string.move_list_search_placeholder)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.move_list_clear_search_description)
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@Composable
fun NoMatchingMovesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppStyleDefaults.SpacingLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.move_list_no_matches_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            TagColorStrip(colors = moveWithTags.moveTags.stripColors())
            Row(
                modifier = Modifier
                    .weight(1f)
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
                    MoveTag(id = "t1", name = "Fast", color = TagColor.RED),
                    MoveTag(id = "t2", name = "Setup", color = TagColor.BLUE)
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