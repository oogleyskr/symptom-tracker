package com.symptomtracker.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.symptomtracker.data.db.entity.LogEntry
import com.symptomtracker.data.db.entity.LogType
import com.symptomtracker.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
                val file = withContext(Dispatchers.IO) { buildPdf(logs) }
                shareFile(file)
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    private fun buildPdf(logs: List<LogEntry>): File {
        val fmt = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        val symptoms = logs.filter { it.type == LogType.SYMPTOM }
        val meds = logs.filter { it.type == LogType.MEDICATION }
        val avgSev = symptoms.mapNotNull { it.value.toFloatOrNull() }.average()

        val pageWidth = 595  // A4
        val pageHeight = 842
        val margin = 50f
        val contentWidth = pageWidth - (margin * 2)

        val document = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = margin

        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#6B3FA0")
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val headerPaint = TextPaint().apply {
            color = Color.parseColor("#333333")
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#444444")
            textSize = 11f
            isAntiAlias = true
        }
        val subtitlePaint = TextPaint().apply {
            color = Color.parseColor("#666666")
            textSize = 10f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CCCCCC")
            strokeWidth = 1f
        }
        val accentPaint = Paint().apply {
            color = Color.parseColor("#6B3FA0")
            strokeWidth = 2f
        }

        fun newPage(): Canvas {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            yPos = margin
            return page.canvas
        }

        fun ensureSpace(needed: Float): Canvas {
            if (yPos + needed > pageHeight - margin) {
                canvas = newPage()
            }
            return canvas
        }

        fun drawWrappedText(paint: TextPaint, text: String, maxWidth: Float): Float {
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, maxWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.2f)
                .build()
            val totalHeight = layout.height.toFloat()

            canvas = ensureSpace(totalHeight)
            canvas.save()
            canvas.translate(margin, yPos)
            layout.draw(canvas)
            canvas.restore()
            yPos += totalHeight
            return totalHeight
        }

        // Title
        drawWrappedText(titlePaint, "Symptom Tracker AI", contentWidth)
        yPos += 4f
        drawWrappedText(subtitlePaint, "Health Report — Generated ${fmt.format(Date())}", contentWidth)
        yPos += 8f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, accentPaint)
        yPos += 16f

        // Summary box
        drawWrappedText(headerPaint, "Summary (Last 30 Days)", contentWidth)
        yPos += 8f
        val summaryText = buildString {
            appendLine("Symptom entries: ${symptoms.size}")
            appendLine("Medication entries: ${meds.size}")
            append("Average symptom severity: ${if (avgSev.isNaN()) "N/A" else String.format("%.1f", avgSev)}/10")
        }
        drawWrappedText(bodyPaint, summaryText, contentWidth)
        yPos += 16f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 16f

        // Symptoms section
        if (symptoms.isNotEmpty()) {
            canvas = ensureSpace(30f)
            drawWrappedText(headerPaint, "Symptoms (${symptoms.size} entries)", contentWidth)
            yPos += 8f

            symptoms.forEach { s ->
                val line = buildString {
                    append("\u2022 ${s.refName} — Severity: ${s.value}/10 — ${fmt.format(Date(s.timestamp))}")
                    if (s.notes.isNotBlank()) append("\n  Notes: ${s.notes}")
                }
                canvas = ensureSpace(30f)
                drawWrappedText(bodyPaint, line, contentWidth - 10f)
                yPos += 4f
            }
            yPos += 12f
            canvas = ensureSpace(4f)
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
            yPos += 16f
        }

        // Medications section
        if (meds.isNotEmpty()) {
            canvas = ensureSpace(30f)
            drawWrappedText(headerPaint, "Medications (${meds.size} entries)", contentWidth)
            yPos += 8f

            meds.forEach { m ->
                val line = "\u2022 ${m.refName} — ${m.value} — ${fmt.format(Date(m.timestamp))}"
                canvas = ensureSpace(30f)
                drawWrappedText(bodyPaint, line, contentWidth - 10f)
                yPos += 4f
            }
            yPos += 12f
        }

        // Footer
        canvas = ensureSpace(40f)
        yPos += 8f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, accentPaint)
        yPos += 12f
        drawWrappedText(subtitlePaint, "Generated by Symptom Tracker AI — For informational purposes only. Not medical advice.", contentWidth)

        document.finishPage(page)

        val dir = File(context.filesDir, "reports").also { it.mkdirs() }
        val file = File(dir, "health_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return file
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Health Report").also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
