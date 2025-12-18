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
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.GoalStage

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goalStage.name,
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

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = onAddRepsClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null // Decorative
                    )
                    Spacer(modifier = Modifier.width(AppStyleDefaults.SpacingSmall))
                    Text(text = "Add ${goalStage.unit}")
                }
            }

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))

            Text(
                text = "${goalStage.currentCount} / ${goalStage.targetCount}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
            LinearProgressIndicator(
                progress = {
                    if (goalStage.targetCount > 0) {
                        goalStage.currentCount.toFloat() / goalStage.targetCount.toFloat()
                    } else {
                        0f
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
