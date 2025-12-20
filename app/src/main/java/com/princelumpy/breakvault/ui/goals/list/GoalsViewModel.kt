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
 * UI State for the Goals screen.
 * @param goals The list of active goals to display.
 * @param isLoading Whether the initial data is being loaded.
 * @param goalToArchive The goal selected by the user for the archive confirmation dialog.
 */
data class GoalsScreenUiState(
    val goals: List<GoalWithStages> = emptyList(),
    val isLoading: Boolean = true,
    val goalToArchive: GoalWithStages? = null
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _goalToArchive = MutableStateFlow<GoalWithStages?>(null)
    private val _isLoading = MutableStateFlow(true)

    /** The single source of truth for the UI's state, combining multiple flows. */
    val uiState: StateFlow<GoalsScreenUiState> = combine(
        goalRepository.getActiveGoalsWithStages(),
        _isLoading,
        _goalToArchive
    ) { goals, isLoading, goalToArchive ->
        // Once the first list of goals is emitted, we can set isLoading to false
        if (_isLoading.value) {
            _isLoading.value = false
        }
        GoalsScreenUiState(
            goals = goals,
            isLoading = isLoading,
            goalToArchive = goalToArchive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsScreenUiState()
    )

    /** Marks a goal to be archived and shows the confirmation dialog. */
    fun onGoalArchiveClicked(goal: GoalWithStages) {
        _goalToArchive.value = goal
    }

    /** Cancels the archive action and hides the dialog. */
    fun onCancelGoalArchive() {
        _goalToArchive.value = null
    }

    /** Confirms the archive action and updates the goal in the database. */
    fun onConfirmGoalArchive() {
        viewModelScope.launch {
            _goalToArchive.value?.let { goalToArchive ->
                val archivedGoal = goalToArchive.goal.copy(isArchived = true)
                goalRepository.updateGoal(archivedGoal)
                // Hide the dialog after the operation is complete
                _goalToArchive.value = null
            }
        }
    }
}
