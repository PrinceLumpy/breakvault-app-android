package com.princelumpy.breakvault.ui.moves.managetags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.repository.MoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MoveTagListUiState(
    val tags: List<MoveTag> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: MoveTag? = null,
    val showDeleteDialog: MoveTag? = null,
    val newTagName: String = "",
    val tagNameForEdit: String = ""
)

@HiltViewModel
class MoveTagListViewModel @Inject constructor(
    private val moveRepository: MoveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoveTagListUiState())
    val uiState: StateFlow<MoveTagListUiState> = _uiState.asStateFlow()

    init {
        // Use Flow and collect it within the viewModelScope
        moveRepository.getAllTags()
            .onEach { tags ->
                _uiState.update { it.copy(tags = tags) }
            }.launchIn(viewModelScope)
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
                // Use repository to insert the tag
                moveRepository.insertMoveTag(newMoveTag)
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
                // Use repository to update the tag name
                moveRepository.updateTagName(tagToEdit.id, newName)
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
                // Use repository to delete the tag
                moveRepository.deleteTagCompletely(tagToDelete)
                _uiState.update { it.copy(showDeleteDialog = null) }
            }
        }
    }
    // onCleared is no longer needed to remove observers
}
