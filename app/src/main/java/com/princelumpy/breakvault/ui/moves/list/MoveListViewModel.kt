package com.princelumpy.breakvault.ui.moves.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import com.princelumpy.breakvault.data.repository.MoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val moveList: List<MoveWithTags> = emptyList(),
    val allTags: List<MoveTag> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val moveToDelete: MoveWithTags? = null,
    val isLoading: Boolean = true,
    val userMessage: String? = null
)

@HiltViewModel
class MoveListViewModel @Inject constructor(
    private val moveRepository: MoveRepository
) : ViewModel() {
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _moveToDelete = MutableStateFlow<MoveWithTags?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> = combine(
        moveRepository.getAllMovesWithTags(),
        moveRepository.getAllTags(),
        _selectedTags,
        _moveToDelete,
        _userMessage
    ) { allMoves, allTags, selectedTags, moveToDelete, userMessage ->
        val filteredMoves = if (selectedTags.isEmpty()) {
            allMoves  // Show all if no tags selected
        } else {
            allMoves.filter { moveWithTags ->
                // Keep move if it has at least one selected tag
                moveWithTags.moveTags.any { tag -> tag.name in selectedTags }
            }
        }

        UiState(
            moveList = filteredMoves,
            allTags = allTags,
            selectedTags = selectedTags,
            moveToDelete = moveToDelete,
            userMessage = userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    // User actions
    fun toggleTagFilter(tagName: String) {
        _selectedTags.update { current ->
            if (tagName in current) {
                current - tagName  // Remove if already selected
            } else {
                current + tagName  // Add if not selected
            }
        }
        // ✅ Filtering happens automatically in combine()
    }

    fun clearFilters() {
        _selectedTags.value = emptySet()
    }

    fun onDeleteMoveClick(moveWithTags: MoveWithTags) {
        _moveToDelete.value = moveWithTags
    }

    fun onConfirmMoveDelete() {
        val move = _moveToDelete.value ?: return
        viewModelScope.launch {
            moveRepository.deleteMove(move.move)
            _moveToDelete.value = null
            _userMessage.value = "Move deleted"
        }
    }

    fun onCancelMoveDelete() {
        _moveToDelete.value = null
    }

    fun clearMessage() {
        _userMessage.value = null
    }
}
