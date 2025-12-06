package com.princelumpy.breakvault.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.MoveListTag
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesByTagScreen(
    navController: NavController,
    moveListTag: MoveListTag,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    var moves by remember { mutableStateOf<List<Move>>(emptyList()) }

    // This is a simple, one-off fetch. For more complex scenarios, 
    // a dedicated LiveData in the ViewModel would be better.
    LaunchedEffect(key1 = moveListTag.id) {
        moves = moveViewModel.getMovesForTag(moveListTag.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.moves_for_tag_title, moveListTag.name)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back_button_description))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (moves.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_moves_for_tag_message))
                    }
                }
            } else {
                items(moves) { move ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(text = move.name, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}
