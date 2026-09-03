package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.HealthCategoryColor
import com.health.calculator.bmi.tracker.data.model.HealthMetricSummary
import com.health.calculator.bmi.tracker.ui.theme.ActionRowShape
import com.health.calculator.bmi.tracker.ui.theme.HealthColors
import com.health.calculator.bmi.tracker.ui.theme.WellnessMetricTextStyle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HealthOverviewCard(
    metric: HealthMetricSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (metric.categoryColor) {
        HealthCategoryColor.EXCELLENT -> HealthColors.Healthy
        HealthCategoryColor.GOOD -> HealthColors.Good
        HealthCategoryColor.MODERATE -> HealthColors.Warning
        HealthCategoryColor.WARNING -> HealthColors.Caution
        HealthCategoryColor.DANGER -> HealthColors.Danger
        HealthCategoryColor.NEUTRAL -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ActionRowShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WellnessIconBadge(
                icon = metricIcon(metric.label),
                tint = categoryColor,
                container = categoryColor.copy(alpha = 0.13f),
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = metric.value,
                    style = WellnessMetricTextStyle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = metric.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatRelativeTime(metric.lastUpdated),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun metricIcon(label: String) = when {
    label.contains("blood", ignoreCase = true) -> Icons.Outlined.MonitorHeart
    label.contains("heart", ignoreCase = true) -> Icons.Outlined.FavoriteBorder
    label.contains("weight", ignoreCase = true) -> Icons.Outlined.ShowChart
    else -> Icons.Outlined.Assessment
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
