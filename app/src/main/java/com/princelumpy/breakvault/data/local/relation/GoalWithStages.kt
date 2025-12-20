package com.princelumpy.breakvault.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage

data class GoalWithStages(
    @Embedded val goal: Goal,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId",
    )
    val stages: List<GoalStage>
)