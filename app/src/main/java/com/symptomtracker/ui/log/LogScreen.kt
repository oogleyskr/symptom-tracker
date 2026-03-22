package com.symptomtracker.ui.log

import androidx.compose.animation.*
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
import com.symptomtracker.data.db.entity.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(viewModel: LogViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Success snackbar
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "Quick Log",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "What's going on?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Success banner
        AnimatedVisibility(visible = uiState.successMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(uiState.successMessage ?: "",
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                text = { Text("Symptom") },
                icon = { Icon(Icons.Default.Sick, contentDescription = null) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                text = { Text("Medication") },
                icon = { Icon(Icons.Default.Medication, contentDescription = null) })
        }

        when (selectedTab) {
            0 -> SymptomLogTab(onLogSymptom = viewModel::logSymptom)
            1 -> MedicationLogTab(
                medications = uiState.activeMedications,
                onLogMedication = viewModel::logMedication,
            )
        }
    }
}

@Composable
private fun SymptomLogTab(onLogSymptom: (String, Int, String) -> Unit) {
    var symptomName by remember { mutableStateOf("") }
    var severity by remember { mutableFloatStateOf(5f) }
    var notes by remember { mutableStateOf("") }

    // Common symptoms for quick tap
    val commonSymptoms = listOf("Headache", "Nausea", "Fatigue", "Pain", "Dizziness",
        "Anxiety", "Fever", "Cough", "Shortness of breath", "Chest pain")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Quick select", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            // Quick tap grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                commonSymptoms.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { symptom ->
                            FilterChip(
                                selected = symptomName == symptom,
                                onClick = { symptomName = symptom },
                                label = { Text(symptom) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = symptomName,
                onValueChange = { symptomName = it },
                label = { Text("Or type a symptom") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            )
        }
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Severity", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "${severity.toInt()}/10",
                        style = MaterialTheme.typography.labelLarge,
                        color = severityColor(severity.toInt()),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Slider(
                    value = severity,
                    onValueChange = { severity = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = severityColor(severity.toInt()),
                        activeTrackColor = severityColor(severity.toInt()),
                    )
                )
            }
        }
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
        }
        item {
            Button(
                onClick = {
                    onLogSymptom(symptomName, severity.toInt(), notes)
                    symptomName = ""
                    severity = 5f
                    notes = ""
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = symptomName.isNotBlank(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log Symptom", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun MedicationLogTab(
    medications: List<Medication>,
    onLogMedication: (Medication, String) -> Unit,
) {
    if (medications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Medication, contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text("No medications added yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Add medications in Timeline > Medications",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Tap to log as taken",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(medications, key = { it.id }) { med ->
            MedicationCard(medication = med, onLog = { onLogMedication(med, "") })
        }
    }
}

@Composable
private fun MedicationCard(medication: Medication, onLog: () -> Unit) {
    ElevatedCard(
        onClick = onLog,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Medication, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(medication.name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("${medication.dose} · ${medication.frequency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.AddCircle, contentDescription = "Log",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun severityColor(severity: Int) = when {
    severity <= 3 -> MaterialTheme.colorScheme.secondary
    severity <= 6 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.error
}
