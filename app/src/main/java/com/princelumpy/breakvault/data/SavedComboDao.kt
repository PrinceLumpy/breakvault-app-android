package com.princelumpy.breakvault.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SavedComboDao {

    // --- Methods for Export ---
    @Query("SELECT * FROM saved_combos")
    suspend fun getAllSavedCombosList(): List<SavedCombo>

    @Query("SELECT * FROM saved_combo_move_link")
    suspend fun getAllSavedComboMoveLinksList(): List<SavedComboMoveLink>

    // --- Methods for Import ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavedCombos(savedCombos: List<SavedCombo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavedComboMoveLinks(links: List<SavedComboMoveLink>)

    // --- Existing Methods ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedCombo(savedCombo: SavedCombo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedComboMoveLinks(links: List<SavedComboMoveLink>)

    @Transaction // Ensures that reading the SavedCombo and its related Moves is an atomic operation.
    @Query("SELECT * FROM saved_combos ORDER BY createdAt DESC")
    fun getAllSavedCombosWithMoves(): LiveData<List<SavedComboWithMoves>>

    @Transaction
    suspend fun saveFullCombo(savedCombo: SavedCombo, moves: List<Move>) {
        insertSavedCombo(savedCombo)
        val links = moves.mapIndexed { index, move ->
            SavedComboMoveLink(
                savedComboId = savedCombo.id,
                moveId = move.id,
                orderInCombo = index
            )
        }
        insertSavedComboMoveLinks(links)
    }

    @Query("DELETE FROM saved_combos WHERE id = :savedComboId")
    suspend fun deleteSavedComboById(savedComboId: String)

    @Transaction
    @Query("SELECT * FROM saved_combos WHERE id = :savedComboId")
    suspend fun getSavedComboWithMovesById(savedComboId: String): SavedComboWithMoves?

    @Query("UPDATE saved_combos SET name = :newName WHERE id = :savedComboId")
    suspend fun updateSavedComboName(savedComboId: String, newName: String)
}
