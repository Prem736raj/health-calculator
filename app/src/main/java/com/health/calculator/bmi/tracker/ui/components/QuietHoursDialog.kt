package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.health.calculator.bmi.tracker.data.models.QuietHours

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuietHoursDialog(
    quietHours: QuietHours,
    onToggle: (Boolean) -> Unit,
    onSetStart: (Int, Int) -> Unit,
    onSetEnd: (Int, Int) -> Unit,
    onToggleEmergency: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DoNotDisturb, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Quiet Hours",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Suppress non-essential notifications during this period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Enable Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Enable Quiet Hours", modifier = Modifier.weight(1f))
                    Switch(checked = quietHours.isEnabled, onCheckedChange = onToggle)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Start Time
                ListItem(
                    headlineContent = { Text("Start Time") },
                    trailingContent = {
                        Text(
                            quietHours.startTimeFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (quietHours.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = { Icon(Icons.Default.AccessTime, null) },
                    modifier = Modifier.clickable(enabled = quietHours.isEnabled) { showStartTimePicker = true }
                )

                // End Time
                ListItem(
                    headlineContent = { Text("End Time") },
                    trailingContent = {
                        Text(
                            quietHours.endTimeFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (quietHours.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = { Icon(Icons.Default.AccessTime, null) },
                    modifier = Modifier.clickable(enabled = quietHours.isEnabled) { showEndTimePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Emergency Override
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Emergency Alerts")
                        Text(
                            "High priority reminders will still alert you",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Checkbox(
                        checked = quietHours.allowEmergencyOverride,
                        onCheckedChange = onToggleEmergency,
                        enabled = quietHours.isEnabled
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }

    if (showStartTimePicker) {
        val state = rememberTimePickerState(initialHour = quietHours.startHour, initialMinute = quietHours.startMinute)
        TimePickerDialog(
            state = state,
            onConfirm = { onSetStart(state.hour, state.minute) },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        val state = rememberTimePickerState(initialHour = quietHours.endHour, initialMinute = quietHours.endMinute)
        TimePickerDialog(
            state = state,
            onConfirm = { onSetEnd(state.hour, state.minute) },
            onDismiss = { showEndTimePicker = false }
        )
    }
}
