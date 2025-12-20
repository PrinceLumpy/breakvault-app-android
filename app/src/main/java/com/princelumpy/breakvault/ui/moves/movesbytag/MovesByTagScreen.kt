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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesByTagScreen(
    onNavigateUp: () -> Unit,
    onNavigateToMove: (String) -> Unit,
    tagId: String,
    tagName: String,
    movesByTagViewModel: MovesByTagViewModel = viewModel()
) {
    val uiState by movesByTagViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = tagId) {
        movesByTagViewModel.loadMovesByTag(tagId, tagName)
    }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
            contentPadding = PaddingValues(AppStyleDefaults.SpacingLarge)
        ) {
            if (uiState.moves.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_moves_for_tag_message))
                    }
                }
            } else {
                items(uiState.moves) { move ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMove(move.id) }) {
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
