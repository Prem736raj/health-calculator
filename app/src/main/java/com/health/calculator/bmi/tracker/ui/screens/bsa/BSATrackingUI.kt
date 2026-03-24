package com.health.calculator.bmi.tracker.ui.screens.bsa

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.BSARecord
import com.health.calculator.bmi.tracker.data.model.BSAStatistics
import com.health.calculator.bmi.tracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun BSATrackingSection(
    records: List<BSARecord>,
    statistics: BSAStatistics?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "BSA Progress & History",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (records.isEmpty()) {
            EmptyTrackingState()
        } else {
            // Statistics
            if (statistics != null) {
                BSAStatisticsCard(stats = statistics)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Comparison with previous
            if (records.size >= 2) {
                PreviousComparisonCard(
                    current = records.first(),
                    previous = records[1]
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Trend Graph
            if (records.size >= 2) {
                BSATrendGraph(records = records.reversed()) // Chronological for graph
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                SingleReadingPrompt()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recent Readings
            BSAReadingsTimeline(records = records.take(10))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EmptyTrackingState() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No BSA readings yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Calculate and save your first BSA result to start tracking changes over time. This is especially useful for:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            UseCaseChip(emoji = "👶", text = "Monitoring growth in children")
            UseCaseChip(emoji = "⚖️", text = "Tracking weight change effects")
            UseCaseChip(emoji = "🏥", text = "Medical follow-ups over time")
        }
    }
}

@Composable
private fun UseCaseChip(emoji: String, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SingleReadingPrompt() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = HealthBlue.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ShowChart,
                contentDescription = null,
                tint = HealthBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "One more reading to see your trend",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Save another BSA calculation to unlock the trend graph and track changes over time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun BSAStatisticsCard(stats: BSAStatistics) {
    val changeColor = when {
        abs(stats.changeFromFirst) < 0.01f -> HealthGreen
        stats.changeFromFirst > 0 -> HealthOrange
        else -> HealthBlue
    }

    val changeArrow = when {
        abs(stats.changeFromFirst) < 0.01f -> "➡️"
        stats.changeFromFirst > 0 -> "↗️"
        else -> "↘️"
    }

    val changeWord = when {
        abs(stats.changeFromFirst) < 0.01f -> "Stable"
        stats.changeFromFirst > 0 -> "Increased"
        else -> "Decreased"
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BSA Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${stats.totalReadings} readings",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatValueColumn(
                    label = "Current",
                    value = "%.4f".format(stats.currentBSA),
                    unit = "m²",
                    color = MaterialTheme.colorScheme.primary
                )
                StatValueColumn(
                    label = "Average",
                    value = "%.4f".format(stats.averageBSA),
                    unit = "m²",
                    color = HealthBlue
                )
                StatValueColumn(
                    label = "First",
                    value = "%.4f".format(stats.firstBSA),
                    unit = "m²",
                    color = HealthTeal
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Change and formula row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Change from first
                Column {
                    Text(
                        text = "Overall Change",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(changeArrow, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$changeWord ${"%.4f".format(abs(stats.changeFromFirst))} m²",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                    }
                    Text(
                        text = "(${"%.1f".format(abs(stats.changePercent))}% ${if (stats.changeFromFirst >= 0) "increase" else "decrease"})",
                        style = MaterialTheme.typography.labelSmall,
                        color = changeColor.copy(alpha = 0.7f)
                    )
                }

                // Most used formula
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Most Used Formula",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stats.mostUsedFormula,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatValueColumn(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun PreviousComparisonCard(current: BSARecord, previous: BSARecord) {
    val bsaChange = current.bsaValue - previous.bsaValue
    val weightChange = current.weightKg - previous.weightKg
    val heightChange = current.heightCm - previous.heightCm

    val overallColor = when {
        abs(bsaChange) < 0.005f -> HealthGreen
        bsaChange > 0 -> HealthOrange
        else -> HealthBlue
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = overallColor.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Compare,
                    contentDescription = null,
                    tint = overallColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "vs Previous Reading",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChangeIndicator(
                    label = "BSA",
                    change = bsaChange,
                    format = "%.4f m²",
                    positiveIsBad = true
                )
                ChangeIndicator(
                    label = "Weight",
                    change = weightChange,
                    format = "%.1f kg",
                    positiveIsBad = true
                )
                ChangeIndicator(
                    label = "Height",
                    change = heightChange,
                    format = "%.1f cm",
                    positiveIsBad = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Previous: ${previous.dateTime.take(10)} • Current: ${current.dateTime.take(10)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ChangeIndicator(
    label: String,
    change: Float,
    format: String,
    positiveIsBad: Boolean
) {
    val isPositive = change > 0
    val isNeutral = abs(change) < 0.001f

    val color = when {
        isNeutral -> HealthGreen
        positiveIsBad && isPositive -> HealthOrange
        positiveIsBad && !isPositive -> HealthBlue
        !positiveIsBad && isPositive -> HealthGreen
        else -> HealthOrange
    }

    val arrow = when {
        isNeutral -> "—"
        isPositive -> "▲"
        else -> "▼"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = arrow,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = if (isNeutral) "No change" else format.format(abs(change)),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun BSATrendGraph(records: List<BSARecord>) {
    val sorted = remember(records) { records.sortedBy { it.timestamp } }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "bsa_graph_anim"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "BSA Trend Over Time",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track growth or weight change effects",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val lineColor = MaterialTheme.colorScheme.primary
            val dotColor = MaterialTheme.colorScheme.primary
            val avgLineColor = HealthGreen.copy(alpha = 0.5f)
            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant
            val density = LocalDensity.current

            val values = sorted.map { it.bsaValue }
            val avgValue = values.average().toFloat()
            val minVal = (values.min() - 0.1f).coerceAtLeast(0f)
            val maxVal = values.max() + 0.1f
            val range = (maxVal - minVal).coerceAtLeast(0.01f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val pL = 45.dp.toPx()
                val pB = 30.dp.toPx()
                val pT = 10.dp.toPx()
                val pR = 16.dp.toPx()
                val gW = size.width - pL - pR
                val gH = size.height - pT - pB

                // Grid lines & Y labels
                val ySteps = 4
                for (i in 0..ySteps) {
                    val yVal = minVal + (range * i / ySteps)
                    val y = pT + gH - (gH * i / ySteps)
                    drawLine(gridColor, Offset(pL, y), Offset(size.width - pR, y), 1.dp.toPx())
                    drawContext.canvas.nativeCanvas.drawText(
                        "%.2f".format(yVal),
                        pL - 6.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textColor.hashCode()
                            textSize = with(density) { 10.sp.toPx() }
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )
                }

                // Average line (dashed)
                val avgY = pT + gH - (gH * (avgValue - minVal) / range)
                drawLine(
                    color = avgLineColor,
                    start = Offset(pL, avgY),
                    end = Offset(size.width - pR, avgY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "Avg",
                    size.width - pR + 2.dp.toPx(),
                    avgY - 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = avgLineColor.hashCode()
                        textSize = with(density) { 9.sp.toPx() }
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )

                // Data points
                val points = sorted.mapIndexed { idx, record ->
                    val x = pL + (gW * idx / (sorted.size - 1).coerceAtLeast(1))
                    val y = pT + gH - (gH * (record.bsaValue - minVal) / range)
                    Offset(x, y)
                }

                // Animated line
                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        val visibleCount = (animatedProgress * points.size).toInt().coerceAtLeast(1)
                        for (i in 1 until visibleCount.coerceAtMost(points.size)) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(path, lineColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                }

                // Dots
                val visibleCount = (animatedProgress * points.size).toInt().coerceAtLeast(1)
                points.forEachIndexed { idx, pt ->
                    if (idx < visibleCount) {
                        drawCircle(Color.White, 7.dp.toPx(), pt)
                        drawCircle(dotColor, 5.dp.toPx(), pt)
                    }
                }

                // X labels
                val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                val step = (sorted.size / 4).coerceAtLeast(1)
                sorted.forEachIndexed { idx, record ->
                    if (idx % step == 0 || idx == sorted.size - 1) {
                        val x = pL + (gW * idx / (sorted.size - 1).coerceAtLeast(1))
                        drawContext.canvas.nativeCanvas.drawText(
                            dateFormat.format(Date(record.timestamp)),
                            x,
                            size.height - 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = textColor.hashCode()
                                textSize = with(density) { 10.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }

            // Legend
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendItem(color = lineColor, label = "BSA Value")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(color = avgLineColor, label = "Average", dashed = true)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, dashed: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (dashed) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(color, RoundedCornerShape(1.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BSAReadingsTimeline(records: List<BSARecord>) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recent Readings",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            records.forEachIndexed { index, record ->
                val changeText = if (index < records.size - 1) {
                    val prev = records[index + 1]
                    val diff = record.bsaValue - prev.bsaValue
                    when {
                        abs(diff) < 0.001f -> "→ No change"
                        diff > 0 -> "↑ +${"%.4f".format(diff)}"
                        else -> "↓ ${"%.4f".format(diff)}"
                    }
                } else null

                val changeColor = if (index < records.size - 1) {
                    val prev = records[index + 1]
                    val diff = record.bsaValue - prev.bsaValue
                    when {
                        abs(diff) < 0.001f -> HealthGreen
                        diff > 0 -> HealthOrange
                        else -> HealthBlue
                    }
                } else null

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timeline dot + line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                        )
                        if (index < records.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Content
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = record.dateTime.take(16),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${"%.4f".format(record.bsaValue)} m²",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (index == 0) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                            if (changeText != null && changeColor != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = changeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = changeColor
                                )
                            }
                        }
                        Text(
                            text = "${"%.1f".format(record.weightKg)} kg • ${"%.1f".format(record.heightCm)} cm",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Formula badge
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = record.formulaName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
