package com.princelumpy.breakvault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.local.relation.GoalWithStages
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    suspend fun getAllGoals(): List<Goal>

    @Query("SELECT * FROM goal_stages")
    suspend fun getAllGoalStages(): List<GoalStage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<Goal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoalStages(goalStages: List<GoalStage>)

    @Transaction
    @Query("SELECT * FROM goals WHERE isArchived = 0 ORDER BY lastUpdated DESC")
    fun getAllActiveGoalsWithStages(): Flow<List<GoalWithStages>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalWithStages(goalId: String): GoalWithStages?

    @Transaction
    @Query("SELECT * FROM goals WHERE isArchived = 1 ORDER BY lastUpdated DESC")
    fun getArchivedGoalsWithStages(): Flow<List<GoalWithStages>>

    @Query("SELECT * FROM goals WHERE isArchived = 0 ORDER BY lastUpdated DESC")
    fun getAllActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoal(goalId: String): Flow<Goal?>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalById(goalId: String): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goal_stages WHERE goalId = :goalId ORDER BY createdAt ASC")
    fun getStagesForGoal(goalId: String): Flow<List<GoalStage>>

    @Query("SELECT * FROM goal_stages WHERE id = :stageId")
    suspend fun getGoalStage(stageId: String): GoalStage?

    @Query("SELECT * FROM goal_stages WHERE id = :stageId")
    fun getGoalStageById(stageId: String): GoalStage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalStage(stage: GoalStage)

    @Update
    suspend fun updateGoalStage(stage: GoalStage)

    @Delete
    suspend fun deleteGoalStage(stage: GoalStage)

    @Query("DELETE FROM goal_stages WHERE goalId = :goalId")
    suspend fun deleteAllStagesForGoal(goalId: String)
}
