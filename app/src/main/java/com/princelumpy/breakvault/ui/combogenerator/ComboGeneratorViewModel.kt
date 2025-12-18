package com.princelumpy.breakvault.ui.combogenerator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.dao.MoveDao
import com.princelumpy.breakvault.data.local.dao.SavedComboDao
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.SavedCombo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class GenerationMode {
    Random, Structured
}

data class ComboGeneratorUiState(
    val currentMode: GenerationMode = GenerationMode.Random,
    val allTags: List<MoveTag> = emptyList(),
    val selectedTags: Set<MoveTag> = emptySet(),
    val generatedComboText: String = "",
    val currentGeneratedMoves: List<Move> = emptyList(),
    val selectedLength: Int? = 4,
    val allowRepeats: Boolean = false,
    val structuredMoveTagSequence: List<MoveTag> = emptyList(),
    val showLengthWarningDialog: Boolean = false,
    val warningDialogMessage: String = "",
    val snackbarMessage: String? = null,
    val lengthDropdownExpanded: Boolean = false
)

@HiltViewModel
class ComboGeneratorViewModel @Inject constructor(
    private val moveDao: MoveDao,
    private val savedComboDao: SavedComboDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComboGeneratorUiState())
    val uiState: StateFlow<ComboGeneratorUiState> = _uiState.asStateFlow()

    init {
        loadAllTags()
    }

    fun generateCombo() {
        viewModelScope.launch {
            val moves = when (_uiState.value.currentMode) {
                GenerationMode.Random -> generateRandomMoves()
                GenerationMode.Structured -> generateStructuredMoves()
            }
            updateGeneratedCombo(moves)
        }
    }

    private suspend fun generateRandomMoves(): List<Move> {
        val state = _uiState.value
        val availableMoves = moveDao.getMovesByTags(state.selectedTags.map { it.id })

        if (availableMoves.isEmpty()) {
            showSnackbar("No moves found for the selected tags.")
            return emptyList()
        }

        val comboLength = state.selectedLength ?: (3..8).random()

        return if (state.allowRepeats) {
            (1..comboLength).map { availableMoves.random() }
        } else {
            if (comboLength > availableMoves.size) {
                _uiState.update {
                    it.copy(
                        showLengthWarningDialog = true,
                        warningDialogMessage = "Cannot generate a combo of length $comboLength without repeats from only ${availableMoves.size} moves. Please select more moves or allow repeats."
                    )
                }
                return emptyList()
            }
            availableMoves.shuffled().take(comboLength)
        }
    }

    private suspend fun generateStructuredMoves(): List<Move> {
        val state = _uiState.value
        val structuredSequence = state.structuredMoveTagSequence.ifEmpty {
            showSnackbar("Please define a sequence for Structured mode.")
            return emptyList()
        }

        val generatedMoves = mutableListOf<Move>()
        for (tag in structuredSequence) {
            val moveForTag = moveDao.getMovesByTags(listOf(tag.id)).randomOrNull()
            if (moveForTag != null) {
                generatedMoves.add(moveForTag)
            } else {
                showSnackbar("Could not find a move for tag: ${tag.name}")
            }
        }
        return generatedMoves
    }

    private fun updateGeneratedCombo(moves: List<Move>) {
        val comboText = moves.joinToString(" -> ") { it.name }
        _uiState.update {
            it.copy(
                currentGeneratedMoves = moves,
                generatedComboText = comboText
            )
        }
    }

    fun onModeChange(newMode: GenerationMode) {
        _uiState.update { it.copy(currentMode = newMode) }
    }

    fun onTagsChange(tags: Set<MoveTag>) {
        _uiState.update { it.copy(selectedTags = tags) }
    }

    fun onAllowRepeatsChange(allow: Boolean) {
        _uiState.update { it.copy(allowRepeats = allow) }
    }

    fun onLengthChange(length: Int?) {
        _uiState.update { it.copy(selectedLength = length) }
    }

    fun onAddTagToSequence(tag: MoveTag) {
        _uiState.update {
            it.copy(structuredMoveTagSequence = it.structuredMoveTagSequence + tag)
        }
    }

    fun onRemoveLastTagFromSequence() {
        _uiState.update {
            if (it.structuredMoveTagSequence.isNotEmpty()) {
                it.copy(structuredMoveTagSequence = it.structuredMoveTagSequence.dropLast(1))
            } else {
                it
            }
        }
    }

    fun onDropdownExpand(expanded: Boolean) {
        _uiState.update { it.copy(lengthDropdownExpanded = expanded) }
    }

    fun onSnackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun saveCombo() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.currentGeneratedMoves.isNotEmpty()) {
                val comboName =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val newCombo = SavedCombo(
                    name = comboName,
                    moves = state.currentGeneratedMoves.map { it.name }
                )
                savedComboDao.insertSavedCombo(newCombo)
                showSnackbar("Combo saved successfully!")
            } else {
                showSnackbar("No combo to save.")
            }
        }
    }

    fun onDismissLengthWarning() {
        _uiState.update { it.copy(showLengthWarningDialog = false, warningDialogMessage = "") }
    }

    private fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            moveDao.getAllTagsAsFlow().collect { tags ->
                _uiState.update { it.copy(allTags = tags) }
            }
        }
    }
}
