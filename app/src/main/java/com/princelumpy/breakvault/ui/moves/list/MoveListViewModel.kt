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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Private state to hold only the user's direct interactions
private data class UserInteractions(
    val selectedTagNames: Set<String> = emptySet()
)

// Final state for the UI. Contains only the data the UI needs to draw.
data class MoveListUiState(
    val moveList: List<MoveWithTags> = emptyList(),
    val allTags: List<MoveTag> = emptyList(),
    val selectedTagNames: Set<String> = emptySet(),
    val moveToDelete: MoveWithTags? = null,
    val userMessage: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class MoveListViewModel @Inject constructor(
    private val moveRepository: MoveRepository
) : ViewModel() {

    // Consolidate user-driven state
    private val _userInteractions = MutableStateFlow(UserInteractions())

    // State for transient UI events (dialogs, snackbars)
    private val _moveToDelete = MutableStateFlow<MoveWithTags?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MoveListUiState> = combine(
        moveRepository.getAllMovesWithTags(),
        moveRepository.getAllTags(),
        _userInteractions,
        _moveToDelete,
        _userMessage
    ) { allMoves, allTags, interactions, moveToDelete, userMessage ->

        val filteredMoves = if (interactions.selectedTagNames.isEmpty()) {
            allMoves
        } else {
            allMoves.filter { moveWithTags ->
                moveWithTags.moveTags.any { tag -> tag.name in interactions.selectedTagNames }
            }
        }

        MoveListUiState(
            moveList = filteredMoves,
            allTags = allTags,
            selectedTagNames = interactions.selectedTagNames,
            moveToDelete = moveToDelete,
            userMessage = userMessage,
            isLoading = false // Assuming loading is done once the first items arrive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MoveListUiState()
    )

    // User actions now update the single interactions state
    fun toggleTagFilter(tagName: String) {
        _userInteractions.update { current ->
            val newSelectedTags = if (tagName in current.selectedTagNames) {
                current.selectedTagNames - tagName
            } else {
                current.selectedTagNames + tagName
            }
            current.copy(selectedTagNames = newSelectedTags)
        }
    }

    fun clearFilters() {
        _userInteractions.update { it.copy(selectedTagNames = emptySet()) }
    }

    // These methods manage transient state and can remain as they are
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
