package com.princelumpy.breakvault.ui.goals.list

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

data class GoalsScreenUiState(
    val goals: List<GoalWithStages> = emptyList(),
    val isLoading: Boolean = true,
    val goalToArchive: GoalWithStages? = null
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalDao: GoalDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsScreenUiState())
    val uiState: StateFlow<GoalsScreenUiState> = _uiState.asStateFlow()

    private val activeGoalsWithStages: LiveData<List<GoalWithStages>> =
        goalDao.getActiveGoalsWithStages()

    private val goalsObserver = Observer<List<GoalWithStages>> { goals ->
        _uiState.update { it.copy(goals = goals, isLoading = false) }
    }

    init {
        activeGoalsWithStages.observeForever(goalsObserver)
    }

    private suspend fun archiveGoal(goal: Goal) {
        val archivedGoal = goal.copy(isArchived = true)
        goalDao.updateGoal(archivedGoal)
    }

    fun onGoalArchiveClicked(goal: GoalWithStages) {
        _uiState.update { it.copy(goalToArchive = goal) }
    }

    fun onCancelGoalArchive() {
        _uiState.update { it.copy(goalToArchive = null) }
    }

    fun onConfirmGoalArchive() {
        viewModelScope.launch {
            _uiState.value.goalToArchive?.let { goalToArchive ->
                archiveGoal(goalToArchive.goal)
                _uiState.update { it.copy(goalToArchive = null) }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        activeGoalsWithStages.removeObserver(goalsObserver)
    }
}
