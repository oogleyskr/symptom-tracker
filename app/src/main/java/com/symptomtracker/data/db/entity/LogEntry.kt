package com.symptomtracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LogType { SYMPTOM, MEDICATION, SIDE_EFFECT, MOOD, NOTE }

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: LogType,
    val refId: Long? = null,         // FK to symptom or medication id
    val refName: String = "",        // denormalized name for display speed
    val timestamp: Long = System.currentTimeMillis(),
    val value: String = "",          // severity as string, dose taken, etc.
    val notes: String = "",
)
