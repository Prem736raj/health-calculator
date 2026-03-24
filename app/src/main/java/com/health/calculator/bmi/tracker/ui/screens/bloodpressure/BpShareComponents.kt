// ui/screens/bloodpressure/BpShareComponents.kt
package com.health.calculator.bmi.tracker.ui.screens.bloodpressure

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.data.export.BpExportManager
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import com.health.calculator.bmi.tracker.data.model.BloodPressureReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BpShareButtons(
    reading: BloodPressureReading,
    savedReadingId: Long?,
    onNavigateToExport: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCreatingImage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Share & Export",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share as text
                OutlinedButton(
                    onClick = {
                        shareReadingAsText(context, reading)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF7B1FA2).copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7B1FA2)
                    )
                ) {
                    Icon(
                        Icons.Outlined.TextSnippet,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Text", style = MaterialTheme.typography.labelMedium)
                }

                // Share as image
                OutlinedButton(
                    onClick = {
                        isCreatingImage = true
                        scope.launch {
                            shareReadingAsImage(context, reading)
                            isCreatingImage = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isCreatingImage,
                    border = BorderStroke(1.dp, Color(0xFFFF6F00).copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF6F00)
                    )
                ) {
                    if (isCreatingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFFF6F00)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Image", style = MaterialTheme.typography.labelMedium)
                }

                // Full export
                OutlinedButton(
                    onClick = onNavigateToExport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E88E5).copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1E88E5)
                    )
                ) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private fun shareReadingAsText(context: Context, reading: BloodPressureReading) {
    val exportManager = BpExportManager(context)

    val categoryDisplay = reading.category.displayName
    val sb = StringBuilder()
    sb.appendLine("🩺 Blood Pressure Reading")
    sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━")
    sb.appendLine("📊 ${reading.systolic}/${reading.diastolic} mmHg")
    sb.appendLine("📋 Category: $categoryDisplay (WHO)")

    reading.pulse?.let {
        sb.appendLine("❤️ Pulse: $it BPM")
    }

    sb.appendLine("📅 ${reading.formattedDateTime}")
    sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━")
    sb.appendLine("Tracked with Health Calculator: BMI Tracker")

    exportManager.shareText(sb.toString())
}

private suspend fun shareReadingAsImage(context: Context, reading: BloodPressureReading) {
    val exportManager = BpExportManager(context)

    // Create a temporary entity for image generation
    val entity = com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity(
        systolic = reading.systolic,
        diastolic = reading.diastolic,
        pulse = reading.pulse,
        category = reading.category.name,
        riskLevel = reading.riskLevel.name,
        arm = reading.arm?.name,
        position = reading.position?.name,
        timeOfDay = reading.timeOfDay?.name,
        measurementTimestamp = reading.measurementTime
            .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
        pulsePressure = reading.pulsePressure,
        meanArterialPressure = reading.meanArterialPressure
    )

    val uri = withContext(Dispatchers.IO) {
        exportManager.createReadingImage(entity)
    }
    uri?.let { exportManager.shareImage(it) }
}
