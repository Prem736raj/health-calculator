// ui/screens/home/components/WaterHomeCard.kt
package com.health.calculator.bmi.tracker.ui.screens.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private val WaterBlueLight = Color(0xFF64B5F6)
private val WaterBlueMedium = Color(0xFF2196F3)
private val WaterBlueDark = Color(0xFF1565C0)

@Composable
fun WaterHomeCard(
    currentMl: Int,
    goalMl: Int,
    streakDays: Int,
    onCardClick: () -> Unit,
    onQuickAdd: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val percentage = if (goalMl > 0) (currentMl.toFloat() / goalMl * 100).coerceAtMost(150f) else 0f
    val isGoalMet = currentMl >= goalMl

    // Animated progress
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "water_progress"
    )

    // Pulse animation when goal is met
    val infiniteTransition = rememberInfiniteTransition(label = "water_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isGoalMet) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Quick add menu state
    var showQuickAddMenu by remember { mutableStateOf(false) }
    var quickAddSuccess by remember { mutableStateOf(false) }

    // Auto-dismiss success message
    LaunchedEffect(quickAddSuccess) {
        if (quickAddSuccess) {
            delay(1500)
            quickAddSuccess = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isGoalMet)
                            listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                        else
                            listOf(WaterBlueMedium, WaterBlueDark)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mini Progress Ring with Dynamic Glass Icon
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MiniWaterProgressRing(
                        percentage = animatedPercentage,
                        isGoalMet = isGoalMet
                    )
                    Text(
                        text = getGlassIcon(animatedPercentage),
                        fontSize = 28.sp
                    )
                }

                // Progress information
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Daily Water Intake",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (isGoalMet) {
                            Text("✓", fontSize = 14.sp, color = Color.White)
                        }
                    }

                    // Progress in liters
                    val currentL = currentMl / 1000f
                    val goalL = goalMl / 1000f
                    Text(
                        text = "${String.format("%.1f", currentL)}L / ${String.format("%.1f", goalL)}L",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )

                    // Percentage and streak
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "${animatedPercentage.toInt()}%",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (streakDays > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🔥", fontSize = 12.sp)
                                Text(
                                    "$streakDays day${if (streakDays > 1) "s" else ""}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Dynamic status message
                    val statusMessage = when {
                        isGoalMet -> "🎉 Goal achieved!"
                        percentage >= 75 -> "💪 Almost there!"
                        percentage >= 50 -> "👍 Halfway done"
                        percentage >= 25 -> "💧 Keep drinking"
                        else -> "🌅 Start hydrating"
                    }
                    Text(
                        text = statusMessage,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                // Quick add button with dropdown
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box {
                        FilledIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showQuickAddMenu = !showQuickAddMenu
                            },
                            modifier = Modifier.size(44.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                "Quick add water",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Dropdown menu for quick add options
                        DropdownMenu(
                            expanded = showQuickAddMenu,
                            onDismissRequest = { showQuickAddMenu = false }
                        ) {
                            listOf(
                                100 to "💧 Sip (100ml)",
                                250 to "🥛 Glass (250ml)",
                                500 to "🍶 Bottle (500ml)"
                            ).forEach { (amount, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onQuickAdd(amount)
                                        showQuickAddMenu = false
                                        quickAddSuccess = true
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        "Quick\nAdd",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Success overlay when quick adding
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = quickAddSuccess,
                    enter = fadeIn(tween(200)) + scaleIn(tween(300)),
                    exit = fadeOut(tween(400))
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Text(
                            "✓ Added!",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniWaterProgressRing(
    percentage: Float,
    isGoalMet: Boolean
) {
    val sweepAngle = (percentage / 100f * 360f).coerceAtMost(360f)

    val ringColor = when {
        isGoalMet -> Color(0xFF81C784)
        percentage >= 75 -> WaterBlueLight
        percentage >= 50 -> WaterBlueMedium
        else -> Color.White.copy(alpha = 0.5f)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 6f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Background ring
        drawCircle(
            color = Color.White.copy(alpha = 0.2f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = ringColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Glow dot at the end of progress
        if (sweepAngle > 10f) {
            val endAngle = Math.toRadians((-90f + sweepAngle).toDouble())
            val dotX = center.x + radius * cos(endAngle).toFloat()
            val dotY = center.y + radius * sin(endAngle).toFloat()
            drawCircle(
                color = ringColor.copy(alpha = 0.5f),
                radius = strokeWidth,
                center = Offset(dotX, dotY)
            )
        }
    }
}

/**
 * Returns different glass/water icons based on progress percentage
 */
private fun getGlassIcon(percentage: Float): String {
    return when {
        percentage >= 100 -> "🥳"
        percentage >= 87.5f -> "🥛"
        percentage >= 75f -> "🥛"
        percentage >= 62.5f -> "🥛"
        percentage >= 50f -> "🥤"
        percentage >= 37.5f -> "🥤"
        percentage >= 25f -> "🫗"
        percentage >= 12.5f -> "🫗"
        else -> "🪣"
    }
}
