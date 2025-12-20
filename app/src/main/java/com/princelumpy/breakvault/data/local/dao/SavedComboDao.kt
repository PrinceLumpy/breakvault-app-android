package com.princelumpy.breakvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedComboDao {
    /** Gets a flow of all saved combos, ordered by their creation date. */
    @Query("SELECT * FROM saved_combos ORDER BY createdAt DESC")
    fun getAllSavedCombos(): Flow<List<SavedCombo>>

    /** Gets a list of all saved combos, ordered by their creation date. */
    @Query("SELECT * FROM saved_combos ORDER BY createdAt DESC")
    suspend fun getAllSavedCombosList(): List<SavedCombo>


    /** Inserts a list of saved combos, replacing any conflicts. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavedCombos(savedCombos: List<SavedCombo>)

    /** Retrieves a single saved combo by its unique ID. */
    @Query("SELECT * FROM saved_combos WHERE id = :savedComboId")
    suspend fun getSavedComboById(savedComboId: String): SavedCombo?

    /** Inserts a single saved combo, replacing it if it already exists. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedCombo(savedCombo: SavedCombo)

    /** Updates an existing saved combo in the database. */
    @Update
    suspend fun updateSavedCombo(savedCombo: SavedCombo)

    /** Updates the name, moves, and timestamp of a specific combo. */
    @Query("UPDATE saved_combos SET name = :newName, moves = :newMoves, modifiedAt = :modifiedAt WHERE id = :comboId")
    suspend fun updateSavedCombo(
        comboId: String,
        newName: String,
        newMoves: List<String>,
        modifiedAt: Long
    )

    /** Updates only the name of a specific saved combo. */
    @Query("UPDATE saved_combos SET name = :newName WHERE id = :savedComboId")
    suspend fun updateSavedComboName(savedComboId: String, newName: String)

    /** Deletes a saved combo from the database using its unique ID. */
    @Query("DELETE FROM saved_combos WHERE id = :savedComboId")
    suspend fun deleteSavedComboById(savedComboId: String)
}
