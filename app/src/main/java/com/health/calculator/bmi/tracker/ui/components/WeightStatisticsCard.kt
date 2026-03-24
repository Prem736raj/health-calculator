package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.WeightTrendDirection
import com.health.calculator.bmi.tracker.data.model.WeightStatistics
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightStatisticsCard(
    statistics: WeightStatistics,
    useMetric: Boolean,
    modifier: Modifier = Modifier
) {
    if (statistics.totalEntries == 0) return

    val unit = if (useMetric) "kg" else "lbs"
    val multiplier = if (useMetric) 1.0 else 2.20462

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Trend indicator
                val trendColor = when (statistics.trendDirection) {
                    WeightTrendDirection.LOSING -> Color(0xFF4CAF50)
                    WeightTrendDirection.GAINING -> Color(0xFFFF9800)
                    WeightTrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "${statistics.trendDirection.emoji} ${statistics.trendDirection.label}",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = trendColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Current",
                    value = statistics.currentWeight?.let {
                        String.format("%.1f %s", it * multiplier, unit)
                    } ?: "—",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Starting",
                    value = statistics.startingWeight?.let {
                        String.format("%.1f %s", it * multiplier, unit)
                    } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Lowest",
                    value = statistics.lowestWeight?.let {
                        String.format("%.1f %s", it * multiplier, unit)
                    } ?: "—",
                    modifier = Modifier.weight(1f),
                    valueColor = Color(0xFF4CAF50)
                )
                StatItem(
                    label = "Highest",
                    value = statistics.highestWeight?.let {
                        String.format("%.1f %s", it * multiplier, unit)
                    } ?: "—",
                    modifier = Modifier.weight(1f),
                    valueColor = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Total Change",
                    value = statistics.totalChange?.let {
                        val formatted = String.format("%.1f %s", kotlin.math.abs(it * multiplier), unit)
                        if (it >= 0) "+$formatted" else "-$formatted"
                    } ?: "—",
                    modifier = Modifier.weight(1f),
                    valueColor = statistics.totalChange?.let {
                        if (it <= 0) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    }
                )
                StatItem(
                    label = "Avg Weekly",
                    value = statistics.averageWeeklyChange?.let {
                        val formatted = String.format("%.2f %s/wk", kotlin.math.abs(it * multiplier), unit)
                        if (it >= 0) "+$formatted" else "-$formatted"
                    } ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${statistics.totalEntries} entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                statistics.firstEntryDate?.let {
                    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    Text(
                        text = "Since ${sdf.format(Date(it))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
