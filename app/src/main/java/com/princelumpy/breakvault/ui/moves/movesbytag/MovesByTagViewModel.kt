package com.princelumpy.breakvault.ui.moves.movesbytag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.repository.MoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MovesByTagUiState(
    val moves: List<Move> = emptyList(),
    val tagName: String = "",
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovesByTagViewModel @Inject constructor(
    private val moveRepository: MoveRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Reactively get the tagId from navigation arguments.
    private val tagId: StateFlow<String?> = savedStateHandle.getStateFlow("tagId", null)

    // The single source of truth for the UI.
    val uiState: StateFlow<MovesByTagUiState> = combine(
        // Get the tagName from navigation arguments.
        savedStateHandle.getStateFlow("tagName", ""),

        // Use flatMapLatest to reactively fetch moves whenever the tagId changes.
        tagId.flatMapLatest { id ->
            if (id == null) {
                // If there's no tagId, return a flow with an empty list.
                flowOf(emptyList())
            } else {
                // Fetch the moves for the given tagId from the repository.
                moveRepository.getMovesByTagId(id)
            }
        }
    ) { tagName, moves ->
        MovesByTagUiState(
            tagName = tagName,
            moves = moves,
            // Loading is complete once the first set of moves is emitted.
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        // The initial state shows a loading indicator.
        initialValue = MovesByTagUiState()
    )
}
