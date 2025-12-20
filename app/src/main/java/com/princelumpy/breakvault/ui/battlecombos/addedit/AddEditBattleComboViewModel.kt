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

// State representing the user's direct inputs and selections.
// CORRECTED: Removed 'private' to make it visible to AddEditBattleComboUiState.
// It remains file-local and won't pollute the global namespace.
data class UserInputs(
    val comboId: String? = null,
    val description: String = "",
    val selectedEnergy: EnergyLevel = EnergyLevel.NONE,
    val selectedStatus: TrainingStatus = TrainingStatus.TRAINING,
    val isUsed: Boolean = false,
    val selectedTags: Set<String> = emptySet(),
    val newTagName: String = "",
    val isNewCombo: Boolean = true
)

// Final state for the UI, combining UserInputs and data from repositories.
data class AddEditBattleComboUiState(
    val userInputs: UserInputs = UserInputs(),
    val allBattleTags: List<BattleTag> = emptyList(),
    val allPracticeCombos: List<SavedCombo> = emptyList(),
    val showImportDialog: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class AddEditBattleComboViewModel @Inject constructor(
    private val battleRepository: BattleRepository,
    private val savedComboRepository: SavedComboRepository
) : ViewModel() {

    // Holds the state of user's direct inputs.
    private val _userInputs = MutableStateFlow(UserInputs())

    // For transient UI states like dialogs and snack bars.
    private val _showImportDialog = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AddEditBattleComboUiState> = combine(
        _userInputs,
        battleRepository.getAllTags(),
        savedComboRepository.getSavedCombos(),
        _showImportDialog,
        _snackbarMessage
    ) { userInputs, tags, practiceCombos, showImportDialog, snackbarMessage ->
        AddEditBattleComboUiState(
            userInputs = userInputs,
            allBattleTags = tags,
            allPracticeCombos = practiceCombos,
            showImportDialog = showImportDialog,
            snackbarMessage = snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEditBattleComboUiState()
    )

    /** Loads a combo for editing or prepares for creating a new one. */
    fun loadCombo(comboId: String?) {
        if (comboId == null) {
            _userInputs.value = UserInputs() // Reset to a fresh state for new combo
            return
        }

        viewModelScope.launch {
            val comboWithTags = battleRepository.getBattleComboWithTags(comboId)
            if (comboWithTags != null) {
                _userInputs.value = UserInputs(
                    comboId = comboId,
                    description = comboWithTags.battleCombo.description,
                    selectedEnergy = comboWithTags.battleCombo.energy,
                    selectedStatus = comboWithTags.battleCombo.status,
                    isUsed = comboWithTags.battleCombo.isUsed,
                    selectedTags = comboWithTags.tags.map { it.name }.toSet(),
                    isNewCombo = false
                )
            } else {
                _snackbarMessage.value = "Could not find combo."
            }
        }
    }

    /** Updates the combo description. */
    fun onDescriptionChange(newDescription: String) {
        _userInputs.update { it.copy(description = newDescription) }
    }

    /** Updates the selected energy level. */
    fun onEnergyChange(newEnergy: EnergyLevel) {
        _userInputs.update { it.copy(selectedEnergy = newEnergy) }
    }

    /** Updates the selected training status. */
    fun onStatusChange(newStatus: TrainingStatus) {
        _userInputs.update { it.copy(selectedStatus = newStatus) }
    }

    /** Toggles the selection of a tag. */
    fun onTagSelected(tagName: String) {
        _userInputs.update { state ->
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
        _userInputs.update { it.copy(newTagName = newTagName) }
    }

    /** Adds a new tag to the database. */
    fun addBattleTag() {
        val newTagName = uiState.value.userInputs.newTagName.trim()
        val allTagNames = uiState.value.allBattleTags.map { it.name }

        if (newTagName.isNotBlank() && !allTagNames.any {
                it.equals(
                    newTagName,
                    ignoreCase = true
                )
            }) {
            viewModelScope.launch {
                battleRepository.insertBattleTag(BattleTag(name = newTagName))
                // After inserting, update the user inputs state
                _userInputs.update {
                    it.copy(
                        newTagName = "",
                        selectedTags = it.selectedTags + newTagName // Auto-select new tag
                    )
                }
            }
        } else {
            _snackbarMessage.value = "Tag already exists or is empty."
        }
    }

    /** Imports moves from a saved practice combo into the description. */
    fun onImportCombo(combo: SavedCombo) {
        _userInputs.update { it.copy(description = combo.moves.joinToString(" -> ")) }
        showImportDialog(false)
    }

    /** Shows or hides the import dialog. */
    fun showImportDialog(show: Boolean) {
        _showImportDialog.value = show
    }

    /** Saves the new or edited combo to the database. */
    fun saveCombo(onSuccess: () -> Unit) {
        val currentInputs = _userInputs.value
        if (currentInputs.description.isBlank()) {
            _snackbarMessage.value = "Description cannot be empty"
            return
        }

        viewModelScope.launch {
            val battleCombo = BattleCombo(
                id = currentInputs.comboId ?: "", // ID will be replaced for new combos
                description = currentInputs.description,
                energy = currentInputs.selectedEnergy,
                status = currentInputs.selectedStatus,
                isUsed = currentInputs.isUsed
            )

            if (currentInputs.isNewCombo) {
                battleRepository.insertBattleComboWithTags(
                    battleCombo,
                    currentInputs.selectedTags.toList()
                )
            } else {
                battleRepository.updateBattleComboWithTags(
                    battleCombo,
                    currentInputs.selectedTags.toList()
                )
            }
            onSuccess()
        }
    }

    /** Clears the snackbar message after it has been shown. */
    fun onSnackbarMessageShown() {
        _snackbarMessage.value = null
    }
}
