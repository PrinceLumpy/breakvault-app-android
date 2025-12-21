package com.princelumpy.breakvault.ui.components

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme

@Composable
fun GoalStageItem(
    goalStage: GoalStage,
    onEditClick: () -> Unit,
    onAddRepsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppStyleDefaults.SpacingSmall)
    ) {
        Column(modifier = Modifier.padding(AppStyleDefaults.SpacingMedium)) {
            GoalStageHeader(
                name = goalStage.name,
                onEditClick = onEditClick
            )

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AddRepsButton(
                    unit = goalStage.unit,
                    onAddRepsClick = onAddRepsClick
                )
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

            GoalStageProgress(
                currentCount = goalStage.currentCount,
                targetCount = goalStage.targetCount
            )
        }
    }
}

@Composable
private fun GoalStageHeader(
    name: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onEditClick) {
            Icon(
                Icons.Default.Edit,
                contentDescription = stringResource(id = R.string.goal_stage_item_edit_button)
            )
        }
    }
}

@Composable
private fun AddRepsButton(
    unit: String,
    onAddRepsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = onAddRepsClick, modifier = modifier) {
        Icon(
            Icons.Default.Add,
            contentDescription = null // Decorative
        )
        Spacer(modifier = Modifier.width(AppStyleDefaults.SpacingSmall))
        Text(text = "Add $unit")
    }
}

@Composable
private fun GoalStageProgress(
    currentCount: Int,
    targetCount: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$currentCount / $targetCount",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
        LinearProgressIndicator(
            progress = {
                if (targetCount > 0) {
                    currentCount.toFloat() / targetCount.toFloat()
                } else {
                    0f
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


//region Previews
@Preview(showBackground = true)
@Composable
private fun GoalStageItemPreview() {
    BreakVaultTheme {
        GoalStageItem(
            goalStage = GoalStage(
                id = "456",
                goalId = "123",
                name = "Push-ups",
                currentCount = 25,
                targetCount = 100,
                unit = "reps"
            ),
            onEditClick = {},
            onAddRepsClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun GoalStageHeaderPreview() {
    BreakVaultTheme {
        GoalStageHeader(name = "Master the Windmill", onEditClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun AddRepsButtonPreview() {
    BreakVaultTheme {
        AddRepsButton(unit = "sets", onAddRepsClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun GoalStageProgressPreview() {
    BreakVaultTheme {
        GoalStageProgress(currentCount = 75, targetCount = 150)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun GoalStageProgressZeroTargetPreview() {
    BreakVaultTheme {
        GoalStageProgress(currentCount = 0, targetCount = 0)
    }
}
//endregion
