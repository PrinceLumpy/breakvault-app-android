package com.princelumpy.breakvault.data

import com.princelumpy.breakvault.data.transfer.AppDataExport
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class PracticeSerializationTest {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `Move serializes correctly`() {
        val move = Move(id = "m1", name = "Windmill")
        val string = json.encodeToString(move)
        val decoded = json.decodeFromString<Move>(string)
        assertEquals(move, decoded)
    }

    @Test
    fun `Tag serializes correctly`() {
        val moveListTag = MoveListTag(id = "t1", name = "Power")
        val string = json.encodeToString(moveListTag)
        val decoded = json.decodeFromString<MoveListTag>(string)
        assertEquals(moveListTag, decoded)
    }

    @Test
    fun `SavedCombo serializes correctly`() {
        val combo = SavedCombo(
            id = UUID.randomUUID().toString(),
            name = "Best Combo",
            moves = listOf("Windmill", "Flare"),
            createdAt = 123456789L
        )
        val string = json.encodeToString(combo)
        val decoded = json.decodeFromString<SavedCombo>(string)
        assertEquals(combo, decoded)
    }

    @Test
    fun `MoveTagCrossRef serializes correctly`() {
        val ref = MoveTagCrossRef(moveId = "m1", tagId = "t1")
        val string = json.encodeToString(ref)
        val decoded = json.decodeFromString<MoveTagCrossRef>(string)
        assertEquals(ref, decoded)
    }

    @Test
    fun `AppDataExport serializes correctly with all fields`() {
        val export = AppDataExport(
            moves = listOf(Move("m1", "Windmill")),
            moveListTags = listOf(MoveListTag("t1", "Power")),
            moveTagCrossRefs = listOf(MoveTagCrossRef("m1", "t1")),
            savedCombos = listOf(SavedCombo(id = "s1", name = "My Combo", moves = listOf("Windmill"))),
            battleCombos = listOf(BattleCombo(id = "b1", description = "Battle Round")),
            battleTags = listOf(BattleTag("bt1", "Aggressive")),
            battleComboTagCrossRefs = listOf(BattleComboTagCrossRef("b1", "bt1"))
        )

        val string = json.encodeToString(export)
        val decoded = json.decodeFromString<AppDataExport>(string)

        assertEquals(export, decoded)
        assertEquals(1, decoded.battleCombos.size)
    }

    @Test
    fun `AppDataExport handles missing battle fields (Backward Compatibility)`() {
        // Simulate an old JSON export that doesn't have battle fields
        val oldJson = """
            {
                "moves": [{"id": "m1", "name": "Windmill"}],
                "moveListTags": [],
                "moveTagCrossRefs": [],
                "savedCombos": []
            }
        """.trimIndent()

        val decoded = json.decodeFromString<AppDataExport>(oldJson)

        assertEquals(1, decoded.moves.size)
        assertEquals("Windmill", decoded.moves[0].name)

        // Verify defaults are applied
        assertTrue(decoded.battleCombos.isEmpty())
        assertTrue(decoded.battleTags.isEmpty())
    }
}
