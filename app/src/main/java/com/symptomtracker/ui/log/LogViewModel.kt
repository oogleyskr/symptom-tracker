package com.symptomtracker.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.symptomtracker.data.db.entity.Medication
import com.symptomtracker.data.repository.LogRepository
import com.symptomtracker.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogUiState(
    val activeMedications: List<Medication> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val medicationRepository: MedicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            medicationRepository.activeMedications.collect { meds ->
                _uiState.update { it.copy(activeMedications = meds) }
            }
        }
    }

    fun logSymptom(name: String, severity: Int, notes: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                logRepository.logSymptom(name, severity, notes)
                _uiState.update { it.copy(isLoading = false, successMessage = "Symptom logged!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun logMedication(medication: Medication, note: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                logRepository.logMedicationTaken(medication, note)
                _uiState.update { it.copy(isLoading = false, successMessage = "${medication.name} logged!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, error = null) }
    }
}
