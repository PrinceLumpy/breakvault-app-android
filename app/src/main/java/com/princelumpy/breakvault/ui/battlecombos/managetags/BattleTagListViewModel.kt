package com.princelumpy.breakvault.ui.battlecombos.managetags // Corrected package name

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

// Final state for the UI, combining all data sources.
data class BattleTagListUiState(
    val tags: List<BattleTag> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState()
)

@HiltViewModel
class BattleTagListViewModel @Inject constructor(
    private val battleRepository: BattleRepository
) : ViewModel() {

    // Private state flows for each distinct concern
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogState = MutableStateFlow(DialogState())

    // The single source of truth for the UI
    val uiState: StateFlow<BattleTagListUiState> = combine(
        battleRepository.getAllTags(), // Data from repository
        _userInputs,                   // User's text input
        _dialogState                   // State of dialogs
    ) { tags, userInputs, dialogState ->
        BattleTagListUiState(
            tags = tags,
            userInputs = userInputs,
            dialogState = dialogState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BattleTagListUiState()
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
        _userInputs.update { it.copy(newTagName = "") } // Clear previous input
        _dialogState.update { it.copy(showAddDialog = true) }
    }

    fun onAddTagDialogDismiss() {
        _dialogState.update { it.copy(showAddDialog = false) }
    }

    fun onEditTagClicked(tag: BattleTag) {
        _userInputs.update { it.copy(tagNameForEdit = tag.name) } // Pre-fill input
        _dialogState.update { it.copy(tagForEditDialog = tag) }
    }

    fun onEditTagDialogDismiss() {
        _dialogState.update { it.copy(tagForEditDialog = null) }
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
        if (newTagName.isNotBlank()) {
            viewModelScope.launch {
                val newBattleTag = BattleTag(name = newTagName, id = UUID.randomUUID().toString())
                battleRepository.insertBattleTag(newBattleTag)
                // Hide dialog on success
                _dialogState.update { it.copy(showAddDialog = false) }
            }
        }
    }

    fun onUpdateTag() {
        val tagToEdit = _dialogState.value.tagForEditDialog ?: return
        val newName = _userInputs.value.tagNameForEdit.trim()

        if (newName.isNotBlank() && newName != tagToEdit.name) {
            viewModelScope.launch {
                battleRepository.updateTagName(tagToEdit.id, newName)
                // Hide dialog on success
                _dialogState.update { it.copy(tagForEditDialog = null) }
            }
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
