package com.princelumpy.breakvault.ui.savedcombos.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import com.princelumpy.breakvault.data.repository.SavedComboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Constants for character limits
private const val COMBO_NAME_CHARACTER_LIMIT = 30

// State for the user's direct inputs.
data class UserInputs(
    val comboName: String = "",
    val selectedMoves: List<String> = emptyList(),
    val searchText: String = ""
)

// State for transient UI events like dialogs, dropdowns, and errors.
data class UiDialogsAndMessages(
    val dropdownExpanded: Boolean = false,
    val comboNameError: String? = null,
    val movesError: String? = null,
    val showDeleteDialog: Boolean = false,
    val snackbarMessage: String? = null
)

// The final, combined state for the UI to consume.
data class AddEditComboUiState(
    val comboId: String? = null,
    val allMoves: List<Move> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogsAndMessages: UiDialogsAndMessages = UiDialogsAndMessages(),
    val isNewCombo: Boolean = true
)

@HiltViewModel
class AddEditComboViewModel @Inject constructor(
    private val savedComboRepository: SavedComboRepository
) : ViewModel() {

    // Separate state flows for each concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogsAndMessages = MutableStateFlow(UiDialogsAndMessages())
    private val _metadata =
        MutableStateFlow<Pair<String?, Boolean>>(null to true) // Pair<comboId, isNewCombo>

    val uiState: StateFlow<AddEditComboUiState> = combine(
        savedComboRepository.getAllMoves(),
        _userInputs,
        _dialogsAndMessages,
        _metadata
    ) { allMoves, userInputs, dialogsAndMessages, metadata ->
        AddEditComboUiState(
            comboId = metadata.first,
            isNewCombo = metadata.second,
            allMoves = allMoves,
            userInputs = userInputs,
            dialogsAndMessages = dialogsAndMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEditComboUiState()
    )

    fun loadCombo(comboId: String?) {
        if (comboId == null) {
            // New combo: Reset state to defaults.
            _metadata.value = null to true
            _userInputs.value = UserInputs()
            return
        }

        // Existing combo: Load from repository.
        viewModelScope.launch {
            val comboToEdit = savedComboRepository.getSavedComboById(comboId)
            if (comboToEdit != null) {
                _userInputs.value = UserInputs(
                    comboName = comboToEdit.name,
                    selectedMoves = comboToEdit.moves
                )
                _metadata.value = comboId to false
            }
        }
    }

    // LAYER 2: State Sanitization
    fun onComboNameChange(newName: String) {
        if (newName.length <= COMBO_NAME_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(comboName = newName) }

            // Clear error on valid input
            if (_dialogsAndMessages.value.comboNameError != null) {
                _dialogsAndMessages.update { it.copy(comboNameError = null) }
            }
        }
    }

    fun onSearchTextChange(newText: String) {
        _userInputs.update { it.copy(searchText = newText) }
        // Also expand the dropdown when the user starts typing.
        _dialogsAndMessages.update { it.copy(dropdownExpanded = true) }
    }

    fun addMoveToCombo(move: String) {
        _userInputs.update {
            it.copy(
                selectedMoves = it.selectedMoves + move,
                searchText = "" // Clear search text after selection
            )
        }

        // Clear moves error when user adds a move
        if (_dialogsAndMessages.value.movesError != null) {
            _dialogsAndMessages.update { it.copy(movesError = null) }
        }

        // Hide dropdown after selection.
        _dialogsAndMessages.update { it.copy(dropdownExpanded = false) }
    }

    fun removeMoveFromCombo(index: Int) {
        _userInputs.update {
            val newSelectedMoves = it.selectedMoves.toMutableList().apply { removeAt(index) }
            it.copy(selectedMoves = newSelectedMoves)
        }
    }

    fun onExpandedChange(expanded: Boolean) {
        _dialogsAndMessages.update { it.copy(dropdownExpanded = expanded) }
    }

    // LAYER 3: Action Guard
    fun saveCombo(onSuccess: () -> Unit) {
        val currentUiState = uiState.value
        val inputs = currentUiState.userInputs

        // Defensive guards against all business rules
        when {
            inputs.comboName.isBlank() -> {
                _dialogsAndMessages.update {
                    it.copy(comboNameError = "Combo name cannot be empty.")
                }
                return
            }

            inputs.comboName.length > COMBO_NAME_CHARACTER_LIMIT -> {
                _dialogsAndMessages.update {
                    it.copy(comboNameError = "Combo name cannot exceed $COMBO_NAME_CHARACTER_LIMIT characters.")
                }
                return
            }

            inputs.selectedMoves.isEmpty() -> {
                _dialogsAndMessages.update {
                    it.copy(movesError = "Please add at least one move to the combo.")
                }
                return
            }
        }

        viewModelScope.launch {
            if (currentUiState.isNewCombo) {
                savedComboRepository.insertSavedCombo(
                    SavedCombo(
                        name = inputs.comboName,
                        moves = inputs.selectedMoves
                    )
                )
                _dialogsAndMessages.update {
                    it.copy(snackbarMessage = "Combo \"${inputs.comboName}\" created successfully!")
                }
            } else {
                savedComboRepository.updateSavedCombo(
                    currentUiState.comboId!!,
                    inputs.comboName,
                    inputs.selectedMoves
                )
                _dialogsAndMessages.update {
                    it.copy(snackbarMessage = "Combo \"${inputs.comboName}\" updated successfully!")
                }
            }
            onSuccess()
        }
    }

    fun onSnackbarShown() {
        _dialogsAndMessages.update { it.copy(snackbarMessage = null) }
    }

    fun onDeleteComboClick() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = true) }
    }

    fun onConfirmComboDelete(onSuccess: () -> Unit) {
        val comboId = uiState.value.comboId ?: return

        viewModelScope.launch {
            savedComboRepository.deleteSavedCombo(comboId)
            onSuccess()
        }
    }

    fun onCancelComboDelete() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = false) }
    }
}