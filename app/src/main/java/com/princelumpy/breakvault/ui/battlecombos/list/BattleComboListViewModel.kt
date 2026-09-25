// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.ui.battlecombos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.TagColor
import com.princelumpy.breakvault.data.local.relation.BattleComboWithTags
import com.princelumpy.breakvault.data.repository.BattleRepository
import com.princelumpy.breakvault.data.repository.BattleSortOption
import com.princelumpy.breakvault.data.repository.UserPreferencesRepository
import com.princelumpy.breakvault.ui.common.stripColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Private state to hold only the user's direct interactions
private data class UserInteractions(
    val selectedTagNames: Set<String> = emptySet(),
)

/** A combo plus its derived color strip (distinct tag colors in palette order). */
data class BattleComboListItem(
    val comboWithTags: BattleComboWithTags,
    val stripColors: List<TagColor> = comboWithTags.tags.stripColors()
)

// Final state for the UI. Contains only the data the UI needs to draw.
data class BattleComboListUiState(
    val allCombos: List<BattleComboWithTags> = emptyList(),
    val filteredAndSortedCombos: List<BattleComboListItem> = emptyList(),
    val allTags: List<BattleTag> = emptyList(),
    val selectedTagNames: Set<String> = emptySet(),
    val sortOption: BattleSortOption = BattleSortOption.NEWEST,
    val showResetConfirmDialog: Boolean = false,
    val isLoading: Boolean = true
)

/** Lexicographic comparison of palette positions, so e.g. [RED] < [RED, BLUE] < [BLUE]. */
private val stripColorsComparator = Comparator<List<TagColor>> { a, b ->
    a.zip(b).firstOrNull { (x, y) -> x != y }
        ?.let { (x, y) -> x.ordinal.compareTo(y.ordinal) }
        ?: a.size.compareTo(b.size)
}

/**
 * Sorts for the battle list. Used combos always sink to the bottom.
 * COLOR groups by the first (top) strip color; uncolored combos go last.
 */
internal fun List<BattleComboListItem>.sortedFor(option: BattleSortOption): List<BattleComboListItem> {
    val usedLast = compareBy<BattleComboListItem> { it.comboWithTags.battleCombo.isUsed }
    val comparator = when (option) {
        BattleSortOption.NEWEST -> usedLast
            .thenByDescending { it.comboWithTags.battleCombo.createdAt }

        BattleSortOption.NAME -> usedLast
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.comboWithTags.battleCombo.title }
            .thenByDescending { it.comboWithTags.battleCombo.createdAt }

        BattleSortOption.COLOR -> usedLast
            .thenBy { it.stripColors.isEmpty() }
            .thenBy(stripColorsComparator) { it.stripColors }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.comboWithTags.battleCombo.title }
    }
    return sortedWith(comparator)
}

@HiltViewModel
class BattleComboListViewModel @Inject constructor(
    private val battleRepository: BattleRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Consolidate user-driven state
    private val _userInteractions = MutableStateFlow(UserInteractions())
    private val _showResetDialog = MutableStateFlow(false)

    val uiState: StateFlow<BattleComboListUiState> = combine(
        battleRepository.getAllBattleCombosWithTags(),
        battleRepository.getAllTags(),
        userPreferencesRepository.battleSortOption,
        _userInteractions,
        _showResetDialog
    ) { combos, tags, sortOption, interactions, showReset ->
        // Perform filtering
        val filteredCombos = if (interactions.selectedTagNames.isEmpty()) {
            combos
        } else {
            combos.filter { comboWithTags ->
                comboWithTags.tags.any { tag -> tag.name in interactions.selectedTagNames }
            }
        }

        // Return a new state object for the UI
        BattleComboListUiState(
            allCombos = combos,
            allTags = tags,
            filteredAndSortedCombos = filteredCombos.map { BattleComboListItem(it) }.sortedFor(sortOption),
            selectedTagNames = interactions.selectedTagNames,
            sortOption = sortOption,
            showResetConfirmDialog = showReset,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BattleComboListUiState()
    )

    fun toggleTagFilter(tagName: String) {
        _userInteractions.update { current ->
            val newSelectedTags = if (tagName in current.selectedTagNames) {
                current.selectedTagNames - tagName
            } else {
                current.selectedTagNames + tagName
            }
            current.copy(selectedTagNames = newSelectedTags)
        }
    }

    fun clearFilters() {
        _userInteractions.update { it.copy(selectedTagNames = emptySet()) }
    }

    fun changeSortOption(sortOption: BattleSortOption) {
        viewModelScope.launch {
            userPreferencesRepository.setBattleSortOption(sortOption)
        }
    }

    fun showResetDialog() {
        _showResetDialog.value = true
    }

    fun cancelReset() {
        _showResetDialog.value = false
    }

    fun confirmReset() {
        viewModelScope.launch {
            battleRepository.resetAllBattleCombosUsage()
        }
        _showResetDialog.value = false
    }

    // Marking a combo used is a battle-day action, not an edit, so modifiedAt is left alone.
    fun toggleUsed(combo: BattleCombo) {
        viewModelScope.launch {
            battleRepository.updateBattleCombo(combo.copy(isUsed = !combo.isUsed))
        }
    }
}
