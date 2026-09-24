// Modified by Claude Code - 2026-09-24
package com.princelumpy.breakvault.ui.battlecombos

import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.TagColor
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import com.princelumpy.breakvault.data.repository.BattleSortOption
import com.princelumpy.breakvault.ui.battlecombos.common.stripColors
import com.princelumpy.breakvault.ui.battlecombos.list.BattleComboListItem
import com.princelumpy.breakvault.ui.battlecombos.list.sortedFor
import org.junit.Assert.assertEquals
import org.junit.Test

class BattleComboColorsTest {

    private fun tag(color: TagColor?) = BattleTag(name = color?.name ?: "plain", color = color)

    private fun item(title: String, vararg colors: TagColor?, isUsed: Boolean = false, createdAt: Long = 0) =
        BattleComboListItem(
            BattleComboWithTags(
                battleCombo = BattleCombo(id = title, title = title, isUsed = isUsed, createdAt = createdAt),
                tags = colors.map { tag(it) }
            )
        )

    @Test
    fun stripColors_dedupesDropsUncoloredAndUsesPaletteOrder() {
        val tags = listOf(tag(TagColor.BLUE), tag(null), tag(TagColor.RED), tag(TagColor.BLUE))

        assertEquals(listOf(TagColor.RED, TagColor.BLUE), tags.stripColors())
    }

    @Test
    fun stripColors_noColoredTags_isEmpty() {
        assertEquals(emptyList<TagColor>(), listOf(tag(null)).stripColors())
    }

    @Test
    fun sortNewest_newestFirstWithUsedLast() {
        val items = listOf(
            item("old", createdAt = 1),
            item("usedNew", isUsed = true, createdAt = 9),
            item("new", createdAt = 5)
        )

        assertEquals(
            listOf("new", "old", "usedNew"),
            items.sortedFor(BattleSortOption.NEWEST).map { it.comboWithTags.battleCombo.title }
        )
    }

    @Test
    fun sortColor_groupsByTopColorWithUncoloredLast() {
        val items = listOf(
            item("none"),
            item("blue", TagColor.BLUE),
            item("redBlue", TagColor.BLUE, TagColor.RED),
            item("red", TagColor.RED)
        )

        assertEquals(
            listOf("red", "redBlue", "blue", "none"),
            items.sortedFor(BattleSortOption.COLOR).map { it.comboWithTags.battleCombo.title }
        )
    }
}
