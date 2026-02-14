package com.princelumpy.breakvault.ui.practicecombos.list

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import com.princelumpy.breakvault.ui.common.FlexibleItemList
import com.princelumpy.breakvault.ui.moves.list.GenerateComboButton
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

@Composable
fun PracticeComboListScreen(
    onNavigateToAddEditCombo: (String?) -> Unit,
    onNavigateToComboGenerator: () -> Unit,
    onOpenDrawer: () -> Unit,
    practiceComboListViewModel: PracticeComboListViewModel = hiltViewModel()
) {
    val uiState by practiceComboListViewModel.uiState.collectAsStateWithLifecycle()

    PracticeComboListScaffold(
        practiceCombos = uiState.practiceCombos,
        isLoading = uiState.isLoading,
        onNavigateToAddEditCombo = onNavigateToAddEditCombo,
        onNavigateToComboGenerator = onNavigateToComboGenerator,
        onOpenDrawer = onOpenDrawer,
        onEditClick = { onNavigateToAddEditCombo(it.id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticeComboListScaffold(
    practiceCombos: List<PracticeCombo>,
    isLoading: Boolean,
    onNavigateToAddEditCombo: (String?) -> Unit,
    onNavigateToComboGenerator: () -> Unit,
    onOpenDrawer: () -> Unit,
    onEditClick: (PracticeCombo) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.practice_combos_screen_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onOpenDrawer() }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(id = R.string.drawer_content_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (practiceCombos.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { onNavigateToAddEditCombo(null) },
                    modifier = Modifier.imePadding(),
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.create_combo_fab_description)
                    )
                }
            }
        }
    ) { padding ->
        if (isLoading) {
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
                    .padding(top = padding.calculateTopPadding()),
            ) {
                GenerateComboButton(onClick = onNavigateToComboGenerator)

                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

                if (practiceCombos.isEmpty()) {
                    EmptyState(
                        onNavigateToAddEditCombo = { onNavigateToAddEditCombo(null) },
                        modifier = Modifier
                            .weight(1f)
                    )
                } else {
                    ComboList(
                        practiceCombos = practiceCombos,
                        onEditClick = onEditClick,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * A stateless list of practice combos.
 */
@Composable
private fun ComboList(
    practiceCombos: List<PracticeCombo>,
    onEditClick: (PracticeCombo) -> Unit,
    modifier: Modifier = Modifier
) {
    FlexibleItemList(
        items = practiceCombos,
        getItemKey = { it.id },
        modifier = modifier
    ) { practiceCombo ->
        PracticeComboItem(
            practiceCombo = practiceCombo,
            onEditClick = { onEditClick(practiceCombo) }
        )
    }
}

/**
 * A stateless composable for the empty state of the combo list.
 */
@Composable
private fun EmptyState(
    onNavigateToAddEditCombo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AppStyleDefaults.SpacingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.practice_combos_no_combos_message),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(id = R.string.practice_combos_empty_state_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
        )
        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))
        Button(onClick = onNavigateToAddEditCombo) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
            Text(stringResource(id = R.string.create_combo_button_text))
        }
    }
}

/**
 * A stateless item for a single practice combo in the list.
 */
@Composable
fun PracticeComboItem(
    practiceCombo: PracticeCombo,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall)
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = AppStyleDefaults.SpacingLarge,
                    vertical = AppStyleDefaults.SpacingMedium
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = practiceCombo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                Text(
                    text = practiceCombo.moves.joinToString(separator = " -> "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.padding(start = AppStyleDefaults.SpacingMedium)
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = stringResource(id = R.string.edit_combo_description),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun PracticeComboListScaffold_WithCombos_Preview() {
    val previewCombos = listOf(
        PracticeCombo(
            id = "1",
            name = "Windmill Freeze",
            moves = listOf("Windmill", "Baby Freeze")
        ),
        PracticeCombo(
            id = "2",
            name = "Flare Swipe",
            moves = listOf("Flare", "Swipe", "Elbow Freeze")
        ),
    )

    BreakVaultTheme {
        PracticeComboListScaffold(
            practiceCombos = previewCombos,
            isLoading = false,
            onNavigateToAddEditCombo = {},
            onNavigateToComboGenerator = {},
            onEditClick = {},
            onOpenDrawer = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PracticeComboListScaffold_Empty_Preview() {
    BreakVaultTheme {
        PracticeComboListScaffold(
            practiceCombos = emptyList(),
            isLoading = false,
            onNavigateToAddEditCombo = {},
            onNavigateToComboGenerator = {},
            onEditClick = {},
            onOpenDrawer = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PracticeComboItemPreview() {
    BreakVaultTheme {
        val sampleCombo = PracticeCombo(
            id = "preview1",
            name = "Awesome Combo",
            moves = listOf("Jab", "Cross", "Hook")
        )
        PracticeComboItem(
            practiceCombo = sampleCombo,
            onEditClick = {}
        )
    }
}