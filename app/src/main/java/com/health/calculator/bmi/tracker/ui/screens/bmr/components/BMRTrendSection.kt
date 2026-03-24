// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/BMRTrendSection.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*
import kotlin.math.abs
import kotlin.math.roundToInt

private val BMRLineColor = Color(0xFF5C6BC0)
private val TDEELineColor = Color(0xFF26A69A)

@Composable
fun BMRTrendSection(
    historyPoints: List<BMRHistoryPoint>,
    stats: BMRTrendStats,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
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
                    Text(text = "📈", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "BMR & TDEE Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${stats.totalReadings} reading${if (stats.totalReadings != 1) "s" else ""} recorded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    historyPoints.isEmpty() -> EmptyTrendState()
                    historyPoints.size == 1 -> SingleReadingState(stats)
                    else -> {
                        // Previous comparison badge
                        PreviousComparisonBadge(stats = stats)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dual line graph
                        BMRTDEEGraph(
                            points = historyPoints,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Graph legend
                        GraphLegend()

                        Spacer(modifier = Modifier.height(16.dp))

                        // Statistics
                        TrendStatisticsGrid(stats = stats)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Insight
                        InsightCard(stats = stats)
                    }
                }
            }
        }
    }
}

// ============================================================
// Empty & Single Reading States
// ============================================================
@Composable
private fun EmptyTrendState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📊", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No BMR Readings Yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Save your first BMR calculation to start tracking your metabolic trends. We recommend checking monthly for the best insights!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SingleReadingState(stats: BMRTrendStats) {
    Column {
        // Show current stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SingleStatCard(
                label = "Current BMR",
                value = "${stats.currentBMR.toInt()}",
                unit = "kcal/day",
                color = BMRLineColor,
                modifier = Modifier.weight(1f)
            )
            SingleStatCard(
                label = "Current TDEE",
                value = "${stats.currentTDEE.toInt()}",
                unit = "kcal/day",
                color = TDEELineColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            )
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                Text(text = "💡", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Great start! Save another BMR reading in a few weeks to see your metabolic trends over time. Tracking changes helps you understand how diet and exercise affect your metabolism.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SingleStatCard(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.06f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

// ============================================================
// Previous Comparison Badge
// ============================================================
@Composable
private fun PreviousComparisonBadge(stats: BMRTrendStats) {
    if (!stats.hasPreviousReading) return

    val change = stats.changeFromPrevious
    val isUp = change > 0
    val isStable = abs(change) < 5f
    val emoji = when {
        isStable -> "➡️"
        isUp -> "↑"
        else -> "↓"
    }
    val color = when {
        isStable -> MaterialTheme.colorScheme.onSurfaceVariant
        isUp -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }
    val text = when {
        isStable -> "Stable since last reading"
        isUp -> "${emoji} Up ${abs(change).toInt()} kcal since last reading"
        else -> "${emoji} Down ${abs(change).toInt()} kcal since last reading"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

// ============================================================
// Dual Line Graph (BMR + TDEE)
// ============================================================
@Composable
private fun BMRTDEEGraph(
    points: List<BMRHistoryPoint>,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember { mutableIntStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    Column {
        // Tooltip
        AnimatedVisibility(
            visible = selectedPointIndex >= 0 && selectedPointIndex < points.size,
            enter = fadeIn(tween(150)) + expandVertically(tween(150)),
            exit = fadeOut(tween(100)) + shrinkVertically(tween(100))
        ) {
            if (selectedPointIndex >= 0 && selectedPointIndex < points.size) {
                val point = points[selectedPointIndex]
                PointTooltip(point = point)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Canvas(
            modifier = modifier.pointerInput(points) {
                detectTapGestures { offset ->
                    val paddingLeft = 50.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val chartWidth = size.width - paddingLeft - paddingRight

                    if (points.size > 1) {
                        val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)
                        val index = ((offset.x - paddingLeft) / stepX).roundToInt()
                            .coerceIn(0, points.size - 1)
                        selectedPointIndex = if (selectedPointIndex == index) -1 else index
                    }
                }
            }
        ) {
            val paddingLeft = 50.dp.toPx()
            val paddingRight = 16.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingBottom = 30.dp.toPx()

            val chartWidth = size.width - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop - paddingBottom

            val allValues = points.flatMap { listOf(it.bmr, it.tdee) }
            val minVal = (allValues.min() - 100f).coerceAtLeast(500f)
            val maxVal = allValues.max() + 100f
            val valueRange = maxVal - minVal

            fun valToY(value: Float): Float =
                paddingTop + chartHeight - ((value - minVal) / valueRange * chartHeight * animProgress.value)

            fun indexToX(index: Int): Float {
                return if (points.size == 1) paddingLeft + chartWidth / 2
                else paddingLeft + (index.toFloat() / (points.size - 1)) * chartWidth
            }

            // Grid
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val value = minVal + (valueRange * i / gridSteps)
                val y = paddingTop + chartHeight - (chartHeight * i / gridSteps)
                drawLine(gridColor, Offset(paddingLeft, y), Offset(size.width - paddingRight, y), 1.dp.toPx())

                val label = textMeasurer.measure(
                    "${value.toInt()}",
                    TextStyle(fontSize = 9.sp, color = labelColor)
                )
                drawText(label, topLeft = Offset(2.dp.toPx(), y - label.size.height / 2))
            }

            // Date labels
            val labelIndices = when {
                points.size <= 5 -> points.indices.toList()
                else -> listOf(0, points.size / 2, points.size - 1)
            }
            labelIndices.forEach { i ->
                if (i < points.size) {
                    val x = indexToX(i)
                    val dateLabel = textMeasurer.measure(
                        points[i].dateLabel,
                        TextStyle(fontSize = 8.sp, color = labelColor)
                    )
                    drawText(
                        dateLabel,
                        topLeft = Offset(
                            (x - dateLabel.size.width / 2).coerceIn(paddingLeft, size.width - paddingRight - dateLabel.size.width.toFloat()),
                            size.height - paddingBottom + 4.dp.toPx()
                        )
                    )
                }
            }

            if (points.size > 1) {
                // TDEE line (draw first so BMR overlays)
                val tdeePath = Path()
                val tdeeFillPath = Path()
                points.forEachIndexed { index, point ->
                    val x = indexToX(index)
                    val y = valToY(point.tdee)
                    if (index == 0) {
                        tdeePath.moveTo(x, y)
                        tdeeFillPath.moveTo(x, paddingTop + chartHeight)
                        tdeeFillPath.lineTo(x, y)
                    } else {
                        val prevX = indexToX(index - 1)
                        val prevY = valToY(points[index - 1].tdee)
                        val cpX = (prevX + x) / 2
                        tdeePath.cubicTo(cpX, prevY, cpX, y, x, y)
                        tdeeFillPath.cubicTo(cpX, prevY, cpX, y, x, y)
                    }
                }
                tdeeFillPath.lineTo(indexToX(points.lastIndex), paddingTop + chartHeight)
                tdeeFillPath.close()

                drawPath(tdeeFillPath, TDEELineColor.copy(alpha = 0.06f))
                drawPath(tdeePath, TDEELineColor.copy(alpha = 0.5f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

                // BMR line
                val bmrPath = Path()
                val bmrFillPath = Path()
                points.forEachIndexed { index, point ->
                    val x = indexToX(index)
                    val y = valToY(point.bmr)
                    if (index == 0) {
                        bmrPath.moveTo(x, y)
                        bmrFillPath.moveTo(x, paddingTop + chartHeight)
                        bmrFillPath.lineTo(x, y)
                    } else {
                        val prevX = indexToX(index - 1)
                        val prevY = valToY(points[index - 1].bmr)
                        val cpX = (prevX + x) / 2
                        bmrPath.cubicTo(cpX, prevY, cpX, y, x, y)
                        bmrFillPath.cubicTo(cpX, prevY, cpX, y, x, y)
                    }
                }
                bmrFillPath.lineTo(indexToX(points.lastIndex), paddingTop + chartHeight)
                bmrFillPath.close()

                drawPath(bmrFillPath, BMRLineColor.copy(alpha = 0.08f))
                drawPath(bmrPath, BMRLineColor, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
            }

            // Data points
            points.forEachIndexed { index, point ->
                val x = indexToX(index)
                val bmrY = valToY(point.bmr)
                val tdeeY = valToY(point.tdee)
                val isSelected = index == selectedPointIndex
                val dotSize = if (isSelected) 7.dp.toPx() else 4.dp.toPx()

                // TDEE dot
                drawCircle(TDEELineColor.copy(alpha = 0.4f), dotSize * 0.8f, Offset(x, tdeeY))

                // BMR dot
                if (isSelected) {
                    drawCircle(BMRLineColor.copy(alpha = 0.2f), dotSize * 2f, Offset(x, bmrY))
                }
                drawCircle(Color.White, dotSize, Offset(x, bmrY))
                drawCircle(BMRLineColor, dotSize * 0.7f, Offset(x, bmrY))

                // Vertical guide line when selected
                if (isSelected) {
                    drawLine(
                        BMRLineColor.copy(alpha = 0.15f),
                        Offset(x, paddingTop),
                        Offset(x, paddingTop + chartHeight),
                        1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }
            }
        }
    }
}

@Composable
private fun PointTooltip(point: BMRHistoryPoint) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = point.dateLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TooltipItem("BMR", "${point.bmr.toInt()} kcal", BMRLineColor)
                TooltipItem("TDEE", "${point.tdee.toInt()} kcal", TDEELineColor)
                TooltipItem("Weight", "${String.format(java.util.Locale.getDefault(), "%.1f", point.weightKg)} kg", MaterialTheme.colorScheme.inverseOnSurface)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${point.formulaName} • ${point.activityLevel}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TooltipItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.8f),
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun GraphLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(color = BMRLineColor, label = "BMR")
        Spacer(modifier = Modifier.width(20.dp))
        LegendDot(color = TDEELineColor, label = "TDEE")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============================================================
// Statistics Grid
// ============================================================
@Composable
private fun TrendStatisticsGrid(stats: BMRTrendStats) {
    Column {
        Text(
            text = "📊 Statistics",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TrendStatCard(
                label = "Current",
                value = "${stats.currentBMR.toInt()}",
                unit = "kcal",
                color = BMRLineColor,
                modifier = Modifier.weight(1f)
            )
            TrendStatCard(
                label = "Average",
                value = "${stats.averageBMR.toInt()}",
                unit = "kcal",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            TrendStatCard(
                label = "Highest",
                value = "${stats.highestBMR.toInt()}",
                unit = "kcal",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            TrendStatCard(
                label = "Lowest",
                value = "${stats.lowestBMR.toInt()}",
                unit = "kcal",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Change from first reading
        if (stats.totalReadings > 1) {
            val changeColor = when {
                abs(stats.changeFromFirst) < 5f -> MaterialTheme.colorScheme.onSurfaceVariant
                stats.changeFromFirst > 0 -> Color(0xFF4CAF50)
                else -> Color(0xFFFF9800)
            }
            val sign = if (stats.changeFromFirst >= 0) "+" else ""

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = changeColor.copy(alpha = 0.06f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Change from first reading",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$sign${stats.changeFromFirst.toInt()} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = changeColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$sign${stats.changePercentFromFirst.roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = changeColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendStatCard(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.06f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
        }
    }
}

// ============================================================
// Insight Card
// ============================================================
@Composable
private fun InsightCard(stats: BMRTrendStats) {
    val insight = stats.getChangeInsight()
    val bgColor = if (insight.isPositive)
        Color(0xFF4CAF50).copy(alpha = 0.06f)
    else Color(0xFFFF9800).copy(alpha = 0.06f)
    val borderColor = if (insight.isPositive)
        Color(0xFF4CAF50).copy(alpha = 0.15f)
    else Color(0xFFFF9800).copy(alpha = 0.15f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = insight.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = insight.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            // Weight context if available
            if (stats.hasPreviousReading && abs(stats.weightChange) > 0.1f) {
                Spacer(modifier = Modifier.height(8.dp))
                val wSign = if (stats.weightChange > 0) "+" else ""
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚖️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Weight changed ${wSign}${String.format(java.util.Locale.getDefault(), "%.1f", stats.weightChange)} kg since last reading",
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
