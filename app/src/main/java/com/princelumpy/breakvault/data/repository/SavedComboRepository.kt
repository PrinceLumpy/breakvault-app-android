package com.princelumpy.breakvault.data.repository

import com.princelumpy.breakvault.data.local.dao.MoveDao
import com.princelumpy.breakvault.data.local.dao.SavedComboDao
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedComboRepository @Inject constructor(
    private val savedComboDao: SavedComboDao,
    private val moveDao: MoveDao
) {

    /**
     * Gets a flow of all saved combos, ordered by the last update time.
     * The flow automatically updates when the underlying data changes.
     */
    fun getSavedCombos(): Flow<List<SavedCombo>> {
        return savedComboDao.getAllSavedCombos()
    }

    /**
     * Gets a flow of all moves, extracting just the Move entity.
     */
    fun getAllMoves(): Flow<List<Move>> {
        return moveDao.getAllMovesWithTags().map { movesWithTags ->
            movesWithTags.map { it.move }
        }
    }

    /**
     * Retrieves a single saved combo by its ID. Main-safe.
     */
    suspend fun getSavedComboById(comboId: String): SavedCombo? {
        return withContext(Dispatchers.IO) {
            savedComboDao.getSavedComboById(comboId)
        }
    }

    /**
     * Inserts a new saved combo. Main-safe.
     */
    suspend fun insertSavedCombo(combo: SavedCombo) {
        withContext(Dispatchers.IO) {
            savedComboDao.insertSavedCombo(combo)
        }
    }

    /**
     * Updates an existing saved combo. Main-safe.
     */
    suspend fun updateSavedCombo(
        comboId: String,
        newName: String,
        newMoves: List<String>
    ) {
        withContext(Dispatchers.IO) {
            savedComboDao.updateSavedCombo(
                comboId,
                newName,
                newMoves,
                System.currentTimeMillis()
            )
        }
    }

    /**
     * Deletes a saved combo from the database by its ID. Main-safe.
     *
     * @param comboId The unique ID of the combo to be deleted.
     */
    suspend fun deleteSavedCombo(comboId: String) {
        withContext(Dispatchers.IO) {
            savedComboDao.deleteSavedComboById(comboId)
        }
    }
}
