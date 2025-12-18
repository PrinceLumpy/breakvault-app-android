package com.princelumpy.breakvault.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import com.princelumpy.breakvault.data.local.entity.BattleTag

@Dao
interface BattleDao {

    @Query("SELECT * FROM battle_combos")
    suspend fun getAllBattleCombos(): List<BattleCombo>

    @Query("SELECT * FROM battle_tags")
    suspend fun getAllBattleTags(): List<BattleTag>

    @Query("SELECT * FROM battle_combo_tag_cross_ref")
    suspend fun getAllBattleComboTagCrossRefs(): List<BattleComboTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBattleCombos(battleCombos: List<BattleCombo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBattleTags(battleTags: List<BattleTag>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBattleComboTagCrossRefs(crossRefs: List<BattleComboTagCrossRef>)

    @Transaction
    @Query("SELECT * FROM battle_combos ORDER BY energy ASC")
    fun getAllBattleCombosWithTags(): LiveData<List<BattleComboWithTags>>

    @Transaction
    @Query("SELECT * FROM battle_combos WHERE id = :id")
    suspend fun getBattleComboWithTags(id: String): BattleComboWithTags?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattleCombo(battleCombo: BattleCombo)

    @Update
    suspend fun updateBattleCombo(battleCombo: BattleCombo)

    @Transaction
    suspend fun updateBattleComboWithTags(battleCombo: BattleCombo, tags: List<String>) {
        updateBattleCombo(battleCombo)
        unlinkBattleComboFromAllTags(battleCombo.id)
        tags.forEach { tagName ->
            val tag = getBattleTagByName(tagName)
            if (tag != null) {
                link(BattleComboTagCrossRef(battleCombo.id, tag.id))
            }
        }
    }

    @Delete
    suspend fun deleteBattleCombo(battleCombo: BattleCombo)

    @Query("UPDATE battle_combos SET isUsed = 0")
    suspend fun resetAllBattleCombosUsage()

    @Query("SELECT * FROM battle_tags ORDER BY name ASC")
    fun getAllBattleTagsLiveData(): LiveData<List<BattleTag>>

    @Query("SELECT * FROM battle_tags WHERE name = :name")
    suspend fun getBattleTagByName(name: String): BattleTag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattleTag(tag: BattleTag)

    @Update
    suspend fun updateBattleTag(tag: BattleTag)

    @Delete
    suspend fun deleteBattleTag(tag: BattleTag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun link(crossRef: BattleComboTagCrossRef)

    @Query("DELETE FROM battle_combo_tag_cross_ref WHERE battleComboId = :battleComboId")
    suspend fun unlinkBattleComboFromAllTags(battleComboId: String)

    @Query("DELETE FROM battle_combo_tag_cross_ref WHERE battleTagId = :battleTagId")
    suspend fun unlinkTagFromAllBattleCombos(battleTagId: String)
}
