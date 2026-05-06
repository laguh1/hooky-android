package com.hooky.app.data.db.converters

import androidx.room.TypeConverter
import com.hooky.app.domain.model.CareInstructions
import com.hooky.app.domain.model.WorkSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

class Converters {

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromWorkSessionList(list: List<WorkSession>): String {
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun toWorkSessionList(value: String): List<WorkSession> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromCareInstructions(careInstructions: CareInstructions): String {
        return try {
            json.encodeToString(careInstructions)
        } catch (e: Exception) {
            "{}"
        }
    }

    @TypeConverter
    fun toCareInstructions(value: String): CareInstructions {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            CareInstructions()
        }
    }
}
