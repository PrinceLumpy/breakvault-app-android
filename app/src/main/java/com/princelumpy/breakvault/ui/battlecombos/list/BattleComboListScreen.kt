// Modified by Claude Code - 2026-09-24
package com.princelumpy.breakvault.ui.battlecombos.list

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.TagColor
import com.princelumpy.breakvault.data.repository.BattleSortOption
import com.princelumpy.breakvault.ui.battlecombos.common.TagColorStrip
import com.princelumpy.breakvault.ui.common.TagFilterRow
import AppStyleDefaults

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
        onSortOptionChange = battleComboListViewModel::changeSortOption,
        onToggleTagFilter = battleComboListViewModel::toggleTagFilter,
        onClearFilters = battleComboListViewModel::clearFilters,
        onShowResetDialog = battleComboListViewModel::showResetDialog,
        onConfirmReset = battleComboListViewModel::confirmReset,
        onCancelReset = battleComboListViewModel::cancelReset,
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
    onToggleTagFilter: (String) -> Unit,
    onClearFilters: () -> Unit,
    onShowResetDialog: () -> Unit,
    onConfirmReset: () -> Unit,
    onCancelReset: () -> Unit,
    onToggleUsed: (BattleCombo) -> Unit,
    onNavigateToAddEditBattleCombo: (String?) -> Unit,
    onNavigateToBattleTagList: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(uiState.selectedTagNames, uiState.sortOption) {
        lazyListState.animateScrollToItem(0)
    }

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
                            contentDescription = stringResource(id = R.string.battle_combo_list_manage_tags_description)
                        )
                    }
                    // Sort Button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(id = R.string.battle_combo_list_sort_description)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            listOf(
                                BattleSortOption.NEWEST to R.string.battle_combo_list_sort_newest,
                                BattleSortOption.NAME to R.string.battle_combo_list_sort_name,
                                BattleSortOption.COLOR to R.string.battle_combo_list_sort_color
                            ).forEach { (option, labelResId) ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = labelResId)) },
                                    trailingIcon = if (option == uiState.sortOption) {
                                        { Icon(Icons.Filled.Check, contentDescription = null) }
                                    } else null,
                                    onClick = {
                                        onSortOptionChange(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    // Reset Button
                    IconButton(onClick = onShowResetDialog) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource(id = R.string.battle_combo_list_reset_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.allCombos.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onNavigateToAddEditBattleCombo(null) },
                    modifier = Modifier.imePadding()
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.battle_combo_list_add_combo_description)
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
            if (uiState.filteredAndSortedCombos.isEmpty() && uiState.allCombos.isEmpty()) {
                // Empty state - apply full padding
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(AppStyleDefaults.SpacingLarge),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.battle_combo_list_no_combos_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(id = R.string.battle_combo_list_no_combos_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
                    )
                    Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))
                    Button(onClick = { onNavigateToAddEditBattleCombo(null) }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
                        Text(stringResource(id = R.string.battle_combo_list_add_combo_button))
                    }
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
                            onTagSelected = onToggleTagFilter,
                            getTagName = { it.name },
                            onClearFilters = onClearFilters
                        )
                    }

                    if (uiState.filteredAndSortedCombos.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(AppStyleDefaults.SpacingLarge),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.battle_combo_list_no_matches_filter),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = AppStyleDefaults.SpacingLarge,
                                end = AppStyleDefaults.SpacingLarge,
                                bottom = AppStyleDefaults.SpacingExtraLarge * 4
                            ),
                            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
                        ) {
                            items(
                                items = uiState.filteredAndSortedCombos,
                                key = { it.comboWithTags.battleCombo.id }
                            ) { item ->
                                BattleComboItem(
                                    item = item,
                                    onClick = { onToggleUsed(item.comboWithTags.battleCombo) },
                                    onEditClick = {
                                        onNavigateToAddEditBattleCombo(item.comboWithTags.battleCombo.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = onCancelReset,
                title = { Text(stringResource(id = R.string.battle_combo_list_reset_dialog_title)) },
                text = { Text(stringResource(id = R.string.battle_combo_list_reset_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = onConfirmReset) {
                        Text(stringResource(id = R.string.battle_combo_list_reset_confirm_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCancelReset) {
                        Text(stringResource(id = R.string.common_cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun BattleComboItem(
    item: BattleComboListItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val combo = item.comboWithTags.battleCombo
    val tags = item.comboWithTags.tags
    val isUsed = combo.isUsed

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
            TagColorStrip(colors = item.stripColors)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = combo.title,
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

                // Edit Button
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.battle_combo_list_edit_description),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// PREVIEWS
private val previewTags = listOf(
    BattleTag(name = "Power", color = TagColor.RED),
    BattleTag(name = "Technique", color = TagColor.BLUE),
    BattleTag(name = "Filler")
)

private val previewCombos = listOf(
    BattleComboWithTags(
        battleCombo = BattleCombo(id = "1", title = "Jab -> Cross -> Hook"),
        tags = previewTags
    ),
    BattleComboWithTags(
        battleCombo = BattleCombo(id = "2", title = "Windmill -> Freeze"),
        tags = listOf(previewTags[0])
    ),
    BattleComboWithTags(
        battleCombo = BattleCombo(id = "3", title = "Uppercut -> Body Shot", isUsed = true),
        tags = emptyList()
    )
)

@Preview(showBackground = true)
@Composable
fun PreviewBattleComboListScreen() {
    BattleComboListContent(
        uiState = BattleComboListUiState(
            allCombos = previewCombos,
            filteredAndSortedCombos = previewCombos.map { BattleComboListItem(it) },
            allTags = previewTags,
            selectedTagNames = setOf("Power"),
            showResetConfirmDialog = false,
            isLoading = false
        ),
        onSortOptionChange = {},
        onToggleTagFilter = {},
        onClearFilters = {},
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
        uiState = BattleComboListUiState(isLoading = false),
        onSortOptionChange = {},
        onToggleTagFilter = {},
        onClearFilters = {},
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
        onToggleTagFilter = {},
        onClearFilters = {},
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
        item = BattleComboListItem(previewCombos[0]),
        onClick = {},
        onEditClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBattleComboItemUsed() {
    BattleComboItem(
        item = BattleComboListItem(previewCombos[2]),
        onClick = {},
        onEditClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTagFilterRow() {
    TagFilterRow(
        tags = previewTags,
        selectedTagNames = setOf("Power"),
        onTagSelected = {},
        getTagName = { it.name },
        onClearFilters = {}
    )
}
