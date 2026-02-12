package com.princelumpy.breakvault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.princelumpy.breakvault.data.local.entity.Move
import com.princelumpy.breakvault.data.local.entity.MoveTag
import com.princelumpy.breakvault.data.local.entity.MoveTagCrossRef
import com.princelumpy.breakvault.data.local.relation.MoveWithTags
import com.princelumpy.breakvault.data.local.relation.TagWithMoves
import kotlinx.coroutines.flow.Flow

@Dao
interface MoveDao {
    @Query("SELECT * FROM moves")
    suspend fun getAllMoves(): List<Move>

    @Query("SELECT * FROM move_tags")
    suspend fun getAllMoveTags(): List<MoveTag>

    @Query("SELECT * FROM move_tag_cross_refs")
    suspend fun getAllMoveTagCrossRefs(): List<MoveTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoves(moves: List<Move>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoveTags(moveTags: List<MoveTag>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoveTagCrossRefs(crossRefs: List<MoveTagCrossRef>)

    @Transaction
    @Query("SELECT * FROM moves")
    fun getAllMovesWithTags(): Flow<List<MoveWithTags>>

    @Transaction
    @Query("SELECT * FROM moves")
    suspend fun getMovesWithTagsList(): List<MoveWithTags>

    @Transaction
    @Query("SELECT * FROM moves WHERE id = :moveId")
    suspend fun getMoveWithTags(moveId: String): MoveWithTags?

    @Insert
    suspend fun insertMove(move: Move)

    @Update
    suspend fun updateMove(move: Move)

    @Query("UPDATE moves SET name = :name, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateMoveName(id: String, name: String, modifiedAt: Long)

    @Delete
    suspend fun deleteMove(move: Move)

    @Transaction
    suspend fun deleteMoveCompletely(move: Move) {
        unlinkMoveFromAllTags(move.id)
        deleteMove(move)
    }

    @Transaction
    @Query("SELECT * FROM move_tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<MoveTag>>

    @Query("SELECT * FROM move_tags ORDER BY name ASC")
    fun getAllTagsAsFlow(): Flow<List<MoveTag>>

    @Transaction
    @Query("SELECT * FROM move_tags")
    fun getTagsWithMoves(): Flow<List<TagWithMoves>>

    @Transaction
    @Query("SELECT * FROM move_tags WHERE id = :tagId")
    suspend fun getTagWithMoves(tagId: String): TagWithMoves?

    @Insert
    suspend fun insertMoveTag(moveTag: MoveTag)

    @Update
    suspend fun updateMoveTag(moveTag: MoveTag)

    @Query("UPDATE move_tags SET name = :name, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateTagName(id: String, name: String, modifiedAt: Long)

    @Delete
    suspend fun deleteTag(moveTag: MoveTag)

    @Transaction
    suspend fun deleteTagCompletely(moveTag: MoveTag) {
        unlinkTagFromAllMoves(moveTag.id)
        deleteTag(moveTag)
    }

    @Query(
        """
    SELECT DISTINCT m.* FROM moves m
    INNER JOIN move_tag_cross_refs AS mt_cross ON m.id = mt_cross.moveId
    WHERE mt_cross.tagId IN (:tagIds)
"""
    )
    suspend fun getMovesByTags(tagIds: List<String>): List<Move>

    @Query("SELECT * FROM moves WHERE id IN (SELECT moveId FROM move_tag_cross_refs WHERE tagId = :tagId)")
    fun getMovesByTagId(tagId: String): Flow<List<Move>>


    @Query("DELETE FROM move_tag_cross_refs WHERE moveId = :moveId")
    suspend fun unlinkMoveFromAllTags(moveId: String)

    @Query("DELETE FROM move_tag_cross_refs WHERE tagId = :tagId")
    suspend fun unlinkTagFromAllMoves(tagId: String)

    @Insert
    suspend fun link(moveTag: MoveTagCrossRef)
}