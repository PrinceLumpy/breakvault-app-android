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

// State for the user's direct inputs.
data class UserInputs(
    val comboName: String = "",
    val selectedMoves: List<String> = emptyList(),
    val searchText: String = ""
)

// State for transient UI events like dialogs or dropdowns.
data class DialogState(
    val dropdownExpanded: Boolean = false
)

// The final, combined state for the UI to consume.
data class AddEditComboUiState(
    val comboId: String? = null,
    val allMoves: List<Move> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState(),
    val isNewCombo: Boolean = true
)

@HiltViewModel
class AddEditComboViewModel @Inject constructor(
    private val savedComboRepository: SavedComboRepository
) : ViewModel() {

    // Separate state flows for each concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogState = MutableStateFlow(DialogState())
    private val _metadata =
        MutableStateFlow<Pair<String?, Boolean>>(null to true) // Pair<comboId, isNewCombo>

    val uiState: StateFlow<AddEditComboUiState> = combine(
        savedComboRepository.getAllMoves(),
        _userInputs,
        _dialogState,
        _metadata
    ) { allMoves, userInputs, dialogState, metadata ->
        AddEditComboUiState(
            comboId = metadata.first,
            isNewCombo = metadata.second,
            allMoves = allMoves,
            userInputs = userInputs,
            dialogState = dialogState
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

    // --- User Input Handlers ---

    fun onComboNameChange(newName: String) {
        if (newName.length <= 30) {
            _userInputs.update { it.copy(comboName = newName) }
        }
    }

    fun onSearchTextChange(newText: String) {
        _userInputs.update { it.copy(searchText = newText) }
        // Also expand the dropdown when the user starts typing.
        _dialogState.update { it.copy(dropdownExpanded = true) }
    }

    fun addMoveToCombo(move: String) {
        _userInputs.update {
            it.copy(
                selectedMoves = it.selectedMoves + move,
                searchText = "" // Clear search text after selection
            )
        }
        // Hide dropdown after selection.
        _dialogState.update { it.copy(dropdownExpanded = false) }
    }

    fun removeMoveFromCombo(index: Int) {
        _userInputs.update {
            val newSelectedMoves = it.selectedMoves.toMutableList().apply { removeAt(index) }
            it.copy(selectedMoves = newSelectedMoves)
        }
    }

    // --- Dialog State Handlers ---

    fun onExpandedChange(expanded: Boolean) {
        _dialogState.update { it.copy(dropdownExpanded = expanded) }
    }

    // --- Data Operation Handlers ---

    fun saveCombo(onSuccess: () -> Unit) {
        val currentUiState = uiState.value
        val inputs = currentUiState.userInputs

        if (inputs.comboName.isNotBlank() && inputs.selectedMoves.isNotEmpty()) {
            viewModelScope.launch {
                if (currentUiState.isNewCombo) {
                    savedComboRepository.insertSavedCombo(
                        SavedCombo(
                            name = inputs.comboName,
                            moves = inputs.selectedMoves
                        )
                    )
                } else {
                    savedComboRepository.updateSavedCombo(
                        currentUiState.comboId!!,
                        inputs.comboName,
                        inputs.selectedMoves
                    )
                }
                onSuccess()
            }
        }
    }
}
