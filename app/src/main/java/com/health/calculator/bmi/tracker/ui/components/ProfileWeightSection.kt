package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.WeightStatistics
import com.health.calculator.bmi.tracker.data.model.WeightTrendDirection

@Composable
fun ProfileWeightSection(
    latestWeight: Double?,
    statistics: WeightStatistics?,
    useMetric: Boolean,
    onLogWeight: () -> Unit,
    onViewTrends: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unit = if (useMetric) "kg" else "lbs"
    val multiplier = if (useMetric) 1.0 else 2.20462

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.MonitorWeight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Weight Tracking",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (statistics != null && statistics.totalEntries > 0) {
                    val trendColor = when (statistics.trendDirection) {
                        WeightTrendDirection.LOSING -> MaterialTheme.colorScheme.primary
                        WeightTrendDirection.GAINING -> MaterialTheme.colorScheme.error
                        WeightTrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = "${statistics.trendDirection.emoji} ${statistics.trendDirection.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (latestWeight != null) {
                Text(
                    text = String.format("%.1f %s", latestWeight * multiplier, unit),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                statistics?.averageWeeklyChange?.let { weekly ->
                    val weeklyFormatted = String.format("%.2f", kotlin.math.abs(weekly * multiplier))
                    val direction = if (weekly < 0) "losing" else "gaining"
                    Text(
                        text = "~$weeklyFormatted $unit/week ($direction)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No weight logged yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLogWeight,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Weight")
                }

                OutlinedButton(
                    onClick = onViewTrends,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Outlined.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Trends")
                }
            }
        }
    }
}
