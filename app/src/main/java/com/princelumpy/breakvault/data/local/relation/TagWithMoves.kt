package com.princelumpy.breakvault.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.MoveTagCrossRef

data class TagWithMoves(
    @Embedded val moveTag: MoveTag,
    @Relation(
        parentColumn = "id", // Primary key of the MoveTag entity
        entityColumn = "id", // Primary key of the Move entity
        associateBy = Junction(
            value = MoveTagCrossRef::class,
            parentColumn = "tagId",   // Foreign key in MoveTagCrossRef linking to MoveTag
            entityColumn = "moveId"  // Foreign key in MoveTagCrossRef linking to Move
        )
    )
    val moves: List<Move>
)