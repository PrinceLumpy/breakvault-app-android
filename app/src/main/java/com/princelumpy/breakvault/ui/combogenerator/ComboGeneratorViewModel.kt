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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class GenerationMode {
    Random, Structured
}

// State for all user-configurable generation settings.
data class GenerationSettings(
    val currentMode: GenerationMode = GenerationMode.Random,
    val selectedTags: Set<MoveTag> = emptySet(),
    val selectedLength: Int? = 4,
    val allowRepeats: Boolean = false,
    val structuredMoveTagSequence: List<MoveTag> = emptyList()
)

// State for the output of the generation.
data class GeneratedComboState(
    val moves: List<Move> = emptyList(),
    val text: String = ""
)

// State for transient UI events like dialogs and messages.
data class DialogAndMessageState(
    val showLengthWarningDialog: Boolean = false,
    val warningDialogMessage: String = "",
    val snackbarMessage: String? = null,
    val lengthDropdownExpanded: Boolean = false
)

// The final, combined state for the UI to consume.
data class ComboGeneratorUiState(
    val allTags: List<MoveTag> = emptyList(),
    val settings: GenerationSettings = GenerationSettings(),
    val generatedCombo: GeneratedComboState = GeneratedComboState(),
    val dialogAndMessages: DialogAndMessageState = DialogAndMessageState()
)

@HiltViewModel
class ComboGeneratorViewModel @Inject constructor(
    private val moveDao: MoveDao,
    private val savedComboDao: SavedComboDao
) : ViewModel() {

    // Separate state flows for each concern.
    private val _settings = MutableStateFlow(GenerationSettings())
    private val _generatedCombo = MutableStateFlow(GeneratedComboState())
    private val _dialogAndMessages = MutableStateFlow(DialogAndMessageState())

    // The single, combined source of truth for the UI.
    val uiState: StateFlow<ComboGeneratorUiState> = combine(
        moveDao.getAllTagsAsFlow(), // Data source from DB
        _settings,
        _generatedCombo,
        _dialogAndMessages
    ) { allTags, settings, generatedCombo, dialogAndMessages ->
        ComboGeneratorUiState(
            allTags = allTags,
            settings = settings,
            generatedCombo = generatedCombo,
            dialogAndMessages = dialogAndMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ComboGeneratorUiState()
    )

    fun generateCombo() {
        viewModelScope.launch {
            val moves = when (_settings.value.currentMode) {
                GenerationMode.Random -> generateRandomMoves()
                GenerationMode.Structured -> generateStructuredMoves()
            }
            updateGeneratedCombo(moves)
        }
    }

    private suspend fun generateRandomMoves(): List<Move> {
        val settings = _settings.value
        val availableMoves = moveDao.getMovesByTags(settings.selectedTags.map { it.id })

        if (availableMoves.isEmpty()) {
            showSnackbar("No moves found for the selected tags.")
            return emptyList()
        }

        val comboLength = settings.selectedLength ?: (3..8).random()

        return if (settings.allowRepeats) {
            (1..comboLength).map { availableMoves.random() }
        } else {
            if (comboLength > availableMoves.size) {
                _dialogAndMessages.update {
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
        val settings = _settings.value
        val structuredSequence = settings.structuredMoveTagSequence.ifEmpty {
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
        _generatedCombo.value = GeneratedComboState(moves = moves, text = comboText)
    }

    // --- Settings Handlers ---
    fun onModeChange(newMode: GenerationMode) {
        _settings.update { it.copy(currentMode = newMode) }
    }

    fun onTagsChange(tags: Set<MoveTag>) {
        _settings.update { it.copy(selectedTags = tags) }
    }

    fun onAllowRepeatsChange(allow: Boolean) {
        _settings.update { it.copy(allowRepeats = allow) }
    }

    fun onLengthChange(length: Int?) {
        _settings.update { it.copy(selectedLength = length) }
    }

    fun onAddTagToSequence(tag: MoveTag) {
        _settings.update { it.copy(structuredMoveTagSequence = it.structuredMoveTagSequence + tag) }
    }

    fun onRemoveLastTagFromSequence() {
        _settings.update {
            it.copy(
                structuredMoveTagSequence = it.structuredMoveTagSequence.dropLast(
                    1
                )
            )
        }
    }

    // --- Dialog and Message Handlers ---
    fun onDropdownExpand(expanded: Boolean) {
        _dialogAndMessages.update { it.copy(lengthDropdownExpanded = expanded) }
    }

    fun onSnackbarShown() {
        _dialogAndMessages.update { it.copy(snackbarMessage = null) }
    }

    fun onDismissLengthWarning() {
        _dialogAndMessages.update {
            it.copy(
                showLengthWarningDialog = false,
                warningDialogMessage = ""
            )
        }
    }

    private fun showSnackbar(message: String) {
        _dialogAndMessages.update { it.copy(snackbarMessage = message) }
    }

    // --- Data Operation Handlers ---
    fun saveCombo() {
        viewModelScope.launch {
            val combo = _generatedCombo.value
            if (combo.moves.isNotEmpty()) {
                val comboName =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val newCombo = SavedCombo(
                    name = comboName,
                    moves = combo.moves.map { it.name }
                )
                savedComboDao.insertSavedCombo(newCombo)
                showSnackbar("Combo saved successfully!")
            } else {
                showSnackbar("No combo to save.")
            }
        }
    }
}
