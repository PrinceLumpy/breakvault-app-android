package com.princelumpy.breakvault.ui.goals.list

import AppStyleDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.local.relation.GoalWithStages
import com.princelumpy.breakvault.ui.common.AppLinearProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateToAddEditGoal: (String?) -> Unit,
    onNavigateToAddEditStage: (String, String?) -> Unit,
    onOpenDrawer: () -> Unit,
    goalsViewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by goalsViewModel.uiState.collectAsStateWithLifecycle()

    GoalsContent(
        uiState = uiState,
        onNavigateToAddEditGoal = onNavigateToAddEditGoal,
        onNavigateToAddEditStage = onNavigateToAddEditStage,
        onOpenDrawer = onOpenDrawer,
        onAddRepsClicked = goalsViewModel::onAddRepsClicked,
        onAddRepsDismissed = goalsViewModel::onAddRepsDismissed,
        onAddRepsConfirmed = goalsViewModel::addRepsToStage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsContent(
    uiState: GoalsScreenUiState,
    onNavigateToAddEditGoal: (String?) -> Unit,
    onNavigateToAddEditStage: (String, String?) -> Unit,
    onOpenDrawer: () -> Unit,
    onAddRepsClicked: (GoalStage) -> Unit,
    onAddRepsDismissed: () -> Unit,
    onAddRepsConfirmed: (GoalStage, Int) -> Unit
) {
    // Show add reps dialog if a stage is selected
    uiState.dialogState.addingRepsToStage?.let { stage ->
        AddRepsDialog(
            stageName = stage.name,
            stageUnit = stage.unit,
            onDismiss = onAddRepsDismissed,
            onConfirm = { reps ->
                onAddRepsConfirmed(stage, reps)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.goals_screen_title),
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
            if (!uiState.isLoading) {
                FloatingActionButton(
                    onClick = { onNavigateToAddEditGoal(null) },
                    modifier = Modifier.imePadding()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.goals_screen_add_goal_description)
                    )
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState()
            uiState.goals.isEmpty() -> EmptyGoalsState(onCreateGoal = { onNavigateToAddEditGoal(null) })
            else -> GoalsList(
                goals = uiState.goals,
                onEditGoalClick = onNavigateToAddEditGoal,
                onEditStageClick = onNavigateToAddEditStage,
                onAddRepsClicked = onAddRepsClicked,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyGoalsState(onCreateGoal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppStyleDefaults.SpacingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.goals_screen_no_goals_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(id = R.string.goals_screen_no_goals_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
        )
        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))
        Button(onClick = onCreateGoal) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
            Text(stringResource(id = R.string.goals_screen_create_goal_button))
        }
    }
}

@Composable
fun GoalsList(
    goals: List<GoalWithStages>,
    onEditGoalClick: (String?) -> Unit,
    onEditStageClick: (String, String?) -> Unit,
    onAddRepsClicked: (GoalStage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
        contentPadding = AppStyleDefaults.LazyListPadding
    ) {
        items(goals, key = { it.goal.id }) { goalWithStages ->
            ExpandableGoalCard(
                goalWithStages = goalWithStages,
                onEditGoalClick = { onEditGoalClick(goalWithStages.goal.id) },
                onEditStageClick = onEditStageClick,
                onAddRepsClicked = onAddRepsClicked
            )
        }
    }
}

@Composable
fun ExpandableGoalCard(
    goalWithStages: GoalWithStages,
    onEditGoalClick: () -> Unit,
    onEditStageClick: (String, String?) -> Unit,
    onAddRepsClicked: (GoalStage) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val progress = calculateGoalProgress(goalWithStages.stages)
    val hasStages = goalWithStages.stages.isNotEmpty()

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron rotation"
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(AppStyleDefaults.SpacingLarge)) {
            // Goal Header - clickable to expand
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hasStages) { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = goalWithStages.goal.title.ifBlank {
                                stringResource(id = R.string.goals_screen_untitled_goal)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasStages) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotationAngle),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (hasStages) {
                        Text(
                            text = "${goalWithStages.stages.size} ${if (goalWithStages.stages.size == 1) "stage" else "stages"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onEditGoalClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.goals_screen_edit_goal_description),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Goal Description
            if (goalWithStages.goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                Text(
                    text = goalWithStages.goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Overall Progress
            if (hasStages) {
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))
                AppLinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                Text(
                    text = stringResource(
                        id = R.string.goals_screen_progress_text,
                        (progress * 100).toInt()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Expandable Stages List
            AnimatedVisibility(visible = isExpanded && hasStages) {
                Column(
                    modifier = Modifier.padding(top = AppStyleDefaults.SpacingMedium)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingSmall))

                    goalWithStages.stages.forEach { stage ->
                        GoalStageItem(
                            stage = stage,
                            onEditClick = { onEditStageClick(goalWithStages.goal.id, stage.id) },
                            onAddRepsClick = { onAddRepsClicked(stage) }
                        )
                        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                    }
                }
            }
        }
    }
}

@Composable
fun GoalStageItem(
    stage: GoalStage,
    onEditClick: () -> Unit,
    onAddRepsClick: () -> Unit
) {
    val stageProgress = if (stage.targetCount > 0) {
        (stage.currentCount.toDouble() / stage.targetCount.toDouble()).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(AppStyleDefaults.SpacingMedium)
        ) {
            if (stage.targetCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stage.name.ifBlank { "Untitled Stage" },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onAddRepsClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add ${stage.unit}",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                AppLinearProgressIndicator(
                    progress = { stageProgress.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stage.currentCount} / ${stage.targetCount} ${stage.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stage.name.ifBlank { "Untitled Stage" },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun AddRepsDialog(
    stageName: String,
    stageUnit: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var reps by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${stageUnit.ifBlank { "Reps" }}") },
        text = {
            Column {
                Text(
                    text = stageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))
                OutlinedTextField(
                    value = reps,
                    onValueChange = {
                        // Allow negative numbers for subtraction
                        if (it.isEmpty() || it == "-" || it.toIntOrNull() != null) {
                            reps = it
                        }
                    },
                    label = { Text(stageUnit.replaceFirstChar { it.uppercase() }) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            reps.toIntOrNull()?.let { onConfirm(it) }
                        }
                    ),
                    singleLine = true,
                    supportingText = {
                        Text(
                            "Use negative numbers to subtract",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { reps.toIntOrNull()?.let { onConfirm(it) } },
                enabled = reps.toIntOrNull() != null
            ) {
                Text(stringResource(id = R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}

// HELPER FUNCTIONS
fun calculateGoalProgress(stages: List<GoalStage>): Double {
    if (stages.isEmpty()) return 0.0
    val totalProgress = stages.sumOf {
        it.currentCount.toDouble().coerceAtMost(it.targetCount.toDouble()) / it.targetCount
    }
    return totalProgress / stages.size
}

// PREVIEWS
@Preview(showBackground = true, name = "Full GoalList Screen")
@Composable
fun PreviewGoalsScreen() {
    MaterialTheme {
        GoalsContent(
            uiState = GoalsScreenUiState(
                goals = listOf(
                    GoalWithStages(
                        goal = Goal(
                            id = "1",
                            title = "Master Muay Thai",
                            description = "Complete all fundamental techniques",
                            isArchived = false,
                            createdAt = 0L,
                            lastUpdated = 0L
                        ),
                        stages = listOf(
                            GoalStage(
                                id = "1",
                                goalId = "1",
                                name = "Basic Kicks",
                                targetCount = 100,
                                currentCount = 50,
                                createdAt = 0L,
                                lastUpdated = 0L,
                                unit = "reps"
                            ),
                            GoalStage(
                                id = "2",
                                goalId = "1",
                                name = "Advanced Combos",
                                targetCount = 50,
                                currentCount = 10,
                                createdAt = 0L,
                                lastUpdated = 0L,
                                unit = "reps"
                            )
                        )
                    ),
                    GoalWithStages(
                        goal = Goal(
                            id = "2",
                            title = "Learn Boxing",
                            description = "",
                            isArchived = false,
                            createdAt = 0L,
                            lastUpdated = 0L
                        ),
                        stages = emptyList()
                    )
                ),
                isLoading = false
            ),
            onNavigateToAddEditGoal = {},
            onNavigateToAddEditStage = { _, _ -> },
            onAddRepsClicked = {},
            onAddRepsDismissed = {},
            onAddRepsConfirmed = { _, _ -> },
            onOpenDrawer = {}
        )
    }
}

@Preview(showBackground = true, name = "Add Reps Dialog")
@Composable
fun PreviewAddRepsDialog() {
    MaterialTheme {
        AddRepsDialog(
            stageName = "Basic Kicks",
            stageUnit = "reps",
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, name = "Expandable Goal Card with Stages")
@Composable
fun PreviewExpandableGoalCard() {
    MaterialTheme {
        ExpandableGoalCard(
            goalWithStages = GoalWithStages(
                goal = Goal(
                    id = "1",
                    title = "Master Muay Thai",
                    description = "Complete all fundamental techniques",
                    isArchived = false,
                    createdAt = 0L,
                    lastUpdated = 0L
                ),
                stages = listOf(
                    GoalStage(
                        id = "1",
                        goalId = "1",
                        name = "Basic Kicks",
                        targetCount = 100,
                        currentCount = 50,
                        createdAt = 0L,
                        lastUpdated = 0L,
                        unit = "reps"
                    ),
                    GoalStage(
                        id = "2",
                        goalId = "1",
                        name = "Advanced Combos",
                        targetCount = 50,
                        currentCount = 10,
                        createdAt = 0L,
                        lastUpdated = 0L,
                        unit = "sets"
                    )
                )
            ),
            onEditGoalClick = {},
            onEditStageClick = { _, _ -> },
            onAddRepsClicked = {}
        )
    }
}

@Preview(showBackground = true, name = "Goal Card without Stages")
@Composable
fun PreviewExpandableGoalCardNoStages() {
    MaterialTheme {
        ExpandableGoalCard(
            goalWithStages = GoalWithStages(
                goal = Goal(
                    id = "1",
                    title = "Learn Boxing",
                    description = "Focus on footwork and combinations",
                    isArchived = false,
                    createdAt = 0L,
                    lastUpdated = 0L
                ),
                stages = emptyList()
            ),
            onEditGoalClick = {},
            onEditStageClick = { _, _ -> },
            onAddRepsClicked = {}
        )
    }
}

@Preview(showBackground = true, name = "Stage with Target Count")
@Composable
fun PreviewGoalStageItem() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GoalStageItem(
                stage = GoalStage(
                    id = "1",
                    goalId = "1",
                    name = "Basic Kicks",
                    targetCount = 100,
                    currentCount = 75,
                    createdAt = 0L,
                    lastUpdated = 0L,
                    unit = "reps"
                ),
                onEditClick = {},
                onAddRepsClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Stage without Target Count")
@Composable
fun PreviewGoalStageItemNoTarget() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GoalStageItem(
                stage = GoalStage(
                    id = "1",
                    goalId = "1",
                    name = "Practice Form",
                    targetCount = 0,
                    currentCount = 0,
                    createdAt = 0L,
                    lastUpdated = 0L,
                    unit = "reps"
                ),
                onEditClick = {},
                onAddRepsClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty GoalList State")
@Composable
fun PreviewEmptyGoalsState() {
    MaterialTheme {
        EmptyGoalsState(onCreateGoal = {})
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun PreviewLoadingState() {
    MaterialTheme {
        LoadingState()
    }
}