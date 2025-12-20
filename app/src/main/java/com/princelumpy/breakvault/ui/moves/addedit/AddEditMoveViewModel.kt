package com.princelumpy.breakvault.ui.moves.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import com.princelumpy.breakvault.data.repository.MoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditMoveUiState(
    val moveId: String? = null,
    val moveName: String = "",
    val newTagName: String = "",
    val allTags: List<MoveTag> = emptyList(),
    val selectedTags: Set<MoveTag> = emptySet(),
    val snackbarMessage: String? = null,
    val isNewMove: Boolean = true
)

@HiltViewModel
class AddEditMoveViewModel @Inject constructor(
    private val moveRepository: MoveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditMoveUiState())
    val uiState: StateFlow<AddEditMoveUiState> = _uiState.asStateFlow()

    private var newlyAddedTagName: String? = null
    private val allTags: Flow<List<MoveTag>> = moveRepository.getAllTags()

    init {
        allTags.onEach { tags ->
            _uiState.update { currentState ->
                val newTagName = newlyAddedTagName
                if (newTagName != null) {
                    val newTagObject = tags.find { it.name.equals(newTagName, ignoreCase = true) }
                    if (newTagObject != null) {
                        newlyAddedTagName = null
                        return@update currentState.copy(
                            allTags = tags,
                            selectedTags = currentState.selectedTags + newTagObject
                        )
                    }
                }
                currentState.copy(allTags = tags)
            }
        }.launchIn(viewModelScope)
    }

    fun loadMove(moveId: String?) {
        if (moveId == null) {
            _uiState.value = AddEditMoveUiState()
            return
        }

        viewModelScope.launch {
            val moveBeingEdited = getMoveForEditing(moveId)
            if (moveBeingEdited != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        moveId = moveId,
                        moveName = moveBeingEdited.move.name,
                        selectedTags = moveBeingEdited.moveTags.toSet(),
                        isNewMove = false
                    )
                }
            } else {
                _uiState.update { it.copy(snackbarMessage = "Could not find move with ID $moveId") }
            }
        }
    }

    fun onMoveNameChange(newName: String) {
        if (newName.length <= 100) {
            _uiState.update { it.copy(moveName = newName) }
        }
    }

    fun onNewTagNameChange(newName: String) {
        if (newName.length <= 30) {
            _uiState.update { it.copy(newTagName = newName) }
        }
    }

    fun onTagSelected(tag: MoveTag) {
        val currentSelectedTags = _uiState.value.selectedTags
        val newSelectedTags = if (currentSelectedTags.any { it.id == tag.id }) {
            currentSelectedTags.filterNot { it.id == tag.id }.toSet()
        } else {
            currentSelectedTags + tag
        }
        _uiState.update { it.copy(selectedTags = newSelectedTags) }
    }

    fun addTag() {
        val newTagName = _uiState.value.newTagName.trim()
        if (newTagName.isNotBlank()) {
            if (!_uiState.value.allTags.any { it.name.equals(newTagName, ignoreCase = true) }) {
                newlyAddedTagName = newTagName
                addTag(newTagName)
                _uiState.update { it.copy(newTagName = "") }
            } else {
                _uiState.update { it.copy(snackbarMessage = "Tag '$newTagName' already exists.") }
            }
        }
    }

    fun saveMove(onSuccess: () -> Unit) {
        val currentUiState = _uiState.value
        if (currentUiState.moveName.isNotBlank()) {
            if (currentUiState.isNewMove) {
                addMove(currentUiState.moveName, currentUiState.selectedTags.toList())
            } else {
                updateMoveAndTags(
                    currentUiState.moveId!!,
                    currentUiState.moveName,
                    currentUiState.selectedTags.toList()
                )
            }
            onSuccess()
        } else {
            _uiState.update { it.copy(snackbarMessage = "Move name cannot be blank.") }
        }
    }

    fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun addMove(moveName: String, selectedMoveTags: List<MoveTag>) {
        viewModelScope.launch {
            val newMoveId = UUID.randomUUID().toString()
            val move = Move(id = newMoveId, name = moveName)
            moveRepository.insertMoveWithTags(move, selectedMoveTags)
        }
    }

    private fun addTag(tagName: String) {
        viewModelScope.launch {
            moveRepository.insertMoveTag(MoveTag(id = UUID.randomUUID().toString(), name = tagName))
        }
    }

    private suspend fun getMoveForEditing(moveId: String): MoveWithTags? =
        moveRepository.getMoveWithTags(moveId)

    private fun updateMoveAndTags(
        moveId: String,
        newName: String,
        newSelectedMoveTags: List<MoveTag>
    ) {
        viewModelScope.launch {
            moveRepository.updateMoveWithTags(moveId, newName, newSelectedMoveTags)
        }
    }
}
