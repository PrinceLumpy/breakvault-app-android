package com.princelumpy.breakvault.ui.goals.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.dao.GoalDao
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.entity.GoalStage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditGoalUiState(
    val goalId: String? = null,
    val title: String = "",
    val description: String = "",
    val stages: List<GoalStage> = emptyList(),
    val isNewGoal: Boolean = true,
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null,
    val navigateToAddStageWithGoalId: String? = null,
    val navigateToEditStage: GoalStage? = null,
    val addingRepsToStage: GoalStage? = null
)

@HiltViewModel
class AddEditGoalViewModel @Inject constructor(
    private val goalDao: GoalDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditGoalUiState())
    val uiState: StateFlow<AddEditGoalUiState> = _uiState.asStateFlow()

    private val goalId: String? = savedStateHandle["goalId"]
    private var goal: Goal? = null

    init {
        loadGoal()
    }

    fun loadGoal() {
        if (goalId == null) {
            _uiState.update { it.copy(isLoading = false, isNewGoal = true) }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val goalWithStages = goalDao.getGoalWithStages(goalId)
            if (goalWithStages != null) {
                this@AddEditGoalViewModel.goal = goalWithStages.goal
                _uiState.value = AddEditGoalUiState(
                    goalId = goalId,
                    title = goalWithStages.goal.title,
                    description = goalWithStages.goal.description,
                    stages = goalWithStages.stages,
                    isNewGoal = false,
                    isLoading = false
                )
            } else {
                _uiState.update {
                    it.copy(
                        snackbarMessage = "Could not find goal.",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        if (newTitle.length <= 100) {
            _uiState.update { it.copy(title = newTitle) }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun saveGoal(onSuccess: (goalId: String) -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.title.isBlank()) {
                _uiState.update { it.copy(snackbarMessage = "Goal title cannot be blank.") }
                return@launch
            }

            if (goalId == null) {
                val newGoal = Goal(
                    id = UUID.randomUUID().toString(),
                    title = currentState.title,
                    description = currentState.description,
                    isArchived = false,
                    lastUpdated = System.currentTimeMillis()
                )
                goalDao.insertGoal(newGoal)
                onSuccess(newGoal.id)
            } else {
                goal?.let {
                    val updatedGoal = it.copy(
                        title = currentState.title,
                        description = currentState.description,
                        lastUpdated = System.currentTimeMillis()
                    )
                    goalDao.updateGoal(updatedGoal)
                    onSuccess(goalId)
                }
            }
        }
    }

    fun archiveGoal(onSuccess: () -> Unit) {
        viewModelScope.launch {
            goal?.let {
                goalDao.updateGoal(
                    it.copy(
                        isArchived = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                onSuccess()
            }
        }
    }

    fun deleteGoal(onSuccess: () -> Unit) {
        viewModelScope.launch {
            goal?.let {
                goalDao.deleteAllStagesForGoal(it.id)
                goalDao.deleteGoal(it)
                onSuccess()
            }
        }
    }

    fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onAddStageClicked() {
        if (_uiState.value.isNewGoal) {
            _uiState.update { it.copy(snackbarMessage = "Please save goal before adding a stage.") }
        } else {
            _uiState.update { it.copy(navigateToAddStageWithGoalId = _uiState.value.goalId) }
        }
    }

    fun onNavigateToAddStageDone() {
        _uiState.update { it.copy(navigateToAddStageWithGoalId = null) }
    }

    fun onEditStageClicked(stage: GoalStage) {
        _uiState.update { it.copy(navigateToEditStage = stage) }
    }

    fun onNavigateToEditStageDone() {
        _uiState.update { it.copy(navigateToEditStage = null) }
    }

    fun onAddRepsClicked(stage: GoalStage) {
        _uiState.update { it.copy(addingRepsToStage = stage) }
    }

    fun onAddRepsDismissed() {
        _uiState.update { it.copy(addingRepsToStage = null) }
    }

    fun updateStage(updatedStage: GoalStage) {
        viewModelScope.launch {
            goalDao.updateGoalStage(updatedStage)
            loadGoal()
        }
    }

    fun addRepsToStage(stage: GoalStage, reps: Int) {
        viewModelScope.launch {
            val newCount = stage.currentCount + reps
            if (newCount < 0) {
                _uiState.update {
                    it.copy(
                        snackbarMessage = "Repetitions cannot be negative.",
                        addingRepsToStage = null
                    )
                }
                return@launch
            }
            val updatedStage = stage.copy(currentCount = newCount)
            goalDao.updateGoalStage(updatedStage)
            loadGoal()
        }
    }
}