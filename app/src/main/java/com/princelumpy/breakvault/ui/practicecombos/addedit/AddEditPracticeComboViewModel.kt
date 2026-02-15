package com.princelumpy.breakvault.ui.practicecombos.addedit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import com.princelumpy.breakvault.data.repository.PracticeComboRepository
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
    val isNewCombo: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class AddEditPracticeComboViewModel @Inject constructor(
    private val practiceComboRepository: PracticeComboRepository
) : ViewModel() {

    // Separate state flows for each concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogsAndMessages = MutableStateFlow(UiDialogsAndMessages())
    private val _metadata =
        MutableStateFlow<Pair<String?, Boolean>>(null to true) // Pair<comboId, isNewCombo>
    private val _isInitialLoadDone = MutableStateFlow(false)

    // Track original values to detect changes
    private var originalComboName: String? = null
    private var originalSelectedMoves: List<String>? = null

    // Track pending reordered moves (only saved when FAB clicked)
    private var pendingMovesOrder: List<String>? = null

    val uiState: StateFlow<AddEditComboUiState> = combine(
        practiceComboRepository.getAllMoves(),
        _userInputs,
        _dialogsAndMessages,
        _metadata,
        _isInitialLoadDone
    ) { allMoves, userInputs, dialogsAndMessages, metadata, isInitialLoadDone ->
        AddEditComboUiState(
            comboId = metadata.first,
            isNewCombo = metadata.second,
            allMoves = allMoves,
            userInputs = userInputs,
            dialogsAndMessages = dialogsAndMessages,
            isLoading = !isInitialLoadDone
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
            _isInitialLoadDone.value = true
            return
        }

        // Existing combo: Load from repository.
        viewModelScope.launch {
            val comboToEdit = practiceComboRepository.getPracticeComboById(comboId)
            if (comboToEdit != null) {
                originalComboName = comboToEdit.name
                originalSelectedMoves = comboToEdit.moves
                _userInputs.value = UserInputs(
                    comboName = comboToEdit.name,
                    selectedMoves = comboToEdit.moves
                )
                _metadata.value = comboId to false
            }
            _isInitialLoadDone.value = true
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

    fun onMovesReordered(reorderedMoves: List<String>) {
        // Store the pending order; will be saved to DB when user clicks save FAB
        // Do NOT update _userInputs here - let the UI manage local state during and after reorder
        Log.d("PracticeComboVM", "Received reordered moves: $reorderedMoves")
        pendingMovesOrder = reorderedMoves
        Log.d("PracticeComboVM", "pendingMovesOrder is now: $pendingMovesOrder")
    }

    fun onExpandedChange(expanded: Boolean) {
        _dialogsAndMessages.update { it.copy(dropdownExpanded = expanded) }
    }

    // LAYER 3: Action Guard
    fun saveCombo(onSuccess: () -> Unit) {
        val currentUiState = uiState.value
        val inputs = currentUiState.userInputs

        // Trim input values
        val trimmedComboName = inputs.comboName.trim()
        // Trim moves as well (remove leading/trailing whitespace from each move)
        val trimmedMoves = inputs.selectedMoves.map { it.trim() }

        // Don't save if there are no changes (avoids unnecessary database updates)
        if (!hasUnsavedChanges()) {
            onSuccess()
            return
        }

        // Defensive guards against all business rules
        when {
            trimmedComboName.isBlank() -> {
                _dialogsAndMessages.update {
                    it.copy(comboNameError = "Combo name cannot be empty.")
                }
                return
            }

            trimmedComboName.length > COMBO_NAME_CHARACTER_LIMIT -> {
                _dialogsAndMessages.update {
                    it.copy(comboNameError = "Combo name cannot exceed $COMBO_NAME_CHARACTER_LIMIT characters.")
                }
                return
            }

            trimmedMoves.isEmpty() -> {
                _dialogsAndMessages.update {
                    it.copy(movesError = "Please add at least one move to the combo.")
                }
                return
            }
        }

        viewModelScope.launch {
            // Use pending reordered moves if available, otherwise use current input moves
            val finalMoves = pendingMovesOrder?.map { it.trim() } ?: trimmedMoves
            Log.d("PracticeComboVM", "saveCombo: pendingMovesOrder = $pendingMovesOrder")
            Log.d("PracticeComboVM", "saveCombo: trimmedMoves = $trimmedMoves")
            Log.d("PracticeComboVM", "saveCombo: finalMoves (what will be saved) = $finalMoves")

            if (currentUiState.isNewCombo) {
                practiceComboRepository.insertPracticeCombo(
                    PracticeCombo(
                        name = trimmedComboName,
                        moves = finalMoves
                    )
                )
            } else {
                practiceComboRepository.updatePracticeCombo(
                    currentUiState.comboId!!,
                    trimmedComboName,
                    finalMoves
                )
            }

            // Update original values for change detection
            originalSelectedMoves = finalMoves
            // Clear pending order after save
            pendingMovesOrder = null
            onSuccess()
        }
    }

    fun onDeleteComboClick() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = true) }
    }

    fun onConfirmComboDelete(onSuccess: () -> Unit) {
        val comboId = uiState.value.comboId ?: return

        viewModelScope.launch {
            practiceComboRepository.deletePracticeCombo(comboId)
            onSuccess()
        }
    }

    fun onCancelComboDelete() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = false) }
    }

    /**
     * Check if there are unsaved changes in the form.
     * Returns true if any field has been modified.
     */
    fun hasUnsavedChanges(): Boolean {
        val currentState = uiState.value
        val currentInputs = currentState.userInputs

        // For new combos, check if any fields have been filled
        if (currentState.isNewCombo) {
            return currentInputs.comboName.isNotBlank() ||
                    currentInputs.selectedMoves.isNotEmpty()
        }

        // For existing combos, check if any fields have been modified (including pending reorder)
        return currentInputs.comboName != originalComboName ||
                currentInputs.selectedMoves != originalSelectedMoves ||
                pendingMovesOrder != null
    }
}