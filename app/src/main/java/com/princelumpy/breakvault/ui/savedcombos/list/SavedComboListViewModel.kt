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
 * UI state for the Saved Combos screen.
 * @param savedCombos The list of all saved combos.
 * @param comboToDelete The specific combo that the user has prompted to delete, or null.
 */
data class SavedCombosUiState(
    val savedCombos: List<SavedCombo> = emptyList(),
    val comboToDelete: SavedCombo? = null
)

@HiltViewModel
class SavedComboListViewModel @Inject constructor(
    private val savedComboRepository: SavedComboRepository
) : ViewModel() {

    private val _comboToDelete = MutableStateFlow<SavedCombo?>(null)

    /** The single source of truth for the UI's state. */
    val uiState: StateFlow<SavedCombosUiState> = combine(
        savedComboRepository.getSavedCombos(),
        _comboToDelete
    ) { combos, comboToDelete ->
        SavedCombosUiState(
            savedCombos = combos,
            comboToDelete = comboToDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SavedCombosUiState()
    )

    /** Shows the confirmation dialog for deleting a combo. */
    fun onShowDeleteDialog(savedCombo: SavedCombo) {
        _comboToDelete.value = savedCombo
    }

    /** Cancels the delete action and hides the dialog. */
    fun onCancelDelete() {
        _comboToDelete.value = null
    }

    /** Confirms the deletion of the selected combo. */
    fun onConfirmDelete() {
        _comboToDelete.value?.let { combo ->
            viewModelScope.launch {
                savedComboRepository.deleteSavedCombo(combo.id)
                // The dialog will hide automatically as _comboToDelete is part of the combine
                onCancelDelete()
            }
        }
    }
}
