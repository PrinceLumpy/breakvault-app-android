package com.princelumpy.breakvault.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "battle_combo_tag_cross_ref",
    primaryKeys = ["battleComboId", "battleTagId"],
    foreignKeys = [
        ForeignKey(
            entity = BattleCombo::class,
            parentColumns = ["id"],
            childColumns = ["battleComboId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BattleTag::class,
            parentColumns = ["id"],
            childColumns = ["battleTagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["battleComboId"]),
        Index(value = ["battleTagId"])
    ]
)
data class BattleComboTagCrossRef(
    val battleComboId: String,
    val battleTagId: String
)