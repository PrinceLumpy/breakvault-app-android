package com.princelumpy.breakvault.ui.savedcombos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

/**
 * State to manage which dialogs are shown.
 * This holds the combo currently being considered for deletion.
 */
data class DialogState(
    val comboToDelete: SavedCombo? = null
)

/**
 * The final, combined state for the UI to consume.
 */
data class SavedCombosUiState(
    val savedCombos: List<SavedCombo> = emptyList(),
    val dialogState: DialogState = DialogState()
)

@HiltViewModel
class SavedComboListViewModel @Inject constructor(
    private val savedComboRepository: SavedComboRepository
) : ViewModel() {

    // Single source of truth for all dialog-related states.
    private val _dialogState = MutableStateFlow(DialogState())

    /** The single source of truth for the UI's state, created by combining multiple flows. */
    val uiState: StateFlow<SavedCombosUiState> = combine(
        savedComboRepository.getSavedCombos(),
        _dialogState
    ) { combos, dialogState ->
        SavedCombosUiState(
            savedCombos = combos,
            dialogState = dialogState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavedCombosUiState()
    )

    /** Shows the confirmation dialog for deleting a combo. */
    fun onShowDeleteDialog(savedCombo: SavedCombo) {
        _dialogState.update { it.copy(comboToDelete = savedCombo) }
    }

    /** Cancels the delete action and hides the dialog. */
    fun onCancelDelete() {
        _dialogState.update { it.copy(comboToDelete = null) }
    }

    /** Confirms the deletion of the selected combo. */
    fun onConfirmDelete() {
        _dialogState.value.comboToDelete?.let { combo ->
            viewModelScope.launch {
                savedComboRepository.deleteSavedCombo(combo.id)
                // Hide the dialog after the operation is complete.
                onCancelDelete()
            }
        }
    }
}
