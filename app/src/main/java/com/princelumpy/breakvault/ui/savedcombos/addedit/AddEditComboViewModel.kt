package com.princelumpy.breakvault.ui.savedcombos.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import com.princelumpy.breakvault.data.repository.SavedComboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditComboUiState(
    val comboId: String? = null,
    val comboName: String = "",
    val selectedMoves: List<String> = emptyList(),
    val allMoves: List<Move> = emptyList(),
    val searchText: String = "",
    val expanded: Boolean = false,
    val isNewCombo: Boolean = true
)

@HiltViewModel
class AddEditComboViewModel @Inject constructor(
    private val savedComboRepository: SavedComboRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditComboUiState())
    val uiState: StateFlow<AddEditComboUiState> = _uiState.asStateFlow()

    init {
        // Collect the flow of all moves from the repository
        savedComboRepository.getAllMoves()
            .onEach { moves ->
                _uiState.update { it.copy(allMoves = moves) }
            }.launchIn(viewModelScope)
    }

    fun loadCombo(comboId: String?) {
        if (comboId == null) {
            _uiState.update { AddEditComboUiState(allMoves = it.allMoves) } // Keep the loaded moves
            return
        }

        viewModelScope.launch {
            val comboToEdit = savedComboRepository.getSavedComboById(comboId)
            if (comboToEdit != null) {
                _uiState.update {
                    it.copy(
                        comboId = comboId,
                        comboName = comboToEdit.name,
                        selectedMoves = comboToEdit.moves,
                        isNewCombo = false
                    )
                }
            }
        }
    }

    fun onComboNameChange(newName: String) {
        if (newName.length <= 30) {
            _uiState.update { it.copy(comboName = newName) }
        }
    }

    fun onSearchTextChange(newText: String) {
        if (newText.length <= 50) {
            _uiState.update { it.copy(searchText = newText, expanded = true) }
        }
    }

    fun onExpandedChange(expanded: Boolean) {
        _uiState.update { it.copy(expanded = expanded) }
    }

    fun addMoveToCombo(move: String) {
        val currentMoves = _uiState.value.selectedMoves.toMutableList()
        currentMoves.add(move)
        _uiState.update { it.copy(selectedMoves = currentMoves, searchText = "", expanded = false) }
    }

    fun removeMoveFromCombo(index: Int) {
        val currentMoves = _uiState.value.selectedMoves.toMutableList()
        currentMoves.removeAt(index)
        _uiState.update { it.copy(selectedMoves = currentMoves) }
    }

    fun saveCombo(onSuccess: () -> Unit) {
        val currentUiState = _uiState.value
        if (currentUiState.comboName.isNotBlank() && currentUiState.selectedMoves.isNotEmpty()) {
            viewModelScope.launch {
                if (currentUiState.isNewCombo) {
                    savedComboRepository.insertSavedCombo(
                        SavedCombo(
                            name = currentUiState.comboName,
                            moves = currentUiState.selectedMoves
                        )
                    )
                } else {
                    savedComboRepository.updateSavedCombo(
                        currentUiState.comboId!!,
                        currentUiState.comboName,
                        currentUiState.selectedMoves
                    )
                }
                onSuccess()
            }
        }
    }
    // onCleared() is no longer needed
}
