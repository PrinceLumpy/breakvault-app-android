package com.princelumpy.breakvault.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "move_tag_cross_refs",
    primaryKeys = ["moveId", "tagId"],
    indices = [Index(value = ["tagId"])], // Index on tagId for lookups
    foreignKeys = [
        ForeignKey(
            entity = Move::class,
            parentColumns = ["id"],
            childColumns = ["moveId"],
            onDelete = ForeignKey.Companion.CASCADE // If a Move is deleted, delete its links
        ),
        ForeignKey(
            entity = MoveTag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.Companion.CASCADE // If a MoveTag is deleted, delete its links
        )
    ]
)
data class MoveTagCrossRef(
    val moveId: String,
    val tagId: String
)