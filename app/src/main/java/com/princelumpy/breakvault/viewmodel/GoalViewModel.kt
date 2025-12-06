package com.princelumpy.breakvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.Goal
import com.princelumpy.breakvault.data.GoalStage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val goalDao = AppDB.getDatabase(application).goalDao()

    val activeGoals: LiveData<List<Goal>> = goalDao.getAllActiveGoals()

    fun addGoal(title: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newGoal = Goal(
                title = title,
                description = description
            )
            goalDao.insertGoal(newGoal)
        }
    }
    
    // Variation that returns the ID, useful for navigation
    fun createGoal(title: String, description: String): String {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            val newGoal = Goal(
                id = id,
                title = title,
                description = description
            )
            goalDao.insertGoal(newGoal)
        }
        return id
    }
    
    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(goal.copy(lastUpdated = System.currentTimeMillis()))
        }
    }

    fun archiveGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(goal.copy(isArchived = true, lastUpdated = System.currentTimeMillis()))
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteAllStagesForGoal(goal.id)
            goalDao.deleteGoal(goal)
        }
    }
    
    fun getGoalById(goalId: String): LiveData<Goal?> {
        return goalDao.getGoalByIdLive(goalId)
    }

    // --- Goal Stages ---

    fun getStagesForGoal(goalId: String): LiveData<List<GoalStage>> {
        return goalDao.getStagesForGoal(goalId)
    }
    
    fun addGoalStage(goalId: String, name: String, targetCount: Int, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stage = GoalStage(
                goalId = goalId,
                name = name,
                targetCount = targetCount,
                unit = unit
            )
            goalDao.insertGoalStage(stage)
            // Touch the parent goal to update 'lastUpdated'
            goalDao.getGoalById(goalId)?.let { 
                goalDao.updateGoal(it.copy(lastUpdated = System.currentTimeMillis()))
            }
        }
    }

    fun updateGoalStage(stage: GoalStage) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoalStage(stage)
        }
    }
    
    fun incrementStageProgress(stage: GoalStage, amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCount = (stage.currentCount + amount).coerceAtLeast(0).coerceAtMost(stage.targetCount)
            goalDao.updateGoalStage(stage.copy(currentCount = newCount))
            // Touch parent goal
            goalDao.getGoalById(stage.goalId)?.let { 
                goalDao.updateGoal(it.copy(lastUpdated = System.currentTimeMillis()))
            }
        }
    }

    fun updateGoalStageById(stageId: String, name: String, targetCount: Int, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stage = goalDao.getStageById(stageId)
            stage?.let {
                goalDao.updateGoalStage(it.copy(name = name, targetCount = targetCount, unit = unit))
            }
        }
    }
    
    fun deleteGoalStage(stage: GoalStage) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoalStage(stage)
        }
    }

    fun deleteGoalStageById(stageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stage = goalDao.getStageById(stageId)
            stage?.let {
                goalDao.deleteGoalStage(it)
            }
        }
    }

    suspend fun getStageById(stageId: String): GoalStage? {
        return goalDao.getStageById(stageId)
    }
}
