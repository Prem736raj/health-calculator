package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpQuickLogCard
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpEdgeCaseWarning
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpSaveConfirmation
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.BpEmergencyPulsingAlert
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.QuickLogSuggestion
// import com.health.calculator.bmi.tracker.ui.theme.* 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToTrends: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToEducation: () -> Unit = {},
    viewModel: BloodPressureViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.showSaveSuccess) {
        if (uiState.showSaveSuccess) {
            snackbarHostState.showSnackbar("Reading saved to log", duration = SnackbarDuration.Short)
            viewModel.dismissSaveSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Blood Pressure",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToExport()
                    }) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = "Export & Share"
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToTrends()
                    }) {
                        Icon(
                            Icons.Outlined.Timeline,
                            contentDescription = "View Trends"
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToLogs()
                    }) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = "View Logs"
                        )
                    }
                    if (uiState.systolic.isNotEmpty() || uiState.diastolic.isNotEmpty()) {
                        TextButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onClearAll()
                        }) {
                            Text("Clear All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header note
            BpHeaderNote()

            // Doctor Suggestion Banner
            AnimatedVisibility(
                visible = uiState.showDoctorSuggestion,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                BpDoctorSuggestionBanner(
                    onDismiss = { viewModel.onDismissDoctorSuggestion() },
                    onAction = { onNavigateToReminders() }
                )
            }

            // Streak Card
            BpStreakCard(
                currentStreak = uiState.currentStreak,
                longestStreak = uiState.longestStreak
            )

            // Quick Log Card (Pattern-based suggestion)
            AnimatedVisibility(
                visible = uiState.showQuickLog && uiState.quickLogSuggestion != null && !uiState.showResult,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                uiState.quickLogSuggestion?.let { suggestion ->
                    BpQuickLogCard(
                        suggestion = suggestion,
                        onQuickLogClicked = { viewModel.onQuickLogApplied(it) },
                        onDismiss = { viewModel.onDismissQuickLog() }
                    )
                }
            }

            // Emergency warning banner
            AnimatedVisibility(
                visible = uiState.showEmergencyWarning,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                EmergencyWarningBanner()
            }

            // BP Visual Illustration (hidden when showing results)
            AnimatedVisibility(
                visible = !uiState.showResult,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                BpIllustrationCard()
            }

            // Main Input: Systolic & Diastolic
            BpMainInputCard(
                systolic = uiState.systolic,
                diastolic = uiState.diastolic,
                systolicError = uiState.systolicError,
                diastolicError = uiState.diastolicError,
                crossFieldError = uiState.crossFieldError,
                previousSystolicHint = uiState.previousSystolicHint,
                previousDiastolicHint = uiState.previousDiastolicHint,
                onSystolicChange = viewModel::onSystolicChange,
                onDiastolicChange = viewModel::onDiastolicChange
            )

            // Pulse input
            BpPulseInputCard(
                pulse = uiState.pulse,
                pulseError = uiState.pulseError,
                previousPulseHint = uiState.previousPulseHint,
                onPulseChange = viewModel::onPulseChange
            )

            // Optional details
            BpOptionalDetailsCard(
                selectedArm = uiState.selectedArm,
                selectedPosition = uiState.selectedPosition,
                selectedTimeOfDay = uiState.selectedTimeOfDay,
                measurementTimeFormatted = uiState.measurementTimeFormatted,
                onArmSelected = viewModel::onArmSelected,
                onPositionSelected = viewModel::onPositionSelected,
                onTimeOfDaySelected = viewModel::onTimeOfDaySelected,
                onTimeClicked = { viewModel.onShowTimePicker(true) }
            )

            // Education Link (Subtle)
            if (!uiState.showResult) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToEducation()
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.School,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Learn about blood pressure & how to measure correctly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }

            // Check Button
            BpCheckButton(
                isCalculating = uiState.isCalculating,
                isEnabled = uiState.systolic.isNotEmpty() && uiState.diastolic.isNotEmpty(),
                onClick = {
                    focusManager.clearFocus()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onCheckPressed()
                }
            )

            // Medication Tracking Card
            BpMedicationCard(
                onMedication = uiState.onMedication,
                medicationName = uiState.medicationName,
                onToggle = { viewModel.onMedicationToggle(it) },
                onNameChange = { viewModel.onMedicationNameChange(it) }
            )

            // Set Reminders Button
            OutlinedButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToReminders() 
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Blood Pressure Reminders")
            }

            // --- NEW: Divider for Results ---
            AnimatedVisibility(
                visible = uiState.showResult,
                enter = expandVertically() + fadeIn()
            ) {
                Column {
                    // Save Success Banner
                    BpSaveConfirmation(isVisible = uiState.showSaveSuccess)

                    // Edge Case Warning
                    AnimatedVisibility(
                        visible = uiState.showEdgeCaseWarning && uiState.result != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        uiState.result?.let { reading ->
                            BpEdgeCaseWarning(
                                systolic = reading.systolic,
                                diastolic = reading.diastolic
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "RESULTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
            }
        }

        // --- NEW: Result Section ---
        AnimatedVisibility(
            visible = uiState.showResult && uiState.result != null,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            if (uiState.result != null && uiState.pulsePressureAnalysis != null && 
                uiState.mapAnalysis != null && uiState.riskLevel != null) {
                    
                    // Auto-scroll to results when they appear
                    LaunchedEffect(uiState.showResult) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }

                    BpResultSection(
                        reading = uiState.result!!,
                        pulsePressureAnalysis = uiState.pulsePressureAnalysis!!,
                        mapAnalysis = uiState.mapAnalysis!!,
                        heartRateAnalysis = uiState.heartRateAnalysis,
                        riskLevel = uiState.riskLevel!!,
                        gaugePosition = uiState.gaugePosition,
                        isMultiReadingMode = uiState.isMultiReadingMode,
                        showAverageResult = uiState.showAverageResult,
                        onTakeAnotherReading = viewModel::onTakeAnotherReading,
                        onShowAverage = viewModel::onShowAverage,
                        onAddNote = { viewModel.onShowNoteDialog() },
                        onViewLog = { onNavigateToLogs() },
                        onViewTrends = { onNavigateToTrends() },
                        onViewExport = { onNavigateToExport() },
                        onNavigateToEducation = { onNavigateToEducation() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Time Picker Dialog
    if (uiState.showTimePicker) {
        BpTimePickerDialog(
            onDismiss = { viewModel.onShowTimePicker(false) },
            onTimeSelected = { hour, minute ->
                viewModel.onMeasurementTimeChanged(hour, minute)
            }
        )
    }

    // Emergency Dialog (Enhanced)
    if (uiState.showEmergencyDialog && uiState.result != null) {
        BpEmergencyPulsingAlert(
            reading = uiState.result!!,
            onDismiss = viewModel::dismissEmergencyDialog
        )
    }

    // Note Dialog
    if (uiState.showNoteDialog) {
        BpNoteDialog(
            noteText = uiState.editingNoteText,
            onNoteChange = viewModel::onNoteDialogTextChange,
            onSave = viewModel::onSaveNote,
            onDismiss = viewModel::onDismissNoteDialog
        )
    }

    // Milestone Celebration
    if (uiState.showMilestoneCelebration) {
        BpMilestoneCelebrationDialog(
            streakCount = uiState.currentStreak,
            message = uiState.milestoneMessage ?: "Great job!",
            onDismiss = viewModel::onDismissMilestone
        )
    }
}

// ─── Header Note ───────────────────────────────────────────────────────────────

@Composable
private fun BpHeaderNote() {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(
            animationSpec = tween(500)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Enter your blood pressure reading from your monitor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ─── Emergency Warning Banner ──────────────────────────────────────────────────

@Composable
private fun EmergencyWarningBanner() {
    val infiniteTransition = rememberInfiniteTransition(label = "emergency_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFB71C1C).copy(alpha = pulseAlpha)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    "⚠️ Emergency Reading Detected!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "If your reading is above 180/120, seek immediate medical attention.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// ─── BP Illustration Card ──────────────────────────────────────────────────────

@Composable
private fun BpIllustrationCard() {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 60 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 100)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Understanding Your Reading",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Visual BP reading illustration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Systolic side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFEF5350),
                                            Color(0xFFE53935)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "SYS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Systolic",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE53935)
                        )
                        Text(
                            "Heart beating",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "(Top number)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Divider
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "/",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    // Diastolic side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF42A5F5),
                                            Color(0xFF1E88E5)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "DIA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Diastolic",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E88E5)
                        )
                        Text(
                            "Heart resting",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "(Bottom number)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── Main Input Card (Systolic & Diastolic) ────────────────────────────────────

@Composable
private fun BpMainInputCard(
    systolic: String,
    diastolic: String,
    systolicError: String?,
    diastolicError: String?,
    crossFieldError: String?,
    previousSystolicHint: String?,
    previousDiastolicHint: String?,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit
) {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }
    val focusManager = LocalFocusManager.current

    // Shake animation for errors
    val systolicShake = remember { Animatable(0f) }
    val diastolicShake = remember { Animatable(0f) }

    LaunchedEffect(systolicError) {
        if (systolicError != null) {
            systolicShake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    (-10f) at 50
                    10f at 100
                    (-8f) at 150
                    8f at 200
                    (-5f) at 250
                    5f at 300
                    0f at 400
                }
            )
        }
    }

    LaunchedEffect(diastolicError) {
        if (diastolicError != null) {
            diastolicShake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    (-10f) at 50
                    10f at 100
                    (-8f) at 150
                    8f at 200
                    (-5f) at 250
                    5f at 300
                    0f at 400
                }
            )
        }
    }

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 80 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 200)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "Blood Pressure Reading",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Systolic & Diastolic inputs side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Systolic
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = systolic,
                            onValueChange = onSystolicChange,
                            label = { Text("Systolic") },
                            placeholder = {
                                Text(
                                    previousSystolicHint ?: "120",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            },
                            suffix = { Text("mmHg", style = MaterialTheme.typography.bodySmall) },
                            isError = systolicError != null || crossFieldError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Right) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = systolicShake.value.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE53935),
                                focusedLabelColor = Color(0xFFE53935)
                            )
                        )
                        AnimatedVisibility(visible = systolicError != null) {
                            Text(
                                systolicError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    // Diastolic
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = diastolic,
                            onValueChange = onDiastolicChange,
                            label = { Text("Diastolic") },
                            placeholder = {
                                Text(
                                    previousDiastolicHint ?: "80",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            },
                            suffix = { Text("mmHg", style = MaterialTheme.typography.bodySmall) },
                            isError = diastolicError != null || crossFieldError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = diastolicShake.value.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E88E5),
                                focusedLabelColor = Color(0xFF1E88E5)
                            )
                        )
                        AnimatedVisibility(visible = diastolicError != null) {
                            Text(
                                diastolicError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }
                }

                // Cross-field error
                AnimatedVisibility(visible = crossFieldError != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                crossFieldError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Previous reading hint
                if (previousSystolicHint != null && previousDiastolicHint != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "Last reading: $previousSystolicHint/$previousDiastolicHint mmHg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// ─── Pulse Input Card ──────────────────────────────────────────────────────────

@Composable
private fun BpPulseInputCard(
    pulse: String,
    pulseError: String?,
    previousPulseHint: String?,
    onPulseChange: (String) -> Unit
) {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }

    // Heartbeat animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 300)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = heartScale
                                scaleY = heartScale
                            }
                    )
                    Text(
                        "Pulse / Heart Rate",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Optional",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                OutlinedTextField(
                    value = pulse,
                    onValueChange = onPulseChange,
                    label = { Text("Heart Rate") },
                    placeholder = {
                        Text(
                            previousPulseHint ?: "72",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    },
                    suffix = { Text("BPM", style = MaterialTheme.typography.bodySmall) },
                    isError = pulseError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63),
                        focusedLabelColor = Color(0xFFE91E63)
                    )
                )

                AnimatedVisibility(visible = pulseError != null) {
                    Text(
                        pulseError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

// ─── Optional Details Card ─────────────────────────────────────────────────────

@Composable
private fun BpOptionalDetailsCard(
    selectedArm: BpArm?,
    selectedPosition: BpPosition?,
    selectedTimeOfDay: BpTimeOfDay,
    measurementTimeFormatted: String,
    onArmSelected: (BpArm?) -> Unit,
    onPositionSelected: (BpPosition?) -> Unit,
    onTimeOfDaySelected: (BpTimeOfDay) -> Unit,
    onTimeClicked: () -> Unit
) {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 120 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 400)
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with expand toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isExpanded = !isExpanded
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Measurement Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Optional",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    val rotation by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec = tween(300),
                        label = "chevron_rotation"
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.graphicsLayer { rotationZ = rotation },
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Arm selection
                        Text(
                            "Arm Used",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BpArm.entries.forEach { arm ->
                                FilterChip(
                                    selected = selectedArm == arm,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onArmSelected(if (selectedArm == arm) null else arm)
                                    },
                                    label = { Text(arm.displayName) },
                                    leadingIcon = if (selectedArm == arm) {
                                        {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        // Position selection
                        Text(
                            "Body Position",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            BpPosition.entries.forEach { position ->
                                FilterChip(
                                    selected = selectedPosition == position,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onPositionSelected(
                                            if (selectedPosition == position) null else position
                                        )
                                    },
                                    label = { Text(position.displayName) },
                                    leadingIcon = if (selectedPosition == position) {
                                        {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        // Time of Day
                        Text(
                            "Time of Day",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            BpTimeOfDay.entries.forEach { time ->
                                FilterChip(
                                    selected = selectedTimeOfDay == time,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onTimeOfDaySelected(time)
                                    },
                                    label = { Text(time.displayName) },
                                    leadingIcon = if (selectedTimeOfDay == time) {
                                        {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }

                        // Measurement Time
                        Text(
                            "Measurement Time",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        OutlinedCard(
                            onClick = onTimeClicked,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        measurementTimeFormatted,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = "Edit time",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Check Button ──────────────────────────────────────────────────────────────

@Composable
private fun BpCheckButton(
    isCalculating: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val enterAnim = remember { MutableTransitionState(false).apply { targetState = true } }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )

    AnimatedVisibility(
        visibleState = enterAnim,
        enter = slideInVertically(initialOffsetY = { 140 }) + fadeIn(
            animationSpec = tween(600, delayMillis = 500)
        )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            enabled = isEnabled && !isCalculating,
            shape = RoundedCornerShape(16.dp),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 1.dp
            )
        ) {
            if (isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Checking...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Check Blood Pressure",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



// ─── Time Picker Dialog ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BpTimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val currentTime = java.time.LocalTime.now()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                "Select Measurement Time",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}
