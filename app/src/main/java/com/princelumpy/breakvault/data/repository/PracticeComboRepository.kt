package com.princelumpy.breakvault.data.repository

import com.princelumpy.breakvault.data.local.dao.MoveDao
import com.princelumpy.breakvault.data.local.dao.PracticeComboDao
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeComboRepository @Inject constructor(
    private val practiceComboDao: PracticeComboDao,
    private val moveDao: MoveDao
) {

    /**
     * Gets a flow of all practice combos, ordered by the last update time.
     * The flow automatically updates when the underlying data changes.
     */
    fun getPracticeCombos(): Flow<List<PracticeCombo>> {
        return practiceComboDao.getAllPracticeCombos()
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
     * Retrieves a single practice combo by its ID. Main-safe.
     */
    suspend fun getPracticeComboById(comboId: String): PracticeCombo? {
        return withContext(Dispatchers.IO) {
            practiceComboDao.getPracticeComboById(comboId)
        }
    }

    /**
     * Inserts a new practice combo. Main-safe.
     */
    suspend fun insertPracticeCombo(combo: PracticeCombo) {
        withContext(Dispatchers.IO) {
            practiceComboDao.insertPracticeCombo(combo)
        }
    }

    /**
     * Updates an existing practice combo. Main-safe.
     */
    suspend fun updatePracticeCombo(
        comboId: String,
        newName: String,
        newMoves: List<String>
    ) {
        withContext(Dispatchers.IO) {
            practiceComboDao.updatePracticeCombo(
                comboId,
                newName,
                newMoves,
                System.currentTimeMillis()
            )
        }
    }

    /**
     * Deletes a practice combo from the database by its ID. Main-safe.
     *
     * @param comboId The unique ID of the combo to be deleted.
     */
    suspend fun deletePracticeCombo(comboId: String) {
        withContext(Dispatchers.IO) {
            practiceComboDao.deletePracticeComboById(comboId)
        }
    }
}
