package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.animation.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.IBWHistoryEntry
import com.health.calculator.bmi.tracker.data.repository.IBWStatistics
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun IBWHistorySection(
    entries: List<IBWHistoryEntry>,
    statistics: IBWStatistics,
    showInKg: Boolean,
    onDeleteEntry: (Long) -> Unit
) {
    val factor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "IBW History & Trends",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (entries.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📊", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No IBW calculations yet",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Calculate your ideal weight and save results to track your progress over time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            return
        }

        // Statistics Summary
        StatisticsSummaryCard(statistics, showInKg)

        // Trend Graph (actual vs ideal)
        val entriesWithWeight = entries.filter { it.currentWeightKg != null }
        if (entriesWithWeight.size >= 2) {
            WeightTrendGraph(
                entries = entriesWithWeight.reversed(),
                showInKg = showInKg
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save at least 2 calculations with your current weight to see trends.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Recent entries list
        Text(
            text = "Recent Calculations",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )

        entries.take(10).forEach { entry ->
            HistoryEntryCard(
                entry = entry,
                showInKg = showInKg,
                dateFormat = dateFormat,
                timeFormat = timeFormat,
                onDelete = { onDeleteEntry(entry.id) }
            )
        }
    }
}

@Composable
private fun StatisticsSummaryCard(
    statistics: IBWStatistics,
    showInKg: Boolean
) {
    val factor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📈 Statistics",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total\nReadings",
                    value = "${statistics.totalEntries}",
                    color = MaterialTheme.colorScheme.primary
                )

                statistics.averagePercentOfIBW?.let { avg ->
                    StatItem(
                        label = "Average\n% of IBW",
                        value = "${"%.0f".format(avg)}%",
                        color = when {
                            avg in 90.0..110.0 -> Color(0xFF4CAF50)
                            avg in 80.0..120.0 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }

                statistics.weightChangeSinceFirstKg?.let { change ->
                    val displayChange = change * factor
                    StatItem(
                        label = "Weight\nChange",
                        value = "${if (displayChange > 0) "+" else ""}${"%.1f".format(displayChange)} $unit",
                        color = when {
                            abs(displayChange) < 0.5 -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            // Closest & Furthest from ideal
            statistics.closestToIdealEntry?.let { closest ->
                closest.currentWeightKg?.let { actualKg ->
                    val diff = abs(actualKg - closest.frameAdjustedDevineKg) * factor
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Closest to Ideal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = "${"%.1f".format(diff)} $unit from ideal on ${
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                            .format(Date(closest.timestamp))
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            statistics.furthestFromIdealEntry?.let { furthest ->
                furthest.currentWeightKg?.let { actualKg ->
                    val diff = abs(actualKg - furthest.frameAdjustedDevineKg) * factor
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📉", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Furthest from Ideal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFF9800)
                                )
                                Text(
                                    text = "${"%.1f".format(diff)} $unit from ideal on ${
                                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                            .format(Date(furthest.timestamp))
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun WeightTrendGraph(
    entries: List<IBWHistoryEntry>,
    showInKg: Boolean
) {
    val factor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"

    val actualWeights = entries.mapNotNull { it.currentWeightKg?.times(factor) }
    val idealWeights = entries.map { it.frameAdjustedDevineKg * factor }
    val allWeights = actualWeights + idealWeights
    val minW = (allWeights.minOrNull() ?: 40.0) - 5
    val maxW = (allWeights.maxOrNull() ?: 100.0) + 5
    val range = maxOf(0.1, maxW - minW).toDouble()

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "graphProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Weight vs Ideal Trend",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Actual Weight",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Ideal Weight",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val actualColor = MaterialTheme.colorScheme.primary
            val idealColor = Color(0xFF4CAF50)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val graphWidth = size.width
                val graphHeight = size.height
                val padding = 4.dp.toPx()

                val pointCount = entries.size
                if (pointCount < 2) return@Canvas

                val stepX = (graphWidth - padding * 2) / (pointCount - 1)

                // Draw actual weight line
                val actualPath = Path()
                entries.forEachIndexed { index, entry ->
                    val x = padding + index * stepX
                    val weight = (entry.currentWeightKg ?: 0.0) * factor
                    val y = graphHeight - ((weight - minW) / range * graphHeight).toFloat()
                    val animX = x * animatedProgress

                    if (index == 0) actualPath.moveTo(animX, y)
                    else actualPath.lineTo(animX, y)
                }
                drawPath(
                    path = actualPath,
                    color = actualColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw ideal weight line
                val idealPath = Path()
                entries.forEachIndexed { index, entry ->
                    val x = padding + index * stepX
                    val weight = entry.frameAdjustedDevineKg * factor
                    val y = graphHeight - ((weight - minW) / range * graphHeight).toFloat()
                    val animX = x * animatedProgress

                    if (index == 0) idealPath.moveTo(animX, y)
                    else idealPath.lineTo(animX, y)
                }
                drawPath(
                    path = idealPath,
                    color = idealColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(10f, 10f)
                        )
                    )
                )

                // Draw data points
                entries.forEachIndexed { index, entry ->
                    val x = (padding + index * stepX) * animatedProgress
                    val actualW = (entry.currentWeightKg ?: 0.0) * factor
                    val idealW = entry.frameAdjustedDevineKg * factor

                    val actualY = graphHeight - ((actualW - minW) / range * graphHeight).toFloat()
                    val idealY = graphHeight - ((idealW - minW) / range * graphHeight).toFloat()

                    drawCircle(color = actualColor, radius = 5.dp.toPx(), center = Offset(x, actualY))
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(x, actualY))
                    drawCircle(color = idealColor, radius = 4.dp.toPx(), center = Offset(x, idealY))
                    drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, idealY))
                }
            }

            // Y-axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${"%.0f".format(minW)} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 9.sp
                )
                Text(
                    "${"%.0f".format(maxW)} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(
    entry: IBWHistoryEntry,
    showInKg: Boolean,
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    val factor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "IBW: ${"%.1f".format(entry.frameAdjustedDevineKg * factor)} $unit",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${dateFormat.format(Date(entry.timestamp))} • ${timeFormat.format(Date(entry.timestamp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }

                Row {
                    entry.currentWeightKg?.let { actual ->
                        val diff = actual - entry.frameAdjustedDevineKg
                        val absDiff = abs(diff) * factor
                        val diffColor = when {
                            abs(diff) < entry.frameAdjustedDevineKg * 0.1 -> Color(0xFF4CAF50)
                            abs(diff) < entry.frameAdjustedDevineKg * 0.2 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = diffColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${if (diff > 0) "+" else ""}${"%.1f".format(diff * factor)} $unit",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = diffColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Details",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(4.dp))

                    DetailRow("Height", "${"%.1f".format(entry.heightCm)} cm")
                    DetailRow("Gender", entry.gender)
                    DetailRow("Frame Size", entry.frameSize)
                    entry.age?.let { DetailRow("Age", "$it years") }
                    entry.currentWeightKg?.let {
                        DetailRow("Actual Weight", "${"%.1f".format(it * factor)} $unit")
                    }
                    DetailRow("Devine", "${"%.1f".format(entry.devineKg * factor)} $unit")
                    DetailRow("Robinson", "${"%.1f".format(entry.robinsonKg * factor)} $unit")
                    DetailRow("Miller", "${"%.1f".format(entry.millerKg * factor)} $unit")
                    DetailRow("Hamwi", "${"%.1f".format(entry.hamwiKg * factor)} $unit")
                    DetailRow("Broca", "${"%.1f".format(entry.brocaKg * factor)} $unit")
                    DetailRow("BMI Range", "${"%.1f".format(entry.bmiLowerKg * factor)} - ${"%.1f".format(entry.bmiUpperKg * factor)} $unit")
                    entry.weightCategory?.let { DetailRow("Category", it) }
                    entry.leanBodyWeightKg?.let {
                        DetailRow("Lean Body Weight", "${"%.1f".format(it * factor)} $unit")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp
        )
    }
}
