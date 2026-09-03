package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingFlat
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.WeightStatistics
import com.health.calculator.bmi.tracker.data.model.WeightTrendDirection
import com.health.calculator.bmi.tracker.ui.theme.HealthColors
import com.health.calculator.bmi.tracker.ui.theme.MetricTileShape
import com.health.calculator.bmi.tracker.ui.theme.WellnessMetricTextStyle

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
        shape = MetricTileShape,
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
                        text = stringResource(R.string.txt_weight_tracking),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (statistics != null && statistics.totalEntries > 0) {
                    val trendColor = when (statistics.trendDirection) {
                        WeightTrendDirection.LOSING -> HealthColors.Healthy
                        WeightTrendDirection.GAINING -> HealthColors.Warning
                        WeightTrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val trendIcon = when (statistics.trendDirection) {
                        WeightTrendDirection.LOSING -> Icons.Outlined.TrendingDown
                        WeightTrendDirection.GAINING -> Icons.Outlined.TrendingUp
                        WeightTrendDirection.STABLE -> Icons.Outlined.TrendingFlat
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(statistics.trendDirection.label, style = MaterialTheme.typography.labelSmall, color = trendColor, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (latestWeight != null) {
                Text(
                    text = String.format("%.1f %s", latestWeight * multiplier, unit),
                    style = WellnessMetricTextStyle.copy(fontSize = 28.sp, lineHeight = 32.sp),
                    fontWeight = FontWeight.SemiBold,
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
                    text = stringResource(R.string.txt_no_weight_logged_yet),
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
                    Text(stringResource(R.string.txt_log_weight))
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
                    Text(stringResource(R.string.txt_view_trends))
                }
            }
        }
    }
}
