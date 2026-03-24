package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.BMIGoalData
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun BMIGoalSection(
    currentBMI: Float,
    currentWeight: Float,
    heightCm: Float,
    goalData: BMIGoalData,
    isUnitKg: Boolean,
    onSetGoal: (targetBMI: Float, targetWeight: Float) -> Unit,
    onClearGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalSetter by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🎯 BMI Goal Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (goalData.isGoalSet) {
                    IconButton(
                        onClick = { showGoalSetter = !showGoalSetter },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Goal",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (goalData.isGoalSet && !showGoalSetter) {
                // Show progress toward goal
                GoalProgressView(
                    goalData = goalData.copy(
                        currentBMI = currentBMI,
                        currentWeight = currentWeight,
                        heightCm = heightCm
                    ),
                    isUnitKg = isUnitKg,
                    onEditGoal = { showGoalSetter = true },
                    onClearGoal = onClearGoal
                )
            } else {
                // Show goal setter
                GoalSetterView(
                    currentBMI = currentBMI,
                    currentWeight = currentWeight,
                    heightCm = heightCm,
                    existingGoal = if (goalData.isGoalSet) goalData else null,
                    isUnitKg = isUnitKg,
                    onSetGoal = { targetBMI, targetWeight ->
                        onSetGoal(targetBMI, targetWeight)
                        showGoalSetter = false
                    },
                    onCancel = if (goalData.isGoalSet) {
                        { showGoalSetter = false }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun GoalSetterView(
    currentBMI: Float,
    currentWeight: Float,
    heightCm: Float,
    existingGoal: BMIGoalData?,
    isUnitKg: Boolean,
    onSetGoal: (targetBMI: Float, targetWeight: Float) -> Unit,
    onCancel: (() -> Unit)?
) {
    val defaultTargetBMI = BMIGoalData.NORMAL_BMI_MID
    var targetBMI by remember {
        mutableFloatStateOf(existingGoal?.targetBMI ?: defaultTargetBMI)
    }
    var useWeightMode by remember { mutableStateOf(false) }
    var targetWeightText by remember {
        mutableStateOf(
            existingGoal?.targetWeight?.let {
                if (isUnitKg) String.format("%.1f", it)
                else String.format("%.1f", it * 2.20462f)
            } ?: ""
        )
    }

    val calculatedTargetWeight = BMIGoalData.calculateTargetWeight(targetBMI, heightCm)
    val weightChange = if (useWeightMode) {
        val tw = targetWeightText.toFloatOrNull()?.let {
            if (isUnitKg) it else it / 2.20462f
        } ?: currentWeight
        tw - currentWeight
    } else {
        calculatedTargetWeight - currentWeight
    }

    val isLoss = weightChange < 0
    val absChange = abs(weightChange)

    Column {
        // Motivational message
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💪",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set a goal that feels right for you. Every step toward a healthier you counts!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle between BMI target and Weight target
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = !useWeightMode,
                onClick = { useWeightMode = false },
                label = { Text("Target BMI") },
                leadingIcon = if (!useWeightMode) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = useWeightMode,
                onClick = { useWeightMode = true },
                label = { Text("Target Weight") },
                leadingIcon = if (useWeightMode) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!useWeightMode) {
            // BMI Slider
            Text(
                text = "Target BMI",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Current target display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%.1f", targetBMI),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = getBMIColor(targetBMI)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "BMI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Suggested default note
            Text(
                text = "Suggested: ${BMIGoalData.NORMAL_BMI_MID} (middle of normal range)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = targetBMI,
                onValueChange = { targetBMI = (it * 10).roundToInt() / 10f },
                valueRange = 16f..35f,
                steps = 189,
                colors = SliderDefaults.colors(
                    thumbColor = getBMIColor(targetBMI),
                    activeTrackColor = getBMIColor(targetBMI)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Range labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("16.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Normal: 18.5-24.9", style = MaterialTheme.typography.bodySmall, color = healthyGreen, fontWeight = FontWeight.Medium)
                Text("35.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calculated target weight
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target weight:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isUnitKg) String.format("%.1f kg", calculatedTargetWeight)
                        else String.format("%.1f lbs", calculatedTargetWeight * 2.20462f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            // Weight input
            val weightUnit = if (isUnitKg) "kg" else "lbs"
            OutlinedTextField(
                value = targetWeightText,
                onValueChange = { targetWeightText = it },
                label = { Text("Target Weight ($weightUnit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            val targetWeightKg = targetWeightText.toFloatOrNull()?.let {
                if (isUnitKg) it else it / 2.20462f
            }

            if (targetWeightKg != null && targetWeightKg > 0 && heightCm > 0) {
                val correspondingBMI = BMIGoalData.calculateBMIFromWeight(targetWeightKg, heightCm)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Corresponding BMI:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.1f", correspondingBMI),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = getBMIColor(correspondingBMI)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight change needed
        if (absChange > 0.1f) {
            WeightChangeCard(
                weightChange = weightChange,
                isLoss = isLoss,
                absChange = absChange,
                isUnitKg = isUnitKg,
                heightCm = heightCm,
                targetBMI = if (useWeightMode) {
                    targetWeightText.toFloatOrNull()?.let {
                        val kg = if (isUnitKg) it else it / 2.20462f
                        BMIGoalData.calculateBMIFromWeight(kg, heightCm)
                    } ?: targetBMI
                } else targetBMI
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeline estimate
        if (absChange > 0.1f) {
            TimelineEstimateCard(
                absChange = absChange,
                isLoss = isLoss
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onCancel != null) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
            Button(
                onClick = {
                    val finalTargetBMI: Float
                    val finalTargetWeight: Float
                    if (useWeightMode) {
                        val twKg = targetWeightText.toFloatOrNull()?.let {
                            if (isUnitKg) it else it / 2.20462f
                        } ?: return@Button
                        finalTargetWeight = twKg
                        finalTargetBMI = BMIGoalData.calculateBMIFromWeight(twKg, heightCm)
                    } else {
                        finalTargetBMI = targetBMI
                        finalTargetWeight = calculatedTargetWeight
                    }
                    onSetGoal(finalTargetBMI, finalTargetWeight)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Filled.Flag,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (existingGoal != null) "Update Goal" else "Set My Goal",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WeightChangeCard(
    weightChange: Float,
    isLoss: Boolean,
    absChange: Float,
    isUnitKg: Boolean,
    heightCm: Float,
    targetBMI: Float
) {
    val accentColor = if (isLoss) Color(0xFF2196F3) else Color(0xFF4CAF50)
    val emoji = if (isLoss) "📉" else "📈"
    val action = if (isLoss) "Lose" else "Gain"
    val displayWeight = if (isUnitKg) absChange else absChange * 2.20462f
    val unit = if (isUnitKg) "kg" else "lbs"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$action ${String.format("%.1f", displayWeight)} $unit to reach BMI ${String.format("%.1f", targetBMI)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
private fun TimelineEstimateCard(
    absChange: Float,
    isLoss: Boolean
) {
    val rateMin = if (isLoss) 0.5f else 0.25f
    val rateMax = if (isLoss) 1.0f else 0.5f
    val weeksMin = kotlin.math.ceil(absChange / rateMax.toDouble()).toInt()
    val weeksMax = kotlin.math.ceil(absChange / rateMin.toDouble()).toInt()

    val timeMinText = formatWeeksToReadable(weeksMin)
    val timeMaxText = formatWeeksToReadable(weeksMax)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📅", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Estimated Timeline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimelineItem(
                    label = "Faster pace",
                    time = timeMinText,
                    rate = "${String.format("%.1f", rateMax)} kg/week",
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                TimelineItem(
                    label = "Steady pace",
                    time = timeMaxText,
                    rate = "${String.format("%.2f", rateMin)} kg/week",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Safe rate note
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isLoss) "Safe weight loss rate: 0.5-1 kg per week"
                    else "Safe weight gain rate: 0.25-0.5 kg per week",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    label: String,
    time: String,
    rate: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = rate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatWeeksToReadable(weeks: Int): String {
    return when {
        weeks < 1 -> "< 1 week"
        weeks == 1 -> "1 week"
        weeks < 4 -> "$weeks weeks"
        weeks < 8 -> {
            val months = weeks / 4
            val remainWeeks = weeks % 4
            if (remainWeeks == 0) "$months month${if (months > 1) "s" else ""}"
            else "$months month${if (months > 1) "s" else ""} $remainWeeks wk${if (remainWeeks > 1) "s" else ""}"
        }
        else -> {
            val months = weeks / 4
            "$months month${if (months > 1) "s" else ""}"
        }
    }
}

@Composable
fun GoalProgressView(
    goalData: BMIGoalData,
    isUnitKg: Boolean,
    onEditGoal: () -> Unit,
    onClearGoal: () -> Unit
) {
    val updatedGoal = goalData.copy(
        currentBMI = goalData.currentBMI,
        currentWeight = goalData.currentWeight
    )

    val progressAnimatable = remember { Animatable(0f) }
    LaunchedEffect(updatedGoal.progressPercentage) {
        progressAnimatable.animateTo(
            targetValue = updatedGoal.progressPercentage,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Column {
        if (updatedGoal.isGoalReached) {
            // 🎉 Celebration!
            GoalReachedCelebration()
        } else {
            // Progress display
            ProgressCircle(
                progress = progressAnimatable.value,
                currentBMI = updatedGoal.currentBMI,
                targetBMI = updatedGoal.targetBMI
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Remaining distance
            val displayRemaining = if (isUnitKg) updatedGoal.remainingWeight
            else updatedGoal.remainingWeight * 2.20462f
            val unit = if (isUnitKg) "kg" else "lbs"
            val action = if (updatedGoal.isWeightLoss) "lose" else "gain"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You're ${String.format("%.1f", displayRemaining)} $unit away from your goal!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getMotivationalMessage(updatedGoal.progressPercentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressStatItem(
                    label = "Start",
                    value = String.format("%.1f", updatedGoal.startingBMI),
                    subValue = "BMI"
                )
                ProgressStatItem(
                    label = "Current",
                    value = String.format("%.1f", updatedGoal.currentBMI),
                    subValue = "BMI",
                    isHighlighted = true
                )
                ProgressStatItem(
                    label = "Target",
                    value = String.format("%.1f", updatedGoal.targetBMI),
                    subValue = "BMI"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Linear progress bar
            AnimatedProgressBar(
                progress = progressAnimatable.value / 100f,
                startBMI = updatedGoal.startingBMI,
                currentBMI = updatedGoal.currentBMI,
                targetBMI = updatedGoal.targetBMI
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClearGoal) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Clear Goal",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ProgressCircle(
    progress: Float,
    currentBMI: Float,
    targetBMI: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = when {
        progress >= 75f -> healthyGreen
        progress >= 50f -> Color(0xFF2196F3)
        progress >= 25f -> Color(0xFFFFA726)
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(160.dp)
        ) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            val sweepAngle = (progress / 100f) * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
        }

        // Center content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${progress.roundToInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
            Text(
                text = "progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnimatedProgressBar(
    progress: Float,
    startBMI: Float,
    currentBMI: Float,
    targetBMI: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.7f),
                                primaryColor
                            )
                        )
                    )
            )

            // Progress text on bar
            if (progress > 0.15f) {
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Start: ${String.format("%.1f", startBMI)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Goal: ${String.format("%.1f", targetBMI)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressStatItem(
    label: String,
    value: String,
    subValue: String,
    isHighlighted: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subValue,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GoalReachedCelebration() {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = healthyGreen.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = healthyGreen.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉🏆🎉",
                fontSize = (32 * scale).sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Congratulations!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = healthyGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You've reached your BMI goal!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Amazing work! Keep maintaining your healthy lifestyle. You can set a new goal anytime.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getMotivationalMessage(progress: Float): String {
    return when {
        progress >= 90f -> "Almost there! You're doing incredible! 🔥"
        progress >= 75f -> "Fantastic progress! Keep up the amazing work! 💪"
        progress >= 50f -> "Halfway there! You're crushing it! ⭐"
        progress >= 25f -> "Great start! Consistency is key! 🌟"
        progress >= 10f -> "You're on your way! Every small step counts! 🚶"
        progress > 0f -> "The journey has begun! Stay committed! 💫"
        else -> "Track regularly to see your progress! 📊"
    }
}

@Composable
private fun getBMIColor(bmi: Float): Color {
    return when {
        bmi < 16f -> Color(0xFFB71C1C)
        bmi < 17f -> Color(0xFFD32F2F)
        bmi < 18.5f -> Color(0xFFFF9800)
        bmi < 25f -> healthyGreen
        bmi < 30f -> Color(0xFFFFC107)
        bmi < 35f -> Color(0xFFFF9800)
        bmi < 40f -> Color(0xFFD32F2F)
        else -> Color(0xFFB71C1C)
    }
}

// Reference the app's existing healthy green
private val healthyGreen = Color(0xFF4CAF50)
