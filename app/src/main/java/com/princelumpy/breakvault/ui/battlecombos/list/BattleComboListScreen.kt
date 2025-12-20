package com.princelumpy.breakvault.ui.battlecombos.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.EnergyLevel
import com.princelumpy.breakvault.data.local.entity.TrainingStatus

@Composable
fun BattleComboListScreen(
    onNavigateUp: () -> Unit,
    onNavigateToAddEditBattleCombo: (String?) -> Unit,
    onNavigateToBattleTagList: () -> Unit,
    battleComboListViewModel: BattleComboListViewModel = hiltViewModel()
) {
    val uiState by battleComboListViewModel.uiState.collectAsState()

    BattleComboListContent(
        uiState = uiState,
        onSortOptionChange = battleComboListViewModel::onSortOptionChange,
        onTagSelected = battleComboListViewModel::onTagSelected,
        onShowResetDialog = battleComboListViewModel::onShowResetDialog,
        onConfirmReset = battleComboListViewModel::onConfirmReset,
        onCancelReset = battleComboListViewModel::onCancelReset,
        onToggleUsed = battleComboListViewModel::toggleUsed,
        onNavigateToAddEditBattleCombo = onNavigateToAddEditBattleCombo,
        onNavigateToBattleTagList = onNavigateToBattleTagList
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleComboListContent(
    uiState: BattleComboListUiState,
    onSortOptionChange: (BattleSortOption) -> Unit,
    onTagSelected: (String) -> Unit,
    onShowResetDialog: () -> Unit,
    onConfirmReset: () -> Unit,
    onCancelReset: () -> Unit,
    onToggleUsed: (BattleCombo) -> Unit,
    onNavigateToAddEditBattleCombo: (String?) -> Unit,
    onNavigateToBattleTagList: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battle Combos") },
                actions = {
                    // Manage Tags Button
                    IconButton(onClick = onNavigateToBattleTagList) {
                        Icon(
                            Icons.AutoMirrored.Filled.Label,
                            contentDescription = "Manage Battle Tags"
                        )
                    }
                    // Sort Button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Energy: High -> Low") },
                                onClick = {
                                    onSortOptionChange(BattleSortOption.EnergyHighToLow)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Energy: Low -> High") },
                                onClick = {
                                    onSortOptionChange(BattleSortOption.EnergyLowToHigh)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Status: Battle Ready") },
                                onClick = {
                                    onSortOptionChange(BattleSortOption.StatusFireFirst)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Status: Training") },
                                onClick = {
                                    onSortOptionChange(BattleSortOption.StatusHammerFirst)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                    // Clean Slate Button
                    IconButton(onClick = onShowResetDialog) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Clean Slate")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.allCombos.isNotEmpty()) {
                FloatingActionButton(onClick = { onNavigateToAddEditBattleCombo(null) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Battle Combo")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tip: Hold to edit (Only show if there are combos)
            if (uiState.allCombos.isNotEmpty()) {
                Text(
                    text = "Tip: Hold to edit, Tap to mark used",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            // MoveTag Filter Row
            if (uiState.allTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.allTags, key = { it.name }) { tag ->
                        FilterChip(
                            selected = uiState.selectedTagNames.contains(tag.name),
                            onClick = { onTagSelected(tag.name) },
                            label = { Text(tag.name) }
                        )
                    }
                }
            }

            if (uiState.filteredAndSortedCombos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Check if the base list of combos is empty
                    if (uiState.allCombos.isEmpty()) {
                        Text(
                            text = "You have no battle combos.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add battle-ready combos to track them during sessions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onNavigateToAddEditBattleCombo(null) }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Add Battle Combo")
                        }
                    } else {
                        Text(
                            text = "No combos match your filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        uiState.filteredAndSortedCombos,
                        key = { it.battleCombo.id }) { comboWithTags ->
                        BattleComboItem(
                            comboWithTags = comboWithTags,
                            onClick = { onToggleUsed(comboWithTags.battleCombo) },
                            onEditClick = {
                                onNavigateToAddEditBattleCombo(comboWithTags.battleCombo.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = onCancelReset,
            title = { Text("Clean Slate?") },
            text = { Text("This will set all combos to unused. \nAre you ready?") },
            confirmButton = {
                TextButton(onClick = onConfirmReset) {
                    Text("Clean Slate")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelReset) {
                    Text("Cancel")
                }
            }
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BattleComboItem(
    comboWithTags: BattleComboWithTags,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val combo = comboWithTags.battleCombo
    val tags = comboWithTags.tags
    val isUsed = combo.isUsed

    val energyColor = when (combo.energy) {
        EnergyLevel.LOW -> Color(0xFF4CAF50) // Green
        EnergyLevel.MEDIUM -> Color(0xFFFFC107) // Amber
        EnergyLevel.HIGH -> Color(0xFFF44336) // Red
        EnergyLevel.NONE -> Color.Gray
    }

    val statusIcon = when (combo.status) {
        TrainingStatus.READY -> "🔥"
        TrainingStatus.TRAINING -> "🔨"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onEditClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .graphicsLayer(alpha = if (isUsed) 0.5f else 1.0f)
        ) {
            // Energy Strip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(energyColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = combo.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (isUsed) TextDecoration.LineThrough else null
                    )

                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tags.joinToString(", ") { it.name },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Icon (Right Side)
                Text(
                    text = statusIcon,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, name = "Populated List")
@Composable
private fun BattleComboListContentPreview_Populated() {
    val fakeCombos = listOf(
        BattleComboWithTags(
            BattleCombo("1", "Jab, Cross, Hook", EnergyLevel.HIGH, TrainingStatus.READY, false),
            listOf(BattleTag("1","Boxing"), BattleTag("2","Power"))
        ),
        BattleComboWithTags(
            BattleCombo("2", "Low Kick, High Kick", EnergyLevel.MEDIUM, TrainingStatus.TRAINING, true),
            listOf(BattleTag("3","Kicking"))
        ),
        BattleComboWithTags(
            BattleCombo("3", "Spinning Backfist", EnergyLevel.LOW, TrainingStatus.READY, false),
            listOf()
        )
    )
    val fakeTags = listOf(BattleTag("1","Boxing"), BattleTag("3","Kicking"), BattleTag("2","Power"))

    val uiState = BattleComboListUiState(
        allCombos = fakeCombos,
        allTags = fakeTags,
        filteredAndSortedCombos = fakeCombos,
        selectedTagNames = setOf("Boxing")
    )

    MaterialTheme {
        BattleComboListContent(
            uiState = uiState,
            onSortOptionChange = {},
            onTagSelected = {},
            onShowResetDialog = {},
            onConfirmReset = {},
            onCancelReset = {},
            onToggleUsed = {},
            onNavigateToAddEditBattleCombo = {},
            onNavigateToBattleTagList = {}
        )
    }
}

@Preview(showBackground = true, name = "Initial Empty State")
@Composable
private fun BattleComboListContentPreview_Empty() {
    MaterialTheme {
        BattleComboListContent(
            uiState = BattleComboListUiState(), // Default empty state
            onSortOptionChange = {},
            onTagSelected = {},
            onShowResetDialog = {},
            onConfirmReset = {},
            onCancelReset = {},
            onToggleUsed = {},
            onNavigateToAddEditBattleCombo = {},
            onNavigateToBattleTagList = {}
        )
    }
}

@Preview(showBackground = true, name = "Filtered Empty State")
@Composable
private fun BattleComboListContentPreview_FilteredEmpty() {
    val fakeCombos = listOf(
        BattleComboWithTags(
            BattleCombo("1", "Jab, Cross", EnergyLevel.LOW, TrainingStatus.READY, false),
            listOf(BattleTag("1","Boxing"))
        )
    )
    val fakeTags = listOf(BattleTag("1","Boxing"), BattleTag("3","Kicking"))

    val uiState = BattleComboListUiState(
        allCombos = fakeCombos,
        allTags = fakeTags,
        filteredAndSortedCombos = emptyList(), // No combos match the filter
        selectedTagNames = setOf("Kicking") // "Kicking" is selected, but combo is "Boxing"
    )

    MaterialTheme {
        BattleComboListContent(
            uiState = uiState,
            onSortOptionChange = {},
            onTagSelected = {},
            onShowResetDialog = {},
            onConfirmReset = {},
            onCancelReset = {},
            onToggleUsed = {},
            onNavigateToAddEditBattleCombo = {},
            onNavigateToBattleTagList = {}
        )
    }
}
