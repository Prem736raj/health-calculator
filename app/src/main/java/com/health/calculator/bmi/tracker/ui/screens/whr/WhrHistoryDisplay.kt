package com.health.calculator.bmi.tracker.ui.screens.whr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WhrHistoryEntryCard(
    entry: WhrHistoryEntry,
    onTap: () -> Unit = {},
    onDelete: () -> Unit = {},
    showDeleteOption: Boolean = true,
    modifier: Modifier = Modifier
) {
    val riskColor = when (entry.category) {
        WhrCategory.LOW_RISK -> Color(0xFF4CAF50)
        WhrCategory.MODERATE_RISK -> Color(0xFFFFA726)
        WhrCategory.HIGH_RISK -> Color(0xFFF44336)
    }

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry?", fontWeight = FontWeight.Bold) },
            text = { Text("This WHR reading will be permanently removed from your history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: WHR value + category + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color indicator
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(riskColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "📐",
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                String.format("%.2f", entry.whr),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = riskColor
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = riskColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    entry.category.label,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            "Waist-to-Hip Ratio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        dateFormat.format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Text(
                        timeFormat.format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }

            // Measurement details
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HistoryDetailItem(
                    label = "Waist",
                    value = "${String.format("%.1f", entry.waistCm)} cm",
                    icon = "📏"
                )
                HistoryDetailItem(
                    label = "Hip",
                    value = "${String.format("%.1f", entry.hipCm)} cm",
                    icon = "📏"
                )
                HistoryDetailItem(
                    label = "Shape",
                    value = entry.bodyShape.label,
                    icon = entry.bodyShape.emoji
                )
                HistoryDetailItem(
                    label = "Waist Risk",
                    value = when (entry.waistRiskLevel) {
                        WaistRiskLevel.NORMAL -> "Normal"
                        WaistRiskLevel.INCREASED -> "Increased"
                        WaistRiskLevel.SUBSTANTIALLY_INCREASED -> "High"
                    },
                    icon = when (entry.waistRiskLevel) {
                        WaistRiskLevel.NORMAL -> "✅"
                        WaistRiskLevel.INCREASED -> "⚠️"
                        WaistRiskLevel.SUBSTANTIALLY_INCREASED -> "🔴"
                    }
                )
            }

            // WHtR if available
            entry.whtr?.let { whtr ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (whtr > 0.5f)
                                Color(0xFFF44336).copy(alpha = 0.06f)
                            else
                                Color(0xFF4CAF50).copy(alpha = 0.06f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "WHtR: ${String.format("%.2f", whtr)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        if (whtr > 0.5f) "At Risk" else "Normal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (whtr > 0.5f) Color(0xFFF44336) else Color(0xFF4CAF50)
                    )
                }
            }

            // Gender and Age
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${if (entry.gender == Gender.FEMALE) "👩 Female" else "👨 Male"} • ${entry.age} years",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )

                if (showDeleteOption) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryDetailItem(
    label: String,
    value: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            fontSize = 9.sp
        )
    }
}

@Composable
fun WhrHistoryEntryDetailDialog(
    entry: WhrHistoryEntry,
    onDismiss: () -> Unit
) {
    val riskColor = when (entry.category) {
        WhrCategory.LOW_RISK -> Color(0xFF4CAF50)
        WhrCategory.MODERATE_RISK -> Color(0xFFFFA726)
        WhrCategory.HIGH_RISK -> Color(0xFFF44336)
    }

    val dateFormat = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📐", fontSize = 24.sp)
                Text(
                    "WHR Details",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Date", dateFormat.format(Date(entry.timestamp)))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                DetailRow("WHR", String.format("%.2f", entry.whr), riskColor)
                DetailRow("Category", entry.category.label, riskColor)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                DetailRow("Waist", "${String.format("%.1f", entry.waistCm)} cm")
                DetailRow("Hip", "${String.format("%.1f", entry.hipCm)} cm")
                DetailRow("Body Shape", "${entry.bodyShape.emoji} ${entry.bodyShape.label}")
                DetailRow("Waist Risk", entry.waistRiskLevel.label)
                entry.whtr?.let { DetailRow("WHtR", String.format("%.2f", it)) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                DetailRow("Gender", if (entry.gender == Gender.FEMALE) "Female" else "Male")
                DetailRow("Age", "${entry.age} years")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
