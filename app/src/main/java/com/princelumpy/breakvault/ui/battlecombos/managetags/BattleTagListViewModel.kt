package com.princelumpy.breakvault.ui.battlecombos.managetags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.common.Constants.BATTLE_TAG_CHARACTER_LIMIT
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.repository.BattleRepository
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
    val tagForEditDialog: BattleTag? = null,
    val tagForDeleteDialog: BattleTag? = null
)

// The final, combined state for the UI to consume.
data class BattleTagListUiState(
    val tags: List<BattleTag> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState(),
    val newTagNameError: String? = null,
    val editTagNameError: String? = null
)

@HiltViewModel
class BattleTagListViewModel @Inject constructor(
    private val battleRepository: BattleRepository
) : ViewModel() {

    // Private state flows for each distinct concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogState = MutableStateFlow(DialogState())
    private val _newTagNameError = MutableStateFlow<String?>(null)
    private val _editTagNameError = MutableStateFlow<String?>(null)


    // The single source of truth for the UI, created by combining multiple flows.
    val uiState: StateFlow<BattleTagListUiState> = combine(
        battleRepository.getAllTags(), // Data from repository
        _userInputs,                 // User's text input
        _dialogState,                // State of dialogs
        _newTagNameError,            // Error messages
        _editTagNameError
    ) { tags, userInputs, dialogState, newTagNameError, editTagNameError ->
        BattleTagListUiState(
            tags = tags,
            userInputs = userInputs,
            dialogState = dialogState,
            newTagNameError = newTagNameError,
            editTagNameError = editTagNameError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BattleTagListUiState()
    )

    // --- User Input Handlers ---

    fun onNewTagNameChange(name: String) {
        if (name.length <= BATTLE_TAG_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(newTagName = name) }
            // Clear error if user corrects input
            if (_newTagNameError.value != null) {
                _newTagNameError.update { null }
            }
        }
    }

    fun onTagNameForEditChange(name: String) {
        if (name.length <= BATTLE_TAG_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(tagNameForEdit = name) }
            // Clear error if user corrects input
            if (_editTagNameError.value != null) {
                _editTagNameError.update { null }
            }
        }
    }

    // --- Dialog State Handlers ---

    fun onAddTagClicked() {
        _newTagNameError.update { null } // Clear error
        _dialogState.update { it.copy(showAddDialog = true) }
    }

    fun onAddTagDialogDismiss() {
        _dialogState.update { it.copy(showAddDialog = false) }
        _userInputs.update { it.copy(newTagName = "") } // Clear input
    }

    fun onEditTagClicked(tag: BattleTag) {
        _userInputs.update { it.copy(tagNameForEdit = tag.name) } // Pre-fill input
        _editTagNameError.update { null }
        _dialogState.update { it.copy(tagForEditDialog = tag) }
    }

    fun onEditTagDialogDismiss() {
        _dialogState.update { it.copy(tagForEditDialog = null) }
        _userInputs.update { it.copy(tagNameForEdit = "") }
    }

    fun onDeleteTagClicked(tag: BattleTag) {
        _dialogState.update { it.copy(tagForDeleteDialog = tag) }
    }

    fun onDeleteTagDialogDismiss() {
        _dialogState.update { it.copy(tagForDeleteDialog = null) }
    }

    // --- Data Operation Handlers ---

    fun onAddTag() {
        val newTagName = _userInputs.value.newTagName.trim()
        val allTagNames = uiState.value.tags.map { it.name.lowercase() }

        // begin validation
        if (newTagName.isBlank()) {
            _newTagNameError.value = "Tag name cannot be blank"
            return
        }
        if (newTagName.length > BATTLE_TAG_CHARACTER_LIMIT) {
            _newTagNameError.value = "Tag name cannot exceed $BATTLE_TAG_CHARACTER_LIMIT characters"
            return
        }
        if (allTagNames.contains(newTagName.lowercase())) {
            _newTagNameError.value = "Tag name already exists"
            return
        }
        // end validation
        viewModelScope.launch {
            val newBattleTag = BattleTag(name = newTagName, id = UUID.randomUUID().toString())
            battleRepository.insertBattleTag(newBattleTag)
            onAddTagDialogDismiss()
        }
    }

    fun onUpdateTag() {
        val tagToEdit = _dialogState.value.tagForEditDialog ?: return
        val newName = _userInputs.value.tagNameForEdit.trim()

        val allOtherTagNames = uiState.value.tags
            .filter { it.id != tagToEdit.id }
            .map { it.name.lowercase() }

        // begin validation
        if (newName.isBlank()) {
            _editTagNameError.value = "Tag name cannot be empty."
            return
        }
        if (newName.length > BATTLE_TAG_CHARACTER_LIMIT) {
            _editTagNameError.value = "Tag cannot exceed $BATTLE_TAG_CHARACTER_LIMIT characters."
            return
        }
        if (allOtherTagNames.contains(newName.lowercase())) {
            _editTagNameError.value = "Another tag with this name already exists."
            return
        }
        // end validation

        // If no change, dismiss dialog
        if (newName == tagToEdit.name) {
            onEditTagDialogDismiss()
            return
        }

        viewModelScope.launch {
            battleRepository.updateTagName(tagToEdit.id, newName)
            onEditTagDialogDismiss()
        }
    }

    fun onDeleteTag() {
        val tagToDelete = _dialogState.value.tagForDeleteDialog ?: return
        viewModelScope.launch {
            battleRepository.deleteTagCompletely(tagToDelete)
            // Hide dialog on success
            _dialogState.update { it.copy(tagForDeleteDialog = null) }
        }
    }
}
