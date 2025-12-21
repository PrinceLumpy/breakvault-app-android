package com.princelumpy.breakvault.ui.moves.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.repository.MoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// State for the user's direct text inputs.
data class UserInputs(
    val moveName: String = "",
    val newTagName: String = "",
    val selectedTags: Set<String> = emptySet()
)

// State for transient UI events like dialogs or snack bars.
data class DialogState(
    val snackbarMessage: String? = null
)

// The final, combined state for the UI to consume.
data class AddEditMoveUiState(
    val moveId: String? = null,
    val allTags: List<MoveTag> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState(),
    val isNewMove: Boolean = true
)

@HiltViewModel
class AddEditMoveViewModel @Inject constructor(
    private val moveRepository: MoveRepository
) : ViewModel() {

    // Separate state flows for each concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogState = MutableStateFlow(DialogState())
    private val _metadata =
        MutableStateFlow<Pair<String?, Boolean>>(null to true) // Pair<moveId, isNewMove>

    val uiState: StateFlow<AddEditMoveUiState> = combine(
        moveRepository.getAllTags(),
        _userInputs,
        _dialogState,
        _metadata
    ) { allTags, userInputs, dialogState, metadata ->
        AddEditMoveUiState(
            moveId = metadata.first,
            allTags = allTags,
            userInputs = userInputs,
            dialogState = dialogState,
            isNewMove = metadata.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEditMoveUiState()
    )

    fun loadMove(moveId: String?) {
        if (moveId == null) {
            _metadata.value = null to true
            _userInputs.value = UserInputs()
            return
        }

        viewModelScope.launch {
            val moveWithTags = moveRepository.getMoveWithTags(moveId)
            if (moveWithTags != null) {
                _userInputs.value = UserInputs(
                    moveName = moveWithTags.move.name,
                    selectedTags = moveWithTags.moveTags.map { it.id }.toSet()
                )
                _metadata.value = moveId to false
            } else {
                _dialogState.update { it.copy(snackbarMessage = "Could not find move with ID $moveId") }
            }
        }
    }

    fun onMoveNameChange(newName: String) {
        if (newName.length <= 100) {
            _userInputs.update { it.copy(moveName = newName) }
        }
    }

    fun onNewTagNameChange(newName: String) {
        if (newName.length <= 30) {
            _userInputs.update { it.copy(newTagName = newName) }
        }
    }

    fun onTagSelected(tagId: String) {
        _userInputs.update { currentInputs ->
            val newSelectedTags = if (tagId in currentInputs.selectedTags) {
                currentInputs.selectedTags - tagId
            } else {
                currentInputs.selectedTags + tagId
            }
            currentInputs.copy(selectedTags = newSelectedTags)
        }
    }

    fun addTag() {
        val newTagName = uiState.value.userInputs.newTagName.trim()
        if (newTagName.isNotBlank()) {
            // Use the most recent list of tags from the UI state to check for existence.
            val allTagNames = uiState.value.allTags.map { it.name }
            if (!allTagNames.any { it.equals(newTagName, ignoreCase = true) }) {
                viewModelScope.launch {
                    val newTagId = UUID.randomUUID().toString()
                    moveRepository.insertMoveTag(MoveTag(id = newTagId, name = newTagName))
                    // After inserting, update user inputs to clear the field and select the new tag.
                    _userInputs.update {
                        it.copy(
                            newTagName = "",
                            selectedTags = it.selectedTags + newTagId
                        )
                    }
                }
            } else {
                _dialogState.update { it.copy(snackbarMessage = "Tag '$newTagName' already exists.") }
            }
        }
    }

    fun saveMove(onSuccess: () -> Unit) {
        val currentUiState = uiState.value
        val inputs = currentUiState.userInputs
        if (inputs.moveName.isBlank()) {
            _dialogState.update { it.copy(snackbarMessage = "Move name cannot be blank.") }
            return
        }

        viewModelScope.launch {
            val selectedTagObjects = moveRepository.getAllTags().first()
                .filter { it.id in inputs.selectedTags }

            if (currentUiState.isNewMove) {
                val newMove = Move(id = UUID.randomUUID().toString(), name = inputs.moveName)
                moveRepository.insertMoveWithTags(newMove, selectedTagObjects)
            } else {
                currentUiState.moveId?.let { moveId ->
                    moveRepository.updateMoveWithTags(moveId, inputs.moveName, selectedTagObjects)
                }
            }
            onSuccess()
        }
    }

    fun onSnackbarMessageShown() {
        _dialogState.update { it.copy(snackbarMessage = null) }
    }
}
