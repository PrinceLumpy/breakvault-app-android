package com.princelumpy.breakvault.ui.goals.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.common.Constants.GOAL_DESCRIPTION_CHARACTER_LIMIT
import com.princelumpy.breakvault.common.Constants.GOAL_TITLE_CHARACTER_LIMIT
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
    val navigateToEditStage: Pair<String, String>? = null // (goalId, stageId)
)

// State for UI-specific properties like errors and loading status.
data class UiState(
    val titleError: String? = null,
    val descriptionError: String? = null,
    val isInitialLoadDone: Boolean = false
)

// The final, combined state for the UI.
data class AddEditGoalUiState(
    val goalId: String? = null,
    val stages: List<GoalStage> = emptyList(),
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState(),
    val isNewGoal: Boolean = true,
    val isLoading: Boolean = true,
    val titleError: String? = null,
    val descriptionError: String? = null
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
    private val _uiState = MutableStateFlow(UiState())

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
        _uiState
    ) { goalWithStages, userInputs, dialogState, uiState ->

        // This logic runs when loading an existing goal for the first time.
        if (goalWithStages != null && !uiState.isInitialLoadDone) {
            _userInputs.value = UserInputs(
                title = goalWithStages.goal.title,
                description = goalWithStages.goal.description
            )
            _uiState.update { it.copy(isInitialLoadDone = true) }
        }

        AddEditGoalUiState(
            goalId = goalWithStages?.goal?.id,
            stages = goalWithStages?.stages ?: emptyList(),
            userInputs = userInputs,
            dialogState = dialogState,
            isNewGoal = goalWithStages == null,
            isLoading = goalId.value != null && !uiState.isInitialLoadDone,
            titleError = uiState.titleError,
            descriptionError = uiState.descriptionError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEditGoalUiState()
    )

    // --- User Input Handlers ---
    fun onTitleChange(newTitle: String) {
        if (newTitle.length <= GOAL_TITLE_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(title = newTitle) }
            if (_uiState.value.titleError != null) {
                _uiState.update { it.copy(titleError = null) }
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        if (newDescription.length <= GOAL_DESCRIPTION_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(description = newDescription) }
            // Clear error when user starts typing
            if (_uiState.value.descriptionError != null) {
                _uiState.update { it.copy(descriptionError = null) }
            }
        }
    }

    // --- Data Operation Handlers ---
    fun saveGoal(onSuccess: (goalId: String) -> Unit) {
        val currentInputs = _userInputs.value

        // --- Start of Guarding Block ---
        when {
            currentInputs.title.isBlank() -> {
                _uiState.update { it.copy(titleError = "Goal title cannot be blank.") }
                return
            }
            // Defensive length check for title
            currentInputs.title.length > GOAL_TITLE_CHARACTER_LIMIT -> {
                _uiState.update { it.copy(titleError = "Title cannot exceed ${GOAL_TITLE_CHARACTER_LIMIT} characters.") }
                return
            }
            // Defensive length check for description
            currentInputs.description.length > GOAL_DESCRIPTION_CHARACTER_LIMIT -> {
                _uiState.update { it.copy(descriptionError = "Description cannot exceed ${GOAL_DESCRIPTION_CHARACTER_LIMIT} characters.") }
                return
            }
        }
        // --- End of Guarding Block ---

        viewModelScope.launch {
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

    // --- Navigation and Dialog Handlers ---
    fun onSnackbarMessageShown() {
        _dialogState.update { it.copy(snackbarMessage = null) }
    }

    fun onAddStageClicked() {
        if (uiState.value.isNewGoal) {
            saveGoal { newGoalId ->
                _dialogState.update { it.copy(navigateToAddStageWithGoalId = newGoalId) }
            }
        } else {
            _dialogState.update { it.copy(navigateToAddStageWithGoalId = uiState.value.goalId) }
        }
    }

    fun onNavigateToAddStageDone() {
        _dialogState.update { it.copy(navigateToAddStageWithGoalId = null) }
    }

    fun onEditStageClicked(stage: GoalStage) {
        uiState.value.goalId?.let { gId ->
            _dialogState.update { it.copy(navigateToEditStage = Pair(gId, stage.id)) }
        }
    }

    fun onNavigateToEditStageDone() {
        _dialogState.update { it.copy(navigateToEditStage = null) }
    }
}