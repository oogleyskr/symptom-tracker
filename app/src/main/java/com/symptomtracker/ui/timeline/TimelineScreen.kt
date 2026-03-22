package com.symptomtracker.ui.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.*
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.symptomtracker.data.db.entity.LogEntry
import com.symptomtracker.data.db.entity.LogType
import com.symptomtracker.data.db.entity.Medication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(viewModel: TimelineViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header + search
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Timeline", style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; viewModel.search(it) },
                    placeholder = { Text("Search logs...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }

        // Tabs
        TabRow(selectedTabIndex = uiState.selectedTab) {
            Tab(selected = uiState.selectedTab == 0, onClick = { viewModel.setTab(0) },
                text = { Text("History") }, icon = { Icon(Icons.Default.History, null) })
            Tab(selected = uiState.selectedTab == 1, onClick = { viewModel.setTab(1) },
                text = { Text("Charts") }, icon = { Icon(Icons.Default.BarChart, null) })
            Tab(selected = uiState.selectedTab == 2, onClick = { viewModel.setTab(2) },
                text = { Text("Meds") }, icon = { Icon(Icons.Default.Medication, null) })
        }

        when (uiState.selectedTab) {
            0 -> HistoryTab(logs = uiState.logs, onDelete = viewModel::deleteEntry)
            1 -> ChartsTab(uiState = uiState)
            2 -> MedicationsTab(
                medications = uiState.medications,
                onAdd = viewModel::addMedication,
                onDeactivate = viewModel::deactivateMedication,
            )
        }
    }
}

// ─── History Tab ─────────────────────────────────────────────────────────────

@Composable
private fun HistoryTab(logs: List<LogEntry>, onDelete: (LogEntry) -> Unit) {
    if (logs.isEmpty()) {
        EmptyState(icon = Icons.Default.Timeline, message = "No logs yet",
            sub = "Start logging in the Log tab")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        items(logs, key = { it.id }) { entry ->
            LogEntryCard(entry = entry, onDelete = { onDelete(entry) })
        }
    }
}

@Composable
private fun LogEntryCard(entry: LogEntry, onDelete: () -> Unit) {
    val fmt = remember { SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()) }
    val (icon, color) = when (entry.type) {
        LogType.SYMPTOM -> Icons.Default.Sick to MaterialTheme.colorScheme.error
        LogType.MEDICATION -> Icons.Default.Medication to MaterialTheme.colorScheme.primary
        LogType.SIDE_EFFECT -> Icons.Default.Warning to MaterialTheme.colorScheme.tertiary
        LogType.MOOD -> Icons.Default.Mood to MaterialTheme.colorScheme.secondary
        LogType.NOTE -> Icons.Default.Note to MaterialTheme.colorScheme.onSurfaceVariant
    }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.refName.ifBlank { entry.type.name.lowercase().replaceFirstChar { it.uppercase() } },
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (entry.type == LogType.SYMPTOM)
                    Text("Severity: ${entry.value}/10", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (entry.notes.isNotBlank())
                    Text(entry.notes, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Text(fmt.format(Date(entry.timestamp)), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── Charts Tab ──────────────────────────────────────────────────────────────

@Composable
private fun ChartsTab(uiState: TimelineUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            ChartCard(title = "Symptom Severity (7 days)", subtitle = "Average severity per day") {
                val data = uiState.symptomChartData
                if (data.all { it.value == 0f }) {
                    NoDataLabel()
                } else {
                    val model = remember(data) {
                        CartesianChartModelProducer().also { producer ->
                            producer.runTransaction {
                                lineSeries { series(data.map { it.value.toDouble() }) }
                            }
                        }
                    }
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(
                                lineProvider = LineCartesianLayer.LineProvider.series(
                                    LineCartesianLayer.rememberLine(
                                        fill = LineCartesianLayer.LineFill.single(fill(MaterialTheme.colorScheme.error))
                                    )
                                )
                            ),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = { _, x, _ ->
                                    data.getOrNull(x.toInt())?.dayLabel ?: ""
                                }
                            ),
                        ),
                        modelProducer = model,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                    )
                }
            }
        }
        item {
            ChartCard(title = "Medication Adherence (7 days)", subtitle = "Doses logged per day") {
                val data = uiState.adherenceChartData
                if (data.all { it.value == 0f }) {
                    NoDataLabel()
                } else {
                    val model = remember(data) {
                        CartesianChartModelProducer().also { producer ->
                            producer.runTransaction {
                                columnSeries { series(data.map { it.value.toDouble() }) }
                            }
                        }
                    }
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberColumnCartesianLayer(),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = { _, x, _ ->
                                    data.getOrNull(x.toInt())?.dayLabel ?: ""
                                }
                            ),
                        ),
                        modelProducer = model,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun NoDataLabel() {
    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Text("Log more data to see chart", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Medications Tab ──────────────────────────────────────────────────────────

@Composable
private fun MedicationsTab(
    medications: List<Medication>,
    onAdd: (String, String, String) -> Unit,
    onDeactivate: (Long) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (medications.isEmpty()) {
            EmptyState(icon = Icons.Default.Medication, message = "No medications yet",
                sub = "Add your first medication below")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            ) {
                items(medications, key = { it.id }) { med ->
                    MedicationManageCard(medication = med, onDeactivate = { onDeactivate(med.id) })
                }
            }
        }
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Default.Add, "Add medication")
        }
    }

    if (showDialog) {
        AddMedicationDialog(
            onConfirm = { name, dose, freq -> onAdd(name, dose, freq); showDialog = false },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun MedicationManageCard(medication: Medication, onDeactivate: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(medication.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text("${medication.dose} · ${medication.frequency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (medication.active) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.extraSmall) {
                    Text("Active", style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDeactivate) {
                Icon(Icons.Default.Archive, "Deactivate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AddMedicationDialog(onConfirm: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Once daily") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Medication name") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words))
                OutlinedTextField(value = dose, onValueChange = { dose = it },
                    label = { Text("Dose (e.g. 500mg)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = frequency, onValueChange = { frequency = it },
                    label = { Text("Frequency (e.g. twice daily)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, dose, frequency) }, enabled = name.isNotBlank()) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String, sub: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(sub, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
