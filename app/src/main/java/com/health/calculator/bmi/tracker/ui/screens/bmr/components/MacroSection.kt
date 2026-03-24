// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/MacroSection.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.*
import com.health.calculator.bmi.tracker.ui.utils.CascadeAnimatedItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MacroSection(
    totalCalories: Float,
    visible: Boolean,
    onMacroChanged: (proteinPct: Float, carbsPct: Float, fatPct: Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedDiet by remember { mutableStateOf(DietApproach.BALANCED) }
    var customProtein by remember { mutableFloatStateOf(30f) }
    var customCarbs by remember { mutableFloatStateOf(40f) }
    var customFat by remember { mutableFloatStateOf(30f) }
    var mealsPerDay by remember { mutableIntStateOf(3) }

    val proteinPct = if (selectedDiet == DietApproach.CUSTOM) customProtein else selectedDiet.proteinPercent
    val carbsPct = if (selectedDiet == DietApproach.CUSTOM) customCarbs else selectedDiet.carbsPercent
    val fatPct = if (selectedDiet == DietApproach.CUSTOM) customFat else selectedDiet.fatPercent

    val macroBreakdown = MacroBreakdown(
        totalCalories = totalCalories,
        proteinPercentage = proteinPct,
        carbsPercentage = carbsPct,
        fatPercentage = fatPct,
        mealsPerDay = mealsPerDay,
        dietApproach = selectedDiet
    )

    LaunchedEffect(proteinPct, carbsPct, fatPct) {
        onMacroChanged(proteinPct, carbsPct, fatPct)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Column {
            // ---- Header ----
            CascadeAnimatedItem(index = 0, baseDelay = 80) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🥗", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Macronutrient Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Based on ${totalCalories.toInt()} kcal/day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Diet approach selector
                        DietApproachSelector(
                            selectedDiet = selectedDiet,
                            onDietSelected = { diet ->
                                selectedDiet = diet
                                if (diet != DietApproach.CUSTOM) {
                                    customProtein = diet.proteinPercent
                                    customCarbs = diet.carbsPercent
                                    customFat = diet.fatPercent
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Custom Sliders ----
            AnimatedVisibility(
                visible = selectedDiet == DietApproach.CUSTOM,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                Column {
                    CascadeAnimatedItem(index = 1, baseDelay = 80) {
                        CustomMacroSliders(
                            proteinPct = customProtein,
                            carbsPct = customCarbs,
                            fatPct = customFat,
                            onProteinChange = { newVal ->
                                val diff = newVal - customProtein
                                customProtein = newVal
                                // Distribute change proportionally between carbs and fat
                                val totalOthers = (customCarbs + customFat).coerceAtLeast(1f)
                                val carbsRatio = customCarbs / totalOthers
                                customCarbs = (customCarbs - diff * carbsRatio).coerceIn(0f, 100f)
                                customFat = (100f - customProtein - customCarbs).coerceIn(0f, 100f)
                            },
                            onCarbsChange = { newVal ->
                                val diff = newVal - customCarbs
                                customCarbs = newVal
                                val totalOthers = (customProtein + customFat).coerceAtLeast(1f)
                                val protRatio = customProtein / totalOthers
                                customProtein = (customProtein - diff * protRatio).coerceIn(0f, 100f)
                                customFat = (100f - customProtein - customCarbs).coerceIn(0f, 100f)
                            },
                            onFatChange = { newVal ->
                                val diff = newVal - customFat
                                customFat = newVal
                                val totalOthers = (customProtein + customCarbs).coerceAtLeast(1f)
                                val protRatio = customProtein / totalOthers
                                customProtein = (customProtein - diff * protRatio).coerceIn(0f, 100f)
                                customCarbs = (100f - customProtein - customFat).coerceIn(0f, 100f)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // ---- Donut Chart + Macro Summary ----
            CascadeAnimatedItem(index = 2, baseDelay = 80) {
                MacroChartCard(macroBreakdown = macroBreakdown)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Detailed Grams/Calories ----
            CascadeAnimatedItem(index = 3, baseDelay = 80) {
                MacroDetailCards(macroBreakdown = macroBreakdown)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- Per-Meal Breakdown ----
            CascadeAnimatedItem(index = 4, baseDelay = 80) {
                PerMealBreakdownCard(
                    macroBreakdown = macroBreakdown,
                    mealsPerDay = mealsPerDay,
                    onMealsChanged = { mealsPerDay = it }
                )
            }
        }
    }
}

// ============================================================
// Diet Approach Selector
// ============================================================
@Composable
private fun DietApproachSelector(
    selectedDiet: DietApproach,
    onDietSelected: (DietApproach) -> Unit
) {
    Column {
        Text(
            text = "Diet Approach",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2 rows of 3
        val diets = DietApproach.entries.toList()
        val rows = diets.chunked(3)

        rows.forEachIndexed { rowIndex, rowDiets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowDiets.forEach { diet ->
                    DietChip(
                        diet = diet,
                        isSelected = diet == selectedDiet,
                        onClick = { onDietSelected(diet) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if row has less than 3
                repeat(3 - rowDiets.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (rowIndex < rows.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // Selected diet info
        Spacer(modifier = Modifier.height(10.dp))

        AnimatedContent(
            targetState = selectedDiet,
            transitionSpec = {
                (fadeIn(tween(200)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(150)) + slideOutVertically { -it / 4 })
            },
            label = "dietInfo"
        ) { diet ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = diet.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Best for: ${diet.bestFor}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (diet != DietApproach.CUSTOM) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            MacroMiniTag("P ${diet.proteinPercent.toInt()}%", MacroColors.Protein)
                            Spacer(modifier = Modifier.width(4.dp))
                            MacroMiniTag("C ${diet.carbsPercent.toInt()}%", MacroColors.Carbs)
                            Spacer(modifier = Modifier.width(4.dp))
                            MacroMiniTag("F ${diet.fatPercent.toInt()}%", MacroColors.Fat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DietChip(
    diet: DietApproach,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "dietBg"
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200),
        label = "dietContent"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.97f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "dietScale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = diet.emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = diet.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = content,
                textAlign = TextAlign.Center,
                maxLines = 1,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun MacroMiniTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

// ============================================================
// Custom Macro Sliders
// ============================================================
@Composable
private fun CustomMacroSliders(
    proteinPct: Float,
    carbsPct: Float,
    fatPct: Float,
    onProteinChange: (Float) -> Unit,
    onCarbsChange: (Float) -> Unit,
    onFatChange: (Float) -> Unit
) {
    val total = proteinPct + carbsPct + fatPct
    val isBalanced = total in 99.5f..100.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎛️ Custom Ratios",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isBalanced) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else Color(0xFFF44336).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Total: ${total.roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isBalanced) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stacked preview bar
            CustomMacroPreviewBar(proteinPct, carbsPct, fatPct)

            Spacer(modifier = Modifier.height(16.dp))

            // Protein slider
            MacroSlider(
                label = "Protein",
                emoji = "🔵",
                percentage = proteinPct,
                color = MacroColors.Protein,
                onValueChange = onProteinChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Carbs slider
            MacroSlider(
                label = "Carbs",
                emoji = "🟡",
                percentage = carbsPct,
                color = MacroColors.Carbs,
                onValueChange = onCarbsChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fat slider
            MacroSlider(
                label = "Fat",
                emoji = "🟠",
                percentage = fatPct,
                color = MacroColors.Fat,
                onValueChange = onFatChange
            )

            if (!isBalanced) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Ratios should total 100%. Adjusting one slider auto-balances others.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun CustomMacroPreviewBar(protein: Float, carbs: Float, fat: Float) {
    val total = (protein + carbs + fat).coerceAtLeast(1f)
    val pFrac = protein / total
    val cFrac = carbs / total
    val fFrac = fat / total

    val animP by animateFloatAsState(pFrac, tween(300), label = "pBar")
    val animC by animateFloatAsState(cFrac, tween(300), label = "cBar")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(animP.coerceAtLeast(0.01f))
                    .background(MacroColors.Protein)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(animC.coerceAtLeast(0.01f))
                    .background(MacroColors.Carbs)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((1f - animP - animC).coerceAtLeast(0.01f))
                    .background(MacroColors.Fat)
            )
        }
    }
}

@Composable
private fun MacroSlider(
    label: String,
    emoji: String,
    percentage: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "${percentage.roundToInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Slider(
            value = percentage,
            onValueChange = { onValueChange((it).roundToInt().toFloat()) },
            valueRange = 0f..80f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.15f)
            )
        )
    }
}

// ============================================================
// Macro Donut Chart Card
// ============================================================
@Composable
private fun MacroChartCard(macroBreakdown: MacroBreakdown) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut
                MacroDonutChart(
                    proteinPct = macroBreakdown.proteinPercentage,
                    carbsPct = macroBreakdown.carbsPercentage,
                    fatPct = macroBreakdown.fatPercentage,
                    totalCalories = macroBreakdown.totalCalories,
                    modifier = Modifier.size(140.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Legend
                Column(modifier = Modifier.weight(1f)) {
                    MacroLegendItem(
                        emoji = "🔵",
                        label = "Protein",
                        grams = macroBreakdown.proteinGrams,
                        calories = macroBreakdown.proteinCalories,
                        percentage = macroBreakdown.proteinPercentage,
                        color = MacroColors.Protein
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroLegendItem(
                        emoji = "🟡",
                        label = "Carbs",
                        grams = macroBreakdown.carbsGrams,
                        calories = macroBreakdown.carbsCalories,
                        percentage = macroBreakdown.carbsPercentage,
                        color = MacroColors.Carbs
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MacroLegendItem(
                        emoji = "🟠",
                        label = "Fat",
                        grams = macroBreakdown.fatGrams,
                        calories = macroBreakdown.fatCalories,
                        percentage = macroBreakdown.fatPercentage,
                        color = MacroColors.Fat
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroDonutChart(
    proteinPct: Float,
    carbsPct: Float,
    fatPct: Float,
    totalCalories: Float,
    modifier: Modifier = Modifier
) {
    val total = (proteinPct + carbsPct + fatPct).coerceAtLeast(1f)
    val proteinSweep = (proteinPct / total) * 360f
    val carbsSweep = (carbsPct / total) * 360f
    val fatSweep = (fatPct / total) * 360f

    val animProtein = remember { Animatable(0f) }
    val animCarbs = remember { Animatable(0f) }
    val animFat = remember { Animatable(0f) }

    LaunchedEffect(proteinSweep, carbsSweep, fatSweep) {
        launch {
            animProtein.animateTo(proteinSweep, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            delay(100)
            animCarbs.animateTo(carbsSweep, tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            delay(200)
            animFat.animateTo(fatSweep, tween(700, easing = FastOutSlowInEasing))
        }
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 22.dp.toPx()
            val gapDegrees = 3f
            val radius = (size.minDimension - strokeWidth) / 2
            val arcSize = Size(radius * 2, radius * 2)
            val topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )

            // Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = strokeWidth)
            )

            // Protein arc
            var startAngle = -90f
            drawArc(
                color = MacroColors.Protein,
                startAngle = startAngle,
                sweepAngle = (animProtein.value - gapDegrees).coerceAtLeast(0f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // Carbs arc
            startAngle += animProtein.value
            drawArc(
                color = MacroColors.Carbs,
                startAngle = startAngle,
                sweepAngle = (animCarbs.value - gapDegrees).coerceAtLeast(0f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            // Fat arc
            startAngle += animCarbs.value
            drawArc(
                color = MacroColors.Fat,
                startAngle = startAngle,
                sweepAngle = (animFat.value - gapDegrees).coerceAtLeast(0f),
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${totalCalories.toInt()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "kcal/day",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MacroLegendItem(
    emoji: String,
    label: String,
    grams: Float,
    calories: Float,
    percentage: Float,
    color: Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${percentage.roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${grams.roundToInt()}g  •  ${calories.roundToInt()} kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

// ============================================================
// Detailed Macro Cards
// ============================================================
@Composable
private fun MacroDetailCards(macroBreakdown: MacroBreakdown) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MacroDetailCard(
            emoji = "🔵",
            label = "Protein",
            grams = macroBreakdown.proteinGrams,
            calories = macroBreakdown.proteinCalories,
            percentage = macroBreakdown.proteinPercentage,
            color = MacroColors.Protein,
            calPerGram = "4 kcal/g",
            modifier = Modifier.weight(1f)
        )
        MacroDetailCard(
            emoji = "🟡",
            label = "Carbs",
            grams = macroBreakdown.carbsGrams,
            calories = macroBreakdown.carbsCalories,
            percentage = macroBreakdown.carbsPercentage,
            color = MacroColors.Carbs,
            calPerGram = "4 kcal/g",
            modifier = Modifier.weight(1f)
        )
        MacroDetailCard(
            emoji = "🟠",
            label = "Fat",
            grams = macroBreakdown.fatGrams,
            calories = macroBreakdown.fatCalories,
            percentage = macroBreakdown.fatPercentage,
            color = MacroColors.Fat,
            calPerGram = "9 kcal/g",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MacroDetailCard(
    emoji: String,
    label: String,
    grams: Float,
    calories: Float,
    percentage: Float,
    color: Color,
    calPerGram: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${grams.roundToInt()}g",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${calories.roundToInt()} kcal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Text(
                    text = calPerGram,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

// ============================================================
// Per-Meal Breakdown Card
// ============================================================
@Composable
private fun PerMealBreakdownCard(
    macroBreakdown: MacroBreakdown,
    mealsPerDay: Int,
    onMealsChanged: (Int) -> Unit
) {
    val mealOptions = listOf(3, 4, 5, 6)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🍽️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Per-Meal Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meal count selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meals per day:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                mealOptions.forEach { count ->
                    val isSelected = count == mealsPerDay
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        animationSpec = tween(200),
                        label = "mealBg$count"
                    )
                    val fg by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(200),
                        label = "mealFg$count"
                    )

                    Surface(
                        onClick = { onMealsChanged(count) },
                        shape = RoundedCornerShape(8.dp),
                        color = bg,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = fg
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Per meal values
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Each of your $mealsPerDay meals should contain approximately:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calorie header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "🔥 ${macroBreakdown.caloriesPerMeal.roundToInt()} kcal per meal",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Macro per meal row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PerMealMacroItem(
                            emoji = "🔵",
                            label = "Protein",
                            grams = macroBreakdown.proteinPerMeal,
                            color = MacroColors.Protein
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        )
                        PerMealMacroItem(
                            emoji = "🟡",
                            label = "Carbs",
                            grams = macroBreakdown.carbsPerMeal,
                            color = MacroColors.Carbs
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        )
                        PerMealMacroItem(
                            emoji = "🟠",
                            label = "Fat",
                            grams = macroBreakdown.fatPerMeal,
                            color = MacroColors.Fat
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Visual mini bar per meal
                    PerMealMiniBar(macroBreakdown)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tip
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Outlined.LightbulbCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tip: These are averages per meal. It's fine to vary between meals as long as your daily totals are consistent.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun PerMealMacroItem(
    emoji: String,
    label: String,
    grams: Float,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${grams.roundToInt()}g",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PerMealMiniBar(macroBreakdown: MacroBreakdown) {
    val total = macroBreakdown.proteinPerMeal + macroBreakdown.carbsPerMeal + macroBreakdown.fatPerMeal
    val pFrac = if (total > 0) macroBreakdown.proteinPerMeal / total else 0.33f
    val cFrac = if (total > 0) macroBreakdown.carbsPerMeal / total else 0.33f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(pFrac.coerceAtLeast(0.01f))
                    .background(MacroColors.Protein)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(cFrac.coerceAtLeast(0.01f))
                    .background(MacroColors.Carbs)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight((1f - pFrac - cFrac).coerceAtLeast(0.01f))
                    .background(MacroColors.Fat)
            )
        }
    }
}
