package com.princelumpy.breakvault.ui.goals.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.relation.GoalWithStages
import com.princelumpy.breakvault.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State to manage which dialogs are shown.
 */
data class DialogState(
    val goalToArchive: GoalWithStages? = null
)

/**
 * UI State for the Goals screen.
 */
data class GoalsScreenUiState(
    val goals: List<GoalWithStages> = emptyList(),
    val dialogState: DialogState = DialogState(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    // Single source of truth for all dialog-related states.
    private val _dialogState = MutableStateFlow(DialogState())

    /** The single source of truth for the UI's state, combining multiple flows. */
    val uiState: StateFlow<GoalsScreenUiState> = combine(
        goalRepository.getActiveGoalsWithStages(),
        _dialogState
    ) { goals, dialogState ->
        GoalsScreenUiState(
            goals = goals,
            dialogState = dialogState,
            // Loading is implicitly handled by the initial value of stateIn.
            // Once the first list of goals is emitted, the UI will update.
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsScreenUiState() // isLoading is true in the initial state
    )

    /** Marks a goal to be archived and shows the confirmation dialog. */
    fun onGoalArchiveClicked(goal: GoalWithStages) {
        _dialogState.update { it.copy(goalToArchive = goal) }
    }

    /** Cancels the archive action and hides the dialog. */
    fun onCancelGoalArchive() {
        _dialogState.update { it.copy(goalToArchive = null) }
    }

    /** Confirms the archive action and updates the goal in the database. */
    fun onConfirmGoalArchive() {
        _dialogState.value.goalToArchive?.let { goalToArchive ->
            viewModelScope.launch {
                val archivedGoal = goalToArchive.goal.copy(
                    isArchived = true,
                    lastUpdated = System.currentTimeMillis()
                )
                goalRepository.updateGoal(archivedGoal)
                // Hide the dialog after the operation is complete
                onCancelGoalArchive()
            }
        }
    }
}
