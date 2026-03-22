package com.symptomtracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptoms")
data class Symptom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val severity: Int, // 1-10
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val bodyPart: String = "",
)
