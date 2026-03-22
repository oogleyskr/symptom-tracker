package com.symptomtracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.symptomtracker.data.db.dao.*
import com.symptomtracker.data.db.entity.*

class Converters {
    @TypeConverter
    fun fromLogType(value: LogType): String = value.name

    @TypeConverter
    fun toLogType(value: String): LogType = LogType.valueOf(value)
}

@Database(
    entities = [
        Symptom::class,
        Medication::class,
        LogEntry::class,
        SideEffect::class,
        MedicationReminder::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun symptomDao(): SymptomDao
    abstract fun medicationDao(): MedicationDao
    abstract fun logEntryDao(): LogEntryDao
    abstract fun sideEffectDao(): SideEffectDao
    abstract fun medicationReminderDao(): MedicationReminderDao
}
