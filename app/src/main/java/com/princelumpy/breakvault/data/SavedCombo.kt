package com.princelumpy.breakvault.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable // Added import
import java.util.UUID

@Serializable // Added annotation
@Entity(tableName = "saved_combos")
data class SavedCombo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
