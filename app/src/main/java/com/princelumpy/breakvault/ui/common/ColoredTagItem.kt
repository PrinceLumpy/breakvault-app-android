// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.ui.common

import AppStyleDefaults
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.princelumpy.breakvault.data.local.entity.TagColor

/**
 * Tag card for the manage-tags screens, with the same leading color strip as combo and move
 * cards. Tapping the card opens the edit dialog, like the edit icon.
 */
@Composable
fun ColoredTagItem(
    name: String,
    color: TagColor?,
    editContentDescription: String,
    deleteContentDescription: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onEditClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TagColorStrip(colors = listOfNotNull(color))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        horizontal = AppStyleDefaults.SpacingLarge,
                        vertical = AppStyleDefaults.SpacingMedium
                    )
            )
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = editContentDescription,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.padding(end = AppStyleDefaults.SpacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = deleteContentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColoredTagItemPreview() {
    MaterialTheme {
        ColoredTagItem(
            name = "Power",
            color = TagColor.RED,
            editContentDescription = "Edit",
            deleteContentDescription = "Delete",
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
