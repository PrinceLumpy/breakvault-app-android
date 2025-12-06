package com.princelumpy.breakvault.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "battle_combos")
data class BattleCombo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val description: String,
    val energy: EnergyLevel = EnergyLevel.NONE,
    val status: TrainingStatus = TrainingStatus.TRAINING,
    val isUsed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)
