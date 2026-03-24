package com.health.calculator.bmi.tracker.ui.components.history

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.export.ExportFormat
import com.health.calculator.bmi.tracker.data.export.ExportFrequency
import com.health.calculator.bmi.tracker.data.export.ExportSchedule

@Composable
fun ExportScheduleDialog(
    currentSchedule: ExportSchedule,
    onDismiss: () -> Unit,
    onSave: (ExportSchedule) -> Unit
) {
    var enabled by remember { mutableStateOf(currentSchedule.enabled) }
    var frequency by remember { mutableStateOf(currentSchedule.frequency) }
    var format by remember { mutableStateOf(currentSchedule.format) }
    var email by remember { mutableStateOf(currentSchedule.emailAddress ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Automatic Health Export") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Enabled Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Scheduling", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Automatically export data and save locally",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }

                if (enabled) {
                    Divider()

                    // Frequency Dropdown (Simplified as Radio for this exercise)
                    Text("Frequency", style = MaterialTheme.typography.labelLarge)
                    FrequencySelection(
                        selected = frequency,
                        onSelected = { frequency = it }
                    )

                    Divider()

                    // Format
                    Text("Preferred Format", style = MaterialTheme.typography.labelLarge)
                    FormatSelection(
                        selected = format,
                        onSelected = { format = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(ExportSchedule(enabled, frequency, format, email))
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrequencySelection(
    selected: ExportFrequency,
    onSelected: (ExportFrequency) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExportFrequency.values().forEach { freq ->
            FilterChip(
                selected = selected == freq,
                onClick = { onSelected(freq) },
                label = { Text(freq.label) }
            )
        }
    }
}

@Composable
private fun FormatSelection(
    selected: ExportFormat,
    onSelected: (ExportFormat) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExportFormat.values().forEach { fmt ->
            FilterChip(
                selected = selected == fmt,
                onClick = { onSelected(fmt) },
                label = { Text(fmt.name) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
