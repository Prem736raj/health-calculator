package com.health.calculator.bmi.tracker.ui.screens.reminders

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.models.ReminderCategory
import com.health.calculator.bmi.tracker.notifications.NotificationPermissionHelper
import com.health.calculator.bmi.tracker.ui.components.CategoryPickerDialog
import com.health.calculator.bmi.tracker.ui.components.ReminderEditDialog
import com.health.calculator.bmi.tracker.ui.components.QuietHoursDialog
import com.health.calculator.bmi.tracker.ui.components.PermissionBanner
import com.health.calculator.bmi.tracker.ui.components.QuietHoursIndicator
import com.health.calculator.bmi.tracker.ui.components.EmptyRemindersState
import com.health.calculator.bmi.tracker.ui.components.NotificationChannelSettingsCard
import com.health.calculator.bmi.tracker.ui.components.RateLimitInfoCard
import com.health.calculator.bmi.tracker.ui.components.ReminderCard
import com.health.calculator.bmi.tracker.ui.components.TimePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setPermissionStatus(granted)
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        val granted = NotificationPermissionHelper.isNotificationPermissionGranted(context)
        viewModel.setPermissionStatus(granted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reminders", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.activeCount} active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showQuietHoursSettings() }) {
                        Icon(
                            Icons.Outlined.DoNotDisturb,
                            "Quiet Hours",
                            tint = if (uiState.quietHours.isEnabled)
                                MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!uiState.hasNotificationPermission && NotificationPermissionHelper.needsPermissionRequest()) {
                        viewModel.showPermissionRationale()
                    } else {
                        viewModel.showAddDialog()
                    }
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Reminder") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Permission banner
            if (!uiState.hasNotificationPermission && NotificationPermissionHelper.needsPermissionRequest()) {
                item(key = "permission") {
                    PermissionBanner(
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
            }

            // Quiet hours indicator
            if (uiState.quietHours.isEnabled) {
                item(key = "quiet") {
                    QuietHoursIndicator(
                        quietHours = uiState.quietHours,
                        onClick = { viewModel.showQuietHoursSettings() }
                    )
                }
            }

            if (uiState.isLoading) {
                item { CircularProgressIndicator(modifier = Modifier.padding(32.dp)) }
            } else if (uiState.reminders.isEmpty()) {
                item(key = "empty") { EmptyRemindersState() }
            } else {
                // Group by category
                val grouped = uiState.reminders.groupBy {
                    ReminderCategory.fromName(it.category)
                }

                grouped.forEach { (category, reminders) ->
                    item(key = "header_${category.name}") {
                        Text(
                            text = "${category.icon} ${category.displayName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(
                        items = reminders,
                        key = { it.id }
                    ) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.toggleReminder(reminder)
                            },
                            onEdit = { viewModel.showEditDialog(reminder) },
                            onDelete = { viewModel.confirmDelete(reminder) }
                        )
                    }
                }
            }

            if (!uiState.isLoading && uiState.reminders.isNotEmpty()) {
                item(key = "stats") {
                    RateLimitInfoCard(
                        sentCount = uiState.notificationSentCount,
                        remainingCount = uiState.notificationRemainingCount,
                        tapRate = uiState.notificationTapRate
                    )
                }

                item(key = "channels") {
                    NotificationChannelSettingsCard()
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // Category Picker Dialog
    if (uiState.showCategoryPicker) {
        CategoryPickerDialog(
            onSelect = { viewModel.selectCategoryAndContinue(it) },
            onDismiss = { viewModel.dismissCategoryPicker() }
        )
    }

    // Add/Edit Dialog
    if (uiState.showAddEditDialog && !uiState.showCategoryPicker) {
        ReminderEditDialog(
            isNew = uiState.isCreatingNew,
            category = uiState.selectedCategory,
            title = uiState.editTitle,
            message = uiState.editMessage,
            times = uiState.editTimes,
            days = uiState.editDays,
            soundName = uiState.editSoundName,
            vibration = uiState.editVibration,
            highPriority = uiState.editHighPriority,
            onTitleChange = viewModel::updateEditTitle,
            onMessageChange = viewModel::updateEditMessage,
            onDaysChange = viewModel::updateEditDays,
            onVibrationChange = viewModel::updateEditVibration,
            onHighPriorityChange = viewModel::updateEditHighPriority,
            onAddTime = { viewModel.showTimePickerForNew() },
            onEditTime = { viewModel.showTimePickerForIndex(it) },
            onRemoveTime = viewModel::removeTime,
            onSave = viewModel::saveReminder,
            onDismiss = viewModel::dismissAddEditDialog
        )
    }

    // Time Picker
    if (uiState.showTimePicker) {
        val currentTime = if (uiState.timePickerIndex >= 0 && uiState.timePickerIndex < uiState.editTimes.size) {
            uiState.editTimes[uiState.timePickerIndex]
        } else "09:00"

        val parts = currentTime.split(":")
        val timePickerState = rememberTimePickerState(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 9,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        )

        TimePickerDialog(
            state = timePickerState,
            onConfirm = { viewModel.setTime(timePickerState.hour, timePickerState.minute) },
            onDismiss = viewModel::dismissTimePicker
        )
    }

    // Delete Confirm
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text("Delete Reminder?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Delete \"${uiState.reminderToDelete?.title}\"? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::deleteReminder,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) { Text("Cancel") }
            }
        )
    }

    // Quiet Hours Settings
    if (uiState.showQuietHoursSettings) {
        QuietHoursDialog(
            quietHours = uiState.quietHours,
            onToggle = viewModel::toggleQuietHours,
            onSetStart = viewModel::setQuietStart,
            onSetEnd = viewModel::setQuietEnd,
            onToggleEmergency = viewModel::toggleEmergencyOverride,
            onDismiss = viewModel::dismissQuietHoursSettings
        )
    }

    // Permission Rationale
    if (uiState.showPermissionRationale) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPermissionRationale,
            icon = { Icon(Icons.Outlined.Notifications, null, modifier = Modifier.size(32.dp)) },
            title = { Text("Notifications Needed", fontWeight = FontWeight.Bold) },
            text = {
                Text("Health reminders need notification permission to alert you at the right times. You can change this later in system settings.")
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.dismissPermissionRationale()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text("Allow Notifications") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPermissionRationale) { Text("Not Now") }
            }
        )
    }
}
