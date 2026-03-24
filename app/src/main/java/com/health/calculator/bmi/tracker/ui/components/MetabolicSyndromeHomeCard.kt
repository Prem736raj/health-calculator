package com.health.calculator.bmi.tracker.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.MetabolicSyndromeTrackingRepository
import com.health.calculator.bmi.tracker.ui.theme.*

@Composable
fun MetabolicSyndromeLastResult(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { MetabolicSyndromeTrackingRepository(context) }
    val latestRecord = remember { repository.getLatestRecord() }

    if (latestRecord != null) {
        val riskColor = when {
            latestRecord.criteriaMet >= 4 -> Color(0xFFB71C1C)
            latestRecord.criteriaMet >= 3 -> HealthRed
            latestRecord.criteriaMet >= 2 -> HealthOrange
            latestRecord.criteriaMet >= 1 -> HealthYellow
            else -> HealthGreen
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(riskColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${latestRecord.criteriaMet}/5 • ${latestRecord.riskLevel}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = riskColor
            )
        }
    }
}
