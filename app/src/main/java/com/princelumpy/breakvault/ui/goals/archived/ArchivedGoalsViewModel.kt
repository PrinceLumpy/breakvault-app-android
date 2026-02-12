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
 * State to manage which dialogs are shown.
 * This holds the goal currently being considered for an action.
 */
data class DialogState(
    val goalToUnarchive: GoalWithStages? = null,
    val goalToDelete: GoalWithStages? = null
)

/**
 * The final, combined state for the UI to consume.
 */
data class ArchivedGoalsUiState(
    val archivedGoals: List<GoalWithStages> = emptyList(),
    val dialogState: DialogState = DialogState()
)

@HiltViewModel
class ArchivedGoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    // Single source of truth for all dialog-related states.
    private val _dialogState = MutableStateFlow(DialogState())

    /** The single source of truth for the UI's state, created by combining multiple flows. */
    val uiState: StateFlow<ArchivedGoalsUiState> = combine(
        goalRepository.getArchivedGoalsWithStages(),
        _dialogState
    ) { archivedGoals, dialogState ->
        ArchivedGoalsUiState(
            archivedGoals = archivedGoals,
            dialogState = dialogState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArchivedGoalsUiState()
    )

    // --- Unarchive Logic ---

    /** Shows the confirmation dialog for unarchiving a goal. */
    fun onGoalUnarchiveClicked(goal: GoalWithStages) {
        _dialogState.update { it.copy(goalToUnarchive = goal) }
    }

    /** Cancels the unarchive action and hides the dialog. */
    fun onCancelGoalUnarchive() {
        _dialogState.update { it.copy(goalToUnarchive = null) }
    }

    /** Confirms the unarchive action and updates the goal. */
    fun onConfirmGoalUnarchive() {
        _dialogState.value.goalToUnarchive?.let { goalToUnarchive ->
            viewModelScope.launch {
                val goal = goalToUnarchive.goal.copy(
                    isArchived = false,
                    lastUpdated = System.currentTimeMillis()
                )
                goalRepository.updateGoal(goal)
                // Hide the dialog after the operation
                onCancelGoalUnarchive()
            }
        }
    }

    // --- Delete Logic ---

    /** Shows the confirmation dialog for deleting a goal. */
    fun onGoalDeleteClicked(goal: GoalWithStages) {
        _dialogState.update { it.copy(goalToDelete = goal) }
    }

    /** Cancels the delete action and hides the dialog. */
    fun onCancelGoalDelete() {
        _dialogState.update { it.copy(goalToDelete = null) }
    }

    /** Confirms the deletion of the selected goal. */
    fun onConfirmGoalDelete() {
        _dialogState.value.goalToDelete?.let { goalToDelete ->
            viewModelScope.launch {
                goalRepository.deleteGoalAndStages(goalToDelete.goal.id)
                // Hide the dialog after the operation
                onCancelGoalDelete()
            }
        }
    }
}