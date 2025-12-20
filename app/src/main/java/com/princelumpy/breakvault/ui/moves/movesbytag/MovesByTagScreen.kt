package com.princelumpy.breakvault.ui.moves.movesbytag

import AppStyleDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

/**
 * The main, stateful screen composable that holds the ViewModel and state.
 */
@Composable
fun MovesByTagScreen(
    onNavigateUp: () -> Unit,
    onNavigateToMove: (String) -> Unit,
    movesByTagViewModel: MovesByTagViewModel = hiltViewModel()
) {
    val uiState by movesByTagViewModel.uiState.collectAsStateWithLifecycle()

    MovesByTagScaffold(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onNavigateToMove = onNavigateToMove
    )
}

/**
 * A stateless scaffold that handles the overall layout for the MovesByTag screen.
 */
@Composable
private fun MovesByTagScaffold(
    uiState: MovesByTagUiState,
    onNavigateUp: () -> Unit,
    onNavigateToMove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MovesByTagTopBar(
                tagName = uiState.tagName,
                onNavigateUp = onNavigateUp
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
            uiState.moves.isEmpty() -> {
                EmptyMovesState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                MovesListByTag(
                    moves = uiState.moves,
                    onNavigateToMove = onNavigateToMove,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

/**
 * A stateless top bar for the MovesByTag screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovesByTagTopBar(
    tagName: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(stringResource(R.string.moves_for_tag_title, tagName)) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back_button_description)
                )
            }
        }
    )
}

/**
 * A stateless list of moves for a specific tag.
 */
@Composable
private fun MovesListByTag(
    moves: List<Move>,
    onNavigateToMove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
        contentPadding = AppStyleDefaults.LazyListPadding
    ) {
        items(moves, key = { it.id }) { move ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMove(move.id) }
            ) {
                Text(
                    text = move.name,
                    modifier = Modifier.padding(AppStyleDefaults.SpacingLarge)
                )
            }
        }
    }
}

/**
 * A stateless composable for the loading state.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * A stateless composable for the empty moves state.
 */
@Composable
private fun EmptyMovesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = AppStyleDefaults.SpacingLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_moves_for_tag_message),
            textAlign = TextAlign.Center
        )
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun MovesByTagScaffold_WithMoves_Preview() {
    val dummyMoves = listOf(
        Move(id = "1", name = "Windmill"),
        Move(id = "2", name = "Flare"),
        Move(id = "3", name = "Airflare")
    )
    BreakVaultTheme {
        MovesByTagScaffold(
            uiState = MovesByTagUiState(
                moves = dummyMoves,
                tagName = "Power",
                isLoading = false
            ),
            onNavigateUp = {},
            onNavigateToMove = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovesByTagScaffold_NoMoves_Preview() {
    BreakVaultTheme {
        MovesByTagScaffold(
            uiState = MovesByTagUiState(
                moves = emptyList(),
                tagName = "Empty Tag",
                isLoading = false
            ),
            onNavigateUp = {},
            onNavigateToMove = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovesByTagScaffold_Loading_Preview() {
    BreakVaultTheme {
        MovesByTagScaffold(
            uiState = MovesByTagUiState(isLoading = true),
            onNavigateUp = {},
            onNavigateToMove = {}
        )
    }
}

//endregion
