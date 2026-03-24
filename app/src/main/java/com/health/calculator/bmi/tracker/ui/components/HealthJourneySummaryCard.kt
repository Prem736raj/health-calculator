package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.models.HealthJourneySummary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HealthJourneySummaryCard(
    summary: HealthJourneySummary,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.15f),
                            tertiaryColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌟 Your Health Journey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (summary.daysSinceFirstUse > 0) {
                    Text(
                        text = "You've been tracking your health for",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(summary.daysSinceFirstUse),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    summary.firstUseDate?.let { date ->
                        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                        Text(
                            text = "Since ${sdf.format(Date(date))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Welcome to your health journey!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    JourneyStatItem(
                        value = "${summary.totalCalculations}",
                        label = "Calculations",
                        icon = "📊"
                    )
                    JourneyStatItem(
                        value = "${summary.calculatorsUsed}/${summary.totalCalculatorsAvailable}",
                        label = "Calculators\nUsed",
                        icon = "🧮"
                    )
                    JourneyStatItem(
                        value = "${summary.milestonesEarned}",
                        label = "Milestones\nEarned",
                        icon = "🏅"
                    )
                    JourneyStatItem(
                        value = "${summary.personalRecordsSet}",
                        label = "Personal\nRecords",
                        icon = "🏆"
                    )
                }

                // Health score improvement
                if (summary.healthScoreChange != 0 && summary.currentHealthScore >= 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (summary.healthScoreChange > 0) Color(0xFF4CAF50)
                            else Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (summary.healthScoreChange > 0)
                                "Health score improved by ${summary.healthScoreChange} points!"
                            else "Health score changed by ${summary.healthScoreChange} points",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (summary.healthScoreChange > 0) Color(0xFF4CAF50)
                            else Color(0xFFFF9800)
                        )
                    }
                }

                // Most used calculator
                summary.mostUsedCalculator?.let { calc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Most used: $calc",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyStatItem(
    value: String,
    label: String,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDuration(days: Int): String {
    return when {
        days < 7 -> "$days day${if (days != 1) "s" else ""}"
        days < 30 -> "${days / 7} week${if (days / 7 != 1) "s" else ""}"
        days < 365 -> "${days / 30} month${if (days / 30 != 1) "s" else ""}"
        else -> {
            val years = days / 365
            val remainingMonths = (days % 365) / 30
            if (remainingMonths > 0) "$years yr${if (years > 1) "s" else ""} $remainingMonths mo"
            else "$years year${if (years > 1) "s" else ""}"
        }
    }
}
