package com.princelumpy.breakvault.ui.battlecombos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.TrainingStatus
import com.princelumpy.breakvault.data.repository.BattleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BattleSortOption {
    EnergyHighToLow, EnergyLowToHigh, StatusFireFirst, StatusHammerFirst
}

// Final state for the UI. Contains only the data the UI needs to draw.
data class BattleComboListUiState(
    val allCombos: List<BattleComboWithTags> = emptyList(),
    val filteredAndSortedCombos: List<BattleComboWithTags> = emptyList(),
    val allTags: List<BattleTag> = emptyList(),
    val selectedTagNames: Set<String> = emptySet(),
    val sortOption: BattleSortOption = BattleSortOption.EnergyLowToHigh,
    val showResetConfirmDialog: Boolean = false
)

// Private state to hold only the user's direct interactions
private data class UserInteractions(
    val selectedTagNames: Set<String> = emptySet(),
    val sortOption: BattleSortOption = BattleSortOption.EnergyLowToHigh,
)

@HiltViewModel
class BattleComboListViewModel @Inject constructor(
    private val battleRepository: BattleRepository
) : ViewModel() {

    private val _userInteractions = MutableStateFlow(UserInteractions())
    private val _showResetDialog = MutableStateFlow(false)

    // This is the single source of truth for the UI.
    val uiState: StateFlow<BattleComboListUiState> = combine(
        battleRepository.getAllBattleCombosWithTags(),
        battleRepository.getAllTags(),
        _userInteractions,
        _showResetDialog
    ) { combos, tags, interactions, showReset ->
        // Perform filtering
        val filteredCombos = if (interactions.selectedTagNames.isEmpty()) {
            combos
        } else {
            combos.filter { comboWithTags ->
                comboWithTags.tags.any { tag -> tag.name in interactions.selectedTagNames }
            }
        }

        // Perform sorting
        val sortedCombos = when (interactions.sortOption) {
            BattleSortOption.EnergyHighToLow -> filteredCombos.sortedByDescending { it.battleCombo.energy }
            BattleSortOption.EnergyLowToHigh -> filteredCombos.sortedBy { it.battleCombo.energy }
            BattleSortOption.StatusFireFirst -> filteredCombos.sortedBy { it.battleCombo.status != TrainingStatus.READY }
            BattleSortOption.StatusHammerFirst -> filteredCombos.sortedBy { it.battleCombo.status != TrainingStatus.TRAINING }
        }

        // Return a new state object for the UI
        BattleComboListUiState(
            allCombos = combos,
            allTags = tags,
            filteredAndSortedCombos = sortedCombos,
            selectedTagNames = interactions.selectedTagNames,
            sortOption = interactions.sortOption,
            showResetConfirmDialog = showReset
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BattleComboListUiState()
    )

    fun onTagSelected(tagName: String) {
        _userInteractions.update { currentState ->
            val newSelectedTags = if (tagName in currentState.selectedTagNames) {
                currentState.selectedTagNames - tagName
            } else {
                currentState.selectedTagNames + tagName
            }
            currentState.copy(selectedTagNames = newSelectedTags)
        }
    }

    fun onSortOptionChange(sortOption: BattleSortOption) {
        _userInteractions.update { it.copy(sortOption = sortOption) }
    }

    fun onShowResetDialog() {
        _showResetDialog.value = true
    }

    fun onCancelReset() {
        _showResetDialog.value = false
    }

    fun onConfirmReset() {
        viewModelScope.launch {
            battleRepository.resetAllBattleCombosUsage()
        }
        _showResetDialog.value = false
    }

    fun toggleUsed(combo: BattleCombo) {
        viewModelScope.launch {
            battleRepository.updateBattleCombo(combo.copy(isUsed = !combo.isUsed))
        }
    }
}