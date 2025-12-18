package com.princelumpy.breakvault.ui.battlecombos.managetags

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.local.entity.BattleTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BattleTagListUiState(
    val tags: List<BattleTag> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: BattleTag? = null,
    val showDeleteDialog: BattleTag? = null,
    val newTagName: String = "",
    val tagNameForEdit: String = ""
)

class BattleTagListViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BattleTagListUiState())
    val uiState: StateFlow<BattleTagListUiState> = _uiState.asStateFlow()

    private val battleDao = AppDB.getDatabase(application).battleDao()

    val allBattleTags: LiveData<List<BattleTag>> = battleDao.getAllBattleTagsLiveData()

    init {
        allBattleTags.observeForever { tags ->
            _uiState.update { it.copy(tags = tags) }
        }
    }

    fun onAddTagClicked() {
        _uiState.update { it.copy(showAddDialog = true, newTagName = "") }
    }

    fun onAddTagDialogDismiss() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun onNewTagNameChange(name: String) {
        if (name.length <= 30) {
            _uiState.update { it.copy(newTagName = name) }
        }
    }

    fun onAddTag() {
        val newTagName = _uiState.value.newTagName.trim()
        if (newTagName.isNotBlank()) {
            viewModelScope.launch {
                battleDao.insertBattleTag(BattleTag(name = newTagName))
                _uiState.update { it.copy(showAddDialog = false) }
            }
        }
    }

    fun onEditTagClicked(tag: BattleTag) {
        _uiState.update { it.copy(showEditDialog = tag, tagNameForEdit = tag.name) }
    }

    fun onEditTagDialogDismiss() {
        _uiState.update { it.copy(showEditDialog = null) }
    }

    fun onTagNameForEditChange(name: String) {
        if (name.length <= 30) {
            _uiState.update { it.copy(tagNameForEdit = name) }
        }
    }

    fun onUpdateTag() {
        val state = _uiState.value
        val tagToEdit = state.showEditDialog ?: return
        val newName = state.tagNameForEdit.trim()

        if (newName.isNotBlank() && newName != tagToEdit.name) {
            viewModelScope.launch {
                battleDao.updateBattleTag(tagToEdit.copy(name = newName))
                _uiState.update { it.copy(showEditDialog = null) }
            }
        }
    }

    fun onDeleteTagClicked(tag: BattleTag) {
        _uiState.update { it.copy(showDeleteDialog = tag) }
    }

    fun onDeleteTagDialogDismiss() {
        _uiState.update { it.copy(showDeleteDialog = null) }
    }

    fun onDeleteTag() {
        _uiState.value.showDeleteDialog?.let { tagToDelete ->
            viewModelScope.launch {
                battleDao.deleteBattleTag(tagToDelete)
                _uiState.update { it.copy(showDeleteDialog = null) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        allBattleTags.removeObserver { }
    }
}
