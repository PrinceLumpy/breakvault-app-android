package com.princelumpy.breakvault.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer // Added Observer import
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.MoveTagCrossRef
import com.princelumpy.breakvault.data.MoveWithTags
import com.princelumpy.breakvault.data.SavedCombo
import com.princelumpy.breakvault.data.SavedComboMoveLink
import com.princelumpy.breakvault.data.SavedComboWithMoves
import com.princelumpy.breakvault.data.Tag
import com.princelumpy.breakvault.data.transfer.AppDataExport // Required import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.min

interface IMoveViewModel {
    val movesWithTags: LiveData<List<MoveWithTags>>
    val allTags: LiveData<List<Tag>>
    val savedCombos: LiveData<List<SavedComboWithMoves>>

    fun addMove(moveName: String, selectedTags: List<Tag>)
    fun addTag(tagName: String)
    suspend fun getMoveForEditing(moveId: String): MoveWithTags?
    fun updateMoveAndTags(moveId: String, newName: String, newSelectedTags: List<Tag>)
    fun deleteMove(move: Move)
    fun updateTag(tagId: String, newName: String)
    fun deleteTag(tag: Tag)
    fun generateComboFromTags(selectedTags: Set<Tag>, length: Int? = null): List<Move> // Updated signature
    fun saveCombo(comboName: String, moves: List<Move>)
    fun deleteSavedCombo(savedComboId: String)
    fun updateSavedComboName(savedComboId: String, newName: String)
    fun resetDatabase()

    // Added for Import/Export
    suspend fun getAppDataForExport(): AppDataExport?
    suspend fun importAppData(appData: AppDataExport): Boolean
}

class MoveViewModel(application: Application) : AndroidViewModel(application), IMoveViewModel {

    private val db = AppDB.getDatabase(application)
    private val moveTagDao = db.moveTagDao()
    private val savedComboDao = db.savedComboDao()

    override val movesWithTags: LiveData<List<MoveWithTags>> = moveTagDao.getMovesWithTags()
    override val allTags: LiveData<List<Tag>> = moveTagDao.getAllTags()
    override val savedCombos: LiveData<List<SavedComboWithMoves>> =
        savedComboDao.getAllSavedCombosWithMoves()

    // Observer for debugging movesWithTags
    private val movesWithTagsObserver = Observer<List<MoveWithTags>> { data ->
        if (data.isNullOrEmpty()) {
            Log.d("MoveViewModel_Debug", "movesWithTags LiveData is EMPTY or NULL")
        } else {
            Log.d("MoveViewModel_Debug", "movesWithTags LiveData has ${data.size} items:")
            data.forEach { moveWithTags ->
                val tagNames = moveWithTags.tags.joinToString { it.name }
                Log.d("MoveViewModel_Debug", "  Move: ${moveWithTags.move.name}, Tags: [$tagNames]")
            }
        }
    }

    init {
        Log.d("MoveViewModel_Debug", "MoveViewModel initialized.")
        // Start observing movesWithTags for debugging
        movesWithTags.observeForever(movesWithTagsObserver)

        // You can also log the initial value if available, though LiveData from Room might not have it immediately
        val initialValue = movesWithTags.value
        if (initialValue.isNullOrEmpty()) {
            Log.d("MoveViewModel_Debug", "movesWithTags initial value is EMPTY or NULL in init.")
        } else {
            Log.d("MoveViewModel_Debug", "movesWithTags initial value in init has ${initialValue.size} items.")
        }
    }

    override fun addMove(moveName: String, selectedTags: List<Tag>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newMoveId = UUID.randomUUID().toString()
                val move = Move(id = newMoveId, name = moveName)
                moveTagDao.addMove(move)
                selectedTags.forEach { tag ->
                    moveTagDao.link(MoveTagCrossRef(moveId = newMoveId, tagId = tag.id))
                }
                Log.i("MoveViewModel", "addMove successful for: $moveName")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in addMove", e)
            }
        }
    }

    override fun addTag(tagName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                moveTagDao.addTag(Tag(id = UUID.randomUUID().toString(), name = tagName))
                Log.i("MoveViewModel", "addTag successful for: $tagName")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in addTag", e)
            }
        }
    }

    override suspend fun getMoveForEditing(moveId: String): MoveWithTags? = withContext(Dispatchers.IO) {
        try {
            moveTagDao.getMoveWithTagsById(moveId)
        } catch (e: Exception) {
            Log.e("MoveViewModel", "Error in getMoveForEditing for id: $moveId", e)
            null
        }
    }

    override fun updateMoveAndTags(moveId: String, newName: String, newSelectedTags: List<Tag>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                moveTagDao.updateMove(Move(id = moveId, name = newName))
                moveTagDao.unlinkMoveFromAllTags(moveId)
                newSelectedTags.forEach { tag ->
                    moveTagDao.link(MoveTagCrossRef(moveId = moveId, tagId = tag.id))
                }
                Log.i("MoveViewModel", "updateMoveAndTags successful for id: $moveId")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in updateMoveAndTags for id: $moveId", e)
            }
        }
    }

    override fun deleteMove(move: Move) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                moveTagDao.deleteMoveCompletely(move)
                Log.i("MoveViewModel", "deleteMove successful for: ${move.name}")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in deleteMove for: ${move.name}", e)
            }
        }
    }

    override fun updateTag(tagId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (newName.isNotBlank()) {
                    moveTagDao.updateTag(Tag(id = tagId, name = newName))
                    Log.i("MoveViewModel", "updateTag successful for id: $tagId")
                } else {
                    Log.w("MoveViewModel", "updateTag skipped: newName is blank for id: $tagId")
                }
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in updateTag for id: $tagId", e)
            }
        }
    }

    override fun deleteTag(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                moveTagDao.deleteTagCompletely(tag)
                Log.i("MoveViewModel", "deleteTag successful for: ${tag.name}")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in deleteTag for: ${tag.name}", e)
            }
        }
    }

    override fun generateComboFromTags(selectedTags: Set<Tag>, length: Int?): List<Move> {
        val currentMovesWithTags = movesWithTags.value ?: return emptyList()
        if (selectedTags.isEmpty()) {
            Log.i("MoveViewModel", "generateComboFromTags: No tags selected, returning empty list.")
            return emptyList()
        }

        val selectedTagIds = selectedTags.map { it.id }.toSet()
        val matchingMoves = currentMovesWithTags
            .filter { moveWithTags -> moveWithTags.tags.any { it.id in selectedTagIds } }
            .map { it.move }

        if (matchingMoves.isEmpty()) {
            Log.i("MoveViewModel", "generateComboFromTags: No moves found matching selected tags.")
            return emptyList()
        }

        val effectiveLength = when {
            length == null -> (2..6).random()
            length < 2 -> 2
            length > 6 -> 6
            else -> length
        }

        if (length != null && length > matchingMoves.size) {
            Log.w("MoveViewModel", "User originally requested $length moves, but only ${matchingMoves.size} are available with selected tags. Effective length to attempt is $effectiveLength.")
        } else if (length != null) {
            Log.d("MoveViewModel", "User originally requested $length moves. Effective length to attempt is $effectiveLength.")
        } else {
            Log.d("MoveViewModel", "User selected Auto length. Effective (random) length to attempt is $effectiveLength.")
        }

        val movesToTake = min(effectiveLength, matchingMoves.size)
        Log.d("MoveViewModel", "Generating combo with $movesToTake moves.")
        return matchingMoves.shuffled().take(movesToTake)
    }

    override fun saveCombo(comboName: String, moves: List<Move>) {
        val finalComboName = comboName.ifBlank {
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                .let { "Combo $it" }
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newSavedCombo = SavedCombo(name = finalComboName)
                savedComboDao.saveFullCombo(newSavedCombo, moves)
                Log.i("MoveViewModel", "saveCombo successful for: $finalComboName")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in saveCombo for: $finalComboName", e)
            }
        }
    }

    override fun deleteSavedCombo(savedComboId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                savedComboDao.deleteSavedComboById(savedComboId)
                Log.i("MoveViewModel", "deleteSavedCombo successful for id: $savedComboId")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in deleteSavedCombo for id: $savedComboId", e)
            }
        }
    }

    override fun updateSavedComboName(savedComboId: String, newName: String) {
        if (newName.isBlank()) {
            Log.w("MoveViewModel", "updateSavedComboName skipped: newName is blank for id: $savedComboId")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                savedComboDao.updateSavedComboName(savedComboId, newName)
                Log.i("MoveViewModel", "updateSavedComboName successful for id: $savedComboId")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in updateSavedComboName for id: $savedComboId", e)
            }
        }
    }

    override fun resetDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.clearAllTables()
                AppDB.prepopulateDefaultTags(db) // Use the centralized pre-population method
                Log.i("MoveViewModel", "Database reset and default tags re-populated.")
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in resetDatabase", e)
            }
        }
    }

    override suspend fun getAppDataForExport(): AppDataExport? = withContext(Dispatchers.IO) {
        try {
            val moves = moveTagDao.getAllMovesList()
            val tags = moveTagDao.getAllTagsList()
            val moveTagCrossRefs = moveTagDao.getAllMoveTagCrossRefsList()
            val savedCombos = savedComboDao.getAllSavedCombosList()
            val savedComboMoveLinks = savedComboDao.getAllSavedComboMoveLinksList()
            Log.i("MoveViewModel", "getAppDataForExport successful.")
            AppDataExport(moves, tags, moveTagCrossRefs, savedCombos, savedComboMoveLinks)
        } catch (e: Exception) {
            Log.e("MoveViewModel", "Error in getAppDataForExport", e)
            null
        }
    }

    override suspend fun importAppData(appData: AppDataExport): Boolean = withContext(Dispatchers.IO) {
        try {
            db.clearAllTables() // Clear before importing
            moveTagDao.insertAllMoves(appData.moves)
            moveTagDao.insertAllTags(appData.tags) // Import tags first
            moveTagDao.insertAllMoveTagCrossRefs(appData.moveTagCrossRefs)
            savedComboDao.insertAllSavedCombos(appData.savedCombos)
            savedComboDao.insertAllSavedComboMoveLinks(appData.savedComboMoveLinks)
            AppDB.prepopulateDefaultTags(db) // Ensure default tags exist, especially if import is empty or doesn't have them
            Log.i("MoveViewModel", "importAppData successful.")
            true
        } catch (e: Exception) {
            Log.e("MoveViewModel", "Error in importAppData", e)
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the observer when the ViewModel is cleared to prevent memory leaks
        movesWithTags.removeObserver(movesWithTagsObserver)
        Log.i("MoveViewModelLifecycle", "MoveViewModel cleared and movesWithTagsObserver removed.")
    }
}

// --- FakeMoveViewModel --- (Now includes import/export)
class FakeMoveViewModel : IMoveViewModel {
    private val initialFakeMovesWithTags = listOf(
        MoveWithTags(Move("m1", "Preview Punch"), listOf(Tag("t1", "Starter"))),
        MoveWithTags(
            Move("m2", "Preview Kick"),
            listOf(Tag("t1", "Starter"), Tag("t2", "Finisher"))
        )
    )
    private val initialFakeTags = listOf(
        Tag("t1", "Starter"), Tag("t2", "Finisher"), Tag("t3", "Power Move"), Tag("t4", "Quick")
    )
    private val fakeMove1 = Move("fm1", "Fake Move Alpha")
    private val fakeMove2 = Move("fm2", "Fake Move Beta")
    private val fakeMove3 = Move("fm3", "Fake Move Gamma")
    private val initialFakeSavedCombosWithMoves = listOf(
        SavedComboWithMoves(
            SavedCombo(id = "sc1", name = "My Favorite Combo"),
            listOf(fakeMove1, fakeMove2)
        ),
        SavedComboWithMoves(
            SavedCombo(id = "sc2", name = "Quick Warmup"),
            listOf(fakeMove3, fakeMove1, fakeMove3)
        )
    )

    private val _movesWithTags = MutableLiveData(initialFakeMovesWithTags)
    override val movesWithTags: LiveData<List<MoveWithTags>> = _movesWithTags

    private val _allTags = MutableLiveData(initialFakeTags)
    override val allTags: LiveData<List<Tag>> = _allTags

    private val _savedCombos = MutableLiveData(initialFakeSavedCombosWithMoves)
    override val savedCombos: LiveData<List<SavedComboWithMoves>> = _savedCombos

    override fun addMove(moveName: String, selectedTags: List<Tag>) { Log.d("FakeMoveViewModel", "addMove: $moveName") }
    override fun addTag(tagName: String) { Log.d("FakeMoveViewModel", "addTag: $tagName") }
    override suspend fun getMoveForEditing(moveId: String): MoveWithTags? {
        Log.d("FakeMoveViewModel", "getMoveForEditing: $moveId")
        return _movesWithTags.value?.find { it.move.id == moveId }
    }
    override fun updateMoveAndTags(moveId: String, newName: String, newSelectedTags: List<Tag>) { Log.d("FakeMoveViewModel", "updateMoveAndTags: $moveId") }
    override fun deleteMove(move: Move) { Log.d("FakeMoveViewModel", "deleteMove: ${move.name}") }
    override fun updateTag(tagId: String, newName: String) { Log.d("FakeMoveViewModel", "updateTag: $tagId") }
    override fun deleteTag(tag: Tag) { Log.d("FakeMoveViewModel", "deleteTag: ${tag.name}") }

    override fun generateComboFromTags(selectedTags: Set<Tag>, length: Int?): List<Move> {
        Log.d("FakeMoveViewModel", "generateComboFromTags. Tags: ${selectedTags.joinToString { it.name }}, Length: $length")

        val allFakeMoves = listOf(fakeMove1, fakeMove2, fakeMove3) +
                           initialFakeMovesWithTags.map { it.move } +
                           initialFakeSavedCombosWithMoves.flatMap { it.moves }
        val distinctFakeMoves = allFakeMoves.distinctBy { it.id }

        val matchingMoves = if (selectedTags.isEmpty()) {
            distinctFakeMoves
        } else {
            val selectedTagIds = selectedTags.map { it.id }.toSet()
            initialFakeMovesWithTags // Assuming initialFakeMovesWithTags is the source of truth for tag-move relations in fake
                .filter { mwt -> mwt.tags.any { tag -> tag.id in selectedTagIds } }
                .map { it.move }
                .ifEmpty { distinctFakeMoves } // Fallback to all if no specific tag match found, to ensure some output
        }

        if (matchingMoves.isEmpty()) {
            Log.d("FakeMoveViewModel", "No fake moves available for combo generation after filtering.")
            return emptyList()
        }

        val effectiveLength = when {
            length == null -> (2..6).random()
            length < 2 -> 2
            length > 6 -> 6
            else -> length
        }

        val movesToTake = min(effectiveLength, matchingMoves.size)
        Log.d("FakeMoveViewModel", "Generating fake combo with $movesToTake moves from ${matchingMoves.size} matching moves.")
        return matchingMoves.shuffled().take(movesToTake)
    }

    override fun saveCombo(comboName: String, moves: List<Move>) { Log.d("FakeMoveViewModel", "saveCombo: $comboName") }
    override fun deleteSavedCombo(savedComboId: String) { Log.d("FakeMoveViewModel", "deleteSavedCombo: $savedComboId") }
    override fun updateSavedComboName(savedComboId: String, newName: String) { Log.d("FakeMoveViewModel", "updateSavedComboName: $savedComboId") }
    override fun resetDatabase() {
        Log.d("FakeMoveViewModel", "resetDatabase called")
        _movesWithTags.value = initialFakeMovesWithTags
        _allTags.value = initialFakeTags
        _savedCombos.value = initialFakeSavedCombosWithMoves
    }

    override suspend fun getAppDataForExport(): AppDataExport? {
        Log.d("FakeMoveViewModel", "getAppDataForExport called")
        val moves = _movesWithTags.value?.map { it.move } ?: emptyList()
        val tags = _allTags.value ?: emptyList()
        val moveTagCrossRefs = _movesWithTags.value?.flatMap { mwt -> mwt.tags.map { tag ->
            MoveTagCrossRef(
                mwt.move.id,
                tag.id
            )
        } } ?: emptyList()
        val savedCombosList = _savedCombos.value?.map { it.savedCombo } ?: emptyList()
        val savedComboMoveLinks = _savedCombos.value?.flatMap { scwm ->
            scwm.moves.mapIndexed { index, move ->
                SavedComboMoveLink(
                    scwm.savedCombo.id,
                    move.id,
                    index
                )
            }
        } ?: emptyList()
        return AppDataExport(moves, tags, moveTagCrossRefs, savedCombosList, savedComboMoveLinks)
    }

    override suspend fun importAppData(appData: AppDataExport): Boolean {
        Log.d("FakeMoveViewModel", "importAppData called with ${appData.moves.size} moves, ${appData.tags.size} tags")
        _movesWithTags.value = appData.moves.map { move ->
            MoveWithTags(
                move,
                appData.moveTagCrossRefs.filter { it.moveId == move.id }
                    .mapNotNull { crossRef -> appData.tags.find { it.id == crossRef.tagId } })
        }
        _allTags.value = appData.tags
        _savedCombos.value = appData.savedCombos.map { sc ->
            SavedComboWithMoves(
                sc,
                appData.savedComboMoveLinks.filter { it.savedComboId == sc.id }
                    .sortedBy { it.orderInCombo }
                    .mapNotNull { link -> appData.moves.find { it.id == link.moveId } })
        }
        return true
    }
}
