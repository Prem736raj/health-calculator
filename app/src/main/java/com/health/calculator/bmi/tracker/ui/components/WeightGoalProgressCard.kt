package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.WeightGoalProgress
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightGoalProgressCard(
    progress: WeightGoalProgress,
    useMetric: Boolean,
    modifier: Modifier = Modifier
) {
    val unit = if (useMetric) "kg" else "lbs"
    val multiplier = if (useMetric) 1.0 else 2.20462

    val animatedProgress by animateFloatAsState(
        targetValue = progress.percentageComplete.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "goal_progress"
    )

    val progressColor = when {
        progress.isGoalReached -> Color(0xFF4CAF50)
        progress.percentageComplete >= 0.75f -> Color(0xFF2196F3)
        progress.percentageComplete >= 0.5f -> Color(0xFF00BCD4)
        progress.percentageComplete >= 0.25f -> Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (progress.isGoalReached)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
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
                    text = if (progress.isGoalReached) "🎉 Goal Reached!" else "Goal Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (progress.isGoalReached) Color(0xFF4CAF50)
                    else MaterialTheme.colorScheme.onSurface
                )

                if (progress.isGoalReached) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(6.dp))
                        .background(progressColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Percentage and remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress.percentageComplete * 100).toInt()}% complete",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = progressColor
                )
                if (!progress.isGoalReached) {
                    Text(
                        text = "${String.format("%.1f", progress.remainingToGoal * multiplier)} $unit to ${progress.directionLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GoalInfoItem(
                    label = "Start",
                    value = String.format("%.1f", progress.startingWeight * multiplier),
                    unit = unit
                )
                GoalInfoItem(
                    label = "Current",
                    value = String.format("%.1f", progress.currentWeight * multiplier),
                    unit = unit,
                    isHighlighted = true
                )
                GoalInfoItem(
                    label = "Goal",
                    value = String.format("%.1f", progress.goalWeight * multiplier),
                    unit = unit
                )
            }

            // Estimated completion date
            if (!progress.isGoalReached && progress.estimatedCompletionDate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                val daysLeft = progress.estimatedDaysRemaining ?: 0
                val weeksLeft = daysLeft / 7

                Text(
                    text = "📅 Estimated completion: ${sdf.format(Date(progress.estimatedCompletionDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "~${if (weeksLeft > 0) "$weeksLeft weeks" else "$daysLeft days"} at current pace",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Milestone celebrations
            if (!progress.isGoalReached) {
                val nextMilestone = when {
                    progress.percentageComplete < 0.25f -> 25
                    progress.percentageComplete < 0.50f -> 50
                    progress.percentageComplete < 0.75f -> 75
                    else -> 100
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Next milestone: $nextMilestone%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun GoalInfoItem(
    label: String,
    value: String,
    unit: String,
    isHighlighted: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isHighlighted) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
