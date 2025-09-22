package com.princelumpy.breakvault.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MoveTagDao {

    @Insert
    suspend fun addMove(move: Move)

    @Insert
    suspend fun addTag(tag: Tag)

    @Insert
    suspend fun link(moveTag: MoveTagCrossRef)

    // --- Methods for Export --- 
    @Query("SELECT * FROM moves")
    suspend fun getAllMovesList(): List<Move>

    @Query("SELECT * FROM tags")
    suspend fun getAllTagsList(): List<Tag>

    @Query("SELECT * FROM move_tag_cross_refs")
    suspend fun getAllMoveTagCrossRefsList(): List<MoveTagCrossRef>

    // --- Methods for Import --- 
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoves(moves: List<Move>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTags(tags: List<Tag>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoveTagCrossRefs(crossRefs: List<MoveTagCrossRef>)

    // --- Existing Methods --- 
    @Transaction
    @Query("SELECT * FROM moves")
    fun getMovesWithTags(): LiveData<List<MoveWithTags>>

    @Transaction
    @Query("SELECT * FROM tags")
    fun getTagsWithMoves(): LiveData<List<TagWithMoves>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): LiveData<List<Tag>>

    @Transaction
    @Query("SELECT * FROM moves WHERE id = :moveId")
    suspend fun getMoveWithTagsById(moveId: String): MoveWithTags?

    @Update
    suspend fun updateMove(move: Move)

    @Update
    suspend fun updateTag(tag: Tag)

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
    suspend fun deleteTag(tag: Tag)

    @Transaction
    suspend fun deleteTagCompletely(tag: Tag) {
        unlinkTagFromAllMoves(tag.id)
        deleteTag(tag)
    }
}
