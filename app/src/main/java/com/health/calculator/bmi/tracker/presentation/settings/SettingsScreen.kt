package com.health.calculator.bmi.tracker.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.model.ThemeMode
import com.health.calculator.bmi.tracker.data.model.UnitSystem
import com.health.calculator.bmi.tracker.data.preferences.PlantPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.health.calculator.bmi.tracker.ui.components.InactivityNotificationSettings
import com.health.calculator.bmi.tracker.data.repository.InactivityRepository

// ─── Accent Colors ────────────────────────────────────────────────────────────

private val SettingsAccent = Color(0xFF00ACC1)
private val DangerRed = Color(0xFFE53935)
private val WarningOrange = Color(0xFFFF9800)

// ─── Main Settings Screen ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val packageName = context.packageName

    // ── Entrance Animation ────────────────────────────────────────────────
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // ── Snackbar Messages ─────────────────────────────────────────────────
    LaunchedEffect(uiState.showExportSuccessMessage) {
        if (uiState.showExportSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "📦 Export completed successfully",
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSuccessMessage()
        }
    }

    LaunchedEffect(uiState.showClearSuccessMessage) {
        if (uiState.showClearSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "✅ Data cleared successfully",
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSuccessMessage()
        }
    }

    LaunchedEffect(uiState.exportStatusMessage) {
        uiState.exportStatusMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissExportStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Customize your experience",
                            style = MaterialTheme.typography.bodySmall,
                            color = SettingsAccent.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = 0.6f
                            )
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SettingsAccent)
            }
        } else {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { it / 6 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ── GENERAL SECTION ───────────────────────────────────
                    item { SectionHeader(title = "General", emoji = "⚙️") }

                    item {
                        SettingsCard {
                            // Unit System
                            SettingsClickItem(
                                icon = Icons.Outlined.Straighten,
                                iconTint = SettingsAccent,
                                title = "Unit System",
                                subtitle = uiState.unitSystem.description,
                                trailingText = uiState.unitSystem.displayName,
                                onClick = { viewModel.showUnitSystemPicker() }
                            )

                            SettingsDivider()

                            // Theme
                            val themeIcon = when (uiState.themeMode) {
                                ThemeMode.LIGHT -> Icons.Filled.LightMode
                                ThemeMode.DARK -> Icons.Filled.DarkMode
                                ThemeMode.SYSTEM -> Icons.Filled.PhoneAndroid
                            }
                            SettingsClickItem(
                                icon = themeIcon,
                                iconTint = when (uiState.themeMode) {
                                    ThemeMode.LIGHT -> Color(0xFFFF9800)
                                    ThemeMode.DARK -> Color(0xFF5C6BC0)
                                    ThemeMode.SYSTEM -> SettingsAccent
                                },
                                title = "Theme",
                                subtitle = "App appearance",
                                trailingText = "${uiState.themeMode.emoji} ${uiState.themeMode.displayName}",
                                onClick = { viewModel.showThemePicker() }
                            )
                        }
                    }

                    // ── WATER TRACKER SECTION ─────────────────────────────
                    item { SectionHeader(title = "Water Tracker", emoji = "💧") }

                    item {
                        SettingsCard {
                            val context = LocalContext.current
                            val plantPrefs = remember { PlantPreferences(context) }
                            var plantVisible by remember { mutableStateOf(plantPrefs.isPlantVisible) }

                            SettingsToggleItem(
                                icon = Icons.Outlined.WaterDrop,
                                iconTint = Color(0xFF4CAF50),
                                title = "Hydration Plant Companion",
                                subtitle = "Show a virtual plant that grows over time",
                                checked = plantVisible,
                                onCheckedChange = {
                                    plantVisible = it
                                    plantPrefs.isPlantVisible = it
                                }
                            )
                        }
                    }

                    // ── NOTIFICATIONS SECTION ─────────────────────────────
                    item { SectionHeader(title = "Notifications", emoji = "🔔") }

                    item {
                        SettingsCard {
                            // Master reminders toggle
                            SettingsToggleItem(
                                icon = Icons.Filled.Notifications,
                                iconTint = if (uiState.remindersEnabled)
                                    SettingsAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                title = "Enable Reminders",
                                subtitle = "Master toggle for all notifications",
                                checked = uiState.remindersEnabled,
                                onCheckedChange = { viewModel.toggleReminders(it) }
                            )

                            // Sub-toggles (only interactive when master is on)
                            AnimatedVisibility(visible = uiState.remindersEnabled) {
                                Column {
                                    SettingsDivider()

                                    SettingsToggleItem(
                                        icon = Icons.Outlined.WaterDrop,
                                        iconTint = Color(0xFF0277BD),
                                        title = "Water Intake Reminder",
                                        subtitle = "Daily hydration reminders",
                                        checked = uiState.waterReminderEnabled,
                                        onCheckedChange = { viewModel.toggleWaterReminder(it) },
                                        enabled = uiState.remindersEnabled
                                    )

                                    SettingsDivider()

                                    SettingsToggleItem(
                                        icon = Icons.Outlined.Straighten,
                                        iconTint = Color(0xFF43A047),
                                        title = "Weight Tracking Reminder",
                                        subtitle = "Weekly weigh-in reminders",
                                        checked = uiState.weightReminderEnabled,
                                        onCheckedChange = { viewModel.toggleWeightReminder(it) },
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Re-engagement & Streak Protection
                            val inactivityRepo = remember { InactivityRepository(context) }
                            val inactivityState by inactivityRepo.getInactivityState().collectAsState(initial = com.health.calculator.bmi.tracker.data.models.InactivityState())
                            val freezeCount by inactivityRepo.getStreakFreezeCount().collectAsState(initial = 1)

                            InactivityNotificationSettings(
                                inactivityEnabled = inactivityState.inactivityNotificationsEnabled,
                                streakProtectionEnabled = inactivityState.streakProtectionEnabled,
                                streakFreezeCount = freezeCount,
                                onInactivityToggle = {
                                    scope.launch { inactivityRepo.setInactivityNotificationsEnabled(it) }
                                },
                                onStreakProtectionToggle = {
                                    scope.launch { inactivityRepo.setStreakProtectionEnabled(it) }
                                }
                            )
                        }
                    }

                    // ── DATA SECTION ──────────────────────────────────────
                    item { SectionHeader(title = "Data Management", emoji = "💾") }

                    item {
                        SettingsCard {
                            SettingsClickItem(
                                icon = Icons.Filled.FileDownload,
                                iconTint = Color(0xFF1E88E5),
                                title = "Export All Data",
                                subtitle = "Download as PDF or CSV",
                                onClick = { viewModel.exportData() }
                            )

                            SettingsDivider()

                            SettingsClickItem(
                                icon = Icons.Default.Storage,
                                iconTint = SettingsAccent,
                                title = "Data Management",
                                subtitle = "Storage, cleanup, and integrity",
                                onClick = onNavigateToDataManagement
                            )

                            SettingsDivider()

                            SettingsClickItem(
                                icon = Icons.Default.Backup,
                                iconTint = Color(0xFF4CAF50),
                                title = "Backup & Restore",
                                subtitle = "Cloud and local data sync",
                                onClick = onNavigateToBackup
                            )
                        }
                    }

                    // ── ABOUT SECTION ─────────────────────────────────────
                    item { SectionHeader(title = "About", emoji = "ℹ️") }

                    item {
                        SettingsCard {
                            // App version
                            SettingsInfoItem(
                                icon = Icons.Filled.Info,
                                iconTint = SettingsAccent,
                                title = "App Version",
                                value = "1.0.0 (Build 1)"
                            )

                            SettingsDivider()

                            SettingsClickItem(
                                icon = Icons.Outlined.Shield,
                                iconTint = Color(0xFF43A047),
                                title = "Privacy Policy",
                                subtitle = "How we handle your data",
                                onClick = {
                                    openUrl(context, "https://github.com/Prem736raj/health-calculator")
                                }
                            )

                            SettingsDivider()

                            SettingsClickItem(
                                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                                iconTint = Color(0xFF1E88E5),
                                title = "Terms of Service",
                                subtitle = "Usage terms and conditions",
                                onClick = {
                                    openUrl(context, "https://github.com/Prem736raj/health-calculator/blob/main/LICENSE")
                                }
                            )

                            SettingsDivider()

                            SettingsClickItem(
                                icon = Icons.Filled.Star,
                                iconTint = Color(0xFFFFC107),
                                title = "Rate the App",
                                subtitle = "Share your feedback on Play Store",
                                onClick = { openAppRating(context, packageName) }
                            )

                            SettingsDivider()

                            SettingsClickItem(
                                icon = Icons.Filled.Share,
                                iconTint = Color(0xFF7B1FA2),
                                title = "Share the App",
                                subtitle = "Recommend to friends and family",
                                onClick = { shareApp(context, packageName) }
                            )
                        }
                    }

                    // ── MEDICAL DISCLAIMER ────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        MedicalDisclaimerCard()
                    }

                    // ── Footer ────────────────────────────────────────────
                    item {
                        Text(
                            text = "Made with ❤️ for your health\nHealth Calculator © 2024",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }

    // ─── Dialogs ──────────────────────────────────────────────────────────

    // Unit System Picker
    if (uiState.showUnitSystemPicker) {
        UnitSystemPickerDialog(
            currentSystem = uiState.unitSystem,
            onSystemSelected = { viewModel.updateUnitSystem(it) },
            onDismiss = { viewModel.hideUnitSystemPicker() }
        )
    }

    // Theme Picker
    if (uiState.showThemePicker) {
        ThemePickerDialog(
            currentTheme = uiState.themeMode,
            onThemeSelected = { viewModel.updateThemeMode(it) },
            onDismiss = { viewModel.hideThemePicker() }
        )
    }

    // Clear History Confirmation
    if (uiState.showClearHistoryDialog) {
        ConfirmationDialog(
            icon = Icons.Filled.Delete,
            iconColor = WarningOrange,
            title = "Clear All History?",
            message = "This will permanently delete all your calculation history and records. Your profile data and settings will be kept.\n\nThis action cannot be undone.",
            confirmText = "Clear History",
            confirmColor = WarningOrange,
            onConfirm = { viewModel.confirmClearHistory() },
            onDismiss = { viewModel.hideClearHistoryDialog() }
        )
    }

    // Clear All Data Confirmation
    if (uiState.showClearAllDataDialog) {
        ConfirmationDialog(
            icon = Icons.Filled.DeleteForever,
            iconColor = DangerRed,
            title = "Clear ALL Data?",
            message = "⚠️ This will permanently delete:\n\n• All calculation history\n• Your profile information\n• All saved preferences\n• App settings\n\nThe app will reset to its initial state.\n\nThis action CANNOT be undone.",
            confirmText = "Delete Everything",
            confirmColor = DangerRed,
            onConfirm = { viewModel.confirmClearAllData() },
            onDismiss = { viewModel.hideClearAllDataDialog() }
        )
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    emoji: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Settings Card Container ──────────────────────────────────────────────────

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            content()
        }
    }
}

// ─── Settings Divider ─────────────────────────────────────────────────────────

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 56.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

private fun openAppRating(context: android.content.Context, packageName: String) {
    val marketUri = Uri.parse("market://details?id=$packageName")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

private fun shareApp(context: android.content.Context, packageName: String) {
    val appLink = "https://play.google.com/store/apps/details?id=$packageName"
    val shareText = "Track health metrics with Health Calculator: $appLink"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Health Calculator"))
}

// ─── Clickable Settings Item ──────────────────────────────────────────────────

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    isDangerous: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (isDangerous)
                    DangerRed
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Trailing
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SettingsAccent
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─── Toggle Settings Item ─────────────────────────────────────────────────────

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val itemAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        label = "toggle_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingsAccent,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// ─── Info Settings Item ───────────────────────────────────────────────────────

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Medical Disclaimer Card ──────────────────────────────────────────────────

@Composable
private fun MedicalDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Medical Disclaimer",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Health Calculator is designed for educational and informational purposes only. It is NOT a medical device and should NOT be used as a substitute for professional medical advice, diagnosis, or treatment.\n\nAlways consult a qualified healthcare provider for any health concerns or before making health-related decisions based on calculations from this app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }
}

// ─── Unit System Picker Dialog ────────────────────────────────────────────────

@Composable
private fun UnitSystemPickerDialog(
    currentSystem: UnitSystem,
    onSystemSelected: (UnitSystem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📏 Unit System",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UnitSystem.entries.forEach { system ->
                    val isSelected = system == currentSystem
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected)
                            SettingsAccent.copy(alpha = 0.1f)
                        else
                            Color.Transparent,
                        label = "unit_bg"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .clickable { onSystemSelected(system) }
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) SettingsAccent.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = system.displayName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) SettingsAccent else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = system.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = SettingsAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = SettingsAccent)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ─── Theme Picker Dialog ──────────────────────────────────────────────────────

@Composable
private fun ThemePickerDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎨 App Theme",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == currentTheme
                    val themeIcon = when (mode) {
                        ThemeMode.LIGHT -> Icons.Filled.LightMode
                        ThemeMode.DARK -> Icons.Filled.DarkMode
                        ThemeMode.SYSTEM -> Icons.Filled.PhoneAndroid
                    }
                    val themeColor = when (mode) {
                        ThemeMode.LIGHT -> Color(0xFFFF9800)
                        ThemeMode.DARK -> Color(0xFF5C6BC0)
                        ThemeMode.SYSTEM -> SettingsAccent
                    }
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected)
                            themeColor.copy(alpha = 0.1f)
                        else
                            Color.Transparent,
                        label = "theme_bg"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .clickable { onThemeSelected(mode) }
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) themeColor.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = null,
                            tint = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${mode.emoji} ${mode.displayName}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = themeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = SettingsAccent)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ─── Confirmation Dialog ──────────────────────────────────────────────────────

@Composable
private fun ConfirmationDialog(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f))
                    .border(
                        width = 1.5.dp,
                        color = iconColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = confirmColor
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
