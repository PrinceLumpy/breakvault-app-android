package com.princelumpy.breakvault.viewmodel

import android.app.Application
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.Goal
import com.princelumpy.breakvault.data.GoalDao
import com.princelumpy.breakvault.data.GoalStage
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoalViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private valtestDispatcher = StandardTestDispatcher()
    
    private val app = mockk<Application>(relaxed = true)
    private val db = mockk<AppDB>(relaxed = true)
    private val goalDao = mockk<GoalDao>(relaxed = true)

    private lateinit var viewModel: GoalViewModel

    private val activeGoalsLiveData = MutableLiveData<List<Goal>>()
    private val goalByIdLiveData = MutableLiveData<Goal?>()
    private val stagesLiveData = MutableLiveData<List<GoalStage>>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Mock AppDB
        mockkObject(AppDB)
        every { AppDB.getDatabase(any()) } returns db
        every { db.goalDao() } returns goalDao

        // Mock DAO returns
        every { goalDao.getAllActiveGoals() } returns activeGoalsLiveData
        every { goalDao.getGoalByIdLive(any()) } returns goalByIdLiveData
        every { goalDao.getStagesForGoal(any()) } returns stagesLiveData

        viewModel = GoalViewModel(app)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `activeGoals returns data from DAO`() {
        val goals = listOf(Goal(title = "G1"), Goal(title = "G2"))
        activeGoalsLiveData.value = goals
        
        assertEquals(goals, viewModel.activeGoals.value)
    }

    @Test
    fun `addGoal inserts goal via DAO`() = runTest {
        val title = "New Goal"
        val description = "Desc"

        viewModel.addGoal(title, description)
        advanceUntilIdle()

        coVerify { 
            goalDao.insertGoal(withArg { 
                assertEquals(title, it.title)
                assertEquals(description, it.description)
            }) 
        }
    }
    
    @Test
    fun `createGoal inserts goal and returns ID`() = runTest {
        val title = "New Goal"
        val description = "Desc"

        val id = viewModel.createGoal(title, description)
        advanceUntilIdle()

        assertNotNull(id)
        coVerify { 
            goalDao.insertGoal(withArg { 
                assertEquals(id, it.id)
                assertEquals(title, it.title)
            }) 
        }
    }

    @Test
    fun `updateGoal updates goal via DAO`() = runTest {
        val goal = Goal(id = "1", title = "Old")
        
        viewModel.updateGoal(goal)
        advanceUntilIdle()

        coVerify { goalDao.updateGoal(any()) }
    }

    @Test
    fun `archiveGoal sets isArchived to true and updates`() = runTest {
        val goal = Goal(id = "1", isArchived = false)
        
        viewModel.archiveGoal(goal)
        advanceUntilIdle()

        coVerify { 
            goalDao.updateGoal(withArg { 
                assertEquals(true, it.isArchived)
            }) 
        }
    }

    @Test
    fun `deleteGoal deletes goal and all stages`() = runTest {
        val goal = Goal(id = "1")
        
        viewModel.deleteGoal(goal)
        advanceUntilIdle()

        coVerify { goalDao.deleteAllStagesForGoal("1") }
        coVerify { goalDao.deleteGoal(goal) }
    }

    @Test
    fun `addGoalStage inserts stage and updates parent goal`() = runTest {
        val goalId = "1"
        val name = "Stage 1"
        val target = 10
        val unit = "reps"
        
        val parentGoal = Goal(id = goalId, lastUpdated = 1000)
        coEvery { goalDao.getGoalById(goalId) } returns parentGoal

        viewModel.addGoalStage(goalId, name, target, unit)
        advanceUntilIdle()

        coVerify { 
            goalDao.insertGoalStage(withArg {
                assertEquals(goalId, it.goalId)
                assertEquals(name, it.name)
            })
        }
        
        // Verify parent goal touched
        coVerify { 
            goalDao.updateGoal(withArg {
                assertEquals(goalId, it.id)
                assert(it.lastUpdated >= 1000)
            })
        }
    }
    
    @Test
    fun `incrementStageProgress updates count and clamps value`() = runTest {
        val goalId = "1"
        val stage = GoalStage(
            id = "s1", 
            goalId = goalId, 
            currentCount = 5, 
            targetCount = 10
        )
        
        val parentGoal = Goal(id = goalId)
        coEvery { goalDao.getGoalById(goalId) } returns parentGoal

        // Case 1: Add 3 (Result 8)
        viewModel.incrementStageProgress(stage, 3)
        advanceUntilIdle()
        
        coVerify { 
            goalDao.updateGoalStage(withArg {
                assertEquals(8, it.currentCount)
            })
        }
        
        // Case 2: Add 100 (Result should be 10 due to clamping)
        viewModel.incrementStageProgress(stage, 100)
        advanceUntilIdle()
        
        coVerify { 
            goalDao.updateGoalStage(withArg {
                assertEquals(10, it.currentCount)
            })
        }
        
        // Case 3: Subtract 100 (Result should be 0 due to clamping)
        viewModel.incrementStageProgress(stage, -100)
        advanceUntilIdle()
        
        coVerify { 
            goalDao.updateGoalStage(withArg {
                assertEquals(0, it.currentCount)
            })
        }
    }
}
