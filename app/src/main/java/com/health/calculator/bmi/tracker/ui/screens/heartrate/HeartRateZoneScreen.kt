package com.health.calculator.bmi.tracker.ui.screens.heartrate

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.ui.components.HeartRateFormula
import com.health.calculator.bmi.tracker.ui.components.FitnessLevel
import com.health.calculator.bmi.tracker.util.HeartRateZoneCalculator
import com.health.calculator.bmi.tracker.util.HeartRateZoneResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateZoneScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBPChecker: () -> Unit = {},
    profileAge: Int? = null,
    profileGender: String? = null,
    profileWeightKg: Float? = null,
    lastRestingHR: Int? = null,
    onCalculate: (HeartRateZoneResult) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var showResultScreen by rememberSaveable { mutableStateOf(false) }
    var calculationResult by remember { mutableStateOf<HeartRateZoneResult?>(null) }
    // Input states
    var age by rememberSaveable { mutableStateOf(profileAge?.toString() ?: "") }
    var restingHR by rememberSaveable { mutableStateOf(lastRestingHR?.toString() ?: "") }
    var customMaxHR by rememberSaveable { mutableStateOf("") }
    var selectedFormula by rememberSaveable { mutableStateOf(HeartRateFormula.STANDARD) }
    var selectedFitnessLevel by rememberSaveable { mutableStateOf(FitnessLevel.INTERMEDIATE) }
    var selectedGender by rememberSaveable { mutableStateOf(profileGender ?: "") }
    var showRestingHRGuide by remember { mutableStateOf(false) }
    var showProfileDataBanner by remember { mutableStateOf(profileAge != null) }

    // Validation states
    var ageError by remember { mutableStateOf<String?>(null) }
    var restingHRError by remember { mutableStateOf<String?>(null) }
    var customMaxHRError by remember { mutableStateOf<String?>(null) }

    // Determine if resting HR is required
    val isRestingHRRequired = selectedFormula == HeartRateFormula.KARVONEN

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Heart Rate Zones",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header explanation
            item {
                HeartRateHeaderCard()
            }

            // Profile data banner
            item {
                AnimatedVisibility(
                    visible = showProfileDataBanner,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ProfileDataBanner(
                        onDismiss = { showProfileDataBanner = false }
                    )
                }
            }

            // Age input
            item {
                AgeInputSection(
                    age = age,
                    error = ageError,
                    onAgeChange = { newVal ->
                        age = newVal.filter { it.isDigit() }
                        ageError = validateAge(age)
                    }
                )
            }

            // Gender selection (optional)
            item {
                GenderSelectionSection(
                    selectedGender = selectedGender,
                    onGenderSelect = { gender ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedGender = gender
                        // Auto-suggest Gulati for women
                        if (gender == "Female" && selectedFormula == HeartRateFormula.STANDARD) {
                            // Just a subtle suggestion, don't force change
                        }
                    }
                )
            }

            // Fitness level
            item {
                FitnessLevelSection(
                    selectedLevel = selectedFitnessLevel,
                    onLevelSelect = { level ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedFitnessLevel = level
                    }
                )
            }

            // Formula selection
            item {
                FormulaSelectionSection(
                    selectedFormula = selectedFormula,
                    selectedGender = selectedGender,
                    onFormulaSelect = { formula ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedFormula = formula
                    }
                )
            }

            // Resting HR input (conditional)
            item {
                AnimatedVisibility(
                    visible = isRestingHRRequired || restingHR.isNotEmpty(),
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    RestingHRSection(
                        restingHR = restingHR,
                        error = restingHRError,
                        isRequired = isRestingHRRequired,
                        onRestingHRChange = { newVal ->
                            restingHR = newVal.filter { it.isDigit() }
                            restingHRError = validateRestingHR(restingHR, isRestingHRRequired)
                        },
                        onShowGuide = { showRestingHRGuide = true },
                        onUseBPReading = onNavigateToBPChecker,
                        lastRestingHR = lastRestingHR
                    )
                }
            }

            // Resting HR hint for non-Karvonen
            item {
                AnimatedVisibility(
                    visible = !isRestingHRRequired && restingHR.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    RestingHROptionalHint(
                        onAddRestingHR = {
                            // Just make the field visible by putting focus
                            restingHR = " "
                            restingHR = ""
                        }
                    )
                }
            }

            // Custom Max HR input
            item {
                AnimatedVisibility(
                    visible = selectedFormula == HeartRateFormula.CUSTOM,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    CustomMaxHRSection(
                        customMaxHR = customMaxHR,
                        error = customMaxHRError,
                        onValueChange = { newVal ->
                            customMaxHR = newVal.filter { it.isDigit() }
                            customMaxHRError = validateCustomMaxHR(customMaxHR)
                        }
                    )
                }
            }

            // Calculate button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CalculateButton(
                    enabled = isInputValid(
                        age = age,
                        restingHR = restingHR,
                        customMaxHR = customMaxHR,
                        selectedFormula = selectedFormula,
                        isRestingHRRequired = isRestingHRRequired
                    ),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Validate all fields
                        ageError = validateAge(age)
                        restingHRError = validateRestingHR(restingHR, isRestingHRRequired)
                        if (selectedFormula == HeartRateFormula.CUSTOM) {
                            customMaxHRError = validateCustomMaxHR(customMaxHR)
                        }

                        if (ageError == null && restingHRError == null && customMaxHRError == null) {
                            val ageVal = age.toIntOrNull() ?: return@CalculateButton
                            val restingHRVal = restingHR.toIntOrNull()
                            val customMaxHRVal = customMaxHR.toIntOrNull()

                            val result = HeartRateZoneCalculator.calculateZones(
                                age = ageVal,
                                formula = selectedFormula,
                                restingHR = restingHRVal,
                                gender = selectedGender.ifBlank { null },
                                fitnessLevel = selectedFitnessLevel,
                                customMaxHR = customMaxHRVal
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCalculate(result)
                            calculationResult = result
                            showResultScreen = true
                        }
                    }
                )
            }

            // Clear all
            item {
                TextButton(
                    onClick = {
                        age = ""
                        restingHR = ""
                        customMaxHR = ""
                        selectedFormula = HeartRateFormula.STANDARD
                        selectedFitnessLevel = FitnessLevel.INTERMEDIATE
                        selectedGender = ""
                        ageError = null
                        restingHRError = null
                        customMaxHRError = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All")
                }
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Resting HR Guide Bottom Sheet
        if (showRestingHRGuide) {
            RestingHRGuideSheet(
                onDismiss = { showRestingHRGuide = false },
                onUseBPReading = {
                    showRestingHRGuide = false
                    onNavigateToBPChecker()
                },
                lastRestingHR = lastRestingHR
            )
        }
    }
}

// ============================================================
// HEADER CARD
// ============================================================

@Composable
private fun HeartRateHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated heart icon
            PulsingHeartIcon()

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Heart Rate Zones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Heart rate zones help you exercise at the right intensity for your goals — whether it's fat burning, endurance, or peak performance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PulsingHeartIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                1.15f at 150
                1f at 300
                1.1f at 450
                1f at 600
                1f at 1000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "heart_scale"
    )

    val heartColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFE53935),
        targetValue = Color(0xFFFF5252),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                Color(0xFFE53935) at 0
                Color(0xFFFF5252) at 150
                Color(0xFFE53935) at 300
                Color(0xFFFF5252) at 450
                Color(0xFFE53935) at 600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "heart_color"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(heartColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "❤️",
            fontSize = (28 * scale).sp
        )
    }
}

// ============================================================
// PROFILE DATA BANNER
// ============================================================

@Composable
private fun ProfileDataBanner(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Using profile data",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF2196F3).copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ============================================================
// AGE INPUT SECTION
// ============================================================

@Composable
private fun AgeInputSection(
    age: String,
    error: String?,
    onAgeChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Age",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = age,
            onValueChange = { if (it.length <= 3) onAgeChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter your age") },
            suffix = { Text("years") },
            leadingIcon = {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            isError = error != null,
            supportingText = error?.let {
                { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

// ============================================================
// GENDER SELECTION
// ============================================================

@Composable
private fun GenderSelectionSection(
    selectedGender: String,
    onGenderSelect: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Gender",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "(optional)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Male" to "♂️", "Female" to "♀️").forEach { (gender, icon) ->
                val isSelected = selectedGender == gender

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onGenderSelect(if (isSelected) "" else gender)
                    },
                    label = {
                        Text(
                            "$icon $gender",
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

// ============================================================
// FITNESS LEVEL SECTION
// ============================================================

@Composable
private fun FitnessLevelSection(
    selectedLevel: FitnessLevel,
    onLevelSelect: (FitnessLevel) -> Unit
) {
    Column {
        Text(
            text = "Fitness Level",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FitnessLevel.entries.forEach { level ->
                val isSelected = selectedLevel == level

                FilterChip(
                    selected = isSelected,
                    onClick = { onLevelSelect(level) },
                    label = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = level.emoji,
                                fontSize = 18.sp
                            )
                            Text(
                                text = level.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = selectedLevel.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

// ============================================================
// FORMULA SELECTION SECTION
// ============================================================

@Composable
private fun FormulaSelectionSection(
    selectedFormula: HeartRateFormula,
    selectedGender: String,
    onFormulaSelect: (HeartRateFormula) -> Unit
) {
    Column {
        Text(
            text = "Max HR Formula",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                HeartRateFormula.entries.forEach { formula ->
                    val isSelected = selectedFormula == formula
                    val isGulatiAndMale = formula == HeartRateFormula.GULATI && selectedGender == "Male"

                    FormulaOptionItem(
                        formula = formula,
                        isSelected = isSelected,
                        isDisabled = isGulatiAndMale,
                        onClick = {
                            if (!isGulatiAndMale) {
                                onFormulaSelect(formula)
                            }
                        }
                    )
                }
            }
        }

        // Gulati note for women
        AnimatedVisibility(
            visible = selectedFormula == HeartRateFormula.GULATI,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE91E63).copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("♀️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gulati formula was developed specifically for women and tends to be more accurate for female heart rate estimation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE91E63).copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaOptionItem(
    formula: HeartRateFormula,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "formula_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = !isDisabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { if (!isDisabled) onClick() },
            enabled = !isDisabled
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formula.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    else MaterialTheme.colorScheme.onSurface
                )
                if (formula.badge != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (formula.badge) {
                            "Most Used" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            "Most Personalized" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                            "Women" -> Color(0xFFE91E63).copy(alpha = 0.15f)
                            "Accurate 40+" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = formula.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when (formula.badge) {
                                "Most Used" -> Color(0xFF4CAF50)
                                "Most Personalized" -> Color(0xFF2196F3)
                                "Women" -> Color(0xFFE91E63)
                                "Accurate 40+" -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = formula.formulaText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            if (isDisabled) {
                Text(
                    text = "Designed for women only",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ============================================================
// RESTING HR SECTION
// ============================================================

@Composable
private fun RestingHRSection(
    restingHR: String,
    error: String?,
    isRequired: Boolean,
    onRestingHRChange: (String) -> Unit,
    onShowGuide: () -> Unit,
    onUseBPReading: () -> Unit,
    lastRestingHR: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRequired)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Resting Heart Rate",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (isRequired) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "*",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(required for Karvonen)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(optional, improves accuracy)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = restingHR,
                onValueChange = { if (it.length <= 3) onRestingHRChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Resting HR") },
                suffix = { Text("BPM") },
                leadingIcon = {
                    Icon(
                        Icons.Default.MonitorHeart,
                        contentDescription = null,
                        tint = Color(0xFFE53935)
                    )
                },
                isError = error != null,
                supportingText = error?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = onShowGuide,
                    label = { Text("How to measure", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Help,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )

                if (lastRestingHR != null) {
                    AssistChip(
                        onClick = { onRestingHRChange(lastRestingHR.toString()) },
                        label = {
                            Text(
                                "Use BP reading ($lastRestingHR)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFE53935)
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ============================================================
// RESTING HR OPTIONAL HINT
// ============================================================

@Composable
private fun RestingHROptionalHint(onAddRestingHR: () -> Unit) {
    TextButton(
        onClick = onAddRestingHR,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Add resting heart rate for more accurate zones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    }
}

// ============================================================
// CUSTOM MAX HR INPUT
// ============================================================

@Composable
private fun CustomMaxHRSection(
    customMaxHR: String,
    error: String?,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Your Known Max Heart Rate",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "*",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customMaxHR,
                onValueChange = { if (it.length <= 3) onValueChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Max Heart Rate") },
                suffix = { Text("BPM") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                },
                isError = error != null,
                supportingText = error?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "💡 Best determined through a supervised stress test or recent peak exercise effort.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                lineHeight = 16.sp
            )
        }
    }
}

// ============================================================
// CALCULATE BUTTON
// ============================================================

@Composable
private fun CalculateButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE53935),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFE53935).copy(alpha = 0.3f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Calculate Heart Rate Zones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// VALIDATION FUNCTIONS
// ============================================================

private fun validateAge(age: String): String? {
    if (age.isBlank()) return "Age is required"
    val ageVal = age.toIntOrNull() ?: return "Enter a valid age"
    return when {
        ageVal < 5 -> "Age must be at least 5 years"
        ageVal > 120 -> "Please enter a realistic age"
        else -> null
    }
}

private fun validateRestingHR(restingHR: String, isRequired: Boolean): String? {
    if (restingHR.isBlank()) {
        return if (isRequired) "Resting heart rate is required for Karvonen formula" else null
    }
    val hr = restingHR.toIntOrNull() ?: return "Enter a valid heart rate"
    return when {
        hr < 30 -> "Resting HR seems too low. Normal range: 40-100 BPM"
        hr > 120 -> "This seems high for a resting heart rate. Measure at rest."
        else -> null
    }
}

private fun validateCustomMaxHR(maxHR: String): String? {
    if (maxHR.isBlank()) return "Max heart rate is required for custom formula"
    val hr = maxHR.toIntOrNull() ?: return "Enter a valid heart rate"
    return when {
        hr < 100 -> "Max HR seems too low. Normal range: 150-220 BPM"
        hr > 250 -> "Max HR seems unrealistically high"
        else -> null
    }
}

private fun isInputValid(
    age: String,
    restingHR: String,
    customMaxHR: String,
    selectedFormula: HeartRateFormula,
    isRestingHRRequired: Boolean
): Boolean {
    if (age.isBlank() || age.toIntOrNull() == null) return false
    val ageVal = age.toInt()
    if (ageVal < 5 || ageVal > 120) return false

    if (isRestingHRRequired) {
        if (restingHR.isBlank() || restingHR.toIntOrNull() == null) return false
        val hr = restingHR.toInt()
        if (hr < 30 || hr > 120) return false
    }

    if (selectedFormula == HeartRateFormula.CUSTOM) {
        if (customMaxHR.isBlank() || customMaxHR.toIntOrNull() == null) return false
        val hr = customMaxHR.toInt()
        if (hr < 100 || hr > 250) return false
    }

    return true
}
