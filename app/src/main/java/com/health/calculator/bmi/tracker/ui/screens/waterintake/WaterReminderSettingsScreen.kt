// ui/screens/waterintake/WaterReminderSettingsScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.ReminderFrequency
import com.health.calculator.bmi.tracker.data.model.WaterReminderSettings
import com.health.calculator.bmi.tracker.data.preferences.WaterReminderPreferences
import com.health.calculator.bmi.tracker.notification.WaterReminderScheduler

private val WaterBlueMedium = Color(0xFF2196F3)
private val WaterBlueDark = Color(0xFF1565C0)
private val WaterBlueSurface = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterReminderSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { WaterReminderPreferences(context) }
    val scheduler = remember { WaterReminderScheduler(context) }

    var settings by remember { mutableStateOf(prefs.load()) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showSavedConfirm by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    // Auto-save and schedule when settings change
    LaunchedEffect(settings) {
        prefs.save(settings)
        if (settings.isEnabled) {
            scheduler.schedule(settings)
        } else {
            scheduler.cancel()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔔", fontSize = 22.sp)
                        Text("Water Reminders", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Header Card
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
                ) {
                    ReminderHeaderCard(settings.isEnabled)
                }

                // Main Toggle
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
                ) {
                    EnableToggleCard(
                        enabled = settings.isEnabled,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            settings = settings.copy(isEnabled = it)
                        }
                    )
                }

                // Schedule Settings
                AnimatedVisibility(
                    visible = isVisible && settings.isEnabled,
                    enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                    exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Time Range
                        ScheduleCard(
                            settings = settings,
                            onStartTimeClick = { showStartTimePicker = true },
                            onEndTimeClick = { showEndTimePicker = true }
                        )

                        // Frequency
                        FrequencyCard(
                            selectedMinutes = settings.frequencyMinutes,
                            onSelect = { minutes ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                settings = settings.copy(frequencyMinutes = minutes)
                            },
                            wakingMinutes = settings.wakingMinutes,
                            goalMl = context.getSharedPreferences("water_intake_prefs", android.content.Context.MODE_PRIVATE)
                                .getInt("daily_goal_ml", 2500)
                        )

                        // Notification Style
                        NotificationStyleCard(
                            enableSound = settings.enableSound,
                            enableVibration = settings.enableVibration,
                            onSoundToggle = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                settings = settings.copy(enableSound = it)
                            },
                            onVibrationToggle = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                settings = settings.copy(enableVibration = it)
                            }
                        )

                        // Smart Features
                        SmartFeaturesCard(
                            smartSkip = settings.smartSkipEnabled,
                            behindNudge = settings.behindScheduleNudge,
                            onSmartSkipToggle = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                settings = settings.copy(smartSkipEnabled = it)
                            },
                            onBehindNudgeToggle = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                settings = settings.copy(behindScheduleNudge = it)
                            }
                        )

                        // Reminder summary
                        ReminderSummaryCard(settings)
                    }
                }
            }

            // Time Pickers
            if (showStartTimePicker) {
                WaterTimePicker(
                    initialHour = settings.startHour,
                    initialMinute = settings.startMinute,
                    title = "Reminder Start Time",
                    onConfirm = { hour, minute ->
                        settings = settings.copy(startHour = hour, startMinute = minute)
                        showStartTimePicker = false
                    },
                    onDismiss = { showStartTimePicker = false }
                )
            }

            if (showEndTimePicker) {
                WaterTimePicker(
                    initialHour = settings.endHour,
                    initialMinute = settings.endMinute,
                    title = "Reminder End Time",
                    onConfirm = { hour, minute ->
                        settings = settings.copy(endHour = hour, endMinute = minute)
                        showEndTimePicker = false
                    },
                    onDismiss = { showEndTimePicker = false }
                )
            }
        }
    }
}

@Composable
private fun ReminderHeaderCard(isEnabled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isEnabled) listOf(WaterBlueMedium, WaterBlueDark)
                        else listOf(Color.Gray, Color.DarkGray)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (isEnabled) "🔔" else "🔕",
                    fontSize = 40.sp
                )
                Column {
                    Text(
                        text = if (isEnabled) "Reminders Active" else "Reminders Off",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEnabled)
                            "We'll help you stay hydrated throughout the day"
                        else
                            "Enable reminders to never forget to drink water",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EnableToggleCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (enabled) WaterBlueSurface else Color.Gray.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💧", fontSize = 22.sp)
                }
                Column {
                    Text(
                        "Water Reminders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Get notified to drink water",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = WaterBlueMedium,
                    checkedThumbColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ScheduleCard(
    settings: WaterReminderSettings,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⏰", fontSize = 18.sp)
                Text("Schedule", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Time
                TimePickerButton(
                    label = "Start Time",
                    time = settings.startTimeFormatted,
                    icon = "🌅",
                    onClick = onStartTimeClick,
                    modifier = Modifier.weight(1f)
                )

                // End Time
                TimePickerButton(
                    label = "End Time",
                    time = settings.endTimeFormatted,
                    icon = "🌙",
                    onClick = onEndTimeClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Active hours display
            val hours = settings.wakingMinutes / 60
            val mins = settings.wakingMinutes % 60
            Text(
                text = "Active window: ${hours}h ${if (mins > 0) "${mins}m" else ""}",
                fontSize = 12.sp,
                color = WaterBlueMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TimePickerButton(
    label: String,
    time: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WaterBlueSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text(time, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaterBlueDark)
        }
    }
}

@Composable
private fun FrequencyCard(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
    wakingMinutes: Int,
    goalMl: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔄", fontSize = 18.sp)
                Text("Frequency", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Suggested frequency
            val suggestedMinutes = if (wakingMinutes > 0 && goalMl > 0) {
                val glasses = goalMl / 250
                if (glasses > 0) (wakingMinutes / glasses) else 60
            } else 60

            Text(
                text = "💡 Suggested: every ~${suggestedMinutes} min based on your goal",
                fontSize = 12.sp,
                color = WaterBlueMedium,
                fontWeight = FontWeight.Medium
            )

            // Frequency options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ReminderFrequency.entries.forEach { freq ->
                    val isSelected = selectedMinutes == freq.minutes
                    val borderColor by animateColorAsState(
                        if (isSelected) WaterBlueMedium else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        label = "freq_border"
                    )
                    val bgColor by animateColorAsState(
                        if (isSelected) WaterBlueSurface else Color.Transparent,
                        label = "freq_bg"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor, RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelect(freq.minutes) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            freq.label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) WaterBlueDark else MaterialTheme.colorScheme.onSurface
                        )

                        val remindersPerDay = if (freq.minutes > 0) wakingMinutes / freq.minutes else 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "${remindersPerDay}x/day",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = scaleIn() + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(WaterBlueMedium, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationStyleCard(
    enableSound: Boolean,
    enableVibration: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔊", fontSize = 18.sp)
                Text("Notification Style", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            SettingsToggleRow(
                icon = "🔉",
                title = "Sound",
                subtitle = "Play notification sound",
                checked = enableSound,
                onToggle = onSoundToggle
            )

            SettingsToggleRow(
                icon = "📳",
                title = "Vibration",
                subtitle = "Vibrate on reminder",
                checked = enableVibration,
                onToggle = onVibrationToggle
            )
        }
    }
}

@Composable
private fun SmartFeaturesCard(
    smartSkip: Boolean,
    behindNudge: Boolean,
    onSmartSkipToggle: (Boolean) -> Unit,
    onBehindNudgeToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🧠", fontSize = 18.sp)
                Text("Smart Features", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            SettingsToggleRow(
                icon = "⏭️",
                title = "Smart Skip",
                subtitle = "Skip reminder if you recently logged water",
                checked = smartSkip,
                onToggle = onSmartSkipToggle
            )

            SettingsToggleRow(
                icon = "⚠️",
                title = "Behind Schedule Nudge",
                subtitle = "Extra nudge when you're behind on hydration",
                checked = behindNudge,
                onToggle = onBehindNudgeToggle
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(icon, fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = WaterBlueMedium,
                checkedThumbColor = Color.White
            )
        )
    }
}

@Composable
private fun ReminderSummaryCard(settings: WaterReminderSettings) {
    val remindersPerDay = if (settings.frequencyMinutes > 0) {
        settings.wakingMinutes / settings.frequencyMinutes
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WaterBlueSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📋 Reminder Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WaterBlueDark)

            val summaryItems = listOf(
                "⏰ Active from ${settings.startTimeFormatted} to ${settings.endTimeFormatted}",
                "🔄 ${settings.frequencyLabel} (~$remindersPerDay reminders/day)",
                "🔉 Sound: ${if (settings.enableSound) "On" else "Off"}",
                "📳 Vibration: ${if (settings.enableVibration) "On" else "Off"}",
                if (settings.smartSkipEnabled) "⏭️ Smart skip: Enabled" else "⏭️ Smart skip: Disabled",
                if (settings.behindScheduleNudge) "⚠️ Behind-schedule nudge: Enabled" else "⚠️ Behind-schedule nudge: Disabled"
            )

            summaryItems.forEach { item ->
                Text(
                    text = item,
                    fontSize = 13.sp,
                    color = WaterBlueDark.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaterTimePicker(
    initialHour: Int,
    initialMinute: Int,
    title: String,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WaterBlueDark
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = WaterBlueMedium,
                        containerColor = WaterBlueSurface
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(timePickerState.hour, timePickerState.minute)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaterBlueMedium),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Set Time")
                    }
                }
            }
        }
    }
}
