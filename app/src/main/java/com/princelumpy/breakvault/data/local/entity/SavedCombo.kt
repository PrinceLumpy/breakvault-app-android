package com.princelumpy.breakvault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "saved_combos")
data class SavedCombo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val moves: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)