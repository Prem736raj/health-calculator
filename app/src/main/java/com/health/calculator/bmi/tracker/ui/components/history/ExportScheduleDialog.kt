package com.health.calculator.bmi.tracker.ui.components.history

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

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
        title = { Text(stringResource(R.string.txt_automatic_health_export)) },
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
                        Text(stringResource(R.string.txt_enable_scheduling), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(R.string.txt_automatically_export_data_and_),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }

                if (enabled) {
                    Divider()

                    // Frequency Dropdown (Simplified as Radio for this exercise)
                    Text(stringResource(R.string.txt_frequency), style = MaterialTheme.typography.labelLarge)
                    FrequencySelection(
                        selected = frequency,
                        onSelected = { frequency = it }
                    )

                    Divider()

                    // Format
                    Text(stringResource(R.string.txt_preferred_format), style = MaterialTheme.typography.labelLarge)
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
                Text(stringResource(R.string.txt_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.txt_cancel))
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
