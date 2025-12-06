package com.princelumpy.breakvault.viewmodel

import android.app.Application
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.princelumpy.breakvault.data.*
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.transfer.AppDataExport
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
class MoveViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // Forces LiveData to execute on main thread

    private val testDispatcher = StandardTestDispatcher()
    
    private val app = mockk<Application>(relaxed = true)
    private val db = mockk<AppDB>(relaxed = true) // relaxed for clearAllTables
    
    // Mock DAOs
    private val moveTagDao = mockk<MoveTagDao>(relaxed = true)
    private val savedComboDao = mockk<SavedComboDao>(relaxed = true)
    private val battleComboDao = mockk<BattleComboDao>(relaxed = true)
    private val battleTagDao = mockk<BattleTagDao>(relaxed = true)

    private lateinit var viewModel: MoveViewModel

    // Mock LiveData sources
    private val movesWithTagsLiveData = MutableLiveData<List<MoveWithTags>>()
    private val allTagsLiveData = MutableLiveData<List<MoveListTag>>()
    private val savedCombosLiveData = MutableLiveData<List<SavedCombo>>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        // Mock the static AppDB.getDatabase() call and Prepopulate logic
        mockkObject(AppDB)
        every { AppDB.getDatabase(any()) } returns db
        // FIX: Use coEvery for suspend function
        coEvery { AppDB.prepopulateExampleData(any()) } just Runs
        
        // Mock DAO getters in DB
        every { db.moveTagDao() } returns moveTagDao
        every { db.savedComboDao() } returns savedComboDao
        every { db.battleComboDao() } returns battleComboDao
        every { db.battleTagDao() } returns battleTagDao

        // Mock LiveData returns from DAOs
        every { moveTagDao.getMovesWithTags() } returns movesWithTagsLiveData
        every { moveTagDao.getAllTags() } returns allTagsLiveData
        every { savedComboDao.getAllSavedCombos() } returns savedCombosLiveData

        // Initialize ViewModel
        viewModel = MoveViewModel(app)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `generateStructuredCombo returns valid moves when matching tags found`() = runTest {
        // Given
        val moveListTagToprock = MoveListTag("t1", "Toprock")
        val moveListTagFootwork = MoveListTag("t2", "Footwork")
        
        val moveToprock = Move("m1", "Indian Step")
        val moveFootwork = Move("m2", "6-Step")
        
        val moveWithTags1 = MoveWithTags(moveToprock, listOf(moveListTagToprock))
        val moveWithTags2 = MoveWithTags(moveFootwork, listOf(moveListTagFootwork))
        
        // Seed the ViewModel with data
        movesWithTagsLiveData.value = listOf(moveWithTags1, moveWithTags2)
        
        // Let the LiveData observer fire
        advanceUntilIdle() 

        // When requesting a sequence: Toprock -> Footwork
        val requestedSequence = listOf(moveListTagToprock, moveListTagFootwork)
        val result = viewModel.generateStructuredCombo(requestedSequence)

        // Then
        assertEquals(2, result.size)
        assertEquals("Indian Step", result[0].name)
        assertEquals("6-Step", result[1].name)
    }

    @Test
    fun `generateStructuredCombo skips moves if no match for tag`() = runTest {
        // Given
        val moveListTagToprock = MoveListTag("t1", "Toprock")
        val moveListTagFreeze = MoveListTag("t3", "Freeze") // We have no freeze moves in our mock data
        
        val moveToprock = Move("m1", "Indian Step")
        val moveWithTags1 = MoveWithTags(moveToprock, listOf(moveListTagToprock))
        
        movesWithTagsLiveData.value = listOf(moveWithTags1)
        advanceUntilIdle()

        // When requesting: Toprock -> Freeze
        val requestedSequence = listOf(moveListTagToprock, moveListTagFreeze)
        val result = viewModel.generateStructuredCombo(requestedSequence)

        // Then
        assertEquals(1, result.size)
        assertEquals("Indian Step", result[0].name)
        // The second slot is skipped because no move matches "Freeze"
    }

    @Test
    fun `saveCombo inserts combo via DAO`() = runTest {
        // Given
        val comboName = "My Fresh Combo"
        val moves = listOf("Indian Step", "6-Step")

        // When
        viewModel.saveCombo(comboName, moves)
        advanceUntilIdle()

        // Then
        coVerify { 
            savedComboDao.insertSavedCombo(withArg { savedCombo ->
                assertEquals("My Fresh Combo", savedCombo.name)
                assertEquals(moves, savedCombo.moves)
            })
        }
    }
    
    @Test
    fun `getFlashcardMove respects excluded tags`() = runTest {
        // Given
        val moveListTagToprock = MoveListTag("t1", "Toprock")
        val moveListTagFootwork = MoveListTag("t2", "Footwork")
        
        val moveToprock = Move("m1", "Indian Step")
        val moveFootwork = Move("m2", "6-Step")
        
        movesWithTagsLiveData.value = listOf(
            MoveWithTags(moveToprock, listOf(moveListTagToprock)),
            MoveWithTags(moveFootwork, listOf(moveListTagFootwork))
        )
        advanceUntilIdle()
        
        val targetTags = setOf(moveListTagFootwork)
        val result = viewModel.getFlashcardMove(targetTags)
        
        // Then
        assertEquals("6-Step", result?.name)
    }

    @Test
    fun `getAppDataForExport collects data from all DAOs`() = runTest {
        // Given
        val moves = listOf(Move("m1", "Move1"))
        val moveListTags = listOf(MoveTag("t1", "Tag1"))
        val savedCombos = listOf(SavedCombo(id = "s1", name = "Combo1", moves = listOf("Move1")))
        val battleCombosWithTags = listOf(BattleComboWithTags(BattleCombo(id = "b1", description = "Battle1"), emptyList()))

        // Mock DAO returns
        coEvery { moveTagDao.getAllMovesList() } returns moves
        coEvery { moveTagDao.getAllTagsList() } returns moveListTags
        coEvery { moveTagDao.getAllMoveTagCrossRefsList() } returns emptyList()
        coEvery { savedComboDao.getAllSavedCombosList() } returns savedCombos
        coEvery { battleComboDao.getAllBattleCombosList() } returns battleCombosWithTags
        coEvery { battleTagDao.getAllBattleTagsList() } returns emptyList()
        coEvery { battleComboDao.getAllBattleComboTagCrossRefs() } returns emptyList()

        // When
        val result = viewModel.getAppDataForExport()

        // Then
        assertNotNull(result)
        assertEquals(1, result?.moves?.size)
        assertEquals("Move1", result?.moves?.first()?.name)
        assertEquals(1, result?.battleCombos?.size)
        assertEquals("Battle1", result?.battleCombos?.first()?.description)
    }

    @Test
    fun `importAppData clears DB and inserts all data`() = runTest {
        // Given
        val importData = AppDataExport(
            moves = listOf(Move("m1", "NewMove")),
            moveListTags = emptyList(),
            moveTagCrossRefs = emptyList(),
            savedCombos = emptyList(),
            battleCombos = listOf(BattleCombo(id = "b1", description = "NewBattle")),
            battleTags = emptyList(),
            battleComboTagCrossRefs = emptyList()
        )

        // When
        val success = viewModel.importAppData(importData)

        // Then
        assertEquals(true, success)
        
        // Verify DB Clear
        coVerify { db.clearAllTables() }
        
        // Verify Inserts
        coVerify { moveTagDao.insertAllMoves(match { it.size == 1 && it[0].name == "NewMove" }) }
        coVerify { battleComboDao.insertBattleCombo(match { it.description == "NewBattle" }) }
        
        // FIX: Use coVerify for suspend function
        coVerify { AppDB.prepopulateExampleData(db) }
    }

    @Test
    fun `generateComboFromTags allows exceeding pool size when repeats are enabled`() = runTest {
        // Given: Only 2 unique moves in the database
        val moveListTag = MoveListTag("t1", "MoveListTag")
        val move1 = Move("m1", "Move1")
        val move2 = Move("m2", "Move2")
        val moves = listOf(
            MoveWithTags(move1, listOf(moveListTag)),
            MoveWithTags(move2, listOf(moveListTag))
        )
        movesWithTagsLiveData.value = moves
        advanceUntilIdle()

        // When: We ask for a combo of length 50 with repeats
        // (Note: Logic is now clamped to 6, so requesting 50 should return 6)
        val result = viewModel.generateComboFromTags(
            selectedMoveListTags = setOf(moveListTag),
            length = 50,
            allowRepeats = true
        )

        // Then
        // FIX: Logic now clamps to 6, but repeats allow us to exceed pool size (2).
        assertEquals(6, result.size) 
        assert(result.all { it.name == "Move1" || it.name == "Move2" }) 
    }

    @Test
    fun `generateComboFromTags clamps length to pool size when repeats are disabled`() = runTest {
        // Given: Only 2 unique moves
        val moveListTag = MoveListTag("t1", "MoveListTag")
        val move1 = Move("m1", "Move1")
        val move2 = Move("m2", "Move2")
        val moves = listOf(
            MoveWithTags(move1, listOf(moveListTag)),
            MoveWithTags(move2, listOf(moveListTag))
        )
        movesWithTagsLiveData.value = moves
        advanceUntilIdle()

        // When: We ask for a combo of length 50 WITHOUT repeats
        val result = viewModel.generateComboFromTags(
            selectedMoveListTags = setOf(moveListTag),
            length = 50,
            allowRepeats = false
        )

        // Then
        assertEquals(2, result.size) // Should be clamped to pool size (2)
        assert(result.contains(move1))
        assert(result.contains(move2))
    }

    @Test
    fun `generateComboFromTags clamps length to max limit`() = runTest {
        // Given: Pool of 3 unique moves
        val moveListTag = MoveListTag("t1", "MoveListTag")
        val move1 = Move("m1", "Move1")
        val move2 = Move("m2", "Move2")
        val move3 = Move("m3", "Move3")
        val moves = listOf(
            MoveWithTags(move1, listOf(moveListTag)),
            MoveWithTags(move2, listOf(moveListTag)),
            MoveWithTags(move3, listOf(moveListTag))
        )
        movesWithTagsLiveData.value = moves
        advanceUntilIdle()

        // Case 1: Request 3 (Exact) - No Repeats
        val result3 = viewModel.generateComboFromTags(setOf(moveListTag), 3, false)
        assertEquals(3, result3.size)

        // Case 2: Request 2 (Under) - No Repeats
        val result2 = viewModel.generateComboFromTags(setOf(moveListTag), 2, false)
        assertEquals(2, result2.size)

        // Case 3: Request 60 (Over Max) - Repeats Enabled
        // FIX: Logic clamps to 6.
        val result60 = viewModel.generateComboFromTags(setOf(moveListTag), 60, true)
        assertEquals(6, result60.size)
    }
}
