package com.princelumpy.breakvault.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class BattleDaoTest {

    private lateinit var db: AppDB
    private lateinit var battleComboDao: BattleComboDao
    private lateinit var battleTagDao: BattleTagDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDB::class.java
        ).build()
        battleComboDao = db.battleComboDao()
        battleTagDao = db.battleTagDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetBattleCombo() = runBlocking {
        val combo = BattleCombo(
            id = UUID.randomUUID().toString(),
            description = "Test Combo",
            energy = EnergyLevel.MEDIUM,
            status = TrainingStatus.TRAINING,
            isUsed = false
        )

        battleComboDao.insertBattleCombo(combo)

        val loadedCombo = battleComboDao.getBattleComboById(combo.id)
        assertNotNull(loadedCombo)
        assertEquals("Test Combo", loadedCombo?.battleCombo?.description)
        assertEquals(EnergyLevel.MEDIUM, loadedCombo?.battleCombo?.energy)
    }

    @Test
    fun insertBattleComboWithTags() = runBlocking {
        // 1. Insert Combo
        val comboId = "combo-1"
        val combo = BattleCombo(id = comboId, description = "Tagged Combo")
        battleComboDao.insertBattleCombo(combo)

        // 2. Insert Tags
        val tag1 = BattleTag(id = "moveListTag-1", name = "Power")
        val tag2 = BattleTag(id = "moveListTag-2", name = "Style")
        battleTagDao.insertBattleTag(tag1)
        battleTagDao.insertBattleTag(tag2)

        // 3. Insert CrossRefs
        val crossRef1 = BattleComboTagCrossRef(comboId, "moveListTag-1")
        val crossRef2 = BattleComboTagCrossRef(comboId, "moveListTag-2")
        battleComboDao.link(crossRef1)
        battleComboDao.link(crossRef2)

        // 4. Query
        val loaded = battleComboDao.getBattleComboById(comboId)

        // 5. Verify
        assertNotNull(loaded)
        assertEquals(2, loaded?.tags?.size)
        val tagNames = loaded?.tags?.map { it.name }
        assert(tagNames!!.contains("Power"))
        assert(tagNames.contains("Style"))
    }

    // --- Referential Integrity Tests ---

    @Test
    fun deleteBattleComboCascadesToCrossRef() = runBlocking {
        // Given: Combo linked to MoveListTag
        val comboId = "c1"
        val tagId = "t1"
        battleComboDao.insertBattleCombo(BattleCombo(id = comboId, description = "Desc"))
        battleTagDao.insertBattleTag(BattleTag(id = tagId, name = "MoveListTag"))
        battleComboDao.link(BattleComboTagCrossRef(comboId, tagId))

        // When: Delete Combo
        battleComboDao.deleteBattleCombo(BattleCombo(id = comboId, description = ""))

        // Then: CrossRef should be gone
        val refs = battleComboDao.getAllBattleComboTagCrossRefs()
        assertTrue(refs.isEmpty())
    }

    @Test
    fun deleteBattleTagRemovesTagFromCombo() = runBlocking {
        // Given: Combo linked to MoveListTag
        val comboId = "c1"
        val tagId = "t1"
        battleComboDao.insertBattleCombo(BattleCombo(id = comboId, description = "Desc"))
        battleTagDao.insertBattleTag(BattleTag(id = tagId, name = "MoveListTag"))
        battleComboDao.link(BattleComboTagCrossRef(comboId, tagId))

        // When: Delete MoveListTag
        battleTagDao.deleteBattleTag(BattleTag(id = tagId, name = ""))

        // Then:
        // 1. CrossRef Gone
        val refs = battleComboDao.getAllBattleComboTagCrossRefs()
        assertTrue(refs.isEmpty())

        // 2. Combo still exists
        val combo = battleComboDao.getBattleComboById(comboId)
        assertNotNull(combo)
        assertEquals(0, combo?.tags?.size)
    }

    @Test
    fun cannotInsertCrossRefWithInvalidIds() = runBlocking {
        // Given: IDs that don't exist in DB
        val invalidRef = BattleComboTagCrossRef("bad-combo-id", "bad-moveListTag-id")

        // When / Then
        try {
            battleComboDao.link(invalidRef)
            fail("Should have thrown SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {
            // Expected behavior: Foreign Key constraint failed
        }
    }
}
