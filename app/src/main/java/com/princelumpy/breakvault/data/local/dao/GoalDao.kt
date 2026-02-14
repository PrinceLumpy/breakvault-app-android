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
    @Query("SELECT * FROM goals WHERE isArchived = 1 ORDER BY lastUpdated DESC")
    fun getArchivedGoalsWithStages(): Flow<List<GoalWithStages>>

    // UPDATED: Changed from suspend fun to return a Flow for reactive updates
    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalWithStages(goalId: String): Flow<GoalWithStages?>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): Goal? // Changed from Flow to suspend fun

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goal_stages WHERE goalId = :goalId ORDER BY orderIndex ASC, CreatedAt ASC")
    fun getStagesForGoal(goalId: String): Flow<List<GoalStage>>

    @Query("SELECT * FROM goal_stages WHERE id = :stageId")
    suspend fun getGoalStageById(stageId: String): GoalStage? // Renamed for clarity from getGoalStage

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalStage(stage: GoalStage)

    @Update
    suspend fun updateGoalStage(stage: GoalStage)

    @Update
    suspend fun updateGoalStages(stages: List<GoalStage>)

    @Delete
    suspend fun deleteGoalStage(stage: GoalStage)

    @Query("DELETE FROM goal_stages WHERE goalId = :goalId")
    suspend fun deleteAllStagesForGoal(goalId: String)

    // ADDED: Transaction to ensure atomic deletion of a goal and its stages
    @Transaction
    suspend fun deleteGoalAndStages(goalId: String) {
        // First, get the goal to delete it by object, which cascades deletes if set up,
        // or just to ensure it exists before deleting its children.
        getGoalById(goalId)?.let {
            deleteAllStagesForGoal(goalId)
            deleteGoal(it)
        }
    }

    // ADDED: Query to archive a goal
    @Query("UPDATE goals SET isArchived = 1, lastUpdated = :timestamp WHERE id = :goalId")
    suspend fun archiveGoal(goalId: String, timestamp: Long = System.currentTimeMillis())
}
