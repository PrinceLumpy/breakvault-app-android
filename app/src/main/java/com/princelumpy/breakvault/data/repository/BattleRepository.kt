package com.princelumpy.breakvault.data.repository

import com.princelumpy.breakvault.data.local.dao.BattleDao
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Battle Combo data.
 * Acts as a single source of truth for battle combo related data,
 * handling interactions with the local database via BattleDao.
 *
 * It is provided as a Singleton by Hilt for application-wide use.
 */
@Singleton
class BattleRepository @Inject constructor(
    private val battleDao: BattleDao
) {

    /**
     * Retrieves a flow of all battle combos with their associated tags.
     * The flow automatically updates when the underlying data changes.
     */
    fun getAllBattleCombosWithTags(): Flow<List<BattleComboWithTags>> {
        // Room handles running this Flow-based query on a background thread.
        return battleDao.getAllBattleCombosWithTags()
    }

    /** Retrieves a single battle combo with its tags by ID. Main-safe. */
    suspend fun getBattleComboWithTags(id: String): BattleComboWithTags? {
        return withContext(Dispatchers.IO) {
            battleDao.getBattleComboWithTags(id)
        }
    }

    /**
     * Inserts a new battle combo and links its tags. This is a main-safe suspend function.
     * @param battleCombo The new combo to insert.
     * @param tags The list of tags to associate with the combo.
     */
    suspend fun insertBattleComboWithTags(battleCombo: BattleCombo, tags: List<BattleTag>) {
        withContext(Dispatchers.IO) {
            val comboToInsert = battleCombo.copy(id = UUID.randomUUID().toString())
            battleDao.updateBattleComboWithTags(comboToInsert, tags.map { it.id })
        }
    }

    /**
     * Updates an existing battle combo and its tags. This is a main-safe suspend function.
     * @param battleCombo The combo with updated information.
     * @param tags The new list of tags for the combo.
     */
    suspend fun updateBattleComboWithTags(battleCombo: BattleCombo, tags: List<BattleTag>) {
        withContext(Dispatchers.IO) {
            battleDao.updateBattleComboWithTags(battleCombo, tags.map { it.id })
        }
    }

    /**
     * Updates a battle combo. This is a main-safe suspend function.
     */
    suspend fun updateBattleCombo(combo: BattleCombo) {
        withContext(Dispatchers.IO) {
            battleDao.updateBattleCombo(combo)
        }
    }

    /**
     * Deletes a specific battle combo. This is a main-safe suspend function.
     */
    suspend fun deleteBattleCombo(combo: BattleCombo) {
        withContext(Dispatchers.IO) {
            battleDao.deleteBattleCombo(combo)
        }
    }

    /**
     * Resets the 'isUsed' flag for all battle combos. This is a main-safe suspend function.
     */
    suspend fun resetAllBattleCombosUsage() {
        withContext(Dispatchers.IO) {
            battleDao.resetAllBattleCombosUsage()
        }
    }

    // -- Tags --
    /**
     * Retrieves a flow of all battle tags.
     * Renamed from getAllBattleTagsFlow to getAllTags to match ViewModel usage.
     * The flow automatically updates when the underlying data changes.
     */
    fun getAllTags(): Flow<List<BattleTag>> {
        // Room handles running this Flow-based query on a background thread.
        return battleDao.getAllBattleTagsFlow()
    }

    /** Inserts a new battle tag. Main-safe. */
    suspend fun insertBattleTag(tag: BattleTag) {
        withContext(Dispatchers.IO) {
            battleDao.insertBattleTag(tag)
        }
    }

    /**
     * Updates the name of an existing battle tag. Main-safe.
     * @param tagId The ID of the tag to update.
     * @param newName The new name for the tag.
     */
    suspend fun updateTagName(tagId: String, newName: String) {
        withContext(Dispatchers.IO) {
            battleDao.updateTagName(tagId, newName)
        }
    }

    /**
     * Deletes a tag and all its associations with battle combos. Main-safe.
     * @param tag The tag to delete.
     */
    suspend fun deleteTagCompletely(tag: BattleTag) {
        withContext(Dispatchers.IO) {
            battleDao.deleteTagCompletely(tag.id)
        }
    }
}
