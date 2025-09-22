package com.princelumpy.breakvault.data // Updated package

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey val id: String,
    val name: String
)
