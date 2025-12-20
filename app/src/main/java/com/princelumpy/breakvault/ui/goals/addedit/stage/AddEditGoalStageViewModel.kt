package com.princelumpy.breakvault.ui.goals.addedit.stage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.GoalStage
import com.princelumpy.breakvault.data.repository.GoalRepository // Assuming a repository exists
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
    val targetCount: String = "",
    val unit: String = "reps",
    val isNameError: Boolean = false,
    val isTargetError: Boolean = false
)

// State for transient UI events like dialogs.
data class DialogState(
    val showDeleteDialog: Boolean = false
)

// The final, combined state for the UI to consume.
data class GoalStageUiState(
    val stageId: String? = null,
    val goalId: String,
    val userInputs: UserInputs = UserInputs(),
    val dialogState: DialogState = DialogState(),
    val isNewStage: Boolean = true
)

@HiltViewModel
class AddEditGoalStageViewModel @Inject constructor(
    private val goalRepository: GoalRepository // Injecting repository is better than DAO directly.
) : ViewModel() {

    // Separate state flows for each concern.
    private val _userInputs = MutableStateFlow(UserInputs())
    private val _dialogState = MutableStateFlow(DialogState())

    // Holds static IDs and loading state.
    private val _metadata =
        MutableStateFlow<Pair<String?, Boolean>>(null to true) // Pair<stageId, isNewStage>

    // Nullable state for the initial goalId, which is required.
    private var goalId: String? = null

    val uiState: StateFlow<GoalStageUiState?> = combine(
        _userInputs,
        _dialogState,
        _metadata
    ) { userInputs, dialogState, metadata ->
        // Do not emit a state until goalId is loaded.
        goalId?.let { gid ->
            GoalStageUiState(
                stageId = metadata.first,
                goalId = gid,
                userInputs = userInputs,
                dialogState = dialogState,
                isNewStage = metadata.second
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null // Start as null until goalId is loaded.
    )

    fun loadStage(goalId: String, stageId: String?) {
        this.goalId = goalId
        if (stageId == null) {
            // New stage: Set metadata and reset inputs.
            _metadata.value = null to true
            _userInputs.value = UserInputs()
            return
        }

        // Existing stage: Load from repository.
        viewModelScope.launch {
            val stage = goalRepository.getGoalStageById(stageId)
            if (stage != null) {
                _userInputs.value = UserInputs(
                    name = stage.name,
                    targetCount = stage.targetCount.toString(),
                    unit = stage.unit
                )
                _metadata.value = stage.id to false
            }
            // else: handle error, maybe with a snackbar message
        }
    }

    // --- User Input Handlers ---

    fun onNameChange(newName: String) {
        if (newName.length <= 30) {
            _userInputs.update { it.copy(name = newName, isNameError = false) }
        }
    }

    fun onTargetCountChange(newTarget: String) {
        if (newTarget.isEmpty() || newTarget.all { it.isDigit() }) {
            _userInputs.update { it.copy(targetCount = newTarget, isTargetError = false) }
        }
    }

    fun onUnitChange(newUnit: String) {
        if (newUnit.length <= 10) {
            _userInputs.update { it.copy(unit = newUnit) }
        }
    }

    // --- Dialog Handlers ---

    fun showDeleteDialog(show: Boolean) {
        _dialogState.update { it.copy(showDeleteDialog = show) }
    }

    // --- Data Operation Handlers ---

    fun saveStage(onSuccess: () -> Unit) {
        val currentUiState = uiState.value ?: return
        val inputs = currentUiState.userInputs
        val target = inputs.targetCount.toIntOrNull()

        var hasError = false
        if (inputs.name.isBlank()) {
            _userInputs.update { it.copy(isNameError = true) }
            hasError = true
        }
        if (target == null || target <= 0) {
            _userInputs.update { it.copy(isTargetError = true) }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            val stageToSave = GoalStage(
                id = currentUiState.stageId ?: UUID.randomUUID().toString(),
                goalId = currentUiState.goalId,
                name = inputs.name,
                targetCount = target!!,
                unit = inputs.unit
            )

            if (currentUiState.isNewStage) {
                goalRepository.insertGoalStage(stageToSave)
            } else {
                goalRepository.updateGoalStage(stageToSave)
            }
            onSuccess()
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
