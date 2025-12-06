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

@RunWith(AndroidJUnit4::class)
class PracticeDaoTest {

    private lateinit var db: AppDB
    private lateinit var moveTagDao: MoveTagDao
    private lateinit var savedComboDao: SavedComboDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDB::class.java
        ).build()
        moveTagDao = db.moveTagDao()
        savedComboDao = db.savedComboDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetMoveWithTags() = runBlocking {
        // Given
        val move = Move("m1", "Windmill")
        val moveListTag = MoveListTag("t1", "Power")
        
        // When
        moveTagDao.addMove(move)
        moveTagDao.addTag(moveListTag)
        moveTagDao.link(MoveTagCrossRef(move.id, moveListTag.id))

        // Then
        val loaded = moveTagDao.getMoveWithTagsById("m1")
        assertNotNull(loaded)
        assertEquals("Windmill", loaded?.move?.name)
        assertEquals(1, loaded?.moveListTags?.size)
        assertEquals("Power", loaded?.moveListTags?.first()?.name)
    }

    @Test
    fun insertAndGetSavedCombo() = runBlocking {
        // Given
        val combo = SavedCombo(
            id = "c1",
            name = "My Combo",
            moves = listOf("Move1", "Move2")
        )

        // When
        savedComboDao.insertSavedCombo(combo)
        
        // Then
        val loaded = savedComboDao.getSavedComboById("c1")
        assertNotNull(loaded)
        assertEquals("My Combo", loaded?.name)
        assertEquals(2, loaded?.moves?.size)
        assertEquals("Move1", loaded?.moves?.get(0))
    }

    @Test
    fun deleteMoveCascadesToCrossRef() = runBlocking {
        // Given
        val move = Move("m1", "Delete Me")
        val moveListTag = MoveListTag("t1", "MoveListTag")
        moveTagDao.addMove(move)
        moveTagDao.addTag(moveListTag)
        moveTagDao.link(MoveTagCrossRef(move.id, moveListTag.id))

        // When
        moveTagDao.deleteMove(move)

        // Then
        val refs = moveTagDao.getAllMoveTagCrossRefsList()
        assertTrue(refs.isEmpty()) // Should be empty due to CASCADE
    }

    @Test
    fun deleteTagRemovesTagFromMoves() = runBlocking {
        // Given: A move with two moveListTags (Power, Style)
        val move = Move("m1", "Halo")
        val moveListTag1 = MoveListTag("t1", "Power")
        val moveListTag2 = MoveListTag("t2", "Style")
        
        moveTagDao.addMove(move)
        moveTagDao.addTag(moveListTag1)
        moveTagDao.addTag(moveListTag2)
        
        moveTagDao.link(MoveTagCrossRef(move.id, moveListTag1.id))
        moveTagDao.link(MoveTagCrossRef(move.id, moveListTag2.id))

        // Verify setup
        val initialLoad = moveTagDao.getMoveWithTagsById("m1")
        assertEquals(2, initialLoad?.moveListTags?.size)

        // When: We delete one moveListTag (Power)
        moveTagDao.deleteTagCompletely(moveListTag1)

        // Then:
        // 1. The Move should still exist
        val loadedMove = moveTagDao.getMoveWithTagsById("m1")
        assertNotNull(loadedMove)
        
        // 2. The Move should only have 1 moveListTag left (Style)
        assertEquals(1, loadedMove?.moveListTags?.size)
        assertEquals("Style", loadedMove?.moveListTags?.first()?.name)
        
        // 3. The CrossRef for the deleted moveListTag should be gone
        val allRefs = moveTagDao.getAllMoveTagCrossRefsList()
        assertEquals(1, allRefs.size)
        assertEquals("t2", allRefs.first().tagId)
    }

    // --- Referential Integrity Tests ---

    @Test
    fun deleteMoveDoesNotBreakSavedCombo() = runBlocking {
        // Given: A move "Windmill" and a SavedCombo using it
        val move = Move("m1", "Windmill")
        moveTagDao.addMove(move)
        
        val savedCombo = SavedCombo(
            id = "c1",
            name = "Windmill Combo",
            moves = listOf("Windmill", "Flare")
        )
        savedComboDao.insertSavedCombo(savedCombo)

        // When: We delete the Move "Windmill"
        moveTagDao.deleteMove(move)

        // Then: The SavedCombo should STILL exist (loose coupling)
        val loadedCombo = savedComboDao.getSavedComboById("c1")
        assertNotNull(loadedCombo)
        assertEquals("Windmill", loadedCombo?.moves?.get(0)) 
    }

    @Test
    fun cannotLinkInvalidMoveOrTag() = runBlocking {
        // Given: IDs that don't exist
        val badRef = MoveTagCrossRef("bad-move", "bad-moveListTag")

        // When / Then
        try {
            moveTagDao.link(badRef)
            fail("Should have thrown SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {
            // Expected: FK constraint failed
        }
    }
}
