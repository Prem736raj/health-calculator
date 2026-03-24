package com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetabolicSyndromeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCalculator: (String) -> Unit = {},
    viewModel: MetabolicSyndromeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metabolic Syndrome") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show info dialog */ }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Information"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        val haptic = LocalHapticFeedback.current
        val context = LocalContext.current

        Column(modifier = Modifier.padding(paddingValues)) {
            // Tab row: Assess / Progress / Learn
            var selectedTabIndex by remember {
                mutableIntStateOf(
                    when {
                        uiState.showTracking -> 1
                        else -> 0
                    }
                )
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTabIndex = 0
                        if (uiState.showTracking) viewModel.toggleTracking()
                    },
                    text = { Text("Assess", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            Icons.Filled.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTabIndex = 1
                        if (!uiState.showTracking) viewModel.toggleTracking()
                    },
                    text = { Text("Progress", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.trackingRecords.isNotEmpty()) {
                                    Badge {
                                        Text("${uiState.trackingRecords.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTabIndex = 2
                        if (uiState.showTracking) viewModel.toggleTracking()
                    },
                    text = { Text("Learn", fontWeight = FontWeight.SemiBold) },
                    icon = {
                        Icon(
                            Icons.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(tween(400)) { it } + fadeIn(tween(400)) togetherWith
                                slideOutHorizontally(tween(400)) { -it } + fadeOut(tween(400))
                    } else {
                        slideInHorizontally(tween(400)) { -it } + fadeIn(tween(400)) togetherWith
                                slideOutHorizontally(tween(400)) { it } + fadeOut(tween(400))
                    }
                },
                label = "tab_transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        // Assess tab
                        AnimatedContent(
                            targetState = uiState.showResult,
                            transitionSpec = {
                                slideInHorizontally(tween(400)) { it } + fadeIn(tween(400)) togetherWith
                                        slideOutHorizontally(tween(400)) { -it } + fadeOut(tween(400))
                            },
                            label = "screen_transition"
                        ) { showResult ->
                            if (showResult && uiState.result != null) {
                                MetabolicSyndromeResultScreen(
                                    result = uiState.result!!,
                                    isSaved = uiState.isSaved,
                                    standardsComparison = uiState.standardsComparison,
                                    onSave = { viewModel.saveToHistory() },
                                    onRecalculate = { viewModel.resetResult() },
                                    onShare = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, viewModel.getShareText())
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Share Result")
                                        )
                                    },
                                    onEthnicityChange = { viewModel.updateEthnicity(it) },
                                    onNavigateToCalculator = onNavigateToCalculator
                                )
                            } else {
                                MetabolicSyndromeInputContent(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                    1 -> {
                        // Progress tab
                        MetabolicSyndromeTrackingSection(
                            records = uiState.trackingRecords,
                            comparison = uiState.comparison,
                            isLabReminderEnabled = uiState.isLabReminderEnabled,
                            reminderMonths = uiState.reminderMonths,
                            onSetLabReminder = { enabled, months ->
                                viewModel.setLabReminder(enabled, months)
                            }
                        )
                    }
                    2 -> {
                        // Learn tab
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            MetabolicSyndromeEducationScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetabolicSyndromeInputContent(
    uiState: MetabolicSyndromeUiState,
    viewModel: MetabolicSyndromeViewModel
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Insights / Last Assessment Card
        if (uiState.trackingRecords.isNotEmpty()) {
            val latest = uiState.trackingRecords.first()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Last Assessment: ${latest.dateTime.take(10)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${latest.criteriaMet}/5 Criteria Met • ${latest.riskLevel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // --- Gender Selection ---
        GenderSelectionRow(
            isMale = uiState.isMale,
            onGenderSelected = { viewModel.updateGender(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 1. Waist Circumference ---
        CriterionInputCard(
            title = "Waist Circumference",
            description = "Central obesity indicator",
            icon = "📏",
            value = uiState.waist,
            onValueChange = { viewModel.updateWaist(it) },
            unit = if (uiState.waistUnitCm) "cm" else "in",
            onUnitToggle = { viewModel.toggleWaistUnit() },
            isUnitConfigurable = true,
            error = uiState.waistError,
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Blood Pressure ---
        BloodPressureInputCard(
            systolic = uiState.systolic,
            diastolic = uiState.diastolic,
            onSystolicChange = { viewModel.updateSystolic(it) },
            onDiastolicChange = { viewModel.updateDiastolic(it) },
            systolicError = uiState.systolicError,
            diastolicError = uiState.diastolicError,
            isOnMedication = uiState.onBpMedication,
            onMedicationChange = { viewModel.updateBpMedication(it) },
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Fasting Blood Glucose ---
        CriterionInputCard(
            title = "Fasting Blood Glucose",
            description = "Elevated blood sugar",
            icon = "🩸",
            value = uiState.fastingGlucose,
            onValueChange = { viewModel.updateFastingGlucose(it) },
            unit = if (uiState.glucoseUnitMgDl) "mg/dL" else "mmol/L",
            onUnitToggle = { viewModel.toggleGlucoseUnit() },
            isUnitConfigurable = true,
            error = uiState.glucoseError,
            isOnMedication = uiState.onGlucoseMedication,
            onMedicationChange = { viewModel.updateGlucoseMedication(it) },
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. Triglycerides ---
        CriterionInputCard(
            title = "Triglycerides",
            description = "Elevated blood fat",
            icon = "🧪",
            value = uiState.triglycerides,
            onValueChange = { viewModel.updateTriglycerides(it) },
            unit = if (uiState.triglyceridesUnitMgDl) "mg/dL" else "mmol/L",
            onUnitToggle = { viewModel.toggleTriglyceridesUnit() },
            isUnitConfigurable = true,
            error = uiState.triglyceridesError,
            isOnMedication = uiState.onTriglyceridesMedication,
            onMedicationChange = { viewModel.updateTriglyceridesMedication(it) },
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 5. HDL Cholesterol ---
        CriterionInputCard(
            title = "HDL Cholesterol",
            description = "Low 'good' cholesterol",
            icon = "💛",
            value = uiState.hdl,
            onValueChange = { viewModel.updateHdl(it) },
            unit = if (uiState.hdlUnitMgDl) "mg/dL" else "mmol/L",
            onUnitToggle = { viewModel.toggleHdlUnit() },
            isUnitConfigurable = true,
            error = uiState.hdlError,
            isOnMedication = uiState.onHdlMedication,
            onMedicationChange = { viewModel.updateHdlMedication(it) },
            onNext = { focusManager.clearFocus() } // Last item
        )

        Spacer(modifier = Modifier.height(24.dp))

        // === Live Partial Assessment Indicator ===
        if (uiState.partialResult != null && uiState.partialResult.providedCount > 0) {
            val partial = uiState.partialResult
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        partial.metCount >= 3 -> HealthRed.copy(alpha = 0.08f)
                        partial.metCount >= 2 -> HealthOrange.copy(alpha = 0.08f)
                        partial.metCount >= 1 -> HealthYellow.copy(alpha = 0.08f)
                        else -> HealthGreen.copy(alpha = 0.08f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live Preview: ${partial.metCount} of ${partial.providedCount} entered criteria abnormal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Mini criterion indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        partial.criteria.forEach { c ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(c.icon, fontSize = 16.sp)
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                !c.isProvided -> Color.Gray.copy(alpha = 0.3f)
                                                c.isMet == true -> HealthRed.copy(alpha = 0.8f)
                                                else -> HealthGreen.copy(alpha = 0.8f)
                                            }
                                        )
                                )
                            }
                        }
                    }

                    if (partial.providedCount < 5) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = partial.partialMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Action Buttons ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.clearAll() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Clear")
            }

            Button(
                onClick = { viewModel.calculate() },
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Analyze Risk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GenderSelectionRow(
    isMale: Boolean,
    onGenderSelected: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val selectedColor = MaterialTheme.colorScheme.primary
        val unselectedColor = MaterialTheme.colorScheme.surfaceVariant

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isMale) selectedColor else unselectedColor,
            onClick = { onGenderSelected(true) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Male",
                    color = if (isMale) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (!isMale) selectedColor else unselectedColor,
            onClick = { onGenderSelected(false) }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Female",
                    color = if (!isMale) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriterionInputCard(
    title: String,
    description: String,
    icon: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    onUnitToggle: () -> Unit,
    isUnitConfigurable: Boolean = false,
    error: String? = null,
    isOnMedication: Boolean = false,
    onMedicationChange: ((Boolean) -> Unit)? = null,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = value,
                onValueChange = { if (it.length <= 6) onValueChange(it) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onNext = { onNext() }
                ),
                trailingIcon = {
                    if (isUnitConfigurable) {
                        TextButton(onClick = onUnitToggle) {
                            Text(text = unit, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(text = unit, modifier = Modifier.padding(end = 16.dp))
                    }
                },
                isError = error != null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            if (onMedicationChange != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isOnMedication,
                        onCheckedChange = { onMedicationChange(it) }
                    )
                    Text(
                        text = "On medication for this condition",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureInputCard(
    systolic: String,
    diastolic: String,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit,
    systolicError: String? = null,
    diastolicError: String? = null,
    isOnMedication: Boolean,
    onMedicationChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🫀", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Blood Pressure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Elevated blood pressure",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = systolic,
                    onValueChange = { if (it.length <= 3) onSystolicChange(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Systolic") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    isError = systolicError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { if (it.length <= 3) onDiastolicChange(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Diastolic") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { onNext() }
                    ),
                    isError = diastolicError != null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (systolicError != null || diastolicError != null) {
                Text(
                    text = systolicError ?: diastolicError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isOnMedication,
                    onCheckedChange = { onMedicationChange(it) }
                )
                Text(
                    text = "On medication for high BP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
