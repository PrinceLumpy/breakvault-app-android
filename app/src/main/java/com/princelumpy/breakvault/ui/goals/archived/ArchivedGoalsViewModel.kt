package com.princelumpy.breakvault.ui.goals.archived

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.dao.GoalDao
import com.princelumpy.breakvault.data.local.entity.Goal
import com.princelumpy.breakvault.data.local.relation.GoalWithStages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchivedGoalsUiState(
    val archivedGoals: List<GoalWithStages> = emptyList(),
    val goalToUnarchive: GoalWithStages? = null,
    val goalToDelete: GoalWithStages? = null
)

@HiltViewModel
class ArchivedGoalsViewModel @Inject constructor(
    private val goalDao: GoalDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchivedGoalsUiState())
    val uiState: StateFlow<ArchivedGoalsUiState> = _uiState.asStateFlow()

    private val archivedGoalsWithStages: LiveData<List<GoalWithStages>> =
        goalDao.getArchivedGoalsWithStages()

    private val goalsObserver = Observer<List<GoalWithStages>> { goals ->
        _uiState.update { it.copy(archivedGoals = goals) }
    }

    init {
        archivedGoalsWithStages.observeForever(goalsObserver)
    }

    // --- Unarchive Logic ---

    fun onGoalUnarchiveClicked(goal: GoalWithStages) {
        _uiState.update { it.copy(goalToUnarchive = goal) }
    }

    fun onCancelGoalUnarchive() {
        _uiState.update { it.copy(goalToUnarchive = null) }
    }

    fun onConfirmGoalUnarchive() {
        viewModelScope.launch {
            _uiState.value.goalToUnarchive?.let { goalToUnarchive ->
                unarchiveGoal(goalToUnarchive.goal)
                _uiState.update { it.copy(goalToUnarchive = null) }
            }
        }
    }

    private suspend fun unarchiveGoal(goal: Goal) {
        goalDao.updateGoal(goal.copy(isArchived = false))
    }

    // --- Delete Logic ---

    fun onGoalDeleteClicked(goal: GoalWithStages) {
        _uiState.update { it.copy(goalToDelete = goal) }
    }

    fun onCancelGoalDelete() {
        _uiState.update { it.copy(goalToDelete = null) }
    }

    fun onConfirmGoalDelete() {
        viewModelScope.launch {
            _uiState.value.goalToDelete?.let { goalToDelete ->
                deleteGoal(goalToDelete.goal)
                _uiState.update { it.copy(goalToDelete = null) }
            }
        }
    }

    private suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }

    override fun onCleared() {
        super.onCleared()
        archivedGoalsWithStages.removeObserver(goalsObserver)
    }

}
