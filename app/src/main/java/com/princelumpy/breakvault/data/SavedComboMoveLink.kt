package com.princelumpy.breakvault.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.serialization.Serializable // Added import

@Serializable // Added annotation
@Entity(
    tableName = "saved_combo_move_link",
    primaryKeys = ["saved_combo_id", "order_in_combo"],
    foreignKeys = [
        ForeignKey(
            entity = SavedCombo::class,
            parentColumns = ["id"],
            childColumns = ["saved_combo_id"],
            onDelete = ForeignKey.CASCADE // If a SavedCombo is deleted, its links are also deleted
        ),
        ForeignKey(
            entity = Move::class,
            parentColumns = ["id"],
            childColumns = ["move_id"],
            onDelete = ForeignKey.CASCADE // If a Move is deleted, links to it are also deleted
        )
    ]
)
data class SavedComboMoveLink(
    @ColumnInfo(name = "saved_combo_id", index = true) val savedComboId: String,
    @ColumnInfo(name = "move_id", index = true) val moveId: String,
    @ColumnInfo(name = "order_in_combo") val orderInCombo: Int
)
