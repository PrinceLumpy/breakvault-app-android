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

// Constants for character limits
private const val MOVE_NAME_CHARACTER_LIMIT = 100
private const val MOVE_TAG_CHARACTER_LIMIT = 30

// State for the user's direct text inputs.
data class UserInputs(
    val moveName: String = "",
    val newTagName: String = "",
    val selectedTags: Set<String> = emptySet()
)

// State for transient UI events like dialogs and errors.
data class UiDialogsAndMessages(
    val snackbarMessage: String? = null,
    val moveNameError: String? = null,
    val newTagError: String? = null,
    val showDeleteDialog: Boolean = false
)

// The final, combined state for the UI to consume.
data class AddEditMoveUiState(
    val moveId: String? = null,
    val allTags: List<MoveTag> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogsAndMessages: UiDialogsAndMessages = UiDialogsAndMessages(),
    val isNewMove: Boolean = true
)

@HiltViewModel
class AddEditMoveViewModel @Inject constructor(
    private val moveRepository: MoveRepository
) : ViewModel() {

    // Separate state flows for each concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogsAndMessages = MutableStateFlow(UiDialogsAndMessages())
    private val _metadata =
        MutableStateFlow<Pair<String?, Boolean>>(null to true) // Pair<moveId, isNewMove>

    val uiState: StateFlow<AddEditMoveUiState> = combine(
        moveRepository.getAllTags(),
        _userInputs,
        _dialogsAndMessages,
        _metadata
    ) { allTags, userInputs, dialogsAndMessages, metadata ->
        AddEditMoveUiState(
            moveId = metadata.first,
            allTags = allTags,
            userInputs = userInputs,
            dialogsAndMessages = dialogsAndMessages,
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
                _dialogsAndMessages.update {
                    it.copy(snackbarMessage = "Could not find move with ID $moveId")
                }
            }
        }
    }

    // LAYER 2: State Sanitization
    fun onMoveNameChange(newName: String) {
        if (newName.length <= MOVE_NAME_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(moveName = newName) }

            // Clear error on valid input
            if (_dialogsAndMessages.value.moveNameError != null) {
                _dialogsAndMessages.update { it.copy(moveNameError = null) }
            }
        }
    }

    // LAYER 2: State Sanitization
    fun onNewTagNameChange(newName: String) {
        if (newName.length <= MOVE_TAG_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(newTagName = newName) }

            // Clear error on valid input
            if (_dialogsAndMessages.value.newTagError != null) {
                _dialogsAndMessages.update { it.copy(newTagError = null) }
            }
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

    // LAYER 3: Action Guard
    fun addTag() {
        val newTagName = uiState.value.userInputs.newTagName.trim()
        val allTagNames = uiState.value.allTags.map { it.name }

        // Defensive guards against all business rules
        when {
            newTagName.isBlank() -> {
                _dialogsAndMessages.update {
                    it.copy(newTagError = "Tag name cannot be empty.")
                }
                return
            }

            newTagName.length > MOVE_TAG_CHARACTER_LIMIT -> {
                _dialogsAndMessages.update {
                    it.copy(newTagError = "Tag cannot exceed $MOVE_TAG_CHARACTER_LIMIT characters.")
                }
                return
            }

            allTagNames.any { it.equals(newTagName, ignoreCase = true) } -> {
                _dialogsAndMessages.update {
                    it.copy(newTagError = "Tag '$newTagName' already exists.")
                }
                return
            }
        }

        // If all checks pass, proceed with insertion
        viewModelScope.launch {
            val newTagId = UUID.randomUUID().toString()
            moveRepository.insertMoveTag(MoveTag(id = newTagId, name = newTagName))

            // Clear the field and select the new tag
            _userInputs.update {
                it.copy(
                    newTagName = "",
                    selectedTags = it.selectedTags + newTagId
                )
            }
        }
    }

    // LAYER 3: Action Guard
    fun saveMove(onSuccess: () -> Unit) {
        val currentUiState = uiState.value
        val inputs = currentUiState.userInputs

        // Defensive guards against all business rules
        when {
            inputs.moveName.isBlank() -> {
                _dialogsAndMessages.update {
                    it.copy(moveNameError = "Move name cannot be empty.")
                }
                return
            }

            inputs.moveName.length > MOVE_NAME_CHARACTER_LIMIT -> {
                _dialogsAndMessages.update {
                    it.copy(moveNameError = "Move name cannot exceed $MOVE_NAME_CHARACTER_LIMIT characters.")
                }
                return
            }
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

    fun onDeleteMoveClick() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = true) }
    }

    fun onConfirmMoveDelete(onSuccess: () -> Unit) {
        val moveId = uiState.value.moveId ?: return

        viewModelScope.launch {
            val moveWithTags = moveRepository.getMoveWithTags(moveId)
            moveWithTags?.let {
                moveRepository.deleteMove(it.move)
                onSuccess()
            }
        }
    }

    fun onCancelMoveDelete() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = false) }
    }

    fun onSnackbarMessageShown() {
        _dialogsAndMessages.update { it.copy(snackbarMessage = null) }
    }
}