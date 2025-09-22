package com.princelumpy.breakvault.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MoveWithTags(
    @Embedded val move: Move,
    @Relation(
        parentColumn = "id", // Primary key of the Move entity
        entityColumn = "id", // Primary key of the Tag entity
        associateBy = Junction(
            value = MoveTagCrossRef::class,
            parentColumn = "moveId", // Foreign key in MoveTagCrossRef linking to Move
            entityColumn = "tagId"   // Foreign key in MoveTagCrossRef linking to Tag
        )
    )
    val tags: List<Tag>
)
