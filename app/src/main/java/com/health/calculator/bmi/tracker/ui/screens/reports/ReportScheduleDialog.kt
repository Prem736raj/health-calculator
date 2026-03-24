// ui/screens/reports/ReportScheduleDialog.kt
package com.health.calculator.bmi.tracker.ui.screens.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.ui.components.TimePickerDialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScheduleDialog(
    enabled: Boolean,
    day: Int,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onDayChange: (Int) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val dayNames = mapOf(
        Calendar.MONDAY to "Monday", Calendar.TUESDAY to "Tuesday",
        Calendar.WEDNESDAY to "Wednesday", Calendar.THURSDAY to "Thursday",
        Calendar.FRIDAY to "Friday", Calendar.SATURDAY to "Saturday",
        Calendar.SUNDAY to "Sunday"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(28.dp)) },
        title = { Text("Weekly Report Schedule", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Weekly Reports", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = enabled, onCheckedChange = onToggle)
                }

                if (enabled) {
                    // Day picker
                    Text("Day", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val days = listOf(
                            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
                        )
                        days.forEach { d ->
                            FilterChip(
                                selected = d == day,
                                onClick = { onDayChange(d) },
                                label = { Text(dayNames[d]?.take(3) ?: "", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }

                    // Time picker
                    Text("Time", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            val amPm = if (hour < 12) "AM" else "PM"
                            val displayHour = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
                            Text(
                                String.format("%d:%02d %s", displayHour, minute, amPm),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        "You'll receive a notification with your weekly health summary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } }
    )

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute)
        TimePickerDialog(
            state = timePickerState,
            onConfirm = {
                onTimeChange(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
