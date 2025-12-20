package com.princelumpy.breakvault.data.service.export.model

import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.MoveTagCrossRef
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import kotlinx.serialization.Serializable

@Serializable
data class AppDataExport(
    val moves: List<Move>,
    val moveTags: List<MoveTag>,
    val moveTagCrossRefs: List<MoveTagCrossRef>,
    val savedCombos: List<SavedCombo>,
    val battleCombos: List<BattleCombo> = emptyList(),
    val battleTags: List<BattleTag> = emptyList(),
    val battleComboTagCrossRefs: List<BattleComboTagCrossRef> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val goalStages: List<GoalStage> = emptyList()
)