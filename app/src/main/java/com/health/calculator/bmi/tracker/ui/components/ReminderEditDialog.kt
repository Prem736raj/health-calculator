package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.health.calculator.bmi.tracker.data.models.ReminderCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderEditDialog(
    isNew: Boolean,
    category: ReminderCategory,
    title: String,
    message: String,
    times: List<String>,
    days: List<Int>,
    soundName: String,
    vibration: Boolean,
    highPriority: Boolean,
    onTitleChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onDaysChange: (List<Int>) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onHighPriorityChange: (Boolean) -> Unit,
    onAddTime: () -> Unit,
    onEditTime: (Int) -> Unit,
    onRemoveTime: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isNew) "Set ${category.displayName}" else "Edit Reminder",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Dismiss")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Reminder Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text(category.icon, modifier = Modifier.padding(start = 12.dp)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Message
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    label = { Text("Short Message (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Times Section
                Text("Trigger Times", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    times.forEachIndexed { index, time ->
                        InputChip(
                            selected = false,
                            onClick = { onEditTime(index) },
                            label = { Text(time) },
                            trailingIcon = {
                                if (times.size > 1) {
                                    Icon(
                                        Icons.Default.Cancel,
                                        "Remove",
                                        modifier = Modifier.size(16.dp).clickable { onRemoveTime(index) }
                                    )
                                }
                            }
                        )
                    }
                    AssistChip(
                        onClick = onAddTime,
                        label = { Text("Add Time") },
                        leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Days Section
                Text("Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..7).forEach { day ->
                        val isSelected = days.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newList = if (isSelected) days.filter { it != day } else days + day
                                if (newList.isNotEmpty()) onDaysChange(newList.sorted())
                            },
                            label = { Text(dayNames[day - 1]) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Settings
                Text("Notification Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Vibration, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Vibration", modifier = Modifier.weight(1f))
                    Switch(checked = vibration, onCheckedChange = onVibrationChange)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.PriorityHigh, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("High Priority")
                        Text("Overrides Quiet Hours if allowed", style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(checked = highPriority, onCheckedChange = onHighPriorityChange)
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(24.dp))

                // Save
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (isNew) "Create Reminder" else "Save Changes")
                }
            }
        }
    }
}
