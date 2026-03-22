package com.symptomtracker.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.symptomtracker.ai.GeminiInsightsEngine
import com.symptomtracker.ai.HealthInsight
import com.symptomtracker.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val insights: List<HealthInsight> = emptyList(),
    val isLoading: Boolean = false,
    val isPro: Boolean = false, // wired to billing later
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val insightsEngine: GeminiInsightsEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState(isLoading = true))
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            logRepository.allLogs.collect { logs ->
                val insights = insightsEngine.analyze(logs, _uiState.value.isPro)
                _uiState.update { it.copy(insights = insights, isLoading = false) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val logs = logRepository.getRecentLogs(200)
            val insights = insightsEngine.analyze(logs, _uiState.value.isPro)
            _uiState.update { it.copy(insights = insights, isLoading = false) }
        }
    }
}
