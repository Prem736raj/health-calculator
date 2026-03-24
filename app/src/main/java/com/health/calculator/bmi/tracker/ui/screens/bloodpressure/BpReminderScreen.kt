// ui/screens/bloodpressure/BpReminderScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BpReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: BpReminderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BP Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recommendation banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(
                        "Doctors recommend measuring BP twice daily – morning and evening – for accurate tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Morning Reminder
            ReminderCard(
                title = "🌅 Morning Reminder",
                description = "Best taken within 1 hour of waking",
                isEnabled = uiState.morningEnabled,
                hour = uiState.morningHour,
                minute = uiState.morningMinute,
                onToggle = { viewModel.onMorningToggle(it) },
                onTimeClicked = { viewModel.onShowMorningTimePicker(true) }
            )

            // Evening Reminder
            ReminderCard(
                title = "🌆 Evening Reminder",
                description = "Best taken before dinner or bedtime",
                isEnabled = uiState.eveningEnabled,
                hour = uiState.eveningHour,
                minute = uiState.eveningMinute,
                onToggle = { viewModel.onEveningToggle(it) },
                onTimeClicked = { viewModel.onShowEveningTimePicker(true) }
            )

            // Custom message
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Reminder Message", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = uiState.reminderMessage,
                        onValueChange = { viewModel.onReminderMessageChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Custom message") },
                        placeholder = { Text("Time to check your blood pressure! 🩺") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Doctor appointment reminder
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocalHospital, contentDescription = null, tint = Color(0xFF1E88E5), modifier = Modifier.size(22.dp))
                            Text("Doctor Visit Reminder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text(
                        "Set a reminder for your next doctor appointment to follow up on blood pressure.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    OutlinedButton(
                        onClick = { viewModel.onShowDoctorDatePicker(true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (uiState.doctorReminderSet) "Reminder: ${uiState.doctorReminderDateFormatted}"
                            else "Set Appointment Reminder",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (uiState.doctorReminderSet) {
                        OutlinedTextField(
                            value = uiState.doctorNote,
                            onValueChange = { viewModel.onDoctorNoteChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Notes for visit") },
                            placeholder = { Text("e.g., Ask about medication dosage...") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        TextButton(
                            onClick = { viewModel.onCancelDoctorReminder() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel Reminder")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Time pickers
    if (uiState.showMorningTimePicker) {
        ReminderTimePickerDialog(
            title = "Morning Reminder Time",
            initialHour = uiState.morningHour,
            initialMinute = uiState.morningMinute,
            onDismiss = { viewModel.onShowMorningTimePicker(false) },
            onTimeSelected = { h, m -> viewModel.onMorningTimeSet(h, m) }
        )
    }

    if (uiState.showEveningTimePicker) {
        ReminderTimePickerDialog(
            title = "Evening Reminder Time",
            initialHour = uiState.eveningHour,
            initialMinute = uiState.eveningMinute,
            onDismiss = { viewModel.onShowEveningTimePicker(false) },
            onTimeSelected = { h, m -> viewModel.onEveningTimeSet(h, m) }
        )
    }

    if (uiState.showDoctorDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onShowDoctorDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDoctorDateSet(it)
                    }
                }) { Text("Set Reminder") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDoctorDatePicker(false) }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeClicked: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val timeFormatted = String.format("%02d:%02d %s",
        if (hour % 12 == 0) 12 else hour % 12,
        minute,
        if (hour < 12) "AM" else "PM"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle(it)
                    }
                )
            }

            AnimatedVisibility(
                visible = isEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedCard(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTimeClicked()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(timeFormatted, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Outlined.Edit, contentDescription = "Change time", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onTimeSelected(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { TimePicker(state = state, modifier = Modifier.fillMaxWidth()) }
    )
}
