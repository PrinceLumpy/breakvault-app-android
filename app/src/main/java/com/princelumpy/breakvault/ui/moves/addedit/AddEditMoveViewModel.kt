package com.princelumpy.breakvault.ui.moves.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.local.database.AppDB
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.MoveTagCrossRef
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class AddEditMoveUiState(
    val moveId: String? = null,
    val moveName: String = "",
    val newTagName: String = "",
    val allTags: List<MoveTag> = emptyList(),
    val selectedTags: Set<MoveTag> = emptySet(),
    val snackbarMessage: String? = null,
    val isNewMove: Boolean = true
)

interface IAddEditMoveViewModel {
    val uiState: StateFlow<AddEditMoveUiState>
    val allTags: LiveData<List<MoveTag>>

    fun loadMove(moveId: String?)
    fun onMoveNameChange(newName: String)
    fun onNewTagNameChange(newName: String)
    fun onTagSelected(tag: MoveTag)
    fun addTag()
    fun saveMove(onSuccess: () -> Unit)
    fun onSnackbarMessageShown()
}

class AddEditMoveViewModel(application: Application) : AndroidViewModel(application),
    IAddEditMoveViewModel {

    private val _uiState = MutableStateFlow(AddEditMoveUiState())
    override val uiState: StateFlow<AddEditMoveUiState> = _uiState.asStateFlow()

    private val db = AppDB.getDatabase(application)
    private val moveTagDao = db.moveDao()

    private var newlyAddedTagName: String? = null
    override val allTags: LiveData<List<MoveTag>> = moveTagDao.getAllTags()

    init {
        allTags.observeForever { tags ->
            _uiState.update { currentState ->
                val newTagName = newlyAddedTagName
                if (newTagName != null) {
                    val newTagObject = tags.find { it.name.equals(newTagName, ignoreCase = true) }
                    if (newTagObject != null) {
                        newlyAddedTagName = null
                        return@update currentState.copy(
                            allTags = tags,
                            selectedTags = currentState.selectedTags + newTagObject
                        )
                    }
                }
                currentState.copy(allTags = tags)
            }
        }
    }

    override fun loadMove(moveId: String?) {
        if (moveId == null) {
            _uiState.value = AddEditMoveUiState()
            return
        }

        viewModelScope.launch {
            val moveBeingEdited = getMoveForEditing(moveId)
            if (moveBeingEdited != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        moveId = moveId,
                        moveName = moveBeingEdited.move.name,
                        selectedTags = moveBeingEdited.moveTags.toSet(),
                        isNewMove = false
                    )
                }
            } else {
                _uiState.update { it.copy(snackbarMessage = "Could not find move with ID $moveId") }
            }
        }
    }

    override fun onMoveNameChange(newName: String) {
        if (newName.length <= 100) {
            _uiState.update { it.copy(moveName = newName) }
        }
    }

    override fun onNewTagNameChange(newName: String) {
        if (newName.length <= 30) {
            _uiState.update { it.copy(newTagName = newName) }
        }
    }

    override fun onTagSelected(tag: MoveTag) {
        val currentSelectedTags = _uiState.value.selectedTags
        val newSelectedTags = if (currentSelectedTags.any { it.id == tag.id }) {
            currentSelectedTags.filterNot { it.id == tag.id }.toSet()
        } else {
            currentSelectedTags + tag
        }
        _uiState.update { it.copy(selectedTags = newSelectedTags) }
    }

    override fun addTag() {
        val newTagName = _uiState.value.newTagName.trim()
        if (newTagName.isNotBlank()) {
            if (!_uiState.value.allTags.any { it.name.equals(newTagName, ignoreCase = true) }) {
                newlyAddedTagName = newTagName
                addTag(newTagName)
                _uiState.update { it.copy(newTagName = "") }
            } else {
                _uiState.update { it.copy(snackbarMessage = "Tag '$newTagName' already exists.") }
            }
        }
    }

    override fun saveMove(onSuccess: () -> Unit) {
        val currentUiState = _uiState.value
        if (currentUiState.moveName.isNotBlank()) {
            if (currentUiState.isNewMove) {
                addMove(currentUiState.moveName, currentUiState.selectedTags.toList())
            } else {
                updateMoveAndTags(
                    currentUiState.moveId!!,
                    currentUiState.moveName,
                    currentUiState.selectedTags.toList()
                )
            }
            onSuccess()
        } else {
            _uiState.update { it.copy(snackbarMessage = "Move name cannot be blank.") }
        }
    }

    override fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun addMove(moveName: String, selectedMoveTags: List<MoveTag>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newMoveId = UUID.randomUUID().toString()
            val move = Move(id = newMoveId, name = moveName)
            moveTagDao.insertMove(move)
            selectedMoveTags.forEach { tag ->
                moveTagDao.link(MoveTagCrossRef(moveId = newMoveId, tagId = tag.id))
            }
        }
    }

    private fun addTag(tagName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            moveTagDao.insertMoveTag(MoveTag(id = UUID.randomUUID().toString(), name = tagName))
        }
    }

    private suspend fun getMoveForEditing(moveId: String): MoveWithTags? =
        withContext(Dispatchers.IO) {
            moveTagDao.getMoveWithTags(moveId)
        }

    private fun updateMoveAndTags(
        moveId: String,
        newName: String,
        newSelectedMoveTags: List<MoveTag>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            moveTagDao.updateMoveName(moveId, newName, System.currentTimeMillis())
            moveTagDao.unlinkMoveFromAllTags(moveId)
            newSelectedMoveTags.forEach { tag ->
                moveTagDao.link(MoveTagCrossRef(moveId = moveId, tagId = tag.id))
            }
        }
    }
}

class FakeAddEditMoveViewModel : IAddEditMoveViewModel {
    private val _uiState = MutableStateFlow(
        AddEditMoveUiState(
            allTags = listOf(MoveTag("t1", "Footwork"), MoveTag("t2", "Toprock"))
        )
    )
    override val uiState: StateFlow<AddEditMoveUiState> = _uiState.asStateFlow()

    override val allTags: LiveData<List<MoveTag>> = MutableLiveData(
        listOf(MoveTag("t1", "Footwork"), MoveTag("t2", "Toprock"), MoveTag("t3", "Freeze"))
    )

    override fun loadMove(moveId: String?) {
        if (moveId != null) {
            _uiState.update {
                it.copy(
                    isNewMove = false,
                    moveName = "6-Step",
                    selectedTags = setOf(MoveTag("t1", "Footwork"))
                )
            }
        }
    }

    override fun onMoveNameChange(newName: String) {}
    override fun onNewTagNameChange(newName: String) {}
    override fun onTagSelected(tag: MoveTag) {}
    override fun addTag() {}
    override fun saveMove(onSuccess: () -> Unit) {
        onSuccess()
    }

    override fun onSnackbarMessageShown() {}
}
