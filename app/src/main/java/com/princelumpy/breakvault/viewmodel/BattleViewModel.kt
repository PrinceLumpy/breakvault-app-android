package com.princelumpy.breakvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.BattleCombo
import com.princelumpy.breakvault.data.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.BattleComboWithTags
import com.princelumpy.breakvault.data.BattleTag
import com.princelumpy.breakvault.data.EnergyLevel
import com.princelumpy.breakvault.data.TrainingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BattleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDB.getDatabase(application)
    private val battleComboDao = db.battleComboDao()
    private val battleTagDao = db.battleTagDao()

    val battleCombos: LiveData<List<BattleComboWithTags>> = battleComboDao.getAllBattleCombos()
    val allBattleTags: LiveData<List<BattleTag>> = battleTagDao.getAllBattleTags()

    // Battle Combo Methods
    fun toggleUsed(combo: BattleCombo) {
        viewModelScope.launch(Dispatchers.IO) {
            // Do not update modifiedAt when toggling isUsed
            battleComboDao.updateBattleCombo(combo.copy(isUsed = !combo.isUsed))
        }
    }

    fun resetBattle() {
        viewModelScope.launch(Dispatchers.IO) {
            battleComboDao.resetAllBattleCombosUsage()
        }
    }

    fun addBattleCombo(description: String, energy: EnergyLevel, status: TrainingStatus, tags: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCombo = BattleCombo(
                description = description,
                energy = energy,
                status = status
            )
            // createdAt/modifiedAt defaults are applied in constructor
            battleComboDao.insertBattleCombo(newCombo)

            tags.forEach { tagName ->
                var tag = battleTagDao.getBattleTagByName(tagName)
                // Create moveListTag if it doesn't exist (though UI typically ensures this)
                if (tag == null) {
                    tag = BattleTag(name = tagName)
                    battleTagDao.insertBattleTag(tag)
                }
                // Link
                battleComboDao.link(BattleComboTagCrossRef(newCombo.id, tag.id))
            }
        }
    }
    
    fun updateBattleCombo(combo: BattleCombo, tagNames: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            // Ensure modifiedAt is updated for content changes
            battleComboDao.updateBattleCombo(combo.copy(modifiedAt = System.currentTimeMillis()))
            
            // Update cross-refs: Delete old, Insert new
            battleComboDao.deleteBattleComboTagCrossRefs(combo.id)
            
            tagNames.forEach { tagName ->
                var tag = battleTagDao.getBattleTagByName(tagName)
                if (tag == null) {
                    tag = BattleTag(name = tagName)
                    battleTagDao.insertBattleTag(tag)
                }
                battleComboDao.link(BattleComboTagCrossRef(combo.id, tag.id))
            }
        }
    }

    fun deleteBattleCombo(combo: BattleCombo) {
        viewModelScope.launch(Dispatchers.IO) {
            battleComboDao.deleteBattleCombo(combo)
        }
    }

    suspend fun getBattleComboById(id: String): BattleComboWithTags? {
        return withContext(Dispatchers.IO) {
            battleComboDao.getBattleComboById(id)
        }
    }

    // Battle MoveListTag Methods
    fun addBattleTag(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (battleTagDao.getBattleTagByName(name) == null) {
                battleTagDao.insertBattleTag(BattleTag(name = name))
            }
        }
    }

    fun updateBattleTag(tag: BattleTag) {
        viewModelScope.launch(Dispatchers.IO) {
            battleTagDao.updateBattleTag(tag.copy(modifiedAt = System.currentTimeMillis()))
        }
    }

    fun deleteBattleTag(tag: BattleTag) {
        viewModelScope.launch(Dispatchers.IO) {
            battleTagDao.deleteBattleTag(tag)
        }
    }
}
