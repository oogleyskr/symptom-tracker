package com.symptomtracker.data.repository

import com.symptomtracker.data.db.dao.LogEntryDao
import com.symptomtracker.data.db.dao.SymptomDao
import com.symptomtracker.data.db.dao.MedicationDao
import com.symptomtracker.data.db.dao.SideEffectDao
import com.symptomtracker.data.db.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val logEntryDao: LogEntryDao,
    private val symptomDao: SymptomDao,
    private val medicationDao: MedicationDao,
    private val sideEffectDao: SideEffectDao,
) {
    val allLogs: Flow<List<LogEntry>> = logEntryDao.getAllFlow()

    fun getLogsByDateRange(from: Long, to: Long): Flow<List<LogEntry>> =
        logEntryDao.getByDateRange(from, to)

    fun searchLogs(query: String): Flow<List<LogEntry>> =
        logEntryDao.search(query)

    suspend fun logSymptom(
        symptomName: String,
        severity: Int,
        notes: String = "",
        bodyPart: String = "",
    ): Long {
        val symptom = Symptom(
            name = symptomName,
            severity = severity,
            notes = notes,
            bodyPart = bodyPart,
        )
        val symptomId = symptomDao.insert(symptom)
        val entry = LogEntry(
            type = LogType.SYMPTOM,
            refId = symptomId,
            refName = symptomName,
            value = severity.toString(),
            notes = notes,
        )
        return logEntryDao.insert(entry)
    }

    suspend fun logMedicationTaken(
        medication: Medication,
        doseNote: String = "",
    ): Long {
        val entry = LogEntry(
            type = LogType.MEDICATION,
            refId = medication.id,
            refName = medication.name,
            value = medication.dose,
            notes = doseNote,
        )
        return logEntryDao.insert(entry)
    }

    suspend fun logSideEffect(
        logEntryId: Long,
        description: String,
        severity: Int = 1,
    ): Long {
        val sideEffect = SideEffect(
            logEntryId = logEntryId,
            description = description,
            severity = severity,
        )
        return sideEffectDao.insert(sideEffect)
    }

    suspend fun deleteLog(entry: LogEntry) = logEntryDao.delete(entry)

    suspend fun getRecentLogs(limit: Int = 50) = logEntryDao.getRecent(limit)
}
