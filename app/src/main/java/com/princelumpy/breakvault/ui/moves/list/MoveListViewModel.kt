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

// Private state to hold only the user's direct interactions
private data class UserInteractions(
    val selectedTagNames: Set<String> = emptySet()
)

// Final state for the UI. Contains only the data the UI needs to draw.
data class MoveListUiState(
    val moveList: List<MoveWithTags> = emptyList(),
    val allTags: List<MoveTag> = emptyList(),
    val selectedTagNames: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MoveListViewModel @Inject constructor(
    private val moveRepository: MoveRepository
) : ViewModel() {

    // Consolidate user-driven state
    private val _userInteractions = MutableStateFlow(UserInteractions())

    val uiState: StateFlow<MoveListUiState> = combine(
        moveRepository.getAllMovesWithTags(),
        moveRepository.getAllTags(),
        _userInteractions
    ) { allMoves, allTags, interactions ->

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
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MoveListUiState()
    )

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
}