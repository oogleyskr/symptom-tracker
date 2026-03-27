package com.symptomtracker.data.repository

import com.symptomtracker.data.db.dao.MedicationDao
import com.symptomtracker.data.db.dao.MedicationReminderDao
import com.symptomtracker.data.db.entity.Medication
import com.symptomtracker.data.db.entity.MedicationReminder
import com.symptomtracker.data.notification.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val reminderDao: MedicationReminderDao,
    private val alarmScheduler: AlarmScheduler,
) {
    val activeMedications: Flow<List<Medication>> = medicationDao.getActiveFlow()
    val allMedications: Flow<List<Medication>> = medicationDao.getAllFlow()

    suspend fun addMedication(medication: Medication): Long =
        medicationDao.insert(medication)

    suspend fun updateMedication(medication: Medication) =
        medicationDao.update(medication)

    suspend fun deactivateMedication(id: Long) =
        medicationDao.setActive(id, false)

    suspend fun getMedicationById(id: Long): Medication? =
        medicationDao.getById(id)

    fun getRemindersForMedication(medicationId: Long): Flow<List<MedicationReminder>> =
        reminderDao.getByMedication(medicationId)

    suspend fun addReminder(reminder: MedicationReminder): Long {
        val id = reminderDao.insert(reminder)
        val saved = reminder.copy(id = id)
        alarmScheduler.schedule(saved)
        return id
    }

    suspend fun deleteReminder(reminder: MedicationReminder) {
        alarmScheduler.cancel(reminder)
        reminderDao.delete(reminder)
    }

    suspend fun getAllReminders(): List<MedicationReminder> =
        reminderDao.getAll()
}
