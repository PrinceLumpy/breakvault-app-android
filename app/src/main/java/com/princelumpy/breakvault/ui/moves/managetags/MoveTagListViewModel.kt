package com.princelumpy.breakvault.ui.moves.managetags

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.local.entity.MoveTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class MoveTagListUiState(
    val tags: List<MoveTag> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: MoveTag? = null,
    val showDeleteDialog: MoveTag? = null,
    val newTagName: String = "",
    val tagNameForEdit: String = ""
)

class MoveTagListViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MoveTagListUiState())
    val uiState: StateFlow<MoveTagListUiState> = _uiState.asStateFlow()

    private val moveDao = AppDB.getDatabase(application).moveDao()

    val allTags: LiveData<List<MoveTag>> = moveDao.getAllTags()

    init {
        allTags.observeForever { tags ->
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
                val newMoveTag = MoveTag(name = newTagName, id = UUID.randomUUID().toString())
                moveDao.insertMoveTag(newMoveTag)
                _uiState.update { it.copy(showAddDialog = false) }
            }
        }
    }

    fun onEditTagClicked(tag: MoveTag) {
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
                moveDao.updateTagName(tagToEdit.id, newName, System.currentTimeMillis())
                _uiState.update { it.copy(showEditDialog = null) }
            }
        }
    }

    fun onDeleteTagClicked(tag: MoveTag) {
        _uiState.update { it.copy(showDeleteDialog = tag) }
    }

    fun onDeleteTagDialogDismiss() {
        _uiState.update { it.copy(showDeleteDialog = null) }
    }

    fun onDeleteTag() {
        _uiState.value.showDeleteDialog?.let { tagToDelete ->
            viewModelScope.launch {
                moveDao.deleteTagCompletely(tagToDelete)
                _uiState.update { it.copy(showDeleteDialog = null) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        allTags.removeObserver { }
    }
}
