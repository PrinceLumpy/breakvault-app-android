package com.princelumpy.breakvault.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeComboDao {
    /** Gets a flow of all practice combos, ordered by their creation date. */
    @Query("SELECT * FROM practice_combos ORDER BY createdAt DESC")
    fun getAllPracticeCombos(): Flow<List<PracticeCombo>>

    /** Gets a list of all practice combos, ordered by their creation date. */
    @Query("SELECT * FROM practice_combos ORDER BY createdAt DESC")
    suspend fun getAllPracticeCombosList(): List<PracticeCombo>


    /** Inserts a list of practice combos, replacing any conflicts. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPracticeCombos(practiceCombos: List<PracticeCombo>)

    /** Retrieves a single practice combo by its unique ID. */
    @Query("SELECT * FROM practice_combos WHERE id = :practiceComboId")
    suspend fun getPracticeComboById(practiceComboId: String): PracticeCombo?

    /** Inserts a single practice combo, replacing it if it already exists. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPracticeCombo(practiceCombo: PracticeCombo)

    /** Updates an existing practice combo in the database. */
    @Update
    suspend fun updatePracticeCombo(practiceCombo: PracticeCombo)

    /** Updates the name, moves, and timestamp of a specific combo. */
    @Query("UPDATE practice_combos SET name = :newName, moves = :newMoves, modifiedAt = :modifiedAt WHERE id = :comboId")
    suspend fun updatePracticeCombo(
        comboId: String,
        newName: String,
        newMoves: List<String>,
        modifiedAt: Long
    )

    /** Updates only the name of a specific practice combo. */
    @Query("UPDATE practice_combos SET name = :newName WHERE id = :practiceComboId")
    suspend fun updatePracticeComboName(practiceComboId: String, newName: String)

    /** Deletes a practice combo from the database using its unique ID. */
    @Query("DELETE FROM practice_combos WHERE id = :practiceComboId")
    suspend fun deletePracticeComboById(practiceComboId: String)
}
