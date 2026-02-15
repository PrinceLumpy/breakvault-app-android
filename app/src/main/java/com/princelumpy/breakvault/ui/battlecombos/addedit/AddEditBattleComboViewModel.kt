package com.princelumpy.breakvault.ui.battlecombos.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.common.Constants.BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT
import com.princelumpy.breakvault.common.Constants.BATTLE_TAG_CHARACTER_LIMIT
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.EnergyLevel
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import com.princelumpy.breakvault.data.local.entity.TrainingStatus
import com.princelumpy.breakvault.data.repository.BattleRepository
import com.princelumpy.breakvault.data.repository.PracticeComboRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

// Group transient UI states together
data class UiDialogsAndMessages(
    val showImportDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val descriptionError: String? = null,
    val newTagError: String? = null
)

data class AddEditBattleComboUiState(
    val userInputs: UserInputs = UserInputs(),
    val allBattleTags: List<BattleTag> = emptyList(),
    val allPracticeCombos: List<PracticeCombo> = emptyList(),
    val dialogsAndMessages: UiDialogsAndMessages = UiDialogsAndMessages(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AddEditBattleComboViewModel @Inject constructor(
    private val battleRepository: BattleRepository,
    private val practiceComboRepository: PracticeComboRepository
) : ViewModel() {

    private val _userInputs = MutableStateFlow(UserInputs())

    // Group all transient UI states into a single flow
    private val _dialogsAndMessages = MutableStateFlow(UiDialogsAndMessages())

    private val _isInitialLoadDone = MutableStateFlow(false)

    // Track original values to detect changes
    private var originalDescription: String? = null
    private var originalEnergy: EnergyLevel? = null
    private var originalStatus: TrainingStatus? = null
    private var originalSelectedTags: Set<String>? = null


    val uiState: StateFlow<AddEditBattleComboUiState> = combine(
        _userInputs,
        battleRepository.getAllTags(),
        practiceComboRepository.getPracticeCombos(),
        _dialogsAndMessages,
        _isInitialLoadDone
    ) { userInputs, tags, practiceCombos, dialogsAndMessages, isInitialLoadDone ->
        AddEditBattleComboUiState(
            userInputs = userInputs,
            allBattleTags = tags,
            allPracticeCombos = practiceCombos,
            dialogsAndMessages = dialogsAndMessages,
            isLoading = !isInitialLoadDone
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEditBattleComboUiState()
    )

    fun loadCombo(comboId: String?) {
        if (comboId == null) {
            _userInputs.value = UserInputs()
            _isInitialLoadDone.value = true
            return
        }

        viewModelScope.launch {
            val comboWithTags = battleRepository.getBattleComboWithTags(comboId)
            if (comboWithTags != null) {
                originalDescription = comboWithTags.battleCombo.description
                originalEnergy = comboWithTags.battleCombo.energy
                originalStatus = comboWithTags.battleCombo.status
                originalSelectedTags = comboWithTags.tags.map { it.id }.toSet()
                _userInputs.value = UserInputs(
                    comboId = comboId,
                    description = comboWithTags.battleCombo.description,
                    selectedEnergy = comboWithTags.battleCombo.energy,
                    selectedStatus = comboWithTags.battleCombo.status,
                    isUsed = comboWithTags.battleCombo.isUsed,
                    selectedTags = comboWithTags.tags.map { it.id }.toSet(),
                    isNewCombo = false
                )
            } else {
                _dialogsAndMessages.update { it.copy(snackbarMessage = "Could not find combo.") }
            }
            _isInitialLoadDone.value = true
        }
    }

    // LAYER 2: State Sanitization
    fun onDescriptionChange(newDescription: String) {
        if (newDescription.length <= BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(description = newDescription) }

            // Clear error on valid input
            if (_dialogsAndMessages.value.descriptionError != null) {
                _dialogsAndMessages.update { it.copy(descriptionError = null) }
            }
        }
    }

    fun onEnergyChange(newEnergy: EnergyLevel) {
        _userInputs.update { it.copy(selectedEnergy = newEnergy) }
    }

    fun onStatusChange(newStatus: TrainingStatus) {
        _userInputs.update { it.copy(selectedStatus = newStatus) }
    }

    fun onTagSelected(tagId: String) {
        _userInputs.update { state ->
            val newTags = if (tagId in state.selectedTags) {
                state.selectedTags - tagId
            } else {
                state.selectedTags + tagId
            }
            state.copy(selectedTags = newTags)
        }
    }

    // LAYER 2: State Sanitization
    fun onNewTagNameChange(newTagName: String) {
        if (newTagName.length <= BATTLE_TAG_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(newTagName = newTagName) }

            // Clear error on valid input
            if (_dialogsAndMessages.value.newTagError != null) {
                _dialogsAndMessages.update { it.copy(newTagError = null) }
            }
        }
    }

    // LAYER 3: Action Guard
    fun addBattleTag() {
        val newTagName = uiState.value.userInputs.newTagName.trim()
        val allTagNames = uiState.value.allBattleTags.map { it.name }

        // Defensive guards against all business rules
        when {
            newTagName.isBlank() -> {
                _dialogsAndMessages.update {
                    it.copy(newTagError = "Tag name cannot be empty.")
                }
                return
            }

            newTagName.length > BATTLE_TAG_CHARACTER_LIMIT -> {
                _dialogsAndMessages.update {
                    it.copy(newTagError = "Tag cannot exceed $BATTLE_TAG_CHARACTER_LIMIT characters.")
                }
                return
            }

            allTagNames.any { it.equals(newTagName, ignoreCase = true) } -> {
                _dialogsAndMessages.update {
                    it.copy(newTagError = "Tag '$newTagName' already exists.")
                }
                return
            }
        }

        // If all checks pass, proceed with insertion
        viewModelScope.launch {
            val newTag = BattleTag(name = newTagName)
            battleRepository.insertBattleTag(newTag)
            _userInputs.update {
                it.copy(
                    newTagName = "",
                    selectedTags = it.selectedTags + newTag.id
                )
            }
        }
    }

    fun onImportCombo(combo: PracticeCombo) {
        _userInputs.update { it.copy(description = combo.moves.joinToString(" -> ")) }
        showImportDialog(false)
    }

    fun showImportDialog(show: Boolean) {
        _dialogsAndMessages.update { it.copy(showImportDialog = show) }
    }

    // LAYER 3: Action Guard
    fun saveCombo(onSuccess: () -> Unit) {
        val currentInputs = _userInputs.value

        // Trim input values
        val trimmedDescription = currentInputs.description.trim()

        // Don't save if there are no changes (avoids unnecessary database updates)
        if (!hasUnsavedChanges()) {
            onSuccess()
            return
        }

        // Defensive guards against all business rules
        when {
            trimmedDescription.isBlank() -> {
                _dialogsAndMessages.update {
                    it.copy(descriptionError = "Description cannot be empty.")
                }
                return
            }

            trimmedDescription.length > BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT -> {
                _dialogsAndMessages.update {
                    it.copy(descriptionError = "Description cannot exceed $BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT characters.")
                }
                return
            }
        }

        viewModelScope.launch {
            val battleCombo = BattleCombo(
                id = currentInputs.comboId ?: "",
                description = trimmedDescription,
                energy = currentInputs.selectedEnergy,
                status = currentInputs.selectedStatus,
                isUsed = currentInputs.isUsed
            )

            // Convert selected tag IDs to BattleTag objects
            val selectedTagObjects = uiState.value.allBattleTags
                .filter { it.id in currentInputs.selectedTags }

            if (currentInputs.isNewCombo) {
                battleRepository.insertBattleComboWithTags(
                    battleCombo,
                    selectedTagObjects
                )
                _dialogsAndMessages.update { it.copy(snackbarMessage = "Combo created successfully!") }
            } else {
                battleRepository.updateBattleComboWithTags(
                    battleCombo,
                    selectedTagObjects
                )
                _dialogsAndMessages.update { it.copy(snackbarMessage = "Combo updated successfully!") }
            }
            onSuccess()
        }
    }

    fun onDeleteComboClick() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = true) }
    }

    fun onConfirmComboDelete(onSuccess: () -> Unit) {
        val comboId = uiState.value.userInputs.comboId ?: return

        viewModelScope.launch {
            val comboWithTags = battleRepository.getBattleComboWithTags(comboId)
            comboWithTags?.let {
                battleRepository.deleteBattleCombo(it.battleCombo)
                _dialogsAndMessages.update { it.copy(snackbarMessage = "Combo deleted") }
                onSuccess()
            }
        }
    }

    fun onCancelComboDelete() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = false) }
    }

    fun onSnackbarMessageShown() {
        _dialogsAndMessages.update { it.copy(snackbarMessage = null) }
    }

    /**
     * Check if there are unsaved changes in the form.
     * Returns true if any field has been modified.
     */
    fun hasUnsavedChanges(): Boolean {
        val currentInputs = _userInputs.value

        // For new combos, check if any fields have been filled
        if (currentInputs.isNewCombo) {
            return currentInputs.description.isNotBlank() ||
                    currentInputs.selectedEnergy != EnergyLevel.NONE ||
                    currentInputs.selectedStatus != TrainingStatus.TRAINING ||
                    currentInputs.selectedTags.isNotEmpty()
        }

        // For existing combos, check if any fields have been modified
        return currentInputs.description != originalDescription ||
                currentInputs.selectedEnergy != originalEnergy ||
                currentInputs.selectedStatus != originalStatus ||
                currentInputs.selectedTags != originalSelectedTags
    }
}