package com.princelumpy.breakvault.ui.savedcombos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import com.princelumpy.breakvault.data.repository.SavedComboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * The UI state for the Saved Combos screen.
 */
data class SavedCombosUiState(
    val savedCombos: List<SavedCombo> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SavedComboListViewModel @Inject constructor(
    savedComboRepository: SavedComboRepository
) : ViewModel() {

    /** The single source of truth for the UI's state. */
    val uiState: StateFlow<SavedCombosUiState> = savedComboRepository.getSavedCombos()
        .map { combos ->
            SavedCombosUiState(
                savedCombos = combos,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SavedCombosUiState()
        )
}