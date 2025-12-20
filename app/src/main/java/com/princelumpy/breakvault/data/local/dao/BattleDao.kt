package com.princelumpy.breakvault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface BattleDao {

    /** Retrieves all battle combos from the database. */
    @Query("SELECT * FROM battle_combos")
    suspend fun getAllBattleCombos(): List<BattleCombo>

    /** Retrieves all battle tags from the database. */
    @Query("SELECT * FROM battle_tags")
    suspend fun getAllBattleTags(): List<BattleTag>

    /** Retrieves all battle combo-tag cross-references. */
    @Query("SELECT * FROM battle_combo_tag_cross_ref")
    suspend fun getAllBattleComboTagCrossRefs(): List<BattleComboTagCrossRef>

    /** Inserts a list of battle combos, replacing any conflicts. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBattleCombos(battleCombos: List<BattleCombo>)

    /** Inserts a list of battle tags, replacing any conflicts. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBattleTags(battleTags: List<BattleTag>)

    /** Inserts a list of combo-tag cross-references, replacing any conflicts. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBattleComboTagCrossRefs(crossRefs: List<BattleComboTagCrossRef>)

    /** Retrieves a flow of all battle combos with their associated tags, ordered by energy. */
    @Transaction
    @Query("SELECT * FROM battle_combos ORDER BY energy ASC")
    fun getAllBattleCombosWithTags(): Flow<List<BattleComboWithTags>>

    /** Retrieves a single battle combo with its associated tags by its ID. */
    @Transaction
    @Query("SELECT * FROM battle_combos WHERE id = :id")
    suspend fun getBattleComboWithTags(id: String): BattleComboWithTags?

    /** Inserts a single battle combo, replacing it on conflict. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattleCombo(battleCombo: BattleCombo)

    /** Updates an existing battle combo. */
    @Update
    suspend fun updateBattleCombo(battleCombo: BattleCombo)

    /**
     * Atomically updates a battle combo and its associated tags.
     * It first updates the combo, then removes all existing tag links,
     * and finally creates new links based on the provided tag names.
     */
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

    /** Deletes a battle combo from the database. */
    @Delete
    suspend fun deleteBattleCombo(battleCombo: BattleCombo)

    /** Resets the 'isUsed' status of all battle combos to false. */
    @Query("UPDATE battle_combos SET isUsed = 0")
    suspend fun resetAllBattleCombosUsage()

    /** Retrieves a flow of all battle tags, ordered by name. */
    @Query("SELECT * FROM battle_tags ORDER BY name ASC")
    fun getAllBattleTagsFlow(): Flow<List<BattleTag>>

    /** Retrieves a single battle tag by its name. */
    @Query("SELECT * FROM battle_tags WHERE name = :name")
    suspend fun getBattleTagByName(name: String): BattleTag?

    /** Inserts a single battle tag, replacing it on conflict. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattleTag(tag: BattleTag)

    /** Updates the name of a specific battle tag by its ID. */
    @Query("UPDATE battle_tags SET name = :newName WHERE id = :tagId")
    suspend fun updateTagName(tagId: String, newName: String)

    @Update
    suspend fun updateBattleTag(tag: BattleTag)

    @Delete
    suspend fun deleteBattleTag(tag: BattleTag)

    /**
     * Atomically deletes a battle tag and its associations.
     * This first unlinks the tag from all combos and then deletes the tag itself.
     */
    @Transaction
    suspend fun deleteTagCompletely(tagId: String) {
        unlinkTagFromAllBattleCombos(tagId)
        deleteTagById(tagId)
    }

    /** Deletes a battle tag by its ID. */
    @Query("DELETE FROM battle_tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: String)

    /** Creates a link between a battle combo and a battle tag. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun link(crossRef: BattleComboTagCrossRef)

    /** Removes all tag associations for a specific battle combo. */
    @Query("DELETE FROM battle_combo_tag_cross_ref WHERE battleComboId = :battleComboId")
    suspend fun unlinkBattleComboFromAllTags(battleComboId: String)

    /** Removes all combo associations for a specific battle tag. */
    @Query("DELETE FROM battle_combo_tag_cross_ref WHERE battleTagId = :battleTagId")
    suspend fun unlinkTagFromAllBattleCombos(battleTagId: String)
}
