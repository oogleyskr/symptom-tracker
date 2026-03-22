package com.symptomtracker.data.db.dao

import androidx.room.*
import com.symptomtracker.data.db.entity.MedicationReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationReminderDao {
    @Query("SELECT * FROM medication_reminders WHERE enabled = 1")
    fun getEnabledFlow(): Flow<List<MedicationReminder>>

    @Query("SELECT * FROM medication_reminders WHERE medicationId = :medicationId")
    fun getByMedication(medicationId: Long): Flow<List<MedicationReminder>>

    @Query("SELECT * FROM medication_reminders")
    suspend fun getAll(): List<MedicationReminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: MedicationReminder): Long

    @Delete
    suspend fun delete(reminder: MedicationReminder)

    @Query("DELETE FROM medication_reminders WHERE medicationId = :medicationId")
    suspend fun deleteByMedication(medicationId: Long)
}
