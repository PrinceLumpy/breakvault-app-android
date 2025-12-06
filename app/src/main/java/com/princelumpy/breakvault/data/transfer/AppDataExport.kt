package com.princelumpy.breakvault.data.transfer

import com.princelumpy.breakvault.data.BattleCombo
import com.princelumpy.breakvault.data.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.BattleTag
import com.princelumpy.breakvault.data.Goal
import com.princelumpy.breakvault.data.GoalStage
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.MoveTag
import com.princelumpy.breakvault.data.MoveTagCrossRef
import com.princelumpy.breakvault.data.SavedCombo
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
