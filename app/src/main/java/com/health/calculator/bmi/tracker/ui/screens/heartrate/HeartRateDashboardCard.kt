package com.health.calculator.bmi.tracker.ui.screens.heartrate

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.util.HeartRateZoneResult

@Composable
fun HeartRateDashboardCard(
    maxHR: Int?,
    lastZoneSummary: String?, // e.g., "Z1: 95-114 | Z2: 114-133 | ..."
    lastExerciseZone: Int?, // last logged zone number, null if none
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasData = maxHR != null

    // Pulsing heart animation
    val infiniteTransition = rememberInfiniteTransition(label = "dash_heartbeat")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                1f at 0
                1.12f at 120
                1f at 280
                1.08f at 380
                1f at 500
                1f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_heart_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Heart icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❤️",
                    fontSize = (22 * if (hasData) heartScale else 1f).sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Heart Rate Zones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                if (hasData) {
                    Text(
                        text = "Max HR: $maxHR BPM",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Medium
                    )

                    // Quick zone summary
                    lastZoneSummary?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp
                        )
                    }

                    // Last exercise zone indicator
                    lastExerciseZone?.let { zone ->
                        Spacer(modifier = Modifier.height(4.dp))
                        val zoneColor = when (zone) {
                            1 -> Color(0xFF90CAF9)
                            2 -> Color(0xFF42A5F5)
                            3 -> Color(0xFF66BB6A)
                            4 -> Color(0xFFFFA726)
                            5 -> Color(0xFFEF5350)
                            else -> Color(0xFF9E9E9E)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = zoneColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏃 Last: Zone $zone",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = zoneColor,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Calculate your training zones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Generates a quick zone summary string for the dashboard card
 */
fun generateQuickZoneSummary(result: HeartRateZoneResult): String {
    return result.zones.joinToString(" | ") { zone ->
        "Z${zone.zoneNumber}: ${zone.bpmLow}-${zone.bpmHigh}"
    }
}
