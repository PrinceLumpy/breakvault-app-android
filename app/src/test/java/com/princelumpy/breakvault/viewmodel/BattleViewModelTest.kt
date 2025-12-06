package com.princelumpy.breakvault.viewmodel

import android.app.Application
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.BattleCombo
import com.princelumpy.breakvault.data.BattleComboDao
import com.princelumpy.breakvault.data.BattleComboWithTags
import com.princelumpy.breakvault.data.BattleTag
import com.princelumpy.breakvault.data.BattleTagDao
import com.princelumpy.breakvault.data.EnergyLevel
import com.princelumpy.breakvault.data.TrainingStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BattleViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private val app = mockk<Application>(relaxed = true)
    private val db = mockk<AppDB>()
    private val battleComboDao = mockk<BattleComboDao>(relaxed = true)
    private val battleTagDao = mockk<BattleTagDao>(relaxed = true)

    private lateinit var viewModel: BattleViewModel

    private val battleCombosLiveData = MutableLiveData<List<BattleComboWithTags>>()
    private val battleTagsLiveData = MutableLiveData<List<BattleTag>>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Mock DB
        mockkObject(AppDB)
        every { AppDB.getDatabase(any()) } returns db
        every { db.battleComboDao() } returns battleComboDao
        every { db.battleTagDao() } returns battleTagDao

        // Mock LiveData
        every { battleComboDao.getAllBattleCombos() } returns battleCombosLiveData
        every { battleTagDao.getAllBattleTags() } returns battleTagsLiveData

        viewModel = BattleViewModel(app)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `toggleUsed flips isUsed flag and updates DAO`() = runTest {
        // Given
        val combo = BattleCombo(
            id = "1",
            description = "Test Combo",
            isUsed = false
        )

        // When
        viewModel.toggleUsed(combo)
        advanceUntilIdle()

        // Then
        coVerify {
            battleComboDao.updateBattleCombo(match { updatedCombo ->
                updatedCombo.id == "1" && updatedCombo.isUsed == true
            })
        }
    }

    @Test
    fun `resetBattle calls reset on DAO`() = runTest {
        // When
        viewModel.resetBattle()
        advanceUntilIdle()

        // Then
        coVerify { battleComboDao.resetAllBattleCombosUsage() }
    }

    @Test
    fun `addBattleCombo inserts combo and links existing tags`() = runTest {
        // Given
        val description = "Power Combo"
        val energy = EnergyLevel.HIGH
        val status = TrainingStatus.READY
        val tagName = "Power"
        val existingTag = BattleTag(id = "moveListTag-1", name = tagName)

        // Mock: MoveListTag exists
        coEvery { battleTagDao.getBattleTagByName(tagName) } returns existingTag

        // When
        viewModel.addBattleCombo(description, energy, status, listOf(tagName))
        advanceUntilIdle()

        // Then
        // 1. Verify Combo Insert
        coVerify {
            battleComboDao.insertBattleCombo(match {
                it.description == description && it.energy == energy
            })
        }
        // 2. Verify CrossRef Insert (linking combo to existing moveListTag)
        coVerify {
            battleComboDao.link(match {
                it.battleTagId == "moveListTag-1"
            })
        }
    }

    @Test
    fun `addBattleCombo creates new tag if not found`() = runTest {
        // Given
        val tagName = "New Style"
        // Mock: MoveListTag does NOT exist
        coEvery { battleTagDao.getBattleTagByName(tagName) } returns null

        // When
        viewModel.addBattleCombo("Desc", EnergyLevel.LOW, TrainingStatus.TRAINING, listOf(tagName))
        advanceUntilIdle()

        // Then
        // 1. Verify MoveListTag Creation
        coVerify {
            battleTagDao.insertBattleTag(match { it.name == tagName })
        }
        // 2. Verify CrossRef Insert
        coVerify {
            battleComboDao.link(any())
        }
    }
}
