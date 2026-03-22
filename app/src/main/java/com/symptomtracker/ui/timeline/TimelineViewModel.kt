package com.symptomtracker.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.symptomtracker.data.db.entity.LogEntry
import com.symptomtracker.data.db.entity.LogType
import com.symptomtracker.data.db.entity.Medication
import com.symptomtracker.data.repository.LogRepository
import com.symptomtracker.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ChartPoint(val dayLabel: String, val value: Float)

data class TimelineUiState(
    val logs: List<LogEntry> = emptyList(),
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = false,
    val symptomChartData: List<ChartPoint> = emptyList(),
    val adherenceChartData: List<ChartPoint> = emptyList(),
    val selectedTab: Int = 0,
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            logRepository.allLogs.collect { logs ->
                _uiState.update { it.copy(logs = logs, symptomChartData = buildSymptomChart(logs), adherenceChartData = buildAdherenceChart(logs)) }
            }
        }
        viewModelScope.launch {
            medicationRepository.allMedications.collect { meds ->
                _uiState.update { it.copy(medications = meds) }
            }
        }
    }

    fun setTab(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }

    fun search(query: String) {
        viewModelScope.launch {
            val logs = if (query.isBlank()) logRepository.allLogs.first()
                       else logRepository.searchLogs(query).first()
            _uiState.update { it.copy(logs = logs) }
        }
    }

    fun deleteEntry(entry: LogEntry) {
        viewModelScope.launch { logRepository.deleteLog(entry) }
    }

    fun addMedication(name: String, dose: String, frequency: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            medicationRepository.addMedication(
                Medication(name = name, dose = dose, frequency = frequency)
            )
        }
    }

    fun deactivateMedication(id: Long) {
        viewModelScope.launch { medicationRepository.deactivateMedication(id) }
    }

    private fun buildSymptomChart(logs: List<LogEntry>): List<ChartPoint> {
        val cal = Calendar.getInstance()
        val days = (6 downTo 0).map { daysBack ->
            cal.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, -daysBack)
            }
            val label = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val dayStart = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
            val dayEnd = dayStart + 86_400_000L
            val avgSeverity = logs
                .filter { it.type == LogType.SYMPTOM && it.timestamp in dayStart..dayEnd }
                .mapNotNull { it.value.toFloatOrNull() }
                .average().toFloat().takeIf { !it.isNaN() } ?: 0f
            ChartPoint(label, avgSeverity)
        }
        return days
    }

    private fun buildAdherenceChart(logs: List<LogEntry>): List<ChartPoint> {
        val cal = Calendar.getInstance()
        return (6 downTo 0).map { daysBack ->
            cal.apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, -daysBack)
            }
            val label = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")[cal.get(Calendar.DAY_OF_WEEK) - 1]
            val dayStart = cal.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
            val dayEnd = dayStart + 86_400_000L
            val count = logs.count { it.type == LogType.MEDICATION && it.timestamp in dayStart..dayEnd }.toFloat()
            ChartPoint(label, count)
        }
    }
}
