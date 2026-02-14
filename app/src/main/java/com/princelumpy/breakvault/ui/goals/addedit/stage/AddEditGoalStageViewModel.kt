package com.princelumpy.breakvault.ui.goals.addedit.stage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.common.Constants.GOAL_STAGE_MAX_TARGET_COUNT
import com.princelumpy.breakvault.common.Constants.GOAL_STAGE_TITLE_CHARACTER_LIMIT
import com.princelumpy.breakvault.common.Constants.GOAL_STAGE_UNIT_CHARACTER_LIMIT
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// State for the user's direct text inputs.
data class UserInputs(
    val name: String = "",
    val currentCount: String = "0",
    val targetCount: String = "",
    val unit: String = "reps"
)

// State for transient UI events like dialogs.
data class DialogState(
    val showDeleteDialog: Boolean = false
)

// State for UI-specific properties like errors and loading status.
data class UiState(
    val nameError: String? = null,
    val currentCountError: String? = null,
    val targetError: String? = null,
    val unitError: String? = null,
    val isInitialLoadDone: Boolean = false
)

// The final, combined state for the UI to consume.
data class GoalStageUiState(
    val stageId: String? = null,
    val goalId: String,
    val isNewStage: Boolean = true,
    val isLoading: Boolean = true,
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState(),
    val uiState: UiState = UiState()
)

@HiltViewModel
class AddEditGoalStageViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {
    // Separate state flows for each concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogState = MutableStateFlow(DialogState())
    private val _uiState = MutableStateFlow(UiState())

    private var goalId: String? = null
    private var stageId: String? = null

    val uiState: StateFlow<GoalStageUiState?> = combine(
        _userInputs,
        _dialogState,
        _uiState
    ) { userInputs, dialogState, uiState ->
        // Do not emit a state until goalId is loaded.
        goalId?.let { gid ->
            GoalStageUiState(
                stageId = stageId,
                goalId = gid,
                isNewStage = stageId == null,
                isLoading = !uiState.isInitialLoadDone,
                userInputs = userInputs,
                dialogState = dialogState,
                uiState = uiState
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    fun loadStage(goalId: String, stageId: String?) {
        this.goalId = goalId
        this.stageId = stageId

        if (stageId == null) {
            // New stage, mark as loaded immediately
            _uiState.update { it.copy(isInitialLoadDone = true) }
            return
        }

        // Existing stage: Load from repository.
        viewModelScope.launch {
            val stage = goalRepository.getGoalStageById(stageId)
            if (stage != null) {
                _userInputs.value = UserInputs(
                    name = stage.name,
                    currentCount = stage.currentCount.toString(),
                    targetCount = stage.targetCount.toString(),
                    unit = stage.unit
                )
                _uiState.update {
                    it.copy(isInitialLoadDone = true)
                }
            }
        }
    }

    // --- User Input Handlers ---
    fun onNameChange(newName: String) {
        if (newName.length <= GOAL_STAGE_TITLE_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(name = newName) }
            if (_uiState.value.nameError != null) {
                _uiState.update { it.copy(nameError = null) }
            }
        }
    }

    fun onCurrentCountChange(newCurrent: String) {
        if (newCurrent.isEmpty() || (newCurrent.all { it.isDigit() } && (newCurrent.toLongOrNull()
                ?: 0) <= GOAL_STAGE_MAX_TARGET_COUNT)) {
            _userInputs.update { it.copy(currentCount = newCurrent) }
            if (_uiState.value.currentCountError != null) {
                _uiState.update { it.copy(currentCountError = null) }
            }
        }
    }

    fun onTargetCountChange(newTarget: String) {
        if (newTarget.isEmpty() || (newTarget.all { it.isDigit() } && (newTarget.toLongOrNull()
                ?: 0) <= GOAL_STAGE_MAX_TARGET_COUNT)) {
            _userInputs.update { it.copy(targetCount = newTarget) }
            if (_uiState.value.targetError != null) {
                _uiState.update { it.copy(targetError = null) }
            }
        }
    }

    fun onUnitChange(newUnit: String) {
        if (newUnit.length <= GOAL_STAGE_UNIT_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(unit = newUnit) }
            if (_uiState.value.unitError != null) {
                _uiState.update { it.copy(unitError = null) }
            }
        }
    }

    // --- Dialog Handlers ---
    fun showDeleteDialog(show: Boolean) {
        _dialogState.update { it.copy(showDeleteDialog = show) }
    }

    // --- Data Operation Handlers ---
    fun saveStage(onSuccess: (String) -> Unit) {
        val currentUiState = uiState.value ?: return
        val inputs = currentUiState.userInputs
        val current = inputs.currentCount.toIntOrNull()
        val target = inputs.targetCount.toIntOrNull()

        // --- Start of Guarding Block ---
        when {
            inputs.name.isBlank() -> {
                _uiState.update { it.copy(nameError = "Stage name cannot be empty.") }
                return
            }

            current == null || current < 0 -> {
                _uiState.update { it.copy(currentCountError = "Current count must be a number 0 or greater.") }
                return
            }

            current > GOAL_STAGE_MAX_TARGET_COUNT -> {
                _uiState.update { it.copy(currentCountError = "Current count cannot exceed $GOAL_STAGE_MAX_TARGET_COUNT.") }
                return
            }

            target == null || target <= 0 -> {
                _uiState.update { it.copy(targetError = "Target count must be a number greater than 0.") }
                return
            }

            target > GOAL_STAGE_MAX_TARGET_COUNT -> {
                _uiState.update { it.copy(targetError = "Target cannot exceed $GOAL_STAGE_MAX_TARGET_COUNT.") }
                return
            }

            inputs.unit.length > GOAL_STAGE_UNIT_CHARACTER_LIMIT -> {
                _uiState.update { it.copy(unitError = "Unit cannot exceed $GOAL_STAGE_UNIT_CHARACTER_LIMIT characters.") }
                return
            }
        }
        // --- End of Guarding Block ---

        viewModelScope.launch {
            // For new stages, determine the next orderIndex
            val orderIndex = if (currentUiState.isNewStage) {
                val goalWithStages = goalRepository.getGoalWithStages(currentUiState.goalId)
                    .stateIn(viewModelScope).value
                (goalWithStages?.stages?.maxOfOrNull { it.orderIndex } ?: -1) + 1
            } else {
                // For existing stages, fetch and preserve the current orderIndex
                goalRepository.getGoalStageById(currentUiState.stageId!!)?.orderIndex ?: 0
            }

            val stageToSave = GoalStage(
                id = currentUiState.stageId ?: UUID.randomUUID().toString(),
                goalId = currentUiState.goalId,
                name = inputs.name,
                currentCount = current,
                targetCount = target,
                unit = inputs.unit,
                orderIndex = orderIndex
            )

            if (currentUiState.isNewStage) {
                goalRepository.insertGoalStage(stageToSave)
            } else {
                goalRepository.updateGoalStage(stageToSave)
            }
            onSuccess(stageToSave.goalId)
        }
    }

    fun deleteStage(onSuccess: () -> Unit) {
        val stageId = uiState.value?.stageId ?: return
        viewModelScope.launch {
            goalRepository.getGoalStageById(stageId)?.let { stage ->
                goalRepository.deleteGoalStage(stage)
                onSuccess()
            }
        }
    }
}