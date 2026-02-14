package com.princelumpy.breakvault.ui.goals.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.GoalStage
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

// State for dialog
data class DialogState(
    val addingRepsToStage: GoalStage? = null
)

/**
 * UI State for the GoalList screen.
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

    private val _dialogState = MutableStateFlow(DialogState())

    /** The single source of truth for the UI's state. */
    val uiState: StateFlow<GoalsScreenUiState> = combine(
        goalRepository.getActiveGoalsWithStages(),
        _dialogState
    ) { goals, dialogState ->
        GoalsScreenUiState(
            goals = goals,
            dialogState = dialogState,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsScreenUiState()
    )

    /** Shows the add reps dialog for a stage. */
    fun onAddRepsClicked(stage: GoalStage) {
        _dialogState.update { it.copy(addingRepsToStage = stage) }
    }

    /** Dismisses the add reps dialog. */
    fun onAddRepsDismissed() {
        _dialogState.update { it.copy(addingRepsToStage = null) }
    }

    /** Adds or subtracts reps from a stage. */
    fun addRepsToStage(stage: GoalStage, reps: Int) {
        viewModelScope.launch {
            val newCount = stage.currentCount + reps
            if (newCount >= 0) {
                val updatedStage = stage.copy(
                    currentCount = newCount,
                    lastUpdated = System.currentTimeMillis()
                )
                goalRepository.updateGoalStage(updatedStage)
            }
            onAddRepsDismissed()
        }
    }
}