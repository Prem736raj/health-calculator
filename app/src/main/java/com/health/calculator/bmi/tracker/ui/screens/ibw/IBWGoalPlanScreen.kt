package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.IBWGoal
import com.health.calculator.bmi.tracker.data.model.IBWResult
import com.health.calculator.bmi.tracker.data.model.WeightPaceOption
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun IBWGoalPlanScreen(
    result: IBWResult,
    showInKg: Boolean,
    existingGoal: IBWGoal?,
    paceOptions: List<WeightPaceOption>,
    onGoalSelected: (targetKg: Double, source: String) -> Unit,
    onPaceSelected: (String) -> Unit,
    onSaveGoal: () -> Unit,
    onClearGoal: () -> Unit,
    onNavigateToBMR: ((Int) -> Unit)? = null,
    selectedGoalSource: String?,
    selectedGoalWeightKg: Double?,
    selectedPace: String
) {
    val scrollState = rememberScrollState()
    val conversionFactor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "🎯 Weight Goal Plan",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Existing goal progress (if any)
        existingGoal?.let { goal ->
            if (goal.isActive && result.currentWeightKg != null) {
                ExistingGoalProgressCard(
                    goal = goal,
                    currentWeightKg = result.currentWeightKg,
                    showInKg = showInKg,
                    onClearGoal = onClearGoal
                )
            }
        }

        // Current weight display
        if (result.currentWeightKg != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Current Weight",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${"%.1f".format(result.currentWeightKg * conversionFactor)} $unit",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Goal Weight Selector
        Text(
            text = "Select Your Target Weight",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        GoalOptionsList(
            result = result,
            showInKg = showInKg,
            selectedSource = selectedGoalSource,
            onGoalSelected = onGoalSelected
        )

        // Weight Change Plan (visible when goal is selected and current weight exists)
        if (selectedGoalWeightKg != null && result.currentWeightKg != null) {
            val diff = abs(result.currentWeightKg - selectedGoalWeightKg)
            if (diff > 0.5) {
                Spacer(modifier = Modifier.height(8.dp))

                WeightChangePlanSection(
                    paceOptions = paceOptions,
                    selectedPace = selectedPace,
                    showInKg = showInKg,
                    isWeightLoss = result.currentWeightKg > selectedGoalWeightKg,
                    dateFormat = dateFormat,
                    onPaceSelected = onPaceSelected
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save Goal Button
                Button(
                    onClick = onSaveGoal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save Goal & Start Tracking",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Link to BMR Calculator
                onNavigateToBMR?.let { navigate ->
                    val selectedPaceOption = paceOptions.find { it.name == selectedPace }
                    selectedPaceOption?.let { pace ->
                        OutlinedButton(
                            onClick = { navigate(pace.dailyCalorieAdjustment) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculate daily calories for this goal")
                        }
                    }
                }
            } else {
                // Already at ideal weight
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎉", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "You're already at your ideal weight!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                "Focus on maintaining your current healthy weight.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // No current weight entered
        if (result.currentWeightKg == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Enter your current weight in the calculator to create a personalized goal plan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GoalOptionsList(
    result: IBWResult,
    showInKg: Boolean,
    selectedSource: String?,
    onGoalSelected: (targetKg: Double, source: String) -> Unit
) {
    val conversionFactor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"

    var showCustomInput by remember { mutableStateOf(false) }
    var customWeight by remember { mutableStateOf("") }

    val bmiMidKg = (result.bmiLowerKg + result.bmiUpperKg) / 2.0

    val options = listOf(
        Triple("Devine Formula", result.frameAdjustedDevineKg, "Primary recommendation"),
        Triple("Robinson Formula", result.robinsonKg, "Alternative calculation"),
        Triple("Average of All", result.averageKg, "Average across formulas"),
        Triple("BMI Range Middle", bmiMidKg, "Middle of healthy BMI range")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (source, weightKg, description) ->
            val isSelected = selectedSource == source
            val displayWeight = weightKg * conversionFactor

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGoalSelected(weightKg, source) }
                    .then(
                        if (isSelected) Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(16.dp)
                        ) else Modifier
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onGoalSelected(weightKg, source) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = source,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Text(
                        text = "${"%.1f".format(displayWeight)} $unit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Custom option
        val isCustomSelected = selectedSource == "Custom"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCustomInput = true }
                .then(
                    if (isCustomSelected) Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    ) else Modifier
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCustomSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isCustomSelected) 4.dp else 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isCustomSelected,
                        onClick = { showCustomInput = true }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Custom Target",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        Text(
                            text = "Enter your own target weight",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                AnimatedVisibility(visible = showCustomInput || isCustomSelected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customWeight,
                            onValueChange = { customWeight = it },
                            label = { Text("Target ($unit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        FilledTonalButton(
                            onClick = {
                                val value = customWeight.toDoubleOrNull() ?: return@FilledTonalButton
                                val weightKg = if (showInKg) value else value / 2.20462
                                onGoalSelected(weightKg, "Custom")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Set")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightChangePlanSection(
    paceOptions: List<WeightPaceOption>,
    selectedPace: String,
    showInKg: Boolean,
    isWeightLoss: Boolean,
    dateFormat: SimpleDateFormat,
    onPaceSelected: (String) -> Unit
) {
    val unit = if (showInKg) "kg" else "lbs"
    val conversionFactor = if (showInKg) 1.0 else 2.20462

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isWeightLoss) "Weight Loss Plan" else "Weight Gain Plan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            paceOptions.forEach { option ->
                val isSelected = selectedPace == option.name
                val weeklyDisplay = abs(option.weeklyChangeKg * conversionFactor)
                val action = if (isWeightLoss) "deficit" else "surplus"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPaceSelected(option.name) }
                        .then(
                            if (isSelected) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(16.dp)
                            ) else Modifier
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onPaceSelected(option.name) }
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = option.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                        if (option.name == "Moderate") {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF4CAF50)
                                            ) {
                                                Text(
                                                    "RECOMMENDED",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(
                                                        horizontal = 6.dp,
                                                        vertical = 2.dp
                                                    ),
                                                    fontSize = 8.sp
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isSelected) {
                            Column(
                                modifier = Modifier.padding(start = 48.dp, top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DetailRow(
                                    icon = Icons.Default.Speed,
                                    label = "Rate",
                                    value = "${"%.2f".format(weeklyDisplay)} $unit/week"
                                )
                                DetailRow(
                                    icon = Icons.Default.LocalFireDepartment,
                                    label = "Daily $action",
                                    value = "${abs(option.dailyCalorieAdjustment)} kcal/day"
                                )
                                DetailRow(
                                    icon = Icons.Default.Timer,
                                    label = "Duration",
                                    value = formatDuration(option.estimatedWeeks)
                                )
                                DetailRow(
                                    icon = Icons.Default.CalendarMonth,
                                    label = "Target date",
                                    value = dateFormat.format(Date(option.estimatedDate))
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "At this pace, you'll reach your goal by ${dateFormat.format(Date(option.estimatedDate))}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(8.dp)
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
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ExistingGoalProgressCard(
    goal: IBWGoal,
    currentWeightKg: Double,
    showInKg: Boolean,
    onClearGoal: () -> Unit
) {
    val conversionFactor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"
    val progress = goal.progressPercent(currentWeightKg)
    val remaining = goal.remainingKg(currentWeightKg) * conversionFactor
    val isReached = goal.isGoalReached(currentWeightKg)

    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Milestone celebration
    var showCelebration by remember { mutableStateOf(false) }
    val milestone = when {
        progress >= 100f -> 100
        progress >= 75f -> 75
        progress >= 50f -> 50
        progress >= 25f -> 25
        else -> null
    }

    LaunchedEffect(milestone) {
        if (milestone != null && milestone >= 25) {
            showCelebration = true
            kotlinx.coroutines.delay(3000)
            showCelebration = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReached)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isReached) "🎉 Goal Reached!" else "📊 Your Goal Progress",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isReached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onClearGoal, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear goal",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circular progress
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                val progressColor = when {
                    isReached -> Color(0xFF4CAF50)
                    progress >= 75 -> Color(0xFF8BC34A)
                    progress >= 50 -> MaterialTheme.colorScheme.primary
                    progress >= 25 -> Color(0xFFFF9800)
                    else -> Color(0xFFFF5722)
                }
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    )
                    val arcSize = Size(radius * 2, radius * 2)

                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${"%.0f".format(progress)}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "complete",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeightInfoColumn(
                    label = "Start",
                    value = "${"%.1f".format(goal.startWeightKg * conversionFactor)} $unit"
                )
                WeightInfoColumn(
                    label = "Current",
                    value = "${"%.1f".format(currentWeightKg * conversionFactor)} $unit"
                )
                WeightInfoColumn(
                    label = "Target",
                    value = "${"%.1f".format(goal.targetWeightKg * conversionFactor)} $unit"
                )
            }

            if (!isReached) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "${"%.1f".format(remaining)} $unit remaining to reach your goal",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Milestone celebration overlay
            AnimatedVisibility(
                visible = showCelebration,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = when (milestone) {
                            25 -> "🏅 25% milestone reached! Keep going!"
                            50 -> "🥈 Halfway there! You're doing amazing!"
                            75 -> "🥇 75% done! The finish line is close!"
                            100 -> "🏆 GOAL REACHED! Incredible achievement!"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        color = Color(0xFF795548)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightInfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDuration(weeks: Int): String {
    return when {
        weeks < 4 -> "$weeks week${if (weeks != 1) "s" else ""}"
        weeks < 52 -> {
            val months = weeks / 4
            val remainingWeeks = weeks % 4
            if (remainingWeeks > 0) "$months month${if (months != 1) "s" else ""}, $remainingWeeks week${if (remainingWeeks != 1) "s" else ""}"
            else "$months month${if (months != 1) "s" else ""}"
        }
        else -> {
            val years = weeks / 52
            val remainingMonths = (weeks % 52) / 4
            if (remainingMonths > 0) "$years year${if (years != 1) "s" else ""}, $remainingMonths month${if (remainingMonths != 1) "s" else ""}"
            else "$years year${if (years != 1) "s" else ""}"
        }
    }
}
