package com.princelumpy.breakvault.data

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
            onDelete = ForeignKey.CASCADE // If a Move is deleted, delete its links
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE // If a Tag is deleted, delete its links
        )
    ]
)
data class MoveTagCrossRef(
    val moveId: String,
    val tagId: String
)
