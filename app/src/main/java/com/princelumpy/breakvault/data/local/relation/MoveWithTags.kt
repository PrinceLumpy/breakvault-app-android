package com.princelumpy.breakvault.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.MoveTagCrossRef

data class MoveWithTags(
    @Embedded val move: Move,
    @Relation(
        parentColumn = "id", // Primary key of the Move entity
        entityColumn = "id", // Primary key of the MoveTag entity
        associateBy = Junction(
            value = MoveTagCrossRef::class,
            parentColumn = "moveId", // Foreign key in MoveTagCrossRef linking to Move
            entityColumn = "tagId"   // Foreign key in MoveTagCrossRef linking to MoveTag
        )
    )
    val moveTags: List<MoveTag>
)