package com.symptomtracker.ui.report

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.symptomtracker.data.db.entity.LogEntry
import com.symptomtracker.data.db.entity.LogType
import com.symptomtracker.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ReportUiState(
    val symptomCount: Int = 0,
    val medCount: Int = 0,
    val avgSeverity: String = "N/A",
    val recentSymptoms: List<LogEntry> = emptyList(),
    val isGenerating: Boolean = false,
    val isPro: Boolean = false,
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val logRepository: LogRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        viewModelScope.launch {
            logRepository.getLogsByDateRange(thirtyDaysAgo, System.currentTimeMillis()).collect { logs ->
                val symptoms = logs.filter { it.type == LogType.SYMPTOM }
                val meds = logs.filter { it.type == LogType.MEDICATION }
                val avgSev = symptoms.mapNotNull { it.value.toFloatOrNull() }.average()
                _uiState.update {
                    it.copy(
                        symptomCount = symptoms.size,
                        medCount = meds.size,
                        avgSeverity = if (avgSev.isNaN()) "N/A" else String.format("%.1f", avgSev),
                        recentSymptoms = symptoms,
                    )
                }
            }
        }
    }

    fun generateReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            try {
                val logs = logRepository.getRecentLogs(200)
                val reportText = buildReportText(logs)
                val file = writeReportFile(reportText)
                shareFile(file)
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    private fun buildReportText(logs: List<LogEntry>): String {
        val fmt = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine("SYMPTOM TRACKER AI — HEALTH REPORT")
        sb.appendLine("Generated: ${fmt.format(Date())}")
        sb.appendLine("=".repeat(40))
        sb.appendLine()

        val symptoms = logs.filter { it.type == LogType.SYMPTOM }
        val meds = logs.filter { it.type == LogType.MEDICATION }

        sb.appendLine("SYMPTOMS (${symptoms.size} entries)")
        sb.appendLine("-".repeat(30))
        symptoms.forEach { s ->
            sb.appendLine("• ${s.refName} — Severity: ${s.value}/10 — ${fmt.format(Date(s.timestamp))}")
            if (s.notes.isNotBlank()) sb.appendLine("  Notes: ${s.notes}")
        }

        sb.appendLine()
        sb.appendLine("MEDICATIONS (${meds.size} entries)")
        sb.appendLine("-".repeat(30))
        meds.forEach { m ->
            sb.appendLine("• ${m.refName} — ${m.value} — ${fmt.format(Date(m.timestamp))}")
        }

        val avgSev = symptoms.mapNotNull { it.value.toFloatOrNull() }.average()
        sb.appendLine()
        sb.appendLine("SUMMARY")
        sb.appendLine("-".repeat(30))
        sb.appendLine("Total symptom entries: ${symptoms.size}")
        sb.appendLine("Total medication entries: ${meds.size}")
        sb.appendLine("Average symptom severity: ${if (avgSev.isNaN()) "N/A" else String.format("%.1f", avgSev)}/10")

        return sb.toString()
    }

    private fun writeReportFile(content: String): File {
        val dir = File(context.filesDir, "reports").also { it.mkdirs() }
        val file = File(dir, "health_report_${System.currentTimeMillis()}.txt")
        file.writeText(content)
        return file
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Health Report").also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
