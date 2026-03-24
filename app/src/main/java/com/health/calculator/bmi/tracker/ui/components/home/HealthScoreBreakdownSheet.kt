package com.health.calculator.bmi.tracker.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.util.*

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
                Text(
                    text = healthScore.category.emoji,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Health Score Breakdown",
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
                ScoreComponentCard(
                    component = component,
                    index = index
                )
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
                        text = "📊 How Your Score is Calculated",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your health score is based on ${healthScore.availableMetrics} of ${healthScore.totalMetrics} tracked metrics. " +
                                "The score is normalized to 100 based on available data. " +
                                "Track more metrics for a more complete picture of your health!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val metricsList = listOf(
                        "BMI in normal range" to "20 pts",
                        "BP in optimal/normal" to "20 pts",
                        "WHR in low risk" to "15 pts",
                        "Meeting water goal" to "15 pts",
                        "Meeting calorie target" to "15 pts",
                        "Resting HR healthy" to "15 pts"
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
                Text("Got It", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ScoreComponentCard(
    component: HealthScoreComponent,
    index: Int
) {
    val statusColor = when (component.status) {
        ComponentStatus.EXCELLENT -> Color(0xFF4CAF50)
        ComponentStatus.GOOD -> Color(0xFF2196F3)
        ComponentStatus.FAIR -> Color(0xFFFFC107)
        ComponentStatus.POOR -> Color(0xFFF44336)
        ComponentStatus.NO_DATA -> Color(0xFF9E9E9E)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (component.hasData) component.points.toFloat() / component.maxPoints else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = index * 100,
            easing = FastOutSlowInEasing
        ),
        label = "component_progress_${component.name}"
    )

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
            // Emoji and status dot
            Box(contentAlignment = Alignment.BottomEnd) {
                Text(text = component.emoji, fontSize = 26.sp)
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
                                .fillMaxWidth(animatedProgress)
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
                        style = MaterialTheme.typography.titleMedium,
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
                        color = Color(0xFF9E9E9E).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "No data",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
