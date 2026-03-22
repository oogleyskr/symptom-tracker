package com.symptomtracker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "side_effects",
    foreignKeys = [
        ForeignKey(
            entity = LogEntry::class,
            parentColumns = ["id"],
            childColumns = ["logEntryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class SideEffect(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val logEntryId: Long,
    val description: String,
    val severity: Int = 1, // 1-10
    val timestamp: Long = System.currentTimeMillis(),
)
