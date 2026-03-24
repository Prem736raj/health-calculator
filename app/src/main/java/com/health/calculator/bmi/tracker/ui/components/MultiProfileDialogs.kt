package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.health.calculator.bmi.tracker.data.model.ProfileColor
import com.health.calculator.bmi.tracker.data.model.ProfileShareConfig

/**
 * Dialog for adding a new family member profile.
 */
@Composable
fun AddProfileDialog(
    name: String,
    selectedColor: ProfileColor,
    onNameChange: (String) -> Unit,
    onColorSelect: (ProfileColor) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Create a separate profile for a family member to track their health independently.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. Mom, Brother, Sarah") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Select Avatar Color",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileColor.entries.forEach { color ->
                        ColorCircle(
                            color = color,
                            isSelected = color == selectedColor,
                            onSelect = { onColorSelect(color) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Live Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(selectedColor.colorValue)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (name.isBlank()) "Profile Preview" else name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = name.isNotBlank()
            ) {
                Text("Create Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ColorCircle(
    color: ProfileColor,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(color.colorValue))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Dialog for configuring and sharing profile data.
 */
@Composable
fun ProfileShareDialog(
    config: ProfileShareConfig,
    onConfigChange: (ProfileShareConfig) -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Share Health Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    "Choose what data to include in your shared summary. Your privacy matters.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
                        .padding(8.dp)
                ) {
                    ShareOptionItem("Include Name", config.includeName) { onConfigChange(config.copy(includeName = it)) }
                    ShareOptionItem("Include Age", config.includeAge) { onConfigChange(config.copy(includeAge = it)) }
                    ShareOptionItem("Include Health Score", config.includeHealthScore) { onConfigChange(config.copy(includeHealthScore = it)) }
                    ShareOptionItem("Include BMI", config.includeBmi) { onConfigChange(config.copy(includeBmi = it)) }
                    ShareOptionItem("Include Blood Pressure", config.includeBp) { onConfigChange(config.copy(includeBp = it)) }
                    ShareOptionItem("Include Water Streak", config.includeWaterStreak) { onConfigChange(config.copy(includeWaterStreak = it)) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onShare,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Share Now")
                    }
                }
            }
        }
    }
}

@Composable
fun ShareOptionItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f)
        )
    }
}

/**
 * Alert dialog shown when profile data has changed and calculations might be outdated.
 */
@Composable
fun RecalculatePromptDialog(
    calculators: List<String>,
    onRecalculateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Recalculation Recommended") },
        text = {
            Column {
                Text(
                    "Your profile data has changed. Recent calculations for following tools might be outdated:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                calculators.take(4).forEach { name ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
                if (calculators.size > 4) {
                    Text("...and ${calculators.size - 4} more", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 14.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onRecalculateClick) {
                Text("Open Connections")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}


