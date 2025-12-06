package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.MoveListTag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FlashcardScreen(
    navController: NavController,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val allTags by moveViewModel.allTags.observeAsState(initial = emptyList())
    var selectedMoveListTags by remember { mutableStateOf(setOf<MoveListTag>()) }
    var currentFlashcardMove by remember { mutableStateOf<Move?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.flashcard_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(id = R.string.flashcard_select_tags_prompt), style = MaterialTheme.typography.titleMedium)

            if (allTags.isEmpty()) {
                Text(stringResource(id = R.string.flashcard_no_tags_available_message), style = MaterialTheme.typography.bodySmall)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allTags.forEach { tag ->
                        FilterChip(
                            selected = selectedMoveListTags.contains(tag),
                            onClick = {
                                selectedMoveListTags = if (selectedMoveListTags.contains(tag)) {
                                    selectedMoveListTags - tag
                                } else {
                                    selectedMoveListTags + tag
                                }
                            },
                            label = { Text(tag.name) },
                            leadingIcon = if (selectedMoveListTags.contains(tag)) {
                                { Icon(Icons.Filled.Done, contentDescription = stringResource(id = R.string.add_edit_move_selected_chip_description), modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else { null }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    currentFlashcardMove = moveViewModel.getFlashcardMove(selectedMoveListTags)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMoveListTags.isNotEmpty()
            ) {
                Text(stringResource(id = R.string.flashcard_next_move_button))
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (currentFlashcardMove != null) {
                Flashcard(move = currentFlashcardMove!!)
            } else {
                Text(stringResource(id = R.string.flashcard_initial_prompt), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun Flashcard(move: Move) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(id = R.string.flashcard_prompt, move.name), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlashcardScreenPreview() {
    ComboGeneratorTheme {
        FlashcardScreen(navController = rememberNavController(), moveViewModel = FakeMoveViewModel())
    }
}

@Preview
@Composable
fun FlashcardPreview() {
    ComboGeneratorTheme {
        Flashcard(move = Move(id = "1", name = "6-Step"))
    }
}
