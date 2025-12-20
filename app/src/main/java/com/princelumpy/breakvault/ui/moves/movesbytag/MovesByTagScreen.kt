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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesByTagScreen(
    onNavigateUp: () -> Unit,
    onNavigateToMove: (String) -> Unit,
    // The ViewModel is now injected by Hilt and gets its own arguments.
    movesByTagViewModel: MovesByTagViewModel = hiltViewModel()
) {
    // UPDATED: Use collectAsStateWithLifecycle for better lifecycle management.
    val uiState by movesByTagViewModel.uiState.collectAsStateWithLifecycle()

    // REMOVED: The LaunchedEffect is no longer needed as the ViewModel is fully reactive.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.moves_for_tag_title, uiState.tagName)) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.moves.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_moves_for_tag_message))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
                contentPadding = AppStyleDefaults.LazyListPadding // Using the consistent padding value
            ) {
                items(uiState.moves, key = { it.id }) { move ->
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
    }
}
