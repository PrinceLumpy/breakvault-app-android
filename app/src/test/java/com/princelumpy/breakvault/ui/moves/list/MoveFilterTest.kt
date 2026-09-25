// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.ui.moves.list

import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import org.junit.Assert.assertEquals
import org.junit.Test

class MoveFilterTest {

    private val power = MoveTag(id = "p", name = "Power")
    private val freeze = MoveTag(id = "f", name = "Freeze")

    private fun move(name: String, vararg tags: MoveTag) =
        MoveWithTags(move = Move(id = name, name = name), moveTags = tags.toList())

    private val moves = listOf(
        move("Windmill", power),
        move("Baby Freeze", freeze),
        move("Air Flare", power)
    )

    @Test
    fun filterMoves_searchesOnlyWithinTagFilteredMoves() {
        val result = filterMoves(moves, setOf("Power"), "  wind ")
        assertEquals(listOf("Windmill"), result.map { it.move.name })
    }

    @Test
    fun filterMoves_searchWithNoTagsSelectedMatchesAllMovesCaseInsensitively() {
        val result = filterMoves(moves, emptySet(), "FREEZE")
        assertEquals(listOf("Baby Freeze"), result.map { it.move.name })
    }
}
