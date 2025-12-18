package com.princelumpy.breakvault.ui.savedcombos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import com.princelumpy.breakvault.data.repository.SavedComboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedCombosUiState(
    val savedCombos: List<SavedCombo> = emptyList(),
    val comboToDelete: SavedCombo? = null
)

@HiltViewModel
open class SavedComboListViewModel @Inject constructor(
    private val savedComboRepository: SavedComboRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedCombosUiState())
    open val uiState: StateFlow<SavedCombosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            savedComboRepository.getSavedCombos().asFlow().collect { savedCombos ->
                _uiState.update { it.copy(savedCombos = savedCombos) }
            }
        }
    }

    open fun onShowDeleteDialog(savedCombo: SavedCombo) {
        _uiState.update { it.copy(comboToDelete = savedCombo) }
    }

    open fun onCancelDelete() {
        _uiState.update { it.copy(comboToDelete = null) }
    }

    open fun onConfirmDelete() {
        _uiState.value.comboToDelete?.let { combo ->
            viewModelScope.launch {
                savedComboRepository.deleteSavedCombo(combo.id)
                _uiState.update { it.copy(comboToDelete = null) }
            }
        }
    }
}
