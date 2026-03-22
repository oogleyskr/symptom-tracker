package com.symptomtracker.data.repository

import com.symptomtracker.data.db.dao.MedicationDao
import com.symptomtracker.data.db.dao.MedicationReminderDao
import com.symptomtracker.data.db.entity.Medication
import com.symptomtracker.data.db.entity.MedicationReminder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val reminderDao: MedicationReminderDao,
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

    suspend fun addReminder(reminder: MedicationReminder): Long =
        reminderDao.insert(reminder)

    suspend fun deleteReminder(reminder: MedicationReminder) =
        reminderDao.delete(reminder)

    suspend fun getAllReminders(): List<MedicationReminder> =
        reminderDao.getAll()
}
