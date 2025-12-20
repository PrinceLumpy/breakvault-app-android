package com.princelumpy.breakvault.data.repository

import com.princelumpy.breakvault.data.local.dao.GoalDao
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.local.relation.GoalWithStages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Goal data.
 * It is provided as a Singleton by Hilt for application-wide use.
 */
@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {

    /** Retrieves a flow of all active goals with their stages. */
    fun getActiveGoalsWithStages(): Flow<List<GoalWithStages>> {
        return goalDao.getAllActiveGoalsWithStages()
    }

    /** Retrieves a flow of all archived goals with their stages. */
    fun getArchivedGoalsWithStages(): Flow<List<GoalWithStages>> {
        return goalDao.getArchivedGoalsWithStages()
    }

    /** Retrieves a flow for a single goal with its stages. */
    fun getGoalWithStages(id: String): Flow<GoalWithStages?> {
        return goalDao.getGoalWithStages(id)
    }

    /** Retrieves a single goal by its ID. */
    suspend fun getGoalById(id: String): Goal? {
        return withContext(Dispatchers.IO) {
            goalDao.getGoalById(id)
        }
    }

    /** Inserts a new goal into the database. */
    suspend fun insertGoal(goal: Goal) {
        withContext(Dispatchers.IO) {
            goalDao.insertGoal(goal)
        }
    }

    /**
     * Updates a given goal in the database. This is a main-safe suspend function.
     * @param goal The goal to be updated.
     */
    suspend fun updateGoal(goal: Goal) {
        withContext(Dispatchers.IO) {
            goalDao.updateGoal(goal)
        }
    }

    /**
     * Deletes a given goal from the database. This is a main-safe suspend function.
     * @param goal The goal to be deleted.
     */
    suspend fun deleteGoal(goal: Goal) {
        withContext(Dispatchers.IO) {
            goalDao.deleteGoal(goal)
        }
    }

    /** Deletes a goal and all its associated stages. */
    suspend fun deleteGoalAndStages(goalId: String) {
        withContext(Dispatchers.IO) {
            goalDao.deleteGoalAndStages(goalId)
        }
    }

    /** Archives a goal by its ID. */
    suspend fun archiveGoal(goalId: String) {
        withContext(Dispatchers.IO) {
            goalDao.archiveGoal(goalId)
        }
    }

    /**
     * Retrieves a single goal stage by its ID. This is a main-safe suspend function.
     * @param id The ID of the goal stage.
     */
    suspend fun getGoalStageById(id: String): GoalStage? {
        return withContext(Dispatchers.IO) {
            goalDao.getGoalStageById(id)
        }
    }

    /**
     * Inserts a new goal stage into the database. This is a main-safe suspend function.
     * @param stage The GoalStage to be inserted.
     */
    suspend fun insertGoalStage(stage: GoalStage) {
        withContext(Dispatchers.IO) {
            goalDao.insertGoalStage(stage)
        }
    }

    /**
     * Updates an existing goal stage in the database. This is a main-safe suspend function.
     * @param stage The GoalStage to be updated.
     */
    suspend fun updateGoalStage(stage: GoalStage) {
        withContext(Dispatchers.IO) {
            goalDao.updateGoalStage(stage)
        }
    }

    /**
     * Deletes a given goal stage from the database. This is a main-safe suspend function.
     * @param stage The GoalStage to be deleted.
     */
    suspend fun deleteGoalStage(stage: GoalStage) {
        withContext(Dispatchers.IO) {
            goalDao.deleteGoalStage(stage)
        }
    }

    /** Returns a flow that immediately emits null. Used for new goals. */
    fun getEmptyGoalWithStagesFlow(): Flow<GoalWithStages?> {
        return flowOf(null)
    }
}
