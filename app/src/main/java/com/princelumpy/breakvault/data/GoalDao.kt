package com.princelumpy.breakvault.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface GoalDao {

    // --- Goals ---

    @Query("SELECT * FROM goals WHERE isArchived = 0 ORDER BY lastUpdated DESC")
    fun getAllActiveGoals(): LiveData<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): Goal?
    
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalByIdLive(goalId: String): LiveData<Goal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    // --- Goal Stages ---

    @Query("SELECT * FROM goal_stages WHERE goalId = :goalId ORDER BY createdAt ASC")
    fun getStagesForGoal(goalId: String): LiveData<List<GoalStage>>

    @Query("SELECT * FROM goal_stages WHERE id = :stageId")
    suspend fun getStageById(stageId: String): GoalStage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalStage(stage: GoalStage)

    @Update
    suspend fun updateGoalStage(stage: GoalStage)

    @Delete
    suspend fun deleteGoalStage(stage: GoalStage)
    
    @Query("DELETE FROM goal_stages WHERE goalId = :goalId")
    suspend fun deleteAllStagesForGoal(goalId: String)
}
