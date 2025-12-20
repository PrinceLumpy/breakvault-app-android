package com.princelumpy.breakvault.ui.goals.addedit

import androidx.compose.animation.core.copy
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// State for the user's direct text inputs.
data class UserInputs(
    val title: String = "",
    val description: String = ""
)

// State for transient UI events like dialogs and navigation.
data class DialogState(
    val snackbarMessage: String? = null,
    val navigateToAddStageWithGoalId: String? = null,
    val navigateToEditStage: GoalStage? = null,
    val addingRepsToStage: GoalStage? = null
)

// The final, combined state for the UI.
data class AddEditGoalUiState(
    val goalId: String? = null,
    val stages: List<GoalStage> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState(),
    val isNewGoal: Boolean = true,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddEditGoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val goalId: StateFlow<String?> = savedStateHandle.getStateFlow("goalId", null)

    // Separate state flows for each concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogState = MutableStateFlow(DialogState())
    private val _isInitialLoadDone = MutableStateFlow(false)

    val uiState: StateFlow<AddEditGoalUiState> = combine(
        goalId.flatMapLatest { id ->
            if (id == null) {
                // If it's a new goal, there's no data to fetch.
                goalRepository.getEmptyGoalWithStagesFlow() // Returns flowOf(null)
            } else {
                // If it's an existing goal, fetch its data.
                goalRepository.getGoalWithStages(id)
            }
        },
        _userInputs,
        _dialogState,
        _isInitialLoadDone
    ) { goalWithStages, userInputs, dialogState, isInitialLoadDone ->

        // This logic runs when loading an existing goal for the first time.
        if (goalWithStages != null && !isInitialLoadDone) {
            _userInputs.value = UserInputs(
                title = goalWithStages.goal.title,
                description = goalWithStages.goal.description
            )
            _isInitialLoadDone.value = true // Mark initial load as complete.
        }

        AddEditGoalUiState(
            goalId = goalWithStages?.goal?.id,
            stages = goalWithStages?.stages ?: emptyList(),
            userInputs = userInputs,
            dialogState = dialogState,
            isNewGoal = goalWithStages == null,
            isLoading = goalId.value != null && !isInitialLoadDone
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEditGoalUiState()
    )


    // --- User Input Handlers ---
    fun onTitleChange(newTitle: String) {
        if (newTitle.length <= 100) {
            _userInputs.update { it.copy(title = newTitle) }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _userInputs.update { it.copy(description = newDescription) }
    }

    // --- Data Operation Handlers ---
    fun saveGoal(onSuccess: (goalId: String) -> Unit) {
        viewModelScope.launch {
            val currentInputs = _userInputs.value
            if (currentInputs.title.isBlank()) {
                _dialogState.update { it.copy(snackbarMessage = "Goal title cannot be blank.") }
                return@launch
            }

            val currentGoalId = goalId.value
            if (currentGoalId == null) {
                // Create new goal
                val newGoal = Goal(
                    id = UUID.randomUUID().toString(),
                    title = currentInputs.title,
                    description = currentInputs.description
                )
                goalRepository.insertGoal(newGoal)
                onSuccess(newGoal.id)
            } else {
                // Update existing goal
                goalRepository.getGoalById(currentGoalId)?.let { existingGoal ->
                    val updatedGoal = existingGoal.copy(
                        title = currentInputs.title,
                        description = currentInputs.description,
                        lastUpdated = System.currentTimeMillis()
                    )
                    goalRepository.updateGoal(updatedGoal)
                    onSuccess(currentGoalId)
                }
            }
        }
    }

    fun archiveGoal(onSuccess: () -> Unit) {
        viewModelScope.launch {
            goalId.value?.let {
                goalRepository.archiveGoal(it)
                onSuccess()
            }
        }
    }

    fun deleteGoal(onSuccess: () -> Unit) {
        viewModelScope.launch {
            goalId.value?.let {
                goalRepository.deleteGoalAndStages(it)
                onSuccess()
            }
        }
    }

    fun addRepsToStage(stage: GoalStage, reps: Int) {
        viewModelScope.launch {
            val newCount = stage.currentCount + reps
            if (newCount < 0) {
                _dialogState.update { it.copy(snackbarMessage = "Repetitions cannot be negative.") }
            } else {
                goalRepository.updateGoalStage(stage.copy(currentCount = newCount))
            }
            onAddRepsDismissed() // Always dismiss dialog after action
        }
    }

    // --- Navigation and Dialog Handlers ---
    fun onSnackbarMessageShown() {
        _dialogState.update { it.copy(snackbarMessage = null) }
    }

    fun onAddStageClicked() {
        if (uiState.value.isNewGoal) {
            _dialogState.update { it.copy(snackbarMessage = "Please save the goal before adding a stage.") }
        } else {
            _dialogState.update { it.copy(navigateToAddStageWithGoalId = uiState.value.goalId) }
        }
    }

    fun onNavigateToAddStageDone() {
        _dialogState.update { it.copy(navigateToAddStageWithGoalId = null) }
    }

    fun onEditStageClicked(stage: GoalStage) {
        _dialogState.update { it.copy(navigateToEditStage = stage) }
    }

    fun onNavigateToEditStageDone() {
        _dialogState.update { it.copy(navigateToEditStage = null) }
    }

    fun onAddRepsClicked(stage: GoalStage) {
        _dialogState.update { it.copy(addingRepsToStage = stage) }
    }

    fun onAddRepsDismissed() {
        _dialogState.update { it.copy(addingRepsToStage = null) }
    }
}
