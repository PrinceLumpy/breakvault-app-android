package com.princelumpy.breakvault.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "goal_stages",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index(value = ["goalId"])]
)
data class GoalStage(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val name: String,
    val currentCount: Int = 0,
    val targetCount: Int,
    val unit: String = "reps",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)