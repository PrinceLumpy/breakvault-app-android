// Modified by Claude Code - 2026-09-25
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
    val selectedTagNames: Set<String> = emptySet(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = ""
)

// Final state for the UI. Contains only the data the UI needs to draw.
data class MoveListUiState(
    val moveList: List<MoveWithTags> = emptyList(),
    val allTags: List<MoveTag> = emptyList(),
    val selectedTagNames: Set<String> = emptySet(),
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    // True when the user has any moves at all, so an empty moveList means "no matches".
    val hasAnyMoves: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * Applies the tag filter first (moves carrying at least one of [selectedTagNames]), then searches
 * within those by name (case-insensitive). An empty tag set or blank query skips that step.
 */
internal fun filterMoves(
    moves: List<MoveWithTags>,
    selectedTagNames: Set<String>,
    searchQuery: String
): List<MoveWithTags> {
    val tagFiltered = if (selectedTagNames.isEmpty()) {
        moves
    } else {
        moves.filter { moveWithTags ->
            moveWithTags.moveTags.any { tag -> tag.name in selectedTagNames }
        }
    }
    val query = searchQuery.trim()
    return if (query.isEmpty()) {
        tagFiltered
    } else {
        tagFiltered.filter { it.move.name.contains(query, ignoreCase = true) }
    }
}

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
        MoveListUiState(
            moveList = filterMoves(allMoves, interactions.selectedTagNames, interactions.searchQuery),
            allTags = allTags,
            selectedTagNames = interactions.selectedTagNames,
            isSearchActive = interactions.isSearchActive,
            searchQuery = interactions.searchQuery,
            hasAnyMoves = allMoves.isNotEmpty(),
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

    fun onSearchOpen() {
        _userInteractions.update { it.copy(isSearchActive = true) }
    }

    /** Closing search also clears the query; tag filters are left as they are. */
    fun onSearchClose() {
        _userInteractions.update { it.copy(isSearchActive = false, searchQuery = "") }
    }

    fun onSearchQueryChange(query: String) {
        _userInteractions.update { it.copy(searchQuery = query) }
    }

    fun clearFilters() {
        _userInteractions.update { it.copy(selectedTagNames = emptySet()) }
    }
}
