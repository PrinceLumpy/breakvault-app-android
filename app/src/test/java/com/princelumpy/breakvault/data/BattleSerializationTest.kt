package com.princelumpy.breakvault.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class BattleSerializationTest {

    private val json = Json { 
        encodeDefaults = true 
        ignoreUnknownKeys = true
    }

    @Test
    fun `BattleCombo serializes and deserializes correctly`() {
        // Given
        val originalCombo = BattleCombo(
            id = UUID.randomUUID().toString(),
            description = "Test Combo with Power",
            energy = EnergyLevel.HIGH,
            status = TrainingStatus.READY,
            isUsed = true
        )

        // When
        val jsonString = json.encodeToString(originalCombo)
        val deserializedCombo = json.decodeFromString<BattleCombo>(jsonString)

        // Then
        assertEquals(originalCombo, deserializedCombo)
        assertEquals(EnergyLevel.HIGH, deserializedCombo.energy)
        assertEquals(TrainingStatus.READY, deserializedCombo.status)
    }

    @Test
    fun `BattleTag serializes and deserializes correctly`() {
        // Given
        val originalTag = BattleTag(
            id = "moveListTag-123",
            name = "Power Move"
        )

        // When
        val jsonString = json.encodeToString(originalTag)
        val deserializedTag = json.decodeFromString<BattleTag>(jsonString)

        // Then
        assertEquals(originalTag, deserializedTag)
    }
    
    @Test
    fun `BattleComboTagCrossRef serializes and deserializes correctly`() {
        // Given
        val crossRef = BattleComboTagCrossRef(
            battleComboId = "combo-1",
            battleTagId = "moveListTag-1"
        )

        // When
        val jsonString = json.encodeToString(crossRef)
        val deserializedCrossRef = json.decodeFromString<BattleComboTagCrossRef>(jsonString)

        // Then
        assertEquals(crossRef, deserializedCrossRef)
    }
}
