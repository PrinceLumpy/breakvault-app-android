package com.princelumpy.breakvault.data.transfer

import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.Tag
import com.princelumpy.breakvault.data.MoveTagCrossRef
import com.princelumpy.breakvault.data.SavedCombo
import com.princelumpy.breakvault.data.SavedComboMoveLink
import kotlinx.serialization.Serializable // <--- ENSURED IMPORT

@Serializable // <--- ENSURED ANNOTATION
data class AppDataExport(
    val moves: List<Move>,
    val tags: List<Tag>,
    val moveTagCrossRefs: List<MoveTagCrossRef>,
    val savedCombos: List<SavedCombo>,
    val savedComboMoveLinks: List<SavedComboMoveLink>
)
