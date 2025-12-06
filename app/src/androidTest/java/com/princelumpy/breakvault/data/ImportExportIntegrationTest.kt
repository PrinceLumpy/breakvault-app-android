package com.princelumpy.breakvault.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.princelumpy.breakvault.data.transfer.AppDataExport
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImportExportIntegrationTest {

    private lateinit var db: AppDB

    // DAOs
    private lateinit var moveTagDao: MoveTagDao
    private lateinit var savedComboDao: SavedComboDao
    private lateinit var battleComboDao: BattleComboDao
    private lateinit var battleTagDao: BattleTagDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Use an in-memory database for testing.
        // The data will be wiped when the process dies.
        db = Room.inMemoryDatabaseBuilder(context, AppDB::class.java).build()

        moveTagDao = db.moveTagDao()
        savedComboDao = db.savedComboDao()
        battleComboDao = db.battleComboDao()
        battleTagDao = db.battleTagDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testFullImportCycle() = runBlocking {
        // 1. Prepare Complex Import Data
        val move1 = Move("m1", "Windmill")
        val moveListTag1 = MoveListTag("t1", "Power")
        val crossRef1 = MoveTagCrossRef("m1", "t1")

        val savedCombo = SavedCombo(
            id = "s1",
            name = "Power Set",
            moves = listOf("Windmill", "Windmill")
        )

        val battleCombo = BattleCombo(
            id = "b1",
            description = "Battle Round 1",
            energy = EnergyLevel.HIGH,
            status = TrainingStatus.READY
        )
        val battleTag = BattleTag("bt1", "Aggressive")
        val battleRef = BattleComboTagCrossRef("b1", "bt1")

        val exportData = AppDataExport(
            moves = listOf(move1),
            moveListTags = listOf(moveListTag1),
            moveTagCrossRefs = listOf(crossRef1),
            savedCombos = listOf(savedCombo),
            battleCombos = listOf(battleCombo),
            battleTags = listOf(battleTag),
            battleComboTagCrossRefs = listOf(battleRef)
        )

        // 2. Execute Import Logic (Simulating ViewModel logic manually)
        db.clearAllTables() // Ensure clean slate

        moveTagDao.insertAllMoves(exportData.moves)
        moveTagDao.insertAllTags(exportData.moveListTags)
        moveTagDao.insertAllMoveTagCrossRefs(exportData.moveTagCrossRefs)
        savedComboDao.insertAllSavedCombos(exportData.savedCombos)

        exportData.battleCombos.forEach { battleComboDao.insertBattleCombo(it) }
        exportData.battleTags.forEach { battleTagDao.insertBattleTag(it) }
        exportData.battleComboTagCrossRefs.forEach { battleComboDao.link(it) }

        // 3. Verify Practice Mode Data
        val loadedMove = moveTagDao.getMoveWithTagsById("m1")
        assertNotNull("Move should exist", loadedMove)
        assertEquals("Windmill", loadedMove?.move?.name)
        assertEquals("Power", loadedMove?.moveListTags?.first()?.name)

        val loadedSavedCombo = savedComboDao.getSavedComboById("s1")
        assertNotNull("Saved Combo should exist", loadedSavedCombo)
        assertEquals(2, loadedSavedCombo?.moves?.size)

        // 4. Verify Battle Mode Data
        val loadedBattleCombo = battleComboDao.getBattleComboById("b1")
        assertNotNull("Battle Combo should exist", loadedBattleCombo)
        assertEquals(EnergyLevel.HIGH, loadedBattleCombo?.battleCombo?.energy)
        assertEquals("Aggressive", loadedBattleCombo?.tags?.first()?.name)
    }
}
