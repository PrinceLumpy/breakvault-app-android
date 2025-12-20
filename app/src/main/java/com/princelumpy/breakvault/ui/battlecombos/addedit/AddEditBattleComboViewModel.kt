package com.princelumpy.breakvault.ui.battlecombos.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.EnergyLevel
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import com.princelumpy.breakvault.data.local.entity.TrainingStatus
import com.princelumpy.breakvault.data.repository.BattleRepository
import com.princelumpy.breakvault.data.repository.SavedComboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditBattleComboUiState(
    val comboId: String? = null,
    val description: String = "",
    val selectedEnergy: EnergyLevel = EnergyLevel.NONE,
    val selectedStatus: TrainingStatus = TrainingStatus.TRAINING,
    val isUsed: Boolean = false,
    val allBattleTags: List<BattleTag> = emptyList(),
    val allPracticeCombos: List<SavedCombo> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val newTagName: String = "",
    val showImportDialog: Boolean = false,
    val isNewCombo: Boolean = true,
    val snackbarMessage: String? = null
)

@HiltViewModel
class AddEditBattleComboViewModel @Inject constructor(
    private val battleRepository: BattleRepository,
    private val savedComboRepository: SavedComboRepository
) : ViewModel() {

    private val _internalState = MutableStateFlow(AddEditBattleComboUiState())

    val uiState: StateFlow<AddEditBattleComboUiState> = combine(
        _internalState,
        battleRepository.getAllTags(),
        savedComboRepository.getSavedCombos()
    ) { state, tags, practiceCombos ->
        state.copy(
            allBattleTags = tags,
            allPracticeCombos = practiceCombos
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEditBattleComboUiState()
    )

    /** Loads a combo for editing or prepares for creating a new one. */
    fun loadCombo(comboId: String?) {
        if (comboId == null) {
            _internalState.value = AddEditBattleComboUiState()
            return
        }

        viewModelScope.launch {
            val comboWithTags = battleRepository.getBattleComboWithTags(comboId)
            _internalState.update {
                if (comboWithTags != null) {
                    it.copy(
                        comboId = comboId,
                        description = comboWithTags.battleCombo.description,
                        selectedEnergy = comboWithTags.battleCombo.energy,
                        selectedStatus = comboWithTags.battleCombo.status,
                        isUsed = comboWithTags.battleCombo.isUsed,
                        selectedTags = comboWithTags.tags.map { tag -> tag.name }.toSet(),
                        isNewCombo = false
                    )
                } else {
                    it.copy(snackbarMessage = "Could not find combo.")
                }
            }
        }
    }

    /** Updates the combo description. */
    fun onDescriptionChange(newDescription: String) {
        _internalState.update { it.copy(description = newDescription) }
    }

    /** Updates the selected energy level. */
    fun onEnergyChange(newEnergy: EnergyLevel) {
        _internalState.update { it.copy(selectedEnergy = newEnergy) }
    }

    /** Updates the selected training status. */
    fun onStatusChange(newStatus: TrainingStatus) {
        _internalState.update { it.copy(selectedStatus = newStatus) }
    }

    /** Toggles the selection of a tag. */
    fun onTagSelected(tagName: String) {
        _internalState.update { state ->
            val newTags = if (tagName in state.selectedTags) {
                state.selectedTags - tagName
            } else {
                state.selectedTags + tagName
            }
            state.copy(selectedTags = newTags)
        }
    }

    /** Updates the name for a new tag. */
    fun onNewTagNameChange(newTagName: String) {
        _internalState.update { it.copy(newTagName = newTagName) }
    }

    /** Adds a new tag to the database. */
    fun addBattleTag() {
        val newTagName = uiState.value.newTagName.trim()
        val allTagNames = uiState.value.allBattleTags.map { it.name }
        if (newTagName.isNotBlank() && !allTagNames.any {
                it.equals(
                    newTagName,
                    ignoreCase = true
                )
            }) {
            viewModelScope.launch {
                battleRepository.insertBattleTag(BattleTag(name = newTagName))
                _internalState.update {
                    it.copy(
                        newTagName = "",
                        selectedTags = it.selectedTags + newTagName // Auto-select new tag
                    )
                }
            }
        } else {
            _internalState.update { it.copy(snackbarMessage = "Tag already exists or is empty.") }
        }
    }

    /** Imports moves from a saved practice combo into the description. */
    fun onImportCombo(combo: SavedCombo) {
        _internalState.update {
            it.copy(
                description = combo.moves.joinToString(" -> "),
                showImportDialog = false
            )
        }
    }

    /** Shows or hides the import dialog. */
    fun showImportDialog(show: Boolean) {
        _internalState.update { it.copy(showImportDialog = show) }
    }

    /** Saves the new or edited combo to the database. */
    fun saveCombo(onSuccess: () -> Unit) {
        val currentUiState = uiState.value
        if (currentUiState.description.isBlank()) {
            _internalState.update { it.copy(snackbarMessage = "Description cannot be empty") }
            return
        }

        viewModelScope.launch {
            val battleCombo = BattleCombo(
                id = currentUiState.comboId ?: "", // ID will be replaced for new combos
                description = currentUiState.description,
                energy = currentUiState.selectedEnergy,
                status = currentUiState.selectedStatus,
                isUsed = currentUiState.isUsed
            )


            if (currentUiState.isNewCombo) {
                battleRepository.insertBattleComboWithTags(
                    battleCombo,
                    currentUiState.selectedTags.toList()
                )
            } else {
                battleRepository.updateBattleComboWithTags(
                    battleCombo,
                    currentUiState.selectedTags.toList()
                )
            }
            onSuccess()
        }
    }

    /** Clears the snackbar message after it has been shown. */
    fun onSnackbarMessageShown() {
        _internalState.update { it.copy(snackbarMessage = null) }
    }
}
