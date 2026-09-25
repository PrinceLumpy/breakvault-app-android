// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "move_tags")
data class MoveTag(
    @PrimaryKey val id: String,
    val name: String,
    val color: TagColor? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)