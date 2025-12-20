package com.princelumpy.breakvault.ui.moves.managetags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.repository.MoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// State representing the user's direct inputs.
data class UserInputs(
    val newTagName: String = "",
    val tagNameForEdit: String = ""
)

// State for transient UI events like showing dialogs.
data class DialogState(
    val showAddDialog: Boolean = false,
    val tagForEditDialog: MoveTag? = null,
    val tagForDeleteDialog: MoveTag? = null
)

// The final, combined state for the UI to consume.
data class MoveTagListUiState(
    val tags: List<MoveTag> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState()
)

@HiltViewModel
class MoveTagListViewModel @Inject constructor(
    private val moveRepository: MoveRepository
) : ViewModel() {

    // Private state flows for each distinct concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogState = MutableStateFlow(DialogState())

    // The single source of truth for the UI, created by combining multiple flows.
    val uiState: StateFlow<MoveTagListUiState> = combine(
        moveRepository.getAllTags(), // Data from repository
        _userInputs,                 // User's text input
        _dialogState                 // State of dialogs
    ) { tags, userInputs, dialogState ->
        MoveTagListUiState(
            tags = tags,
            userInputs = userInputs,
            dialogState = dialogState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MoveTagListUiState()
    )

    // --- User Input Handlers ---

    fun onNewTagNameChange(name: String) {
        if (name.length <= 30) {
            _userInputs.update { it.copy(newTagName = name) }
        }
    }

    fun onTagNameForEditChange(name: String) {
        if (name.length <= 30) {
            _userInputs.update { it.copy(tagNameForEdit = name) }
        }
    }

    // --- Dialog State Handlers ---

    fun onAddTagClicked() {
        _userInputs.update { it.copy(newTagName = "") } // Clear previous input before showing
        _dialogState.update { it.copy(showAddDialog = true) }
    }

    fun onAddTagDialogDismiss() {
        _dialogState.update { it.copy(showAddDialog = false) }
    }

    fun onEditTagClicked(tag: MoveTag) {
        _userInputs.update { it.copy(tagNameForEdit = tag.name) } // Pre-fill input
        _dialogState.update { it.copy(tagForEditDialog = tag) }
    }

    fun onEditTagDialogDismiss() {
        _dialogState.update { it.copy(tagForEditDialog = null) }
    }

    fun onDeleteTagClicked(tag: MoveTag) {
        _dialogState.update { it.copy(tagForDeleteDialog = tag) }
    }

    fun onDeleteTagDialogDismiss() {
        _dialogState.update { it.copy(tagForDeleteDialog = null) }
    }

    // --- Data Operation Handlers ---

    fun onAddTag() {
        val newTagName = _userInputs.value.newTagName.trim()
        if (newTagName.isNotBlank()) {
            viewModelScope.launch {
                val newMoveTag = MoveTag(name = newTagName, id = UUID.randomUUID().toString())
                moveRepository.insertMoveTag(newMoveTag)
                // Hide dialog on success
                onAddTagDialogDismiss()
            }
        }
    }

    fun onUpdateTag() {
        val tagToEdit = _dialogState.value.tagForEditDialog ?: return
        val newName = _userInputs.value.tagNameForEdit.trim()

        if (newName.isNotBlank() && newName != tagToEdit.name) {
            viewModelScope.launch {
                moveRepository.updateTagName(tagToEdit.id, newName)
                // Hide dialog on success
                onEditTagDialogDismiss()
            }
        }
    }

    fun onDeleteTag() {
        val tagToDelete = _dialogState.value.tagForDeleteDialog ?: return
        viewModelScope.launch {
            moveRepository.deleteTagCompletely(tagToDelete)
            // Hide dialog on success
            onDeleteTagDialogDismiss()
        }
    }
}
