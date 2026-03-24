package com.health.calculator.bmi.tracker.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.model.BpHomeCardInfo
import com.health.calculator.bmi.tracker.ui.screens.bloodpressure.getBpCategoryColor

@Composable
fun BpHomeCardOverlay(
    info: BpHomeCardInfo,
    modifier: Modifier = Modifier
) {
    if (!info.hasReading) return

    val categoryColor = getBpCategoryColor(info.lastCategory)

    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Last reading
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )
            Text(
                "${info.lastSystolic}/${info.lastDiastolic}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = categoryColor
            )
            Text(
                "mmHg",
                style = MaterialTheme.typography.labelSmall,
                color = categoryColor.copy(alpha = 0.6f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                info.lastCategory.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = categoryColor.copy(alpha = 0.8f)
            )
            Text(
                "• ${info.lastReadingTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        }

        // Streak
        if (info.streakDays > 0) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🔥",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "${info.streakDays}d streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF9800).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Alert indicator for concerning readings
        if (info.isConcerning) {
            val infiniteTransition = rememberInfiniteTransition(label = "concern_pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "concern_alpha"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336).copy(alpha = pulseAlpha),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    "Needs attention",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF44336).copy(alpha = pulseAlpha)
                )
            }
        }
    }
}
