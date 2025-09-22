package com.princelumpy.breakvault.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SavedComboWithMoves(
    @Embedded val savedCombo: SavedCombo,
    @Relation(
        parentColumn = "id", // Refers to SavedCombo.id
        entityColumn = "id",  // Refers to Move.id
        associateBy = Junction(
            value = SavedComboMoveLink::class,
            parentColumn = "saved_combo_id", // Column in SavedComboMoveLink referring to SavedCombo.id
            entityColumn = "move_id"         // Column in SavedComboMoveLink referring to Move.id
        )
    )
    val moves: List<Move> // Room will automatically order this based on the order_in_combo from the link table if the DAO query is set up correctly.
)