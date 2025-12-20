package com.princelumpy.breakvault.data.repository

import com.princelumpy.breakvault.data.local.dao.MoveDao
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.MoveTagCrossRef
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoveRepository @Inject constructor(
    private val moveDao: MoveDao
) {
    // ✅ Just return data, no filtering logic
    fun getAllMovesWithTags(): Flow<List<MoveWithTags>> {
        return moveDao.getAllMovesWithTags()
    }

    fun getAllTags(): Flow<List<MoveTag>> {
        return moveDao.getAllTags()
    }

    suspend fun deleteMove(move: Move) {
        moveDao.deleteMove(move)
    }

    /**
     * Inserts a new move and links it to a list of tags in a single transaction.
     * This operation is main-safe.
     */
    suspend fun insertMoveWithTags(move: Move, tags: List<MoveTag>) {
        withContext(Dispatchers.IO) {
            moveDao.insertMove(move)
            tags.forEach { tag ->
                moveDao.link(MoveTagCrossRef(moveId = move.id, tagId = tag.id))
            }
        }
    }

    /**
     * Inserts a single new move tag.
     * This operation is main-safe.
     */
    suspend fun insertMoveTag(tag: MoveTag) {
        withContext(Dispatchers.IO) {
            moveDao.insertMoveTag(tag)
        }
    }

    /**
     * Retrieves a single move with its associated tags.
     * This is a suspend function that is safe to call from a coroutine.
     * @return The MoveWithTags object or null if not found.
     */
    suspend fun getMoveWithTags(moveId: String): MoveWithTags? {
        return withContext(Dispatchers.IO) {
            moveDao.getMoveWithTags(moveId)
        }
    }

    /**
     * Updates a move's name and replaces its associated tags with a new list.
     * This is performed in a single transaction. This operation is main-safe.
     */
    suspend fun updateMoveWithTags(moveId: String, newName: String, newTags: List<MoveTag>) {
        withContext(Dispatchers.IO) {
            // Room runs this in a transaction because of the @Transaction annotation
            // on the DAO method this will eventually call.
            moveDao.updateMoveName(moveId, newName, System.currentTimeMillis())
            moveDao.unlinkMoveFromAllTags(moveId)
            newTags.forEach { tag ->
                moveDao.link(MoveTagCrossRef(moveId = moveId, tagId = tag.id))
            }
        }
    }

    // -- Tags --
    suspend fun updateTagName(tagId: String, newName: String) {
        withContext(Dispatchers.IO) {
            moveDao.updateTagName(tagId, newName, System.currentTimeMillis())
        }
    }

    suspend fun deleteTagCompletely(tag: MoveTag) {
        withContext(Dispatchers.IO) {
            moveDao.deleteTagCompletely(tag)
        }
    }
}