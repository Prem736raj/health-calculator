// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/MealTimingSection.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val EatingColor = Color(0xFF4CAF50)
private val FastingColor = Color(0xFFFF9800)
private val MealDotColor = Color(0xFF2196F3)
private val SnackDotColor = Color(0xFFAB47BC)

@Composable
fun MealTimingSection(
    totalCalories: Float,
    proteinGrams: Float,
    carbsGrams: Float,
    fatGrams: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedPattern by remember { mutableStateOf(EatingPattern.STANDARD) }
    var startHour by remember { mutableIntStateOf(8) }
    var startMinute by remember { mutableIntStateOf(0) }
    var customWindowHours by remember { mutableIntStateOf(10) }
    var customMealCount by remember { mutableIntStateOf(3) }

    val effectivePattern = if (selectedPattern == EatingPattern.CUSTOM) {
        selectedPattern // handled separately
    } else selectedPattern

    val config = MealTimingConfig(
        pattern = selectedPattern,
        eatingWindowStartHour = startHour,
        eatingWindowStartMinute = startMinute,
        totalCalories = totalCalories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams
    )

    val meals = config.getMeals()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⏰", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Meal Timing & Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Plan when and how often to eat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pattern selector
                PatternSelector(
                    selectedPattern = selectedPattern,
                    onPatternSelected = { selectedPattern = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Eating window adjustment
                EatingWindowControls(
                    pattern = selectedPattern,
                    startHour = startHour,
                    startMinute = startMinute,
                    onStartTimeChanged = { h, m ->
                        startHour = h
                        startMinute = m
                    },
                    customWindowHours = customWindowHours,
                    onCustomWindowChanged = { customWindowHours = it },
                    customMealCount = customMealCount,
                    onCustomMealCountChanged = { customMealCount = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 24-hour visual timeline
                DailyTimeline(
                    config = config,
                    meals = meals,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // IF window summary (for IF patterns)
                if (selectedPattern.isIntermittentFasting) {
                    IFWindowSummary(config = config)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Meal breakdown
                MealBreakdownList(meals = meals)
            }
        }
    }
}

// ============================================================
// Pattern Selector
// ============================================================
@Composable
private fun PatternSelector(
    selectedPattern: EatingPattern,
    onPatternSelected: (EatingPattern) -> Unit
) {
    Column {
        Text(
            text = "Eating Pattern",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Regular patterns row
        Text(
            text = "Regular",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val regular = listOf(
                EatingPattern.STANDARD, EatingPattern.THREE_MEALS,
                EatingPattern.FOUR_MEALS, EatingPattern.SIX_SMALL
            )
            regular.forEach { pattern ->
                PatternChip(
                    pattern = pattern,
                    isSelected = pattern == selectedPattern,
                    onClick = { onPatternSelected(pattern) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // IF patterns row
        Text(
            text = "Intermittent Fasting",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val ifPatterns = listOf(
                EatingPattern.IF_16_8, EatingPattern.IF_18_6,
                EatingPattern.IF_20_4, EatingPattern.CUSTOM
            )
            ifPatterns.forEach { pattern ->
                PatternChip(
                    pattern = pattern,
                    isSelected = pattern == selectedPattern,
                    onClick = { onPatternSelected(pattern) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected description
        AnimatedContent(
            targetState = selectedPattern,
            transitionSpec = {
                (fadeIn(tween(200)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(150)) + slideOutVertically { -it / 4 })
            },
            label = "patternDesc"
        ) { pattern ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = pattern.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = pattern.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = pattern.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatternChip(
    pattern: EatingPattern,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) {
            if (pattern.isIntermittentFasting) FastingColor
            else MaterialTheme.colorScheme.primary
        } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200), label = "chipBg"
    )
    val fg by animateColorAsState(
        targetValue = if (isSelected) Color.White
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200), label = "chipFg"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = pattern.emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pattern.shortName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = fg,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ============================================================
// Eating Window Controls
// ============================================================
@Composable
private fun EatingWindowControls(
    pattern: EatingPattern,
    startHour: Int,
    startMinute: Int,
    onStartTimeChanged: (Int, Int) -> Unit,
    customWindowHours: Int,
    onCustomWindowChanged: (Int) -> Unit,
    customMealCount: Int,
    onCustomMealCountChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Start time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "First meal at:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Time stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = {
                            val newHour = if (startHour == 0) 23 else startHour - 1
                            onStartTimeChanged(newHour, startMinute)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Earlier", modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val displayHour = if (startHour == 0) 12 else if (startHour > 12) startHour - 12 else startHour
                    val amPm = if (startHour < 12) "AM" else "PM"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${displayHour}:${String.format("%02d", startMinute)} $amPm",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = {
                            val newHour = (startHour + 1) % 24
                            onStartTimeChanged(newHour, startMinute)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Later", modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Custom window controls
            if (pattern == EatingPattern.CUSTOM) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Window hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Eating window:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { if (customWindowHours > 2) onCustomWindowChanged(customWindowHours - 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Remove, null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$customWindowHours hrs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EatingColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = { if (customWindowHours < 20) onCustomWindowChanged(customWindowHours + 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Meal count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Number of meals:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalIconButton(
                            onClick = { if (customMealCount > 1) onCustomMealCountChanged(customMealCount - 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Remove, null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$customMealCount meals",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MealDotColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = { if (customMealCount < 8) onCustomMealCountChanged(customMealCount + 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 24-Hour Visual Timeline (Clock-style)
// ============================================================
@Composable
private fun DailyTimeline(
    config: MealTimingConfig,
    meals: List<TimedMealSlot>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(config.pattern, config.eatingWindowStartHour) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Column {
        Text(
            text = "📅 Daily Schedule",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(modifier = modifier) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = (size.minDimension / 2) - 30.dp.toPx()
            val innerRadius = radius - 20.dp.toPx()
            val outerRadius = radius + 4.dp.toPx()

            // Background ring
            drawCircle(
                color = surfaceVariant,
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 24.dp.toPx())
            )

            // Eating window arc
            val startAngle = (config.eatingWindowStartHour * 60 + config.eatingWindowStartMinute) / (24f * 60f) * 360f - 90f
            val sweepAngle = config.pattern.eatingWindowHours / 24f * 360f * animProgress.value

            drawArc(
                color = EatingColor.copy(alpha = 0.3f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Fasting window (remaining)
            drawArc(
                color = FastingColor.copy(alpha = 0.08f),
                startAngle = startAngle + sweepAngle,
                sweepAngle = 360f - sweepAngle,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx()),
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Hour markers
            for (hour in 0..23) {
                val angle = (hour / 24f * 360f - 90f) * (PI.toFloat() / 180f)
                val isMainHour = hour % 6 == 0
                val markerInner = if (isMainHour) radius - 14.dp.toPx() else radius - 8.dp.toPx()
                val markerOuter = radius + 14.dp.toPx()

                val x1 = centerX + cos(angle) * markerInner
                val y1 = centerY + sin(angle) * markerInner

                if (isMainHour) {
                    val labelRadius = radius + 20.dp.toPx()
                    val lx = centerX + cos(angle) * labelRadius
                    val ly = centerY + sin(angle) * labelRadius

                    val displayH = when (hour) {
                        0 -> "12A"
                        6 -> "6A"
                        12 -> "12P"
                        18 -> "6P"
                        else -> "${hour}"
                    }
                    val label = textMeasurer.measure(
                        displayH,
                        TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = onSurfaceVariant)
                    )
                    drawText(
                        label,
                        topLeft = Offset(lx - label.size.width / 2, ly - label.size.height / 2)
                    )
                }

                // Tick mark
                val tickOuter = radius + 2.dp.toPx()
                val tickInner = radius - (if (isMainHour) 6.dp else 3.dp).toPx()
                drawLine(
                    color = onSurfaceVariant.copy(alpha = if (isMainHour) 0.4f else 0.15f),
                    start = Offset(centerX + cos(angle) * tickInner, centerY + sin(angle) * tickInner),
                    end = Offset(centerX + cos(angle) * tickOuter, centerY + sin(angle) * tickOuter),
                    strokeWidth = if (isMainHour) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // Meal dots
            meals.forEach { meal ->
                val mealMinutes = meal.hour * 60 + meal.minute
                val angle = (mealMinutes / (24f * 60f) * 360f - 90f) * (PI.toFloat() / 180f)
                val dotRadius = radius
                val mx = centerX + cos(angle) * dotRadius
                val my = centerY + sin(angle) * dotRadius

                val dotColor = if (meal.type == MealType.MEAL) MealDotColor else SnackDotColor
                val dotSize = if (meal.type == MealType.MEAL) 8.dp.toPx() else 6.dp.toPx()

                // Glow
                drawCircle(dotColor.copy(alpha = 0.2f), dotSize * 2f * animProgress.value, Offset(mx, my))
                // Outer
                drawCircle(Color.White, dotSize * animProgress.value, Offset(mx, my))
                // Inner
                drawCircle(dotColor, dotSize * 0.7f * animProgress.value, Offset(mx, my))
            }

            // Center info
            val centerLabel = textMeasurer.measure(
                "${config.pattern.eatingWindowHours}h eat",
                TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EatingColor)
            )
            drawText(centerLabel, topLeft = Offset(centerX - centerLabel.size.width / 2, centerY - 16.dp.toPx()))

            val fastLabel = textMeasurer.measure(
                "${config.fastingHours}h fast",
                TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = FastingColor)
            )
            drawText(fastLabel, topLeft = Offset(centerX - fastLabel.size.width / 2, centerY + 4.dp.toPx()))
        }

        // Timeline legend
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimelineLegend(color = EatingColor, label = "Eating")
            Spacer(modifier = Modifier.width(12.dp))
            TimelineLegend(color = FastingColor, label = "Fasting")
            Spacer(modifier = Modifier.width(12.dp))
            TimelineLegend(color = MealDotColor, label = "Meal")
            Spacer(modifier = Modifier.width(12.dp))
            TimelineLegend(color = SnackDotColor, label = "Snack")
        }
    }
}

@Composable
private fun TimelineLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}

// ============================================================
// IF Window Summary
// ============================================================
@Composable
private fun IFWindowSummary(config: MealTimingConfig) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = FastingColor.copy(alpha = 0.06f)
        ),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, FastingColor.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⏱️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Intermittent Fasting Schedule",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IFWindowChip(
                    label = "Eating Window",
                    time = "${formatHour(config.eatingWindowStartHour)} – ${formatHour(config.eatingWindowEndHour)}",
                    duration = "${config.pattern.eatingWindowHours} hours",
                    color = EatingColor,
                    emoji = "🍽️"
                )
                IFWindowChip(
                    label = "Fasting Window",
                    time = "${formatHour(config.eatingWindowEndHour)} – ${formatHour(config.eatingWindowStartHour)}",
                    duration = "${config.fastingHours} hours",
                    color = FastingColor,
                    emoji = "🌙"
                )
            }
        }
    }
}

@Composable
private fun IFWindowChip(
    label: String,
    time: String,
    duration: String,
    color: Color,
    emoji: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = duration,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

private fun formatHour(hour: Int): String {
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val amPm = if (hour < 12) "AM" else "PM"
    return "${h}${amPm}"
}

// ============================================================
// Meal Breakdown List
// ============================================================
@Composable
private fun MealBreakdownList(meals: List<TimedMealSlot>) {
    Column {
        Text(
            text = "🍽️ Meal Schedule",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        meals.forEachIndexed { index, meal ->
            MealCard(meal = meal, index = index)
            if (index < meals.lastIndex) {
                // Connector line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tips
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Outlined.LightbulbCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Tip: These are suggested timings. Adjust based on your schedule and listen to your body's hunger cues.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun MealCard(meal: TimedMealSlot, index: Int) {
    val dotColor = if (meal.type == MealType.MEAL) MealDotColor else SnackDotColor
    val isSnack = meal.type == MealType.SNACK

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = dotColor.copy(alpha = 0.04f)
        ),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, dotColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = meal.timeString,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = dotColor,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSnack) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SnackDotColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Snack",
                                style = MaterialTheme.typography.labelSmall,
                                color = SnackDotColor,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroMiniChip("P", "${meal.protein.roundToInt()}g", MacroColors.Protein)
                    MacroMiniChip("C", "${meal.carbs.roundToInt()}g", MacroColors.Carbs)
                    MacroMiniChip("F", "${meal.fat.roundToInt()}g", MacroColors.Fat)
                }
            }

            // Calories
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${meal.calories.roundToInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Text(
                    text = "${meal.portionPercent.roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun MacroMiniChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                fontSize = 9.sp
            )
        }
    }
}
