package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.BattleComboWithTags
import com.princelumpy.breakvault.data.EnergyLevel
import com.princelumpy.breakvault.data.TrainingStatus
import com.princelumpy.breakvault.viewmodel.BattleViewModel
import com.princelumpy.breakvault.Screen

enum class BattleSortOption {
    EnergyHighToLow,
    EnergyLowToHigh,
    StatusFireFirst,
    StatusHammerFirst
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleScreen(
    navController: NavController,
    battleViewModel: BattleViewModel = viewModel()
) {
    val battleCombos by battleViewModel.battleCombos.observeAsState(initial = emptyList())
    val allTags by battleViewModel.allBattleTags.observeAsState(initial = emptyList())
    
    // Filter State
    val selectedTags = remember { mutableStateListOf<String>() }
    
    // Sort State
    var sortOption by remember { mutableStateOf(BattleSortOption.EnergyHighToLow) }
    var showSortMenu by remember { mutableStateOf(false) }

    var showResetConfirm by remember { mutableStateOf(false) }

    // Filter & Sort Logic
    val filteredAndSortedCombos = remember(battleCombos, selectedTags.toList(), sortOption) {
        val filtered = if (selectedTags.isEmpty()) {
            battleCombos
        } else {
            battleCombos.filter { comboWithTags ->
                // OR Logic: Show combo if it has ANY of the selected moveListTags
                comboWithTags.tags.any { it.name in selectedTags }
            }
        }

        when (sortOption) {
            BattleSortOption.EnergyHighToLow -> filtered.sortedWith(
                compareByDescending<BattleComboWithTags> { it.battleCombo.energy }.thenBy { it.battleCombo.status }
            )
            BattleSortOption.EnergyLowToHigh -> filtered.sortedWith(
                compareBy<BattleComboWithTags> { it.battleCombo.energy }.thenBy { it.battleCombo.status }
            )
            BattleSortOption.StatusFireFirst -> filtered.sortedWith(
                compareBy<BattleComboWithTags> { it.battleCombo.status }.thenByDescending { it.battleCombo.energy }
            )
            BattleSortOption.StatusHammerFirst -> filtered.sortedWith(
                compareByDescending<BattleComboWithTags> { it.battleCombo.status }.thenByDescending { it.battleCombo.energy }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battle Arena") },
                actions = {
                    // Manage Tags Button
                    IconButton(onClick = { navController.navigate(Screen.BattleTagList.route) }) {
                        Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "Manage Battle Tags")
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
                                onClick = { sortOption = BattleSortOption.EnergyHighToLow; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Energy: Low -> High") },
                                onClick = { sortOption = BattleSortOption.EnergyLowToHigh; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Status: Battle Ready") },
                                onClick = { sortOption = BattleSortOption.StatusFireFirst; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Status: Training") },
                                onClick = { sortOption = BattleSortOption.StatusHammerFirst; showSortMenu = false }
                            )
                        }
                    }
                    // Clean Slate Button
                    IconButton(onClick = { showResetConfirm = true }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Clean Slate")
                    }
                }
            )
        },
        floatingActionButton = {
            if (battleCombos.isNotEmpty()) {
                FloatingActionButton(onClick = { 
                    navController.navigate(Screen.AddEditBattleCombo.route)
                }) {
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
            if (battleCombos.isNotEmpty()) {
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

            // MoveListTag Filter Row
            if (allTags.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allTags) { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag.name),
                            onClick = {
                                if (selectedTags.contains(tag.name)) {
                                    selectedTags.remove(tag.name)
                                } else {
                                    selectedTags.add(tag.name)
                                }
                            },
                            label = { Text(tag.name) }
                        )
                    }
                }
            }

            if (filteredAndSortedCombos.isEmpty()) {
                 Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (battleCombos.isEmpty()) {
                        Text(
                            text = "The arena is empty.",
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
                        Button(onClick = { navController.navigate(Screen.AddEditBattleCombo.route) }) {
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
                    items(filteredAndSortedCombos, key = { it.battleCombo.id }) { comboWithTags ->
                        BattleComboItem(
                            comboWithTags = comboWithTags,
                            onClick = { battleViewModel.toggleUsed(comboWithTags.battleCombo) },
                            onEditClick = {
                                navController.navigate(Screen.AddEditBattleCombo.withOptionalArgs(mapOf("comboId" to comboWithTags.battleCombo.id)))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Clean Slate?") },
            text = { Text("This will un-cross all your used combos. Are you ready?") },
            confirmButton = {
                TextButton(onClick = {
                    battleViewModel.resetBattle()
                    showResetConfirm = false
                }) {
                    Text("Clean Slate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
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
        colors = CardDefaults.cardColors(
            containerColor = if (combo.isUsed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
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
                        textDecoration = if (combo.isUsed) TextDecoration.LineThrough else null,
                        color = if (combo.isUsed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
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
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
