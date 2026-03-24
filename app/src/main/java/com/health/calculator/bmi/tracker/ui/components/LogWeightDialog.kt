package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWeightDialog(
    weightInput: String,
    noteInput: String,
    dateMillis: Long,
    useMetric: Boolean,
    isSaving: Boolean,
    onWeightChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val unit = if (useMetric) "kg" else "lbs"
    val dateFmt = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val isToday = remember(dateMillis) {
        val cal1 = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val cal2 = Calendar.getInstance()
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    val isValid = weightInput.toDoubleOrNull()?.let { it in 10.0..500.0 } ?: false

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Weight",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Weight input
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = onWeightChange,
                    label = { Text("Weight ($unit)") },
                    leadingIcon = {
                        Icon(Icons.Outlined.MonitorWeight, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = weightInput.isNotEmpty() && !isValid,
                    supportingText = {
                        if (weightInput.isNotEmpty() && !isValid) {
                            Text("Enter a valid weight (10-500)")
                        }
                    }
                )

                // Date selector
                OutlinedCard(
                    onClick = onDateClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isToday) "Today" else dateFmt.format(Date(dateMillis)),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (isToday) {
                                Text(
                                    text = dateFmt.format(Date(dateMillis)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Note (optional)
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = onNoteChange,
                    label = { Text("Note (optional)") },
                    placeholder = { Text("e.g., Morning weigh-in") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Notes, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Tips
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "💡", modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = "Weigh yourself in the morning, after bathroom, before eating for consistency.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = isValid && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
