package com.princelumpy.breakvault.ui.goals.archived

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
 * UI State for the Archived Goals screen.
 * @param archivedGoals The list of goals that have been archived.
 * @param goalToUnarchive The goal selected for the unarchive confirmation dialog.
 * @param goalToDelete The goal selected for the delete confirmation dialog.
 */
data class ArchivedGoalsUiState(
    val archivedGoals: List<GoalWithStages> = emptyList(),
    val goalToUnarchive: GoalWithStages? = null,
    val goalToDelete: GoalWithStages? = null
)

@HiltViewModel
class ArchivedGoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _goalToUnarchive = MutableStateFlow<GoalWithStages?>(null)
    private val _goalToDelete = MutableStateFlow<GoalWithStages?>(null)

    /** The single source of truth for the UI's state, created by combining multiple flows. */
    val uiState: StateFlow<ArchivedGoalsUiState> = combine(
        goalRepository.getArchivedGoalsWithStages(),
        _goalToUnarchive,
        _goalToDelete
    ) { archivedGoals, goalToUnarchive, goalToDelete ->
        ArchivedGoalsUiState(
            archivedGoals = archivedGoals,
            goalToUnarchive = goalToUnarchive,
            goalToDelete = goalToDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArchivedGoalsUiState()
    )

    // --- Unarchive Logic ---

    /** Shows the confirmation dialog for unarchiving a goal. */
    fun onGoalUnarchiveClicked(goal: GoalWithStages) {
        _goalToUnarchive.value = goal
    }

    /** Cancels the unarchive action and hides the dialog. */
    fun onCancelGoalUnarchive() {
        _goalToUnarchive.value = null
    }

    /** Confirms the unarchive action and updates the goal. */
    fun onConfirmGoalUnarchive() {
        _goalToUnarchive.value?.let { goalToUnarchive ->
            viewModelScope.launch {
                val goal = goalToUnarchive.goal.copy(isArchived = false)
                goalRepository.updateGoal(goal)
                // Hide the dialog after the operation
                onCancelGoalUnarchive()
            }
        }
    }

    // --- Delete Logic ---

    /** Shows the confirmation dialog for deleting a goal. */
    fun onGoalDeleteClicked(goal: GoalWithStages) {
        _goalToDelete.value = goal
    }

    /** Cancels the delete action and hides the dialog. */
    fun onCancelGoalDelete() {
        _goalToDelete.value = null
    }

    /** Confirms the deletion of the selected goal. */
    fun onConfirmGoalDelete() {
        _goalToDelete.value?.let { goalToDelete ->
            viewModelScope.launch {
                goalRepository.deleteGoal(goalToDelete.goal)
                // Hide the dialog after the operation
                onCancelGoalDelete()
            }
        }
    }

    // The onCleared() method is no longer needed.
}
