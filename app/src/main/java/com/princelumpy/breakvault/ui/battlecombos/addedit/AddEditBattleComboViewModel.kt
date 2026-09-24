// Modified by Claude Code - 2026-09-24
package com.princelumpy.breakvault.ui.battlecombos.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.common.Constants.BATTLE_COMBO_TITLE_CHARACTER_LIMIT
import com.princelumpy.breakvault.common.Constants.BATTLE_COMBO_DESCRIPTION_CHARACTER_LIMIT
import com.princelumpy.breakvault.common.Constants.BATTLE_TAG_CHARACTER_LIMIT
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import com.princelumpy.breakvault.data.local.entity.TagColor
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
    val title: String = "",
    val description: String = "",
    val isUsed: Boolean = false,
    val selectedTags: Set<String> = emptySet(),
    val newTagName: String = "",
    val newTagColor: TagColor? = null,
    val isNewCombo: Boolean = true
)

// Group transient UI states together
data class UiDialogsAndMessages(
    val showImportDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val titleError: String? = null,
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

    // The combo as loaded; edits are saved as a copy so createdAt (and legacy fields) survive.
    private var originalCombo: BattleCombo? = null

    // Track original values to detect changes
    private var originalTitle: String? = null
    private var originalDescription: String? = null
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
                originalCombo = comboWithTags.battleCombo
                originalTitle = comboWithTags.battleCombo.title
                originalDescription = comboWithTags.battleCombo.description
                originalSelectedTags = comboWithTags.tags.map { it.id }.toSet()
                _userInputs.value = UserInputs(
                    comboId = comboId,
                    title = comboWithTags.battleCombo.title,
                    description = comboWithTags.battleCombo.description,
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
    fun onTitleChange(newTitle: String) {
        if (newTitle.length <= BATTLE_COMBO_TITLE_CHARACTER_LIMIT) {
            _userInputs.update { it.copy(title = newTitle) }

            // Clear error on valid input
            if (_dialogsAndMessages.value.titleError != null) {
                _dialogsAndMessages.update { it.copy(titleError = null) }
            }
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

    fun onNewTagColorChange(color: TagColor?) {
        _userInputs.update { it.copy(newTagColor = color) }
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
            val newTag = BattleTag(name = newTagName, color = uiState.value.userInputs.newTagColor)
            battleRepository.insertBattleTag(newTag)
            _userInputs.update {
                it.copy(
                    newTagName = "",
                    newTagColor = null,
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
        val trimmedTitle = currentInputs.title.trim()
        val trimmedDescription = currentInputs.description.trim()

        // Don't save if there are no changes (avoids unnecessary database updates)
        if (!hasUnsavedChanges()) {
            onSuccess()
            return
        }

        // Defensive guards against all business rules
        when {
            trimmedTitle.isBlank() -> {
                _dialogsAndMessages.update {
                    it.copy(titleError = "Title cannot be empty.")
                }
                return
            }

            trimmedTitle.length > BATTLE_COMBO_TITLE_CHARACTER_LIMIT -> {
                _dialogsAndMessages.update {
                    it.copy(titleError = "Title cannot exceed $BATTLE_COMBO_TITLE_CHARACTER_LIMIT characters.")
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
            // New combos get fresh timestamps from the constructor. Edits copy the loaded combo so
            // createdAt is preserved and only modifiedAt moves.
            val battleCombo = originalCombo
                ?.takeUnless { currentInputs.isNewCombo }
                ?.copy(
                    title = trimmedTitle,
                    description = trimmedDescription,
                    isUsed = currentInputs.isUsed,
                    modifiedAt = System.currentTimeMillis()
                )
                ?: BattleCombo(
                    title = trimmedTitle,
                    description = trimmedDescription,
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
            } else {
                battleRepository.updateBattleComboWithTags(
                    battleCombo,
                    selectedTagObjects
                )
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
                onSuccess()
            }
        }
    }

    fun onCancelComboDelete() {
        _dialogsAndMessages.update { it.copy(showDeleteDialog = false) }
    }

    /**
     * Check if there are unsaved changes in the form.
     * Returns true if any field has been modified.
     */
    fun hasUnsavedChanges(): Boolean {
        val currentInputs = _userInputs.value

        // For new combos, check if any fields have been filled
        if (currentInputs.isNewCombo) {
            return currentInputs.title.isNotBlank() ||
                    currentInputs.description.isNotBlank() ||
                    currentInputs.selectedTags.isNotEmpty()
        }

        // For existing combos, check if any fields have been modified
        return currentInputs.title != originalTitle ||
                currentInputs.description != originalDescription ||
                currentInputs.selectedTags != originalSelectedTags
    }
}