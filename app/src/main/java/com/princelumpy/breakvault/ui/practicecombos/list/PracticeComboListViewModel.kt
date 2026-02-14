package com.princelumpy.breakvault.ui.practicecombos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import com.princelumpy.breakvault.data.repository.PracticeComboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * The UI state for the Practice Combos screen.
 */
data class PracticeCombosUiState(
    val practiceCombos: List<PracticeCombo> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PracticeComboListViewModel @Inject constructor(
    practiceComboRepository: PracticeComboRepository
) : ViewModel() {

    /** The single source of truth for the UI's state. */
    val uiState: StateFlow<PracticeCombosUiState> = practiceComboRepository.getPracticeCombos()
        .map { combos ->
            PracticeCombosUiState(
                practiceCombos = combos,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PracticeCombosUiState()
        )
}