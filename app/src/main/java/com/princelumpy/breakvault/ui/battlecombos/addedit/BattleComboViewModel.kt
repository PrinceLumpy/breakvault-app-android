package com.princelumpy.breakvault.ui.battlecombos.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.local.entity.BattleCombo
import com.princelumpy.breakvault.data.local.entity.BattleTag
import com.princelumpy.breakvault.data.local.entity.EnergyLevel
import com.princelumpy.breakvault.data.local.entity.TrainingStatus
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditBattleComboUiState(
    val comboId: String? = null,
    val description: String = "",
    val selectedEnergy: EnergyLevel = EnergyLevel.NONE,
    val selectedStatus: TrainingStatus = TrainingStatus.TRAINING,
    val isUsed: Boolean = false,
    val allBattleTags: List<BattleTag> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val newTagName: String = "",
    val showImportDialog: Boolean = false,
    val isNewCombo: Boolean = true,
    val snackbarMessage: String? = null
)

class BattleComboViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddEditBattleComboUiState())
    val uiState: StateFlow<AddEditBattleComboUiState> = _uiState.asStateFlow()

    private val battleDao = AppDB.getDatabase(application).battleDao()
    private val savedComboDao = AppDB.getDatabase(application).savedComboDao()

    val allBattleTags: LiveData<List<BattleTag>> = battleDao.getAllBattleTagsLiveData()
    val practiceCombos: LiveData<List<SavedCombo>> = savedComboDao.getAllSavedCombosLiveData()

    init {
        allBattleTags.observeForever { tags ->
            _uiState.update { it.copy(allBattleTags = tags) }
        }
    }

    fun loadCombo(comboId: String?) {
        if (comboId == null) {
            _uiState.update { currentState ->
                AddEditBattleComboUiState(allBattleTags = currentState.allBattleTags)
            }
            return
        }

        viewModelScope.launch {
            val comboWithTags = battleDao.getBattleComboWithTags(comboId)
            if (comboWithTags != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        comboId = comboId,
                        description = comboWithTags.battleCombo.description,
                        selectedEnergy = comboWithTags.battleCombo.energy,
                        selectedStatus = comboWithTags.battleCombo.status,
                        isUsed = comboWithTags.battleCombo.isUsed,
                        selectedTags = comboWithTags.tags.map { it.name }.toSet(),
                        isNewCombo = false
                    )
                }
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onEnergyChange(newEnergy: EnergyLevel) {
        _uiState.update { it.copy(selectedEnergy = newEnergy) }
    }

    fun onStatusChange(newStatus: TrainingStatus) {
        _uiState.update { it.copy(selectedStatus = newStatus) }
    }

    fun onTagSelected(tagName: String) {
        val currentSelectedTags = _uiState.value.selectedTags
        val newSelectedTags = if (currentSelectedTags.contains(tagName)) {
            currentSelectedTags - tagName
        } else {
            currentSelectedTags + tagName
        }
        _uiState.update { it.copy(selectedTags = newSelectedTags) }
    }

    fun onNewTagNameChange(newTagName: String) {
        _uiState.update { it.copy(newTagName = newTagName) }
    }

    fun addBattleTag() {
        val newTagName = _uiState.value.newTagName.trim()
        if (newTagName.isNotBlank()) {
            viewModelScope.launch {
                if (battleDao.getBattleTagByName(newTagName) == null) {
                    battleDao.insertBattleTag(BattleTag(name = newTagName))
                    _uiState.update {
                        it.copy(
                            newTagName = "",
                            selectedTags = it.selectedTags + newTagName
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            newTagName = "",
                            snackbarMessage = "Tag already exists"
                        )
                    }
                }
            }
        }
    }

    fun onImportCombo(combo: SavedCombo) {
        _uiState.update {
            it.copy(
                description = combo.moves.joinToString(" -> "),
                showImportDialog = false
            )
        }
    }

    fun showImportDialog(show: Boolean) {
        _uiState.update { it.copy(showImportDialog = show) }
    }

    fun saveCombo(onSuccess: () -> Unit) {
        val currentUiState = _uiState.value
        if (currentUiState.description.isNotBlank()) {
            viewModelScope.launch {
                val battleCombo = BattleCombo(
                    id = currentUiState.comboId ?: "",
                    description = currentUiState.description,
                    energy = currentUiState.selectedEnergy,
                    status = currentUiState.selectedStatus,
                    isUsed = currentUiState.isUsed
                )
                if (currentUiState.isNewCombo) {
                    battleDao.insertBattleCombo(battleCombo)
                    battleDao.updateBattleComboWithTags(
                        battleCombo,
                        currentUiState.selectedTags.toList()
                    )

                } else {
                    battleDao.updateBattleComboWithTags(
                        battleCombo,
                        currentUiState.selectedTags.toList()
                    )
                }
                onSuccess()
            }
        } else {
            _uiState.update { it.copy(snackbarMessage = "Description cannot be empty") }
        }
    }

    fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        allBattleTags.removeObserver { }
    }
}
