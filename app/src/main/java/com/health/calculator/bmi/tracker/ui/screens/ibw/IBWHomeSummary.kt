package com.health.calculator.bmi.tracker.ui.screens.ibw

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

data class IBWHomeSummaryData(
    val idealWeightKg: Double?,
    val currentWeightKg: Double?,
    val differenceKg: Double?,
    val frameSize: String?,
    val hasData: Boolean = idealWeightKg != null
)

@Composable
fun IBWHomeSummaryContent(
    data: IBWHomeSummaryData,
    showInKg: Boolean
) {
    if (!data.hasData || data.idealWeightKg == null) return

    val factor = if (showInKg) 1.0 else 2.20462
    val unit = if (showInKg) "kg" else "lbs"
    val idealDisplay = "${"%.1f".format(data.idealWeightKg * factor)} $unit"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Ideal: $idealDisplay",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
            data.differenceKg?.let { diff ->
                val absDiff = abs(diff) * factor
                val diffText = when {
                    abs(diff) < 0.5 -> "At ideal weight ✅"
                    diff > 0 -> "${"%.1f".format(absDiff)} $unit above ideal"
                    else -> "${"%.1f".format(absDiff)} $unit below ideal"
                }
                val diffColor = when {
                    abs(diff) < 0.5 -> Color(0xFF4CAF50)
                    abs(diff) < 5.0 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
                Text(
                    text = diffText,
                    style = MaterialTheme.typography.bodySmall,
                    color = diffColor,
                    fontSize = 10.sp
                )
            }
        }
        data.frameSize?.let { frame ->
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "$frame frame",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
