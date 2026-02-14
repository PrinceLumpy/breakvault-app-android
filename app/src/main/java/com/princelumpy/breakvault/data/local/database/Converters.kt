package com.princelumpy.breakvault.data.local.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Json.Default.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}