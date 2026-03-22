package com.symptomtracker.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.symptomtracker.data.db.entity.LogEntry
import com.symptomtracker.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimelineUiState(
    val logs: List<LogEntry> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val logRepository: LogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            logRepository.allLogs.collect { logs ->
                _uiState.update { it.copy(logs = logs) }
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            viewModelScope.launch {
                logRepository.allLogs.first().let { logs ->
                    _uiState.update { it.copy(logs = logs) }
                }
            }
        } else {
            viewModelScope.launch {
                logRepository.searchLogs(query).first().let { logs ->
                    _uiState.update { it.copy(logs = logs) }
                }
            }
        }
    }

    fun deleteEntry(entry: LogEntry) {
        viewModelScope.launch {
            logRepository.deleteLog(entry)
        }
    }
}
