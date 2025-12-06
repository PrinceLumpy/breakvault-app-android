package com.princelumpy.breakvault.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MoveTagDao {

    @Insert
    suspend fun addMove(move: Move)

    @Insert
    suspend fun addTag(moveTag: MoveTag)

    @Insert
    suspend fun link(moveTag: MoveTagCrossRef)

    // --- Methods for Export --- 
    @Query("SELECT * FROM moves")
    suspend fun getAllMovesList(): List<Move>

    @Query("SELECT * FROM move_tags")
    suspend fun getAllTagsList(): List<MoveTag>

    @Query("SELECT * FROM move_tag_cross_refs")
    suspend fun getAllMoveTagCrossRefsList(): List<MoveTagCrossRef>

    // --- Methods for Import --- 
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoves(moves: List<Move>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTags(moveListTags: List<MoveTag>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoveTagCrossRefs(crossRefs: List<MoveTagCrossRef>)

    // --- Existing Methods --- 
    @Transaction
    @Query("SELECT * FROM moves")
    fun getMovesWithTags(): LiveData<List<MoveWithTags>>

    @Transaction
    @Query("SELECT * FROM move_tags")
    fun getTagsWithMoves(): LiveData<List<TagWithMoves>>

    @Query("SELECT * FROM move_tags ORDER BY name ASC")
    fun getAllTags(): LiveData<List<MoveTag>>

    @Transaction
    @Query("SELECT * FROM moves WHERE id = :moveId")
    suspend fun getMoveWithTagsById(moveId: String): MoveWithTags?
    
    @Transaction
    @Query("SELECT * FROM move_tags WHERE id = :tagId")
    suspend fun getTagWithMoves(tagId: String): TagWithMoves?

    @Update
    suspend fun updateMove(move: Move)

    @Query("UPDATE moves SET name = :name, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateMoveName(id: String, name: String, modifiedAt: Long)

    @Update
    suspend fun updateTag(moveListTag: MoveTag)

    @Query("UPDATE move_tags SET name = :name, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateTagName(id: String, name: String, modifiedAt: Long)

    @Query("DELETE FROM move_tag_cross_refs WHERE moveId = :moveId")
    suspend fun unlinkMoveFromAllTags(moveId: String)

    @Delete
    suspend fun deleteMove(move: Move)

    @Transaction
    suspend fun deleteMoveCompletely(move: Move) {
        unlinkMoveFromAllTags(move.id)
        deleteMove(move)
    }

    @Query("DELETE FROM move_tag_cross_refs WHERE tagId = :tagId")
    suspend fun unlinkTagFromAllMoves(tagId: String)

    @Delete
    suspend fun deleteTag(moveListTag: MoveTag)

    @Transaction
    suspend fun deleteTagCompletely(moveListTag: MoveTag) {
        unlinkTagFromAllMoves(moveListTag.id)
        deleteTag(moveListTag)
    }
}
