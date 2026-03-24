// ui/screens/reports/ReportShareDialog.kt
package com.health.calculator.bmi.tracker.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReportShareDialog(
    includeWeight: Boolean,
    includeBmi: Boolean,
    includeBp: Boolean,
    includeWater: Boolean,
    includeCalories: Boolean,
    includeExercise: Boolean,
    includeScore: Boolean,
    onToggleSection: (String, Boolean) -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Share, null, modifier = Modifier.size(24.dp)) },
        title = { Text("Share Report", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Choose what to include:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                ShareToggle("\uD83C\uDFC6 Health Score", includeScore) { onToggleSection("score", it) }
                ShareToggle("⚖️ Weight", includeWeight) { onToggleSection("weight", it) }
                ShareToggle("\uD83D\uDCCA BMI", includeBmi) { onToggleSection("bmi", it) }
                ShareToggle("❤️ Blood Pressure", includeBp) { onToggleSection("bp", it) }
                ShareToggle("\uD83D\uDCA7 Water Intake", includeWater) { onToggleSection("water", it) }
                ShareToggle("\uD83C\uDF7D️ Calories", includeCalories) { onToggleSection("calories", it) }
                ShareToggle("\uD83C\uDFC3 Exercise", includeExercise) { onToggleSection("exercise", it) }
            }
        },
        confirmButton = {
            Button(onClick = onShare) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ShareToggle(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onToggle, modifier = Modifier.height(28.dp))
    }
}
