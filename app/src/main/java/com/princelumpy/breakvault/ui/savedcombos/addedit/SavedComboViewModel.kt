package com.princelumpy.breakvault.ui.savedcombos.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditComboUiState(
    val comboId: String? = null,
    val comboName: String = "",
    val selectedMoves: List<String> = emptyList(),
    val allMoves: List<Move> = emptyList(),
    val searchText: String = "",
    val expanded: Boolean = false,
    val isNewCombo: Boolean = true
)

class SavedComboViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddEditComboUiState())
    val uiState: StateFlow<AddEditComboUiState> = _uiState.asStateFlow()

    private val savedComboDao = AppDB.getDatabase(application).savedComboDao()
    private val moveDao = AppDB.getDatabase(application).moveDao()

    val allMoves: LiveData<List<Move>> =
        moveDao.getAllMovesWithTags().map { it.map { mwt -> mwt.move } }

    init {
        allMoves.observeForever { moves ->
            _uiState.update { it.copy(allMoves = moves) }
        }
    }

    fun loadCombo(comboId: String?) {
        if (comboId == null) {
            _uiState.value = AddEditComboUiState()
            return
        }

        viewModelScope.launch {
            val comboToEdit = savedComboDao.getSavedComboById(comboId)
            if (comboToEdit != null) {
                _uiState.value = AddEditComboUiState(
                    comboId = comboId,
                    comboName = comboToEdit.name,
                    selectedMoves = comboToEdit.moves,
                    isNewCombo = false
                )
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
                    savedComboDao.insertSavedCombo(
                        SavedCombo(
                            name = currentUiState.comboName,
                            moves = currentUiState.selectedMoves
                        )
                    )
                } else {
                    savedComboDao.updateSavedCombo(
                        currentUiState.comboId!!,
                        currentUiState.comboName,
                        currentUiState.selectedMoves,
                        System.currentTimeMillis()
                    )
                }
                onSuccess()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        allMoves.removeObserver { }
    }
}
