package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

// ─── Colors ───────────────────────────────────────────────────────────────────

private val TrendAccent = Color(0xFF1E88E5)
private val GraphLineColor = Color(0xFF1E88E5)
private val GraphDotColor = Color(0xFF1E88E5)

private val ZoneSevereThin = Color(0xFFE53935)
private val ZoneUnderweight = Color(0xFFFF9800)
private val ZoneNormal = Color(0xFF43A047)
private val ZoneOverweight = Color(0xFFFFC107)
private val ZoneObese = Color(0xFFE53935)

/**
 * Complete BMI trend visualization section with line graph and statistics.
 */
@Composable
fun BmiTrendSection(
    trendData: BmiTrendData,
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150)
        showContent = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = tween(400, easing = EaseOutCubic)
            )
        ) {
            TrendHeader(readingsCount = trendData.stats.totalReadings)
        }

        if (trendData.isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Loading trend data...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        } else if (!trendData.hasEnoughData) {
            // Not enough data
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, delayMillis = 200))
            ) {
                NotEnoughDataCard(readingsCount = trendData.stats.totalReadings)
            }
        } else {
            // ── Comparison Card ────────────────────────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(400, delayMillis = 100, easing = EaseOutCubic)
                )
            ) {
                ComparisonCard(stats = trendData.stats)
            }

            // ── Graph ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(500, delayMillis = 200, easing = EaseOutCubic)
                )
            ) {
                BmiTrendGraph(points = trendData.points)
            }

            // ── Statistics ────────────────────────────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, delayMillis = 400)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(400, delayMillis = 400, easing = EaseOutCubic)
                )
            ) {
                StatisticsCard(stats = trendData.stats)
            }
        }
    }
}

// ─── Trend Header ─────────────────────────────────────────────────────────────

@Composable
private fun TrendHeader(readingsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = TrendAccent.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(TrendAccent.copy(alpha = 0.15f), TrendAccent.copy(alpha = 0.08f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Timeline, null,
                    tint = TrendAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    "My BMI Trend",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "$readingsCount reading${if (readingsCount != 1) "s" else ""} tracked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Not Enough Data Card ─────────────────────────────────────────────────────

@Composable
private fun NotEnoughDataCard(readingsCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .offset { IntOffset(0, -pulse.toInt()) }
                    .clip(CircleShape)
                    .background(TrendAccent.copy(alpha = 0.08f))
                    .border(1.dp, TrendAccent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.BarChart, null,
                    tint = TrendAccent.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (readingsCount == 0) "No BMI Readings Yet" else "Just Getting Started!",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (readingsCount == 0)
                    "Calculate your BMI to start tracking your trend over time. Regular tracking helps you understand your health journey."
                else
                    "You have $readingsCount reading so far. Calculate your BMI again later to see how your trend changes over time!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("📊 Track weekly", "📈 See trends", "🎯 Set goals").forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TrendAccent.copy(alpha = 0.06f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, TrendAccent.copy(alpha = 0.15f))
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TrendAccent.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Comparison Card ──────────────────────────────────────────────────────────

@Composable
private fun ComparisonCard(stats: BmiTrendStats) {
    val change = stats.changeFromPrevious ?: return
    val isUp = change > 0.05
    val isDown = change < -0.05
    val isFlat = !isUp && !isDown

    val changeColor = when {
        isFlat -> Color(0xFF43A047)
        abs(change) < 1.0 -> Color(0xFFFFC107)
        else -> if (stats.currentCategory == BmiCategory.NORMAL) Color(0xFF43A047) else Color(0xFFFF9800)
    }

    val changeIcon = when {
        isUp -> Icons.AutoMirrored.Filled.TrendingUp
        isDown -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }

    val changeText = when {
        isUp -> "Up ${String.format("%.1f", abs(change))}"
        isDown -> "Down ${String.format("%.1f", abs(change))}"
        else -> "No change"
    }

    val timeSince = stats.previousTimestamp?.let { formatTimeSince(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = changeColor.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(changeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(changeIcon, null, tint = changeColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$changeText since last check",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = changeColor
                )
                if (timeSince.isNotEmpty()) {
                    Text(
                        text = "Previous: ${String.format("%.1f", stats.previousBmi)} • $timeSince",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.1f", stats.currentBmi),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(stats.currentCategory.colorHex)
                )
                Text(
                    "Current",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ─── BMI Trend Graph ──────────────────────────────────────────────────────────

@Composable
private fun BmiTrendGraph(points: List<BmiTrendPoint>) {
    var selectedPoint by remember { mutableStateOf<BmiTrendPoint?>(null) }

    // Animate line drawing
    val lineProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        lineProgress.snapTo(0f)
        lineProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, delayMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    // Animate dots
    val dotScale = remember { Animatable(0f) }
    LaunchedEffect(points) {
        dotScale.snapTo(0f)
        delay(800)
        dotScale.animateTo(1f, tween(500, easing = EaseOutBack))
    }

    val density = LocalDensity.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BMI Over Time",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendDot(Color(0xFF43A047), "Normal")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected point tooltip
            AnimatedVisibility(visible = selectedPoint != null) {
                selectedPoint?.let { point ->
                    PointTooltip(point = point, onDismiss = { selectedPoint = null })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Graph
            val graphHeight = 200.dp
            val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(graphHeight)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                if (points.isEmpty()) return@detectTapGestures
                                val width = size.width.toFloat()
                                val padding = 40f
                                val graphWidth = width - padding * 2

                                // Find nearest point
                                val nearest = points.withIndex().minByOrNull { (i, _) ->
                                    val x = padding + (i.toFloat() / (points.size - 1).coerceAtLeast(1)) * graphWidth
                                    abs(offset.x - x)
                                }
                                nearest?.let { (_, point) ->
                                    selectedPoint = if (selectedPoint?.id == point.id) null else point
                                }
                            }
                        }
                ) {
                    if (points.isEmpty()) return@Canvas

                    val width = size.width
                    val height = size.height
                    val padding = 40f
                    val graphWidth = width - padding * 2
                    val graphHeight2 = height - padding * 2

                    // BMI range for y-axis
                    val allValues = points.map { it.bmiValue }
                    val minBmi = (allValues.min() - 2).coerceAtLeast(12.0)
                    val maxBmi = (allValues.max() + 2).coerceAtMost(50.0)
                    val bmiRange = maxBmi - minBmi

                    fun bmiToY(bmi: Double): Float {
                        return (padding + graphHeight2 * (1 - (bmi - minBmi) / bmiRange)).toFloat()
                    }

                    fun indexToX(index: Int): Float {
                        return padding + (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * graphWidth
                    }

                    // ── Background Zones ──────────────────────────────
                    val zones = listOf(
                        Triple(0.0, 18.5, ZoneUnderweight.copy(alpha = 0.06f)),
                        Triple(18.5, 25.0, ZoneNormal.copy(alpha = 0.08f)),
                        Triple(25.0, 30.0, ZoneOverweight.copy(alpha = 0.06f)),
                        Triple(30.0, 50.0, ZoneObese.copy(alpha = 0.05f))
                    )

                    zones.forEach { (low, high, color) ->
                        val yTop = bmiToY(high.coerceAtMost(maxBmi))
                        val yBottom = bmiToY(low.coerceAtLeast(minBmi))
                        if (yBottom > yTop) {
                            drawRect(
                                color = color,
                                topLeft = Offset(padding, yTop),
                                size = Size(graphWidth, yBottom - yTop)
                            )
                        }
                    }

                    // ── Normal Range Lines ────────────────────────────
                    listOf(18.5, 25.0).forEach { boundary ->
                        if (boundary in minBmi..maxBmi) {
                            val y = bmiToY(boundary)
                            drawLine(
                                color = if (boundary == 18.5) ZoneNormal.copy(alpha = 0.3f) else ZoneOverweight.copy(alpha = 0.3f),
                                start = Offset(padding, y),
                                end = Offset(padding + graphWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                            )
                        }
                    }

                    // ── Y-Axis Labels ─────────────────────────────────
                    val ySteps = 5
                    for (i in 0..ySteps) {
                        val bmi = minBmi + (bmiRange / ySteps) * i
                        val y = bmiToY(bmi)
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format("%.0f", bmi),
                            8f,
                            y + 4f,
                            android.graphics.Paint().apply {
                                color = surfaceVariant.copy(alpha = 0.4f).hashCode()
                                textSize = 9.sp.toPx()
                                isAntiAlias = true
                            }
                        )
                    }

                    // ── Line Path ─────────────────────────────────────
                    if (points.size >= 2) {
                        val totalPoints = (points.size * lineProgress.value).toInt().coerceAtLeast(2)
                        val visiblePoints = points.take(totalPoints)

                        val linePath = Path()
                        visiblePoints.forEachIndexed { index, point ->
                            val x = indexToX(index)
                            val y = bmiToY(point.bmiValue)
                            if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                        }

                        // Glow effect
                        drawPath(
                            path = linePath,
                            color = GraphLineColor.copy(alpha = 0.15f),
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Main line
                        drawPath(
                            path = linePath,
                            color = GraphLineColor,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Fill under line
                        val fillPath = Path()
                        fillPath.addPath(linePath)
                        val lastX = indexToX(visiblePoints.size - 1)
                        fillPath.lineTo(lastX, height - padding)
                        fillPath.lineTo(padding, height - padding)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GraphLineColor.copy(alpha = 0.12f),
                                    GraphLineColor.copy(alpha = 0.02f)
                                ),
                                startY = 0f,
                                endY = height
                            )
                        )
                    }

                    // ── Data Points ───────────────────────────────────
                    if (dotScale.value > 0f) {
                        points.forEachIndexed { index, point ->
                            val x = indexToX(index)
                            val y = bmiToY(point.bmiValue)
                            val catColor = Color(point.category.colorHex)
                            val isSelected = selectedPoint?.id == point.id
                            val dotRadius = if (isSelected) 7.dp.toPx() else 5.dp.toPx()

                            // Outer circle
                            drawCircle(
                                color = Color.White,
                                radius = (dotRadius + 2.dp.toPx()) * dotScale.value,
                                center = Offset(x, y)
                            )

                            // Inner circle
                            drawCircle(
                                color = catColor,
                                radius = dotRadius * dotScale.value,
                                center = Offset(x, y)
                            )

                            if (isSelected) {
                                drawCircle(
                                    color = catColor.copy(alpha = 0.2f),
                                    radius = 12.dp.toPx() * dotScale.value,
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }

                    // ── X-Axis Date Labels ────────────────────────────
                    val labelCount = (points.size).coerceAtMost(5)
                    val step = ((points.size - 1).toFloat() / (labelCount - 1).coerceAtLeast(1)).toInt().coerceAtLeast(1)
                    for (i in points.indices step step) {
                        val x = indexToX(i)
                        val dateStr = formatShortDate(points[i].timestamp)
                        drawContext.canvas.nativeCanvas.drawText(
                            dateStr,
                            x - 16f,
                            height - 6f,
                            android.graphics.Paint().apply {
                                color = surfaceVariant.copy(alpha = 0.4f).hashCode()
                                textSize = 9.sp.toPx()
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }

            // Zone legend
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ZoneLegend(ZoneUnderweight, "Under")
                Spacer(modifier = Modifier.width(12.dp))
                ZoneLegend(ZoneNormal, "Normal")
                Spacer(modifier = Modifier.width(12.dp))
                ZoneLegend(ZoneOverweight, "Over")
                Spacer(modifier = Modifier.width(12.dp))
                ZoneLegend(ZoneObese, "Obese")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun ZoneLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp, 6.dp).clip(RoundedCornerShape(2.dp)).background(color.copy(alpha = 0.4f)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun PointTooltip(point: BmiTrendPoint, onDismiss: () -> Unit) {
    val catColor = Color(point.category.colorHex)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = catColor.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, catColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(10.dp).clip(CircleShape).background(catColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "BMI ${String.format("%.1f", point.bmiValue)} — ${point.category.label}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = catColor
                )
                Text(
                    "${formatFullDate(point.timestamp)} • ${point.inputSummary}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Close, "Dismiss", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            }
        }
    }
}

// ─── Statistics Card ──────────────────────────────────────────────────────────

@Composable
private fun StatisticsCard(stats: BmiTrendStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(6.dp, 20.dp).clip(RoundedCornerShape(3.dp)).background(TrendAccent)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Statistics", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Current",
                    value = String.format("%.1f", stats.currentBmi),
                    color = Color(stats.currentCategory.colorHex)
                )
                StatItem(
                    label = "Average",
                    value = String.format("%.1f", stats.averageBmi),
                    color = Color(BmiCategory.fromBmi(stats.averageBmi).colorHex)
                )
                StatItem(
                    label = "Lowest",
                    value = String.format("%.1f", stats.lowestBmi),
                    color = Color(BmiCategory.fromBmi(stats.lowestBmi).colorHex)
                )
                StatItem(
                    label = "Highest",
                    value = String.format("%.1f", stats.highestBmi),
                    color = Color(BmiCategory.fromBmi(stats.highestBmi).colorHex)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "📊 ${stats.totalReadings} total readings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ─── Formatting Helpers ───────────────────────────────────────────────────────

private fun formatShortDate(timestamp: Long): String {
    return SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
}

private fun formatFullDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

private fun formatTimeSince(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        days < 30 -> "${days / 7} week${if (days / 7 > 1) "s" else ""} ago"
        days < 365 -> "${days / 30} month${if (days / 30 > 1) "s" else ""} ago"
        else -> "${days / 365} year${if (days / 365 > 1) "s" else ""} ago"
    }
}
