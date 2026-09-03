package com.health.calculator.bmi.tracker.ui.components.home

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.util.*
import com.health.calculator.bmi.tracker.ui.components.WellnessIconBadge
import com.health.calculator.bmi.tracker.ui.theme.HealthColors
import com.health.calculator.bmi.tracker.ui.theme.WellnessMetricTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScoreBreakdownSheet(
    healthScore: HealthScoreResult,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WellnessIconBadge(
                    icon = categoryIcon(healthScore.category),
                    tint = MaterialTheme.colorScheme.tertiary,
                    container = MaterialTheme.colorScheme.tertiaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.txt_health_score_breakdown),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${healthScore.totalScore}/100 — ${healthScore.category.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = healthScore.category.color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Score components
            healthScore.scoreBreakdown.forEachIndexed { index, component ->
                ScoreComponentCard(component = component)
                if (index < healthScore.scoreBreakdown.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // How score is calculated
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.txt_how_your_score_is_calculated),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Wellness Score reflects whether ${healthScore.availableMetrics} of ${healthScore.totalMetrics} selected metrics were recorded or logged. " +
                                "It is a custom informational consistency measure—not a clinical score or diagnosis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val metricsList = listOf(
                        "BMI recorded" to "20 pts",
                        "Blood pressure recorded" to "20 pts",
                        "Waist/hip ratio recorded" to "15 pts",
                        "Hydration logged" to "15 pts",
                        "Calories logged" to "15 pts",
                        "Resting heart rate recorded" to "15 pts"
                    )

                    metricsList.forEach { (metric, points) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = metric,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = points,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Close button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.txt_got_it), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ScoreComponentCard(
    component: HealthScoreComponent
) {
    val statusColor = when (component.status) {
        ComponentStatus.EXCELLENT -> HealthColors.Healthy
        ComponentStatus.GOOD -> HealthColors.Good
        ComponentStatus.FAIR -> HealthColors.Warning
        ComponentStatus.POOR -> HealthColors.Caution
        ComponentStatus.NO_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val progress = if (component.hasData) component.points.toFloat() / component.maxPoints else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (component.hasData) statusColor.copy(alpha = 0.04f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Consistent vector badge and status dot
            Box(contentAlignment = Alignment.BottomEnd) {
                WellnessIconBadge(
                    icon = componentIcon(component.name),
                    tint = statusColor,
                    container = statusColor.copy(alpha = 0.12f)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name and status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = component.statusMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )

                // Progress bar
                if (component.hasData) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(statusColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Points
            Column(horizontalAlignment = Alignment.End) {
                if (component.hasData) {
                    Text(
                        text = "+${component.points}",
                        style = WellnessMetricTextStyle,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Text(
                        text = "/${component.maxPoints} pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = stringResource(R.string.txt_no_data),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun categoryIcon(category: HealthScoreCategory) = when (category) {
    HealthScoreCategory.EXCELLENT, HealthScoreCategory.GOOD -> Icons.Outlined.ShowChart
    HealthScoreCategory.FAIR -> Icons.Outlined.Assessment
    HealthScoreCategory.NEEDS_ATTENTION, HealthScoreCategory.CONCERNING -> Icons.Outlined.Flag
    HealthScoreCategory.INSUFFICIENT_DATA -> Icons.Outlined.Assessment
}

private fun componentIcon(name: String) = when {
    name.contains("BMI", ignoreCase = true) -> Icons.Outlined.Assessment
    name.contains("Pressure", ignoreCase = true) -> Icons.Outlined.MonitorHeart
    name.contains("Waist", ignoreCase = true) -> Icons.Outlined.ShowChart
    name.contains("Hydration", ignoreCase = true) -> Icons.Outlined.WaterDrop
    name.contains("Calories", ignoreCase = true) -> Icons.Outlined.LocalDining
    name.contains("Heart", ignoreCase = true) -> Icons.Outlined.FavoriteBorder
    else -> Icons.Outlined.ShowChart
}
