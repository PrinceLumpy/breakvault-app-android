// Modified by Claude Code - 2026-09-24
package com.princelumpy.breakvault.ui.battlecombos.common

import AppStyleDefaults
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.entity.TagColor

@get:StringRes
private val TagColor.labelResId: Int
    get() = when (this) {
        TagColor.RED -> R.string.tag_color_red
        TagColor.ORANGE -> R.string.tag_color_orange
        TagColor.YELLOW -> R.string.tag_color_yellow
        TagColor.GREEN -> R.string.tag_color_green
        TagColor.TEAL -> R.string.tag_color_teal
        TagColor.BLUE -> R.string.tag_color_blue
        TagColor.PURPLE -> R.string.tag_color_purple
        TagColor.PINK -> R.string.tag_color_pink
    }

/**
 * Row of round swatches for choosing a tag color. The first swatch means "no color".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagColorPicker(
    selectedColor: TagColor?,
    onColorSelected: (TagColor?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.tag_color_label),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = AppStyleDefaults.SpacingSmall)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall),
            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
        ) {
            ColorSwatch(
                color = null,
                label = stringResource(id = R.string.tag_color_none),
                isSelected = selectedColor == null,
                onClick = { onColorSelected(null) }
            )
            TagColor.entries.forEach { tagColor ->
                ColorSwatch(
                    color = tagColor.composeColor,
                    label = stringResource(id = tagColor.labelResId),
                    isSelected = selectedColor == tagColor,
                    onClick = { onColorSelected(tagColor) }
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outline = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color ?: MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) outline else outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        when {
            color == null -> Icon(
                imageVector = Icons.Filled.Block,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            isSelected -> Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TagColorPickerPreview() {
    MaterialTheme {
        TagColorPicker(selectedColor = TagColor.BLUE, onColorSelected = {})
    }
}
