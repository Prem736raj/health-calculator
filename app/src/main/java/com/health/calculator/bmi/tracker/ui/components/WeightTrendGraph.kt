package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.WeightEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun WeightTrendGraph(
    entries: List<WeightEntry>,
    goalWeightKg: Double?,
    useMetric: Boolean,
    modifier: Modifier = Modifier,
    onDataPointTap: (WeightEntry) -> Unit = {}
) {
    if (entries.size < 2) {
        NotEnoughDataCard(entryCount = entries.size, modifier = modifier)
        return
    }

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "graph_anim"
    )

    val sortedEntries = remember(entries) { entries.sortedBy { it.dateMillis } }

    val weights = sortedEntries.map { if (useMetric) it.weightKg else it.weightLbs }
    val allValues = weights.toMutableList()
    goalWeightKg?.let {
        allValues.add(if (useMetric) it else it * 2.20462)
    }

    val minWeight = (allValues.min() - 2).coerceAtLeast(0.0)
    val maxWeight = allValues.max() + 2
    val weightRange = maxWeight - minWeight

    val lineColor = MaterialTheme.colorScheme.primary
    val goalColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val dotColor = MaterialTheme.colorScheme.primary

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    var selectedPoint by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Weight Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(sortedEntries) {
                        detectTapGestures { offset ->
                            val paddingLeft = 45f
                            val paddingRight = 16f
                            val graphWidth = size.width - paddingLeft - paddingRight

                            if (sortedEntries.size < 2) return@detectTapGestures

                            val stepX = graphWidth / (sortedEntries.size - 1)
                            val index = ((offset.x - paddingLeft) / stepX)
                                .toInt()
                                .coerceIn(0, sortedEntries.lastIndex)

                            selectedPoint = index
                            onDataPointTap(sortedEntries[index])
                        }
                    }
            ) {
                val paddingLeft = 45f
                val paddingRight = 16f
                val paddingTop = 16f
                val paddingBottom = 30f
                val graphWidth = size.width - paddingLeft - paddingRight
                val graphHeight = size.height - paddingTop - paddingBottom

                // Grid lines
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = paddingTop + (graphHeight * i / gridLines)
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width - paddingRight, y),
                        strokeWidth = 1f
                    )

                    val labelValue = maxWeight - (weightRange * i / gridLines)
                    val labelText = String.format("%.0f", labelValue)
                    val measured = textMeasurer.measure(labelText, labelStyle)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            paddingLeft - measured.size.width - 6f,
                            y - measured.size.height / 2
                        )
                    )
                }

                // Goal weight dashed line
                goalWeightKg?.let { goal ->
                    val goalVal = if (useMetric) goal else goal * 2.20462
                    val goalY = paddingTop + ((maxWeight - goalVal) / weightRange * graphHeight).toFloat()

                    if (goalY in paddingTop..paddingTop + graphHeight) {
                        val dashPath = Path().apply {
                            moveTo(paddingLeft, goalY)
                            lineTo(size.width - paddingRight, goalY)
                        }
                        drawPath(
                            path = dashPath,
                            color = goalColor,
                            style = Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(12f, 8f)
                                )
                            )
                        )

                        val goalLabel = "Goal: ${String.format("%.0f", goalVal)}"
                        val goalMeasured = textMeasurer.measure(
                            goalLabel,
                            labelStyle.copy(color = goalColor, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textLayoutResult = goalMeasured,
                            topLeft = Offset(
                                size.width - paddingRight - goalMeasured.size.width,
                                goalY - goalMeasured.size.height - 4f
                            )
                        )
                    }
                }

                if (sortedEntries.size >= 2) {
                    val stepX = graphWidth / (sortedEntries.size - 1)

                    // Determine trend color per segment
                    val points = sortedEntries.mapIndexed { index, entry ->
                        val x = paddingLeft + (stepX * index)
                        val value = if (useMetric) entry.weightKg else entry.weightLbs
                        val y = paddingTop + ((maxWeight - value) / weightRange * graphHeight).toFloat()
                        Offset(x, y)
                    }

                    // Draw colored trend line segments
                    val animatedCount = (points.size * animationProgress).toInt().coerceAtLeast(1)
                    for (i in 0 until animatedCount - 1) {
                        val isTowardGoal = goalWeightKg?.let { goal ->
                            val goalVal = if (useMetric) goal else goal * 2.20462
                            val currentVal = weights[i + 1]
                            val prevVal = weights[i]
                            if (goalVal < prevVal) currentVal < prevVal // losing toward goal
                            else currentVal > prevVal // gaining toward goal
                        } ?: true

                        drawLine(
                            color = if (isTowardGoal) Color(0xFF4CAF50) else Color(0xFFF44336),
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw data points
                    for (i in 0 until animatedCount) {
                        val isSelected = selectedPoint == i
                        val radius = if (isSelected) 8f else 5f

                        drawCircle(
                            color = Color.White,
                            radius = radius + 2f,
                            center = points[i]
                        )
                        drawCircle(
                            color = dotColor,
                            radius = radius,
                            center = points[i]
                        )
                    }

                    // Date labels
                    val labelIndices = when {
                        sortedEntries.size <= 5 -> sortedEntries.indices.toList()
                        else -> listOf(0, sortedEntries.size / 2, sortedEntries.lastIndex)
                    }
                    val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())

                    labelIndices.forEach { idx ->
                        if (idx < sortedEntries.size) {
                            val dateLabel = dateFmt.format(Date(sortedEntries[idx].dateMillis))
                            val dateMeasured = textMeasurer.measure(dateLabel, labelStyle)
                            val x = paddingLeft + (stepX * idx)
                            drawText(
                                textLayoutResult = dateMeasured,
                                topLeft = Offset(
                                    x - dateMeasured.size.width / 2,
                                    size.height - dateMeasured.size.height
                                )
                            )
                        }
                    }
                }
            }

            // Selected point tooltip
            selectedPoint?.let { idx ->
                if (idx < sortedEntries.size) {
                    val entry = sortedEntries[idx]
                    val dateFmt = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = entry.formattedWeight(useMetric),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = dateFmt.format(Date(entry.dateMillis)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotEnoughDataCard(entryCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📈", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (entryCount == 0) "No weight entries yet"
                else "Log one more entry to see your trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your weight regularly to see progress over time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
