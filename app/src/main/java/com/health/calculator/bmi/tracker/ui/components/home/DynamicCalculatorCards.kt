package com.health.calculator.bmi.tracker.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// BASE CALCULATOR CARD
// ============================================================

@Composable
fun DynamicCalculatorCard(
    emoji: String,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasData: Boolean = false,
    dataContent: @Composable ColumnScope.() -> Unit = {},
    needsAttention: Boolean = false,
    attentionMessage: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    progressRing: (@Composable BoxScope.() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.18f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji/Progress ring container
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                if (progressRing != null) {
                    progressRing()
                } else {
                    // Default emoji background
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.28f),
                                        accentColor.copy(alpha = 0.08f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }

                // Attention indicator
                if (needsAttention) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF44336)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (hasData) {
                    dataContent()
                } else {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                // Attention message
                if (needsAttention && attentionMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF44336))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = attentionMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF44336),
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Chevron
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// ============================================================
// 1. BMI CALCULATOR CARD
// ============================================================

@Composable
fun BMICalculatorCard(
    lastBMI: Float?,
    lastCategory: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = lastBMI != null
    val categoryColor = remember(lastBMI) {
        when {
            lastBMI == null -> Color(0xFF2196F3)
            lastBMI < 18.5f -> Color(0xFFFF9800)
            lastBMI < 25f -> Color(0xFF4CAF50)
            lastBMI < 30f -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }
    }
    val needsAttention = lastBMI != null && (lastBMI < 18.5f || lastBMI >= 25f)

    DynamicCalculatorCard(
        emoji = "📊",
        title = "BMI Calculator",
        description = "Calculate your Body Mass Index",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = categoryColor,
        needsAttention = needsAttention,
        attentionMessage = if (needsAttention) {
            when {
                lastBMI!! < 18.5f -> "Underweight"
                lastBMI >= 30f -> "Obese range"
                else -> "Overweight range"
            }
        } else null,
        dataContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // BMI value
                Text(
                    text = "%.1f".format(lastBMI),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = categoryColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Category
                lastCategory?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    )
}

// ============================================================
// 2. BMR CALCULATOR CARD
// ============================================================

@Composable
fun BMRCalculatorCard(
    lastBMR: Int?,
    lastTDEE: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = lastBMR != null

    DynamicCalculatorCard(
        emoji = "🔥",
        title = "BMR Calculator",
        description = "Calculate your Basal Metabolic Rate",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = Color(0xFFFF9800),
        dataContent = {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%,d".format(lastBMR),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = " cal/day",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            if (lastTDEE != null) {
                Text(
                    text = "TDEE: %,d cal".format(lastTDEE),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }
        }
    )
}

// ============================================================
// 3. BLOOD PRESSURE CARD
// ============================================================

@Composable
fun BloodPressureCard(
    lastSystolic: Int?,
    lastDiastolic: Int?,
    lastCategory: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = lastSystolic != null && lastDiastolic != null
    val categoryColor = remember(lastSystolic, lastDiastolic) {
        when {
            lastSystolic == null || lastDiastolic == null -> Color(0xFFE53935)
            lastSystolic < 120 && lastDiastolic < 80 -> Color(0xFF4CAF50)
            lastSystolic < 130 && lastDiastolic < 85 -> Color(0xFF8BC34A)
            lastSystolic < 140 && lastDiastolic < 90 -> Color(0xFFFFC107)
            lastSystolic < 160 && lastDiastolic < 100 -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }
    }
    val needsAttention = lastSystolic != null && lastDiastolic != null && 
            (lastSystolic >= 140 || lastDiastolic >= 90)

    DynamicCalculatorCard(
        emoji = "💓",
        title = "Blood Pressure",
        description = "Check your blood pressure category",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = categoryColor,
        needsAttention = needsAttention,
        attentionMessage = if (needsAttention) "Elevated reading" else null,
        dataContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$lastSystolic/$lastDiastolic",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = categoryColor
                )
                Text(
                    text = " mmHg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            lastCategory?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    )
}

// ============================================================
// 4. WHR CARD
// ============================================================

@Composable
fun WHRCalculatorCard(
    lastWHR: Float?,
    lastCategory: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = lastWHR != null
    val riskColor = remember(lastWHR) {
        when {
            lastWHR == null -> Color(0xFF9C27B0)
            lastWHR < 0.85f -> Color(0xFF4CAF50)
            lastWHR < 0.90f -> Color(0xFFFFC107)
            lastWHR < 0.95f -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }
    }
    val needsAttention = lastWHR != null && lastWHR >= 0.90f

    DynamicCalculatorCard(
        emoji = "📏",
        title = "Waist-to-Hip Ratio",
        description = "Assess your body fat distribution",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = riskColor,
        needsAttention = needsAttention,
        attentionMessage = if (needsAttention) "High risk range" else null,
        dataContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(riskColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "%.2f".format(lastWHR),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = riskColor
                )
            }
            lastCategory?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = riskColor.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    )
}

// ============================================================
// 5. WATER INTAKE CARD (with progress ring)
// ============================================================

@Composable
fun WaterIntakeCard(
    currentIntake: Int,
    goalIntake: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = goalIntake > 0
    val progress = if (goalIntake > 0) (currentIntake.toFloat() / goalIntake).coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()
    
    val progressColor = when {
        progress >= 1f -> Color(0xFF4CAF50)
        progress >= 0.7f -> Color(0xFF2196F3)
        progress >= 0.4f -> Color(0xFF03A9F4)
        else -> Color(0xFF03A9F4).copy(alpha = 0.6f)
    }
    
    val needsAttention = hasData && progress < 0.5f && 
            java.time.LocalTime.now().hour >= 14 // After 2 PM

    DynamicCalculatorCard(
        emoji = "💧",
        title = "Water Intake",
        description = "Track your daily hydration",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = progressColor,
        needsAttention = needsAttention,
        attentionMessage = if (needsAttention) "Behind on hydration" else null,
        progressRing = {
            WaterProgressRing(
                progress = progress,
                color = progressColor
            )
        },
        dataContent = {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = progressColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "of goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "${currentIntake}ml / ${goalIntake}ml",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    )
}

@Composable
private fun BoxScope.WaterProgressRing(
    progress: Float,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "water_progress"
    )

    Canvas(modifier = Modifier.size(52.dp)) {
        val strokeWidth = 5.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        // Background circle
        drawCircle(
            color = color.copy(alpha = 0.12f),
            radius = radius,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = animatedProgress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }

    // Emoji in center
    Text(
        text = if (progress >= 1f) "✅" else "💧",
        fontSize = 18.sp,
        modifier = Modifier.align(Alignment.Center)
    )
}

// ============================================================
// 6. METABOLIC SYNDROME CARD
// ============================================================

@Composable
fun MetabolicSyndromeCard(
    criteriaMet: Int?,
    totalCriteria: Int = 5,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = criteriaMet != null
    val riskColor = remember(criteriaMet) {
        when (criteriaMet) {
            null -> Color(0xFF9C27B0)
            0 -> Color(0xFF4CAF50)
            1, 2 -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }
    }
    val needsAttention = criteriaMet != null && criteriaMet >= 3

    DynamicCalculatorCard(
        emoji = "🏥",
        title = "Metabolic Syndrome",
        description = "Assess your metabolic health risk",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = riskColor,
        needsAttention = needsAttention,
        attentionMessage = if (needsAttention) "Metabolic syndrome present" else null,
        dataContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(riskColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$criteriaMet/$totalCriteria",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = riskColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "criteria met",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = when (criteriaMet) {
                    0 -> "No risk factors"
                    1, 2 -> "Borderline risk"
                    else -> "High risk"
                },
                style = MaterialTheme.typography.labelSmall,
                color = riskColor.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    )
}

// ============================================================
// 7. BSA CARD
// ============================================================

@Composable
fun BSACalculatorCard(
    lastBSA: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = lastBSA != null

    DynamicCalculatorCard(
        emoji = "📐",
        title = "Body Surface Area",
        description = "Calculate your body surface area",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = Color(0xFF607D8B),
        dataContent = {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.2f".format(lastBSA),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF607D8B)
                )
                Text(
                    text = " m²",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    )
}

// ============================================================
// 8. IBW CARD
// ============================================================

@Composable
fun IBWCalculatorCard(
    idealWeight: Float?,
    currentWeight: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = idealWeight != null
    val difference = if (idealWeight != null && currentWeight != null) {
        currentWeight - idealWeight
    } else null
    
    val statusColor = remember(difference) {
        when {
            difference == null -> Color(0xFF4CAF50)
            kotlin.math.abs(difference) <= 2f -> Color(0xFF4CAF50)
            kotlin.math.abs(difference) <= 5f -> Color(0xFFFFC107)
            else -> Color(0xFFFF9800)
        }
    }

    DynamicCalculatorCard(
        emoji = "⚖️",
        title = "Ideal Body Weight",
        description = "Find your ideal weight range",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = statusColor,
        dataContent = {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.1f".format(idealWeight),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
                Text(
                    text = " kg ideal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            if (difference != null && currentWeight != null) {
                val diffText = when {
                    kotlin.math.abs(difference) < 0.5f -> "At ideal weight!"
                    difference > 0 -> "+%.1f kg to lose".format(difference)
                    else -> "%.1f kg to gain".format(kotlin.math.abs(difference))
                }
                Text(
                    text = diffText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    )
}

// ============================================================
// 9. CALORIE CARD (with progress ring)
// ============================================================

@Composable
fun CalorieCalculatorCard(
    consumedCalories: Int,
    targetCalories: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = targetCalories > 0
    val progress = if (targetCalories > 0) 
        (consumedCalories.toFloat() / targetCalories).coerceIn(0f, 1.5f) else 0f
    val remaining = targetCalories - consumedCalories
    
    val progressColor = when {
        progress >= 1.2f -> Color(0xFFF44336) // Over by 20%+
        progress >= 1f -> Color(0xFF4CAF50) // At goal
        progress >= 0.7f -> Color(0xFF8BC34A) // Close
        else -> Color(0xFFFF9800) // Under
    }
    
    val needsAttention = hasData && remaining > 500 && 
            java.time.LocalTime.now().hour >= 18 // After 6 PM

    DynamicCalculatorCard(
        emoji = "🔥",
        title = "Daily Calories",
        description = "Track your daily calorie intake",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = progressColor,
        needsAttention = needsAttention,
        attentionMessage = if (needsAttention) "${remaining} cal remaining" else null,
        progressRing = if (hasData) {
            {
                CalorieProgressRing(
                    progress = progress.coerceAtMost(1f),
                    color = progressColor
                )
            }
        } else null,
        dataContent = {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%,d".format(consumedCalories),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = progressColor
                )
                Text(
                    text = " / %,d".format(targetCalories),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = when {
                    remaining > 0 -> "$remaining cal remaining"
                    remaining == 0 -> "Goal reached!"
                    else -> "${-remaining} cal over"
                },
                style = MaterialTheme.typography.labelSmall,
                color = progressColor.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    )
}

@Composable
private fun BoxScope.CalorieProgressRing(
    progress: Float,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "calorie_progress"
    )

    Canvas(modifier = Modifier.size(52.dp)) {
        val strokeWidth = 5.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        drawCircle(
            color = color.copy(alpha = 0.12f),
            radius = radius,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = animatedProgress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }

    Text(
        text = "🔥",
        fontSize = 18.sp,
        modifier = Modifier.align(Alignment.Center)
    )
}

// ============================================================
// 10. HEART RATE ZONES CARD
// ============================================================

@Composable
fun HeartRateZonesCard(
    maxHR: Int?,
    restingHR: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = maxHR != null

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "hr_pulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                1f at 0
                1.1f at 100
                1f at 250
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "heart_scale"
    )

    DynamicCalculatorCard(
        emoji = "❤️",
        title = "Heart Rate Zones",
        description = "Optimize your training intensity",
        onClick = onClick,
        modifier = modifier,
        hasData = hasData,
        accentColor = Color(0xFFE53935),
        progressRing = if (hasData) {
            {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE53935).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "❤️",
                        fontSize = (22 * heartScale).sp
                    )
                }
            }
        } else null,
        dataContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Max: $maxHR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE53935)
                )
                Text(
                    text = " BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            if (restingHR != null) {
                Text(
                    text = "Resting: $restingHR BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }
        }
    )
}
