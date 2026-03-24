package com.health.calculator.bmi.tracker.ui.screens.calorie

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.DietPreset
import com.health.calculator.bmi.tracker.data.model.MacroResult
import kotlin.math.roundToInt

@Composable
fun MacroCalculatorSection(
    macroResult: MacroResult,
    dietPresets: List<DietPreset>,
    selectedPresetId: String,
    customCarbPercent: Int,
    customProteinPercent: Int,
    customFatPercent: Int,
    numberOfMeals: Int,
    proteinRecommendationText: String,
    onPresetSelected: (String) -> Unit,
    onCustomMacrosChanged: (carb: Int, protein: Int, fat: Int) -> Unit,
    onMealCountChanged: (Int) -> Unit
) {
    val proteinColor = Color(0xFFF44336)
    val carbColor = Color(0xFFFFEB3B)
    val fatColor = Color(0xFF4CAF50)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Section Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.PieChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Macronutrient Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Diet Presets
        DietPresetsSection(
            presets = dietPresets,
            selectedId = selectedPresetId,
            onSelect = onPresetSelected
        )

        // Custom Sliders (when custom is selected)
        AnimatedVisibility(
            visible = selectedPresetId == "custom",
            enter = expandVertically(tween(300)) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
        ) {
            CustomMacroSliders(
                carbPercent = customCarbPercent,
                proteinPercent = customProteinPercent,
                fatPercent = customFatPercent,
                onChanged = onCustomMacrosChanged
            )
        }

        // Visual Macro Chart (Updated from Prompt 82)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedMacroDonutChart(
                    protein = macroResult.proteinGrams.toFloat(),
                    carbs = macroResult.carbGrams.toFloat(),
                    fat = macroResult.fatGrams.toFloat(),
                    modifier = Modifier.size(120.dp)
                )

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroLegendItem(
                        color = carbColor,
                        label = "Carbs",
                        percent = "${macroResult.carbPercent.roundToInt()}%"
                    )
                    MacroLegendItem(
                        color = proteinColor,
                        label = "Protein",
                        percent = "${macroResult.proteinPercent.roundToInt()}%"
                    )
                    MacroLegendItem(
                        color = fatColor,
                        label = "Fat",
                        percent = "${macroResult.fatPercent.roundToInt()}%"
                    )
                }
            }
        }

        // Macro Details Cards
        MacroDetailsSection(
            macroResult = macroResult,
            proteinColor = proteinColor,
            carbColor = carbColor,
            fatColor = fatColor,
            proteinRecommendationText = proteinRecommendationText
        )

        // Per-Meal Breakdown
        PerMealMacroSection(
            macroResult = macroResult,
            numberOfMeals = numberOfMeals,
            onMealCountChanged = onMealCountChanged,
            proteinColor = proteinColor,
            carbColor = carbColor,
            fatColor = fatColor
        )
    }
}

@Composable
private fun DietPresetsSection(
    presets: List<DietPreset>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Diet Type",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Grid of presets
            val chunkedPresets = presets.chunked(3)
            chunkedPresets.forEach { rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPresets.forEach { preset ->
                        val isSelected = selectedId == preset.id
                        val presetColor = Color(preset.color)

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelect(preset.id) },
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(
                                2.dp,
                                presetColor
                            ) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    presetColor.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(preset.emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) presetColor
                                    else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (preset.id != "custom") {
                                    Text(
                                        text = "${preset.carbPercent}/${preset.proteinPercent}/${preset.fatPercent}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 9.sp
                                    )
                                } else {
                                    Text(
                                        text = "Adjustable",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                    // Fill remaining space if needed
                    repeat(3 - rowPresets.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowPresets != chunkedPresets.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CustomMacroSliders(
    carbPercent: Int,
    proteinPercent: Int,
    fatPercent: Int,
    onChanged: (carb: Int, protein: Int, fat: Int) -> Unit
) {
    val total = carbPercent + proteinPercent + fatPercent
    val isValid = total == 100

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Custom Ratios",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isValid) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else Color(0xFFF44336).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Total: $total%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isValid) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Carbs slider
            MacroSliderRow(
                label = "Carbs",
                emoji = "🍞",
                percent = carbPercent,
                color = Color(0xFFFFEB3B),
                onValueChange = { newCarb ->
                    // Adjust protein proportionally, keeping fat the same
                    val remaining = 100 - newCarb - fatPercent
                    val newProtein = remaining.coerceIn(5, 70)
                    val adjustedCarb = 100 - newProtein - fatPercent
                    onChanged(adjustedCarb, newProtein, fatPercent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Protein slider
            MacroSliderRow(
                label = "Protein",
                emoji = "🥩",
                percent = proteinPercent,
                color = Color(0xFFF44336),
                onValueChange = { newProtein ->
                    val remaining = 100 - newProtein - fatPercent
                    val newCarb = remaining.coerceIn(5, 70)
                    val adjustedProtein = 100 - newCarb - fatPercent
                    onChanged(newCarb, adjustedProtein, fatPercent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fat slider
            MacroSliderRow(
                label = "Fat",
                emoji = "🥑",
                percent = fatPercent,
                color = Color(0xFF4CAF50),
                onValueChange = { newFat ->
                    val remaining = 100 - newFat - proteinPercent
                    val newCarb = remaining.coerceIn(5, 70)
                    val adjustedFat = 100 - newCarb - proteinPercent
                    onChanged(newCarb, proteinPercent, adjustedFat)
                }
            )

            if (!isValid) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Percentages must total 100%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun MacroSliderRow(
    label: String,
    emoji: String,
    percent: Int,
    color: Color,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Slider(
            value = percent.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 5f..70f,
            steps = 64,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
      // Note: MacroDonutChart replaced by AnimatedMacroDonutChart from CalorieResultScreen.kt
    }
}

@Composable
private fun MacroLegendItem(
    color: Color,
    label: String,
    percent: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = percent,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun MacroDetailsSection(
    macroResult: MacroResult,
    proteinColor: Color,
    carbColor: Color,
    fatColor: Color,
    proteinRecommendationText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Protein Card
        MacroDetailCard(
            emoji = "🥩",
            name = "Protein",
            grams = macroResult.proteinGrams,
            calories = macroResult.proteinCalories,
            percent = macroResult.proteinPercent,
            color = proteinColor,
            extraInfo = listOf(
                "${"%.1f".format(macroResult.proteinPerKg)} g/kg body weight",
                "${"%.0f".format(macroResult.proteinPerMeal)}g per meal"
            ),
            recommendation = proteinRecommendationText
        )

        // Carbs Card
        MacroDetailCard(
            emoji = "🍞",
            name = "Carbohydrates",
            grams = macroResult.carbGrams,
            calories = macroResult.carbCalories,
            percent = macroResult.carbPercent,
            color = carbColor,
            extraInfo = listOf(
                "${"%.0f".format(macroResult.carbPerMeal)}g per meal",
                "Fiber goal: ${"%.0f".format(macroResult.fiberRecommendation)}g/day"
            ),
            recommendation = "Focus on complex carbs: whole grains, vegetables, legumes"
        )

        // Fat Card
        MacroDetailCard(
            emoji = "🥑",
            name = "Fat",
            grams = macroResult.fatGrams,
            calories = macroResult.fatCalories,
            percent = macroResult.fatPercent,
            color = fatColor,
            extraInfo = listOf(
                "Saturated: <${"%.0f".format(macroResult.saturatedFatGrams)}g",
                "Unsaturated: ~${"%.0f".format(macroResult.unsaturatedFatGrams)}g"
            ),
            recommendation = "Minimum 20% fat is essential for hormone production"
        )
    }
}

@Composable
private fun MacroDetailCard(
    emoji: String,
    name: String,
    grams: Double,
    calories: Double,
    percent: Float,
    color: Color,
    extraInfo: List<String>,
    recommendation: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = color
                        )
                        Text(
                            "${"%.0f".format(percent)}% of daily calories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${"%.0f".format(grams)}g",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                    Text(
                        "${"%.0f".format(calories)} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = color.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    extraInfo.forEach { info ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                info,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = color.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                color = color.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Tap hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle details",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun PerMealMacroSection(
    macroResult: MacroResult,
    numberOfMeals: Int,
    onMealCountChanged: (Int) -> Unit,
    proteinColor: Color,
    carbColor: Color,
    fatColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Per Meal Breakdown",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Meal count selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(3, 4, 5, 6).forEach { count ->
                    FilterChip(
                        selected = numberOfMeals == count,
                        onClick = { onMealCountChanged(count) },
                        label = { Text("$count") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Per-meal values
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🍽️ Each of $numberOfMeals meals:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            "${"%.0f".format(macroResult.caloriesPerMeal)} kcal",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Macro bars
                    MacroPerMealBar(
                        label = "Protein",
                        grams = macroResult.proteinPerMeal,
                        color = proteinColor,
                        maxGrams = maxOf(macroResult.proteinPerMeal, macroResult.carbPerMeal, macroResult.fatPerMeal)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MacroPerMealBar(
                        label = "Carbs",
                        grams = macroResult.carbPerMeal,
                        color = carbColor,
                        maxGrams = maxOf(macroResult.proteinPerMeal, macroResult.carbPerMeal, macroResult.fatPerMeal)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MacroPerMealBar(
                        label = "Fat",
                        grams = macroResult.fatPerMeal,
                        color = fatColor,
                        maxGrams = maxOf(macroResult.proteinPerMeal, macroResult.carbPerMeal, macroResult.fatPerMeal)
                    )
                }
            }

            // Quick reference
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickRefChip("🥚 1 egg", "6g P, 5g F")
                QuickRefChip("🍚 1 cup rice", "45g C")
                QuickRefChip("🥜 10 almonds", "3g P, 5g F")
            }
        }
    }
}

@Composable
private fun MacroPerMealBar(
    label: String,
    grams: Double,
    color: Color,
    maxGrams: Double
) {
    val animatedFraction by animateFloatAsState(
        targetValue = (grams / maxGrams).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "barFraction"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(55.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "${"%.0f".format(grams)}g",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun QuickRefChip(food: String, macros: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                food,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp
            )
            Text(
                macros,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
