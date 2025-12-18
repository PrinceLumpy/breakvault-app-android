package com.princelumpy.breakvault.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.local.entity.BattleTag

data class BattleComboWithTags(
    @Embedded val battleCombo: BattleCombo,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BattleComboTagCrossRef::class,
            parentColumn = "battleComboId",
            entityColumn = "battleTagId"
        )
    )
    val tags: List<BattleTag>
)