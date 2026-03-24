package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.BSATrackingRepository
import com.health.calculator.bmi.tracker.ui.theme.*

/**
 * Shows last BSA result on the Home dashboard card.
 */
@Composable
fun BSALastResult(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { BSATrackingRepository(context) }
    // Get records as list and pick last
    val records by repository.getRecordsFlow().collectAsState(initial = emptyList())
    val latestRecord = records.lastOrNull()

    if (latestRecord != null) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${"%.2f".format(latestRecord.bsaValue)} m² • ${latestRecord.formulaName}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
