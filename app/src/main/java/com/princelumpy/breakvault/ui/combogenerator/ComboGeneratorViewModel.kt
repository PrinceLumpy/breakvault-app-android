// Modified by Claude Code - 2026-09-25
package com.princelumpy.breakvault.ui.combogenerator

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.local.dao.MoveDao
import com.princelumpy.breakvault.data.local.dao.PracticeComboDao
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.PracticeCombo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GenerationMode {
    Random, Structured
}

// State for all user-configurable generation settings.
data class GenerationSettings(
    val currentMode: GenerationMode = GenerationMode.Random,
    val selectedTags: Set<MoveTag> = emptySet(),
    val selectedLength: Int = 3,
    val allowRepeats: Boolean = true,
    val structuredMoveTagSequence: List<MoveTag> = emptyList()
)

// A string resource plus its format args, resolved to text by the screen.
data class UiMessage(
    @StringRes val resId: Int,
    val args: List<Any> = emptyList()
)

// State for the output of the generation.
data class GeneratedComboState(
    val moves: List<Move> = emptyList(),
    val text: String = "",
    val error: UiMessage? = null
)

// State for transient UI events like dialogs and messages.
data class DialogAndMessageState(
    val snackbarMessage: UiMessage? = null,
)

// The final, combined state for the UI to consume.
data class ComboGeneratorUiState(
    val allTags: List<MoveTag> = emptyList(),
    val isLoadingTags: Boolean = true,
    val settings: GenerationSettings = GenerationSettings(),
    val generatedCombo: GeneratedComboState = GeneratedComboState(),
    val dialogAndMessages: DialogAndMessageState = DialogAndMessageState()
)

@HiltViewModel
class ComboGeneratorViewModel @Inject constructor(
    private val moveDao: MoveDao,
    private val practiceComboDao: PracticeComboDao
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
            isLoadingTags = allTags.isEmpty(),
            settings = settings,
            generatedCombo = generatedCombo,
            dialogAndMessages = dialogAndMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ComboGeneratorUiState(isLoadingTags = true)
    )

    fun generateCombo() {
        viewModelScope.launch {
            val moves = when (_settings.value.currentMode) {
                GenerationMode.Random -> generateRandomMoves()
                GenerationMode.Structured -> generateStructuredMoves()
            }
            if (moves.isNotEmpty()) {
                updateGeneratedCombo(moves)
            }
        }
    }

    private suspend fun generateRandomMoves(): List<Move> {
        val settings = _settings.value

        val tagsToUse = if (settings.selectedTags.isEmpty()) {
            moveDao.getAllTagsAsFlow().first()
        } else {
            settings.selectedTags.toList()
        }

        val availableMoves = moveDao.getMovesByTags(tagsToUse.map { it.id })

        if (availableMoves.isEmpty()) {
            showSnackbar(UiMessage(R.string.combo_generator_no_moves_found_message))
            return emptyList()
        }

        val comboLength = settings.selectedLength ?: 4

        return if (settings.allowRepeats) {
            (1..comboLength).map { availableMoves.random() }
        } else {
            if (comboLength > availableMoves.size) {
                _generatedCombo.value = GeneratedComboState(
                    error = UiMessage(
                        R.string.combo_generator_not_enough_moves_message,
                        listOf(comboLength, availableMoves.size)
                    )
                )
                return emptyList()
            }
            availableMoves.shuffled().take(comboLength)
        }
    }

    private suspend fun generateStructuredMoves(): List<Move> {
        val settings = _settings.value
        val structuredSequence = settings.structuredMoveTagSequence.ifEmpty {
            showSnackbar(UiMessage(R.string.combo_generator_define_sequence_message))
            return emptyList()
        }

        val generatedMoves = mutableListOf<Move>()
        for (tag in structuredSequence) {
            val moveForTag = moveDao.getMovesByTags(listOf(tag.id)).randomOrNull()
            if (moveForTag != null) {
                generatedMoves.add(moveForTag)
            } else {
                showSnackbar(
                    UiMessage(R.string.combo_generator_no_move_for_tag_message, listOf(tag.name))
                )
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

    fun onLengthChange(length: Float) {
        val lengthAsInt = length.toInt()
        _settings.update { it.copy(selectedLength = lengthAsInt) }
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
    fun onSnackbarShown() {
        _dialogAndMessages.update { it.copy(snackbarMessage = null) }
    }

    private fun showSnackbar(message: UiMessage) {
        _dialogAndMessages.update { it.copy(snackbarMessage = message) }
    }

    // --- Data Operation Handlers ---
    /**
     * Saves the generated combo to the Lab, named from [nameFormat] (e.g. "Generated Combo #%1$d")
     * with the next number after the highest one already in use.
     */
    fun saveCombo(nameFormat: String) {
        viewModelScope.launch {
            val combo = _generatedCombo.value
            if (combo.moves.isNotEmpty()) {
                val existingNames = practiceComboDao.getAllPracticeCombosList().map { it.name }
                val comboName = nextGeneratedComboName(existingNames, nameFormat)
                val newCombo = PracticeCombo(
                    name = comboName,
                    moves = combo.moves.map { it.name }
                )
                practiceComboDao.insertPracticeCombo(newCombo)
                showSnackbar(
                    UiMessage(R.string.combo_generator_combo_saved_snackbar, listOf(comboName))
                )
            } else {
                showSnackbar(UiMessage(R.string.combo_generator_no_combo_to_save_message))
            }
        }
    }
}

/**
 * Fills [nameFormat]'s `%1$d` with one more than the highest number among [existingNames] that
 * match the format. Deleted or renamed combos therefore never produce a duplicate name.
 */
internal fun nextGeneratedComboName(existingNames: List<String>, nameFormat: String): String {
    val parts = nameFormat.split("%1\$d", limit = 2)
    val prefix = parts[0]
    val suffix = parts.getOrElse(1) { "" }
    val pattern = Regex(Regex.escape(prefix) + "(\\d+)" + Regex.escape(suffix))
    val highest = existingNames
        .mapNotNull { pattern.matchEntire(it.trim())?.groupValues?.get(1)?.toIntOrNull() }
        .maxOrNull() ?: 0
    return prefix + (highest + 1) + suffix
}
