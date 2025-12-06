package com.princelumpy.breakvault.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface BattleComboDao {
    @Transaction
    @Query("SELECT * FROM battle_combos ORDER BY energy ASC")
    fun getAllBattleCombos(): LiveData<List<BattleComboWithTags>>

    @Transaction
    @Query("SELECT * FROM battle_combos")
    suspend fun getAllBattleCombosList(): List<BattleComboWithTags>

    @Transaction
    @Query("SELECT * FROM battle_combos WHERE id = :id")
    suspend fun getBattleComboById(id: String): BattleComboWithTags?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattleCombo(battleCombo: BattleCombo)

    @Update
    suspend fun updateBattleCombo(battleCombo: BattleCombo)

    @Delete
    suspend fun deleteBattleCombo(battleCombo: BattleCombo)

    @Query("UPDATE battle_combos SET isUsed = 0")
    suspend fun resetAllBattleCombosUsage()

    // Cross Ref Methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun link(crossRef: BattleComboTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBattleComboTagCrossRefs(crossRefs: List<BattleComboTagCrossRef>)

    @Query("DELETE FROM battle_combo_tag_cross_ref WHERE battleComboId = :battleComboId")
    suspend fun deleteBattleComboTagCrossRefs(battleComboId: String)

    @Query("SELECT * FROM battle_combo_tag_cross_ref")
    suspend fun getAllBattleComboTagCrossRefs(): List<BattleComboTagCrossRef>
}
