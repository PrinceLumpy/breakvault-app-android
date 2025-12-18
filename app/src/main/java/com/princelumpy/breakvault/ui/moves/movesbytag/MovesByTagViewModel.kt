package com.princelumpy.breakvault.ui.moves.movesbytag

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.local.entity.Move
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovesByTagUiState(
    val moves: List<Move> = emptyList(),
    val tagName: String = "",
    val isLoading: Boolean = true
)

class MovesByTagViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MovesByTagUiState())
    val uiState: StateFlow<MovesByTagUiState> = _uiState.asStateFlow()

    private val moveDao = AppDB.getDatabase(application).moveDao()

    fun loadMovesByTag(tagId: String, tagName: String) {
        _uiState.update { it.copy(tagName = tagName) }
        viewModelScope.launch {
            val moves = moveDao.getTagWithMoves(tagId)?.moves ?: emptyList()
            _uiState.update { it.copy(moves = moves, isLoading = false) }
        }
    }
}
