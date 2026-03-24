package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.util.CalorieEdgeCaseHandler.WarningSeverity

@Composable
fun CalorieSafetyWarningCard(
    message: String,
    severity: WarningSeverity,
    modifier: Modifier = Modifier
) {
    if (severity == WarningSeverity.NONE) return

    val containerColor = when (severity) {
        WarningSeverity.INFO -> Color(0xFF2196F3).copy(alpha = 0.1f)
        WarningSeverity.WARNING -> Color(0xFFFF9800).copy(alpha = 0.12f)
        WarningSeverity.DANGER -> Color(0xFFF44336).copy(alpha = 0.12f)
        WarningSeverity.NONE -> Color.Transparent
    }

    val iconColor = when (severity) {
        WarningSeverity.INFO -> Color(0xFF2196F3)
        WarningSeverity.WARNING -> Color(0xFFFF9800)
        WarningSeverity.DANGER -> Color(0xFFF44336)
        WarningSeverity.NONE -> Color.Transparent
    }

    val icon = when (severity) {
        WarningSeverity.INFO -> Icons.Default.Info
        WarningSeverity.WARNING -> Icons.Default.Warning
        WarningSeverity.DANGER -> Icons.Default.Error
        WarningSeverity.NONE -> Icons.Default.Info
    }

    AnimatedVisibility(
        visible = true,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
}
