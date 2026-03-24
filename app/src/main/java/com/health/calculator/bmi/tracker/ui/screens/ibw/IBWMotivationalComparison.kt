package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.IBWResult
import kotlin.math.abs

@Composable
fun IBWMotivationalComparison(
    result: IBWResult,
    showInKg: Boolean
) {
    val factor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"
    val currentWeight = (result.currentWeightKg ?: return) * factor
    val idealWeight = result.frameAdjustedDevineKg * factor
    val bmiLower = result.bmiLowerKg * factor
    val bmiUpper = result.bmiUpperKg * factor
    val diff = currentWeight - idealWeight
    val absDiff = abs(diff)
    val isAbove = diff > 0.5
    val isBelow = diff < -0.5
    val isInRange = currentWeight in bmiLower..bmiUpper

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Weight Journey",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animated visual bar with 3 markers
            WeightRangeVisual(
                currentWeight = currentWeight,
                idealWeight = idealWeight,
                bmiLower = bmiLower,
                bmiUpper = bmiUpper,
                unit = unit
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Three weight circles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeightBubble(
                    label = "Current",
                    weight = "${"%.1f".format(currentWeight)}",
                    unit = unit,
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Person
                )
                WeightBubble(
                    label = "Ideal",
                    weight = "${"%.1f".format(idealWeight)}",
                    unit = unit,
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.Star
                )
                WeightBubble(
                    label = "Range",
                    weight = "${"%.0f".format(bmiLower)}-${"%.0f".format(bmiUpper)}",
                    unit = unit,
                    color = Color(0xFF2196F3),
                    icon = Icons.Default.FitnessCenter
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Motivational message
            MotivationalMessageCard(
                isInRange = isInRange,
                isAbove = isAbove,
                isBelow = isBelow,
                absDiff = absDiff,
                unit = unit
            )
        }
    }
}

@Composable
private fun WeightRangeVisual(
    currentWeight: Double,
    idealWeight: Double,
    bmiLower: Double,
    bmiUpper: Double,
    unit: String
) {
    val allValues = listOf(currentWeight, idealWeight, bmiLower, bmiUpper)
    val minVal = (allValues.minOrNull()!! * 0.9).toFloat()
    val maxVal = (allValues.maxOrNull()!! * 1.1).toFloat()
    val range = maxVal - minVal

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, 300, easing = FastOutSlowInEasing),
        label = "rangeProgress"
    )

    val healthyColor = Color(0xFF4CAF50)
    val currentColor = MaterialTheme.colorScheme.primary
    val idealColor = Color(0xFFFF9800)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barY = size.height * 0.45f
            val barHeight = 14.dp.toPx()
            val markerRadius = 10.dp.toPx()
            val padding = 24.dp.toPx()
            val barWidth = size.width - padding * 2

            // Track background
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(padding, barY - barHeight / 2),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2)
            )

            // Healthy range zone
            val lowerX = padding + ((bmiLower.toFloat() - minVal) / range * barWidth)
            val upperX = padding + ((bmiUpper.toFloat() - minVal) / range * barWidth)
            val healthyWidth = (upperX - lowerX).coerceAtLeast(0f)

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        healthyColor.copy(alpha = 0.2f),
                        healthyColor.copy(alpha = 0.35f),
                        healthyColor.copy(alpha = 0.2f)
                    ),
                    startX = lowerX,
                    endX = upperX
                ),
                topLeft = Offset(lowerX, barY - barHeight / 2),
                size = Size(healthyWidth * animatedProgress, barHeight),
                cornerRadius = CornerRadius(barHeight / 2)
            )

            // Ideal weight marker (diamond)
            val idealX = padding + ((idealWeight.toFloat() - minVal) / range * barWidth) * animatedProgress
            val diamondSize = 8.dp.toPx()
            val diamondPath = Path().apply {
                moveTo(idealX, barY - diamondSize)
                lineTo(idealX + diamondSize, barY)
                lineTo(idealX, barY + diamondSize)
                lineTo(idealX - diamondSize, barY)
                close()
            }
            drawPath(diamondPath, color = idealColor)
            drawPath(diamondPath, color = Color.White, style = Stroke(width = 2.dp.toPx()))

            // Current weight marker (circle with ring)
            val currentX = padding + ((currentWeight.toFloat() - minVal) / range * barWidth) * animatedProgress
            drawCircle(
                color = Color.White,
                radius = markerRadius + 2.dp.toPx(),
                center = Offset(currentX, barY)
            )
            drawCircle(
                color = currentColor,
                radius = markerRadius,
                center = Offset(currentX, barY)
            )
            drawCircle(
                color = Color.White,
                radius = markerRadius * 0.45f,
                center = Offset(currentX, barY)
            )
        }
    }

    // Labels below
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "${"%.0f".format(minVal)} $unit",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            fontSize = 9.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.4f))
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                "Healthy Range",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4CAF50),
                fontSize = 9.sp
            )
        }
        Text(
            "${"%.0f".format(maxVal)} $unit",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            fontSize = 9.sp
        )
    }
}

@Composable
private fun WeightBubble(
    label: String,
    weight: String,
    unit: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bubbleScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size((64 * scale).dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = weight,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = color
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            fontSize = 8.sp
        )
    }
}

@Composable
private fun MotivationalMessageCard(
    isInRange: Boolean,
    isAbove: Boolean,
    isBelow: Boolean,
    absDiff: Double,
    unit: String
) {
    val (emoji, title, message, color) = when {
        isInRange && absDiff < 2 -> listOf(
            "🌟",
            "Amazing! You're at your ideal weight!",
            "You're right where you should be. Keep up your healthy habits and focus on maintaining this great balance!",
            Color(0xFF4CAF50)
        )
        isInRange -> listOf(
            "✅",
            "Great! You're within the healthy range",
            "You're within the healthy BMI weight range even if not at the exact formula result. This is a great place to be!",
            Color(0xFF4CAF50)
        )
        isAbove && absDiff < 5 -> listOf(
            "💪",
            "Almost there! Just ${"%.1f".format(absDiff)} $unit to go",
            "You're so close to your ideal range! Small, consistent changes in diet and activity can get you there. You've got this!",
            Color(0xFF2196F3)
        )
        isAbove && absDiff < 15 -> listOf(
            "🚀",
            "A reachable goal ahead",
            "${"%.1f".format(absDiff)} $unit may seem like a lot, but at a healthy pace of 0.5 $unit/week, you could reach your ideal in about ${(absDiff / 0.5).toInt()} weeks. One step at a time!",
            Color(0xFFFF9800)
        )
        isAbove -> listOf(
            "🎯",
            "Your journey starts here",
            "Every journey begins with a first step. Focus on small, sustainable changes. Even losing 5-10% of your current weight can significantly improve health markers.",
            Color(0xFFFF9800)
        )
        isBelow && absDiff < 5 -> listOf(
            "🍎",
            "Just a little more to go",
            "You're close to your ideal weight. Focus on nutritious, calorie-dense foods and strength training to reach your goal safely.",
            Color(0xFF2196F3)
        )
        else -> listOf(
            "🍽️",
            "Nourish your body",
            "Consider consulting a healthcare provider about a healthy weight gain plan. Focus on nutrient-dense foods and strength training.",
            Color(0xFFFF9800)
        )
    }

    @Suppress("UNCHECKED_CAST")
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = (color as Color).copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(emoji as String, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title as String,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message as String,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
