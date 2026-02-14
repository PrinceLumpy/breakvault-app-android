package com.princelumpy.breakvault.ui.battlecombos.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.EnergyLevel
import com.princelumpy.breakvault.data.local.entity.TrainingStatus
import com.princelumpy.breakvault.ui.common.FlexibleItemList
import com.princelumpy.breakvault.ui.common.TagFilterRow

@Composable
fun BattleComboListScreen(
    onNavigateToAddEditBattleCombo: (String?) -> Unit,
    onNavigateToBattleTagList: () -> Unit,
    onOpenDrawer: () -> Unit,
    battleComboListViewModel: BattleComboListViewModel = hiltViewModel()
) {
    val uiState by battleComboListViewModel.uiState.collectAsStateWithLifecycle()

    BattleComboListContent(
        uiState = uiState,
        onSortOptionChange = battleComboListViewModel::onSortOptionChange,
        onTagSelected = battleComboListViewModel::onTagSelected,
        onShowResetDialog = battleComboListViewModel::onShowResetDialog,
        onConfirmReset = battleComboListViewModel::onConfirmReset,
        onCancelReset = battleComboListViewModel::onCancelReset,
        onToggleUsed = battleComboListViewModel::toggleUsed,
        onNavigateToAddEditBattleCombo = onNavigateToAddEditBattleCombo,
        onNavigateToBattleTagList = onNavigateToBattleTagList,
        onOpenDrawer = onOpenDrawer
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
    onNavigateToBattleTagList: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.battle_combos_screen_title),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onOpenDrawer() }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(id = R.string.drawer_content_description)
                        )
                    }
                },
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
                    // Reset Button
                    IconButton(onClick = onShowResetDialog) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset all to unused")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEditBattleCombo(null) },
                modifier = Modifier.imePadding()
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Battle Combo")
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
                if (uiState.allTags.isNotEmpty()) {
                    TagFilterRow(
                        tags = uiState.allTags,
                        selectedTagNames = uiState.selectedTagNames,
                        onTagSelected = onTagSelected,
                        getTagName = { it.name }
                    )
                }

                if (uiState.filteredAndSortedCombos.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                    FlexibleItemList(
                        items = uiState.filteredAndSortedCombos,
                        getItemKey = { it.battleCombo.id },
                        modifier = Modifier.fillMaxSize()
                    ) { comboWithTags ->
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
}

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
        EnergyLevel.LOW -> Color(0xFF4CAF50)
        EnergyLevel.MEDIUM -> Color(0xFFFFC107)
        EnergyLevel.HIGH -> Color(0xFFF44336)
        EnergyLevel.NONE -> Color.Gray
    }

    val statusIcon = when (combo.status) {
        TrainingStatus.READY -> "🔥"
        TrainingStatus.TRAINING -> "🔨"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

                // Status Icon
                Text(
                    text = statusIcon,
                    style = MaterialTheme.typography.headlineSmall
                )

                // Edit Button
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// PREVIEWS
@Preview(showBackground = true)
@Composable
fun PreviewBattleComboListScreen() {
    BattleComboListContent(
        uiState = BattleComboListUiState(
            allCombos = listOf(
                BattleComboWithTags(
                    battleCombo = BattleCombo(
                        id = "1",
                        description = "Jab -> Cross -> Hook",
                        energy = EnergyLevel.MEDIUM,
                        status = TrainingStatus.READY,
                        isUsed = false
                    ),
                    tags = listOf(BattleTag(name = "Power"), BattleTag(name = "Speed"))
                ),
                BattleComboWithTags(
                    battleCombo = BattleCombo(
                        id = "2",
                        description = "Uppercut -> Body Shot",
                        energy = EnergyLevel.HIGH,
                        status = TrainingStatus.TRAINING,
                        isUsed = true
                    ),
                    tags = emptyList()
                )
            ),
            filteredAndSortedCombos = listOf(
                BattleComboWithTags(
                    battleCombo = BattleCombo(
                        id = "1",
                        description = "Jab -> Cross -> Hook",
                        energy = EnergyLevel.MEDIUM,
                        status = TrainingStatus.READY,
                        isUsed = false
                    ),
                    tags = listOf(BattleTag(name = "Power"), BattleTag(name = "Speed"))
                ),
                BattleComboWithTags(
                    battleCombo = BattleCombo(
                        id = "2",
                        description = "Uppercut -> Body Shot",
                        energy = EnergyLevel.HIGH,
                        status = TrainingStatus.TRAINING,
                        isUsed = true
                    ),
                    tags = emptyList()
                )
            ),
            allTags = listOf(
                BattleTag(name = "Power"),
                BattleTag(name = "Speed"),
                BattleTag(name = "Defense")
            ),
            selectedTagNames = setOf("Power"),
            showResetConfirmDialog = false
        ),
        onSortOptionChange = {},
        onTagSelected = {},
        onShowResetDialog = {},
        onConfirmReset = {},
        onCancelReset = {},
        onToggleUsed = {},
        onNavigateToAddEditBattleCombo = {},
        onNavigateToBattleTagList = {},
        onOpenDrawer = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBattleComboListScreenEmpty() {
    BattleComboListContent(
        uiState = BattleComboListUiState(
            allCombos = emptyList(),
            filteredAndSortedCombos = emptyList(),
            allTags = emptyList(),
            selectedTagNames = emptySet(),
            showResetConfirmDialog = false
        ),
        onSortOptionChange = {},
        onTagSelected = {},
        onShowResetDialog = {},
        onConfirmReset = {},
        onCancelReset = {},
        onToggleUsed = {},
        onNavigateToAddEditBattleCombo = {},
        onNavigateToBattleTagList = {},
        onOpenDrawer = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBattleComboListScreenResetDialog() {
    BattleComboListContent(
        uiState = BattleComboListUiState(
            showResetConfirmDialog = true
        ),
        onSortOptionChange = {},
        onTagSelected = {},
        onShowResetDialog = {},
        onConfirmReset = {},
        onCancelReset = {},
        onToggleUsed = {},
        onNavigateToAddEditBattleCombo = {},
        onNavigateToBattleTagList = {},
        onOpenDrawer = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBattleComboItem() {
    BattleComboItem(
        comboWithTags = BattleComboWithTags(
            battleCombo = BattleCombo(
                id = "1",
                description = "Jab -> Cross -> Hook",
                energy = EnergyLevel.MEDIUM,
                status = TrainingStatus.READY,
                isUsed = false
            ),
            tags = listOf(
                BattleTag(name = "Power"),
                BattleTag(name = "Speed")
            )
        ),
        onClick = {},
        onEditClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBattleComboItemUsed() {
    BattleComboItem(
        comboWithTags = BattleComboWithTags(
            battleCombo = BattleCombo(
                id = "1",
                description = "Uppercut -> Body Shot",
                energy = EnergyLevel.HIGH,
                status = TrainingStatus.TRAINING,
                isUsed = true
            ),
            tags = emptyList()
        ),
        onClick = {},
        onEditClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTagFilterRow() {
    TagFilterRow(
        tags = listOf(
            BattleTag(name = "Power"),
            BattleTag(name = "Speed"),
            BattleTag(name = "Defense")
        ),
        selectedTagNames = setOf("Power"),
        onTagSelected = {},
        getTagName = { it.name }
    )
}