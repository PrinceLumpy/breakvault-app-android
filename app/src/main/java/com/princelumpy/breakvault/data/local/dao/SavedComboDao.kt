package com.princelumpy.breakvault.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.princelumpy.breakvault.data.local.entity.SavedCombo

@Dao
interface SavedComboDao {
    @Query("SELECT * FROM saved_combos")
    suspend fun getAllSavedCombos(): List<SavedCombo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavedCombos(savedCombos: List<SavedCombo>)

    @Query("SELECT * FROM saved_combos ORDER BY createdAt DESC")
    fun getAllSavedCombosLiveData(): LiveData<List<SavedCombo>>

    @Query("SELECT * FROM saved_combos WHERE id = :savedComboId")
    suspend fun getSavedComboById(savedComboId: String): SavedCombo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedCombo(savedCombo: SavedCombo)

    @Update
    suspend fun updateSavedCombo(savedCombo: SavedCombo)

    @Query("UPDATE saved_combos SET name = :newName, moves = :newMoves, modifiedAt = :modifiedAt WHERE id = :comboId")
    suspend fun updateSavedCombo(
        comboId: String,
        newName: String,
        newMoves: List<String>,
        modifiedAt: Long
    )

    @Query("UPDATE saved_combos SET name = :newName WHERE id = :savedComboId")
    suspend fun updateSavedComboName(savedComboId: String, newName: String)

    @Query("DELETE FROM saved_combos WHERE id = :savedComboId")
    suspend fun deleteSavedComboById(savedComboId: String)
}
