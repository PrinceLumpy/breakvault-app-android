package com.princelumpy.breakvault.data.repository

import com.princelumpy.breakvault.data.local.dao.GoalDao
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.relation.GoalWithStages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
}
