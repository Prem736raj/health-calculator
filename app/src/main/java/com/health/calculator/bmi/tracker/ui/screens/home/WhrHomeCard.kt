package com.health.calculator.bmi.tracker.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.WhrCategory
import com.health.calculator.bmi.tracker.data.model.WhrHistoryEntry

@Composable
fun WhrHomeCardContent(
    lastEntry: WhrHistoryEntry?,
    modifier: Modifier = Modifier
) {
    if (lastEntry == null) return

    val riskColor = when (lastEntry.category) {
        WhrCategory.LOW_RISK -> Color(0xFF4CAF50)
        WhrCategory.MODERATE_RISK -> Color(0xFFFFA726)
        WhrCategory.HIGH_RISK -> Color(0xFFF44336)
    }

    val animatedColor by animateColorAsState(
        targetValue = riskColor,
        animationSpec = tween(500),
        label = "whr_home_color"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Last WHR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    String.format("%.2f", lastEntry.whr),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(animatedColor)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = animatedColor.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alert indicator for high risk
                if (lastEntry.category == WhrCategory.HIGH_RISK) {
                    Text("⚠️", fontSize = 10.sp)
                }
                Text(
                    lastEntry.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun WhrHomeAlertIndicator(
    lastEntry: WhrHistoryEntry?,
    modifier: Modifier = Modifier
) {
    if (lastEntry == null) return
    if (lastEntry.category != WhrCategory.HIGH_RISK) return

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color(0xFFF44336)
    ) {
        Box(
            modifier = Modifier.size(8.dp)
        )
    }
}
