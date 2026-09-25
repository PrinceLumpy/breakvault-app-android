// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.TagColor

val TagColor.composeColor: Color get() = Color(argb)

/**
 * The distinct colors of these tags, in palette order (top to bottom on the strip).
 * Uncolored tags contribute nothing; an empty result means no strip.
 */
fun List<BattleTag>.stripColors(): List<TagColor> = map { it.color }.toStripColors()

@JvmName("moveTagStripColors")
fun List<MoveTag>.stripColors(): List<TagColor> = map { it.color }.toStripColors()

private fun List<TagColor?>.toStripColors(): List<TagColor> =
    filterNotNull().distinct().sortedBy { it.ordinal }

/**
 * Vertical strip on the leading edge of a card, split into equal bands, one per color.
 * With no colors it still reserves its width so card content stays aligned.
 * The parent must have a bounded height (e.g. a Row with `height(IntrinsicSize.Min)`).
 */
@Composable
fun TagColorStrip(
    colors: List<TagColor>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(color.composeColor)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 64)
@Composable
private fun TagColorStripPreview() {
    TagColorStrip(colors = listOf(TagColor.RED, TagColor.GREEN, TagColor.BLUE))
}
