package com.health.calculator.bmi.tracker.ui.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class QuickFoodPreset(
    val name: String,
    val calories: Int,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val emoji: String = "🍽️"
)

val defaultQuickPresets = listOf(
    QuickFoodPreset("Glass of Water", 0, emoji = "💧"),
    QuickFoodPreset("Apple", 95, 0.5f, 25f, 0.3f, "🍎"),
    QuickFoodPreset("Banana", 105, 1.3f, 27f, 0.4f, "🍌"),
    QuickFoodPreset("Egg (1 large)", 72, 6.3f, 0.4f, 4.8f, "🥚"),
    QuickFoodPreset("Rice (1 cup)", 206, 4.3f, 45f, 0.4f, "🍚"),
    QuickFoodPreset("Chicken Breast (100g)", 165, 31f, 0f, 3.6f, "🍗"),
    QuickFoodPreset("Bread (1 slice)", 79, 2.7f, 15f, 1f, "🍞"),
    QuickFoodPreset("Milk (1 cup)", 149, 8f, 12f, 8f, "🥛"),
    QuickFoodPreset("Salad (mixed)", 35, 2f, 7f, 0.3f, "🥗"),
    QuickFoodPreset("Coffee with Milk", 30, 1f, 3f, 1f, "☕"),
    QuickFoodPreset("Yogurt (1 cup)", 150, 8.5f, 17f, 3.8f, "🥛"),
    QuickFoodPreset("Protein Shake", 200, 30f, 10f, 3f, "🥤"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFoodLogDialog(
    onDismiss: () -> Unit,
    onLogFood: (name: String, calories: Int, protein: Float, carbs: Float, fat: Float) -> Unit,
    customPresets: List<QuickFoodPreset> = emptyList()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var customFoodName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customFat by remember { mutableStateOf("") }
    var showMacroFields by remember { mutableStateOf(false) }

    val allPresets = defaultQuickPresets + customPresets

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.75f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Quick Food Log",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Quick Add") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Custom") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Quick Presets
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(allPresets) { preset ->
                                QuickPresetItem(
                                    preset = preset,
                                    onClick = {
                                        onLogFood(
                                            preset.name,
                                            preset.calories,
                                            preset.protein,
                                            preset.carbs,
                                            preset.fat
                                        )
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    1 -> {
                        // Custom Entry
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = customFoodName,
                                onValueChange = { customFoodName = it },
                                label = { Text("Food name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = customCalories,
                                onValueChange = { customCalories = it.filter { c -> c.isDigit() } },
                                label = { Text("Calories (kcal)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Toggle macro fields
                            TextButton(
                                onClick = { showMacroFields = !showMacroFields }
                            ) {
                                Icon(
                                    if (showMacroFields) Icons.Default.ExpandLess
                                    else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (showMacroFields) "Hide macros"
                                    else "Add macros (optional)"
                                )
                            }

                            AnimatedVisibility(
                                visible = showMacroFields,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = customProtein,
                                            onValueChange = {
                                                customProtein = it.filter { c -> c.isDigit() || c == '.' }
                                            },
                                            label = { Text("Protein (g)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        OutlinedTextField(
                                            value = customCarbs,
                                            onValueChange = {
                                                customCarbs = it.filter { c -> c.isDigit() || c == '.' }
                                            },
                                            label = { Text("Carbs (g)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    OutlinedTextField(
                                        value = customFat,
                                        onValueChange = {
                                            customFat = it.filter { c -> c.isDigit() || c == '.' }
                                        },
                                        label = { Text("Fat (g)") },
                                        modifier = Modifier.fillMaxWidth(0.49f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    val cal = customCalories.toIntOrNull() ?: 0
                                    if (customFoodName.isNotBlank() && cal > 0) {
                                        onLogFood(
                                            customFoodName,
                                            cal,
                                            customProtein.toFloatOrNull() ?: 0f,
                                            customCarbs.toFloatOrNull() ?: 0f,
                                            customFat.toFloatOrNull() ?: 0f
                                        )
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = customFoodName.isNotBlank() &&
                                        (customCalories.toIntOrNull() ?: 0) > 0,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Log Food", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickPresetItem(
    preset: QuickFoodPreset,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = preset.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (preset.protein > 0 || preset.carbs > 0 || preset.fat > 0) {
                        Text(
                            text = "P: ${preset.protein.toInt()}g • C: ${preset.carbs.toInt()}g • F: ${preset.fat.toInt()}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            AssistChip(
                onClick = onClick,
                label = {
                    Text(
                        text = "${preset.calories} cal",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
