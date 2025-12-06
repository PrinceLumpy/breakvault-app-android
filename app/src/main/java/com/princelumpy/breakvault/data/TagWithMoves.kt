package com.princelumpy.breakvault.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TagWithMoves(
    @Embedded val moveListTag: MoveListTag,
    @Relation(
        parentColumn = "id", // Primary key of the MoveListTag entity
        entityColumn = "id", // Primary key of the Move entity
        associateBy = Junction(
            value = MoveTagCrossRef::class,
            parentColumn = "tagId",   // Foreign key in MoveTagCrossRef linking to MoveListTag
            entityColumn = "moveId"  // Foreign key in MoveTagCrossRef linking to Move
        )
    )
    val moves: List<Move>
)
