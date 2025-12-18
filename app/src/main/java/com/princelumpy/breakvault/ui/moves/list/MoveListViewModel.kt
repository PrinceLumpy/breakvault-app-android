package com.princelumpy.breakvault.ui.moves.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoveListUiState(
    val moves: List<MoveWithTags> = emptyList(),
    val allTags: List<MoveTag> = emptyList(),
    val selectedTags: List<String> = emptyList(),
    val moveToDelete: MoveWithTags? = null,
    val isLoading: Boolean = true
)

class MoveListViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MoveListUiState())
    val uiState: StateFlow<MoveListUiState> = _uiState.asStateFlow()

    private val moveDao = AppDB.getDatabase(application).moveDao()

    init {
        viewModelScope.launch {
            val movesFlow = moveDao.getAllMovesWithTags().asFlow()
            val tagsFlow = moveDao.getAllTags().asFlow()

            combine(movesFlow, tagsFlow, _uiState) { moves, tags, uiState ->
                val filteredMoves = if (uiState.selectedTags.isEmpty()) {
                    moves
                } else {
                    moves.filter { moveWithTags ->
                        moveWithTags.moveTags.any { tag -> tag.name in uiState.selectedTags }
                    }
                }
                uiState.copy(
                    moves = filteredMoves,
                    allTags = tags,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onTagSelected(tag: String) {
        val selectedTags = _uiState.value.selectedTags.toMutableList()
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag)
        } else {
            selectedTags.add(tag)
        }
        _uiState.update { it.copy(selectedTags = selectedTags) }
    }

    fun onMoveDeleteClicked(move: MoveWithTags) {
        _uiState.update { it.copy(moveToDelete = move) }
    }

    fun onConfirmMoveDelete() {
        _uiState.value.moveToDelete?.let { moveToDelete ->
            viewModelScope.launch {
                moveDao.deleteMoveCompletely(moveToDelete.move)
                _uiState.update { it.copy(moveToDelete = null) }
            }
        }
    }

    fun onCancelMoveDelete() {
        _uiState.update { it.copy(moveToDelete = null) }
    }
}
