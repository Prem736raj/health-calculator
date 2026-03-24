package com.health.calculator.bmi.tracker.ui.screens.calorie

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.health.calculator.bmi.tracker.data.model.CalorieResult
import com.health.calculator.bmi.tracker.data.model.DietPreset
import com.health.calculator.bmi.tracker.data.model.MacroResult
import com.health.calculator.bmi.tracker.data.model.MealPlan
import com.health.calculator.bmi.tracker.data.model.IntermittentFastingPlan
import com.health.calculator.bmi.tracker.data.model.WorkoutNutrition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import com.health.calculator.bmi.tracker.ui.components.CalorieCalculatorCrossLinks
import com.health.calculator.bmi.tracker.util.CalorieEdgeCaseHandler
import com.health.calculator.bmi.tracker.ui.components.CalorieSafetyWarningCard

@Composable
fun CalorieResultScreen(
    result: CalorieResult,
    macroResult: MacroResult?,
    mealPlan: MealPlan?,
    ifPlan: IntermittentFastingPlan?,
    workoutNutrition: WorkoutNutrition?,
    selectedMealCount: Int,
    customMealSplits: List<Float>,
    ifType: String,
    ifWindowStart: Int,
    workoutEnabled: Boolean,
    workoutTime: String,
    dietPresets: List<DietPreset>,
    selectedPresetId: String,
    customCarbPercent: Int,
    customProteinPercent: Int,
    customFatPercent: Int,
    numberOfMeals: Int,
    proteinRecommendationText: String,
    isSaved: Boolean,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit,
    onTrackToday: () -> Unit,
    onViewHistory: () -> Unit,
    onPresetSelected: (String) -> Unit,
    onCustomMacrosChanged: (carb: Int, protein: Int, fat: Int) -> Unit,
    onMealCountChanged: (Int) -> Unit,
    onMealPlanCountChanged: (Int) -> Unit,
    onCustomSplitsChanged: (List<Float>) -> Unit,
    onIFTypeChanged: (String) -> Unit,
    onIFWindowStartChanged: (Int) -> Unit,
    onWorkoutEnabledChanged: (Boolean) -> Unit,
    onWorkoutTimeChanged: (String) -> Unit,
    onNavigateToBMR: () -> Unit = {},
    onNavigateToIBW: () -> Unit = {},
    onNavigateToBMI: () -> Unit = {},
    showEducational: Boolean = false,
    onToggleEducational: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Safety Warning
        if (result.isBelowMinimum) {
            val validation = CalorieEdgeCaseHandler.validateCalorieTarget(
                result.goalCalories.toInt(),
                result.gender == "Male",
                result.tdee.toInt()
            )
            CalorieSafetyWarningCard(
                message = validation.warning ?: "",
                severity = validation.severity
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Primary Calorie Target
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500))
        ) {
            PrimaryCalorieCard(result)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // TDEE & BMR Reference
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, 150)) + slideInVertically(tween(600, 150))
        ) {
            TdeeBmrCard(result)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Visual Energy Breakdown
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700, 300)) + slideInVertically(tween(700, 300))
        ) {
            EnergyBreakdownCard(result)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Macronutrient Calculator Section
        AnimatedVisibility(
            visible = visible && macroResult != null,
            enter = fadeIn(tween(800, 450)) + slideInVertically(tween(800, 450))
        ) {
            macroResult?.let {
                MacroCalculatorSection(
                    macroResult = it,
                    dietPresets = dietPresets,
                    selectedPresetId = selectedPresetId,
                    customCarbPercent = customCarbPercent,
                    customProteinPercent = customProteinPercent,
                    customFatPercent = customFatPercent,
                    numberOfMeals = numberOfMeals,
                    proteinRecommendationText = proteinRecommendationText,
                    onPresetSelected = onPresetSelected,
                    onCustomMacrosChanged = onCustomMacrosChanged,
                    onMealCountChanged = onMealCountChanged
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Meal Planning Section
        AnimatedVisibility(
            visible = visible && mealPlan != null,
            enter = fadeIn(tween(900, 600)) + slideInVertically(tween(900, 600))
        ) {
            mealPlan?.let {
                MealPlanningSection(
                    mealPlan = it,
                    ifPlan = ifPlan,
                    workoutNutrition = workoutNutrition,
                    selectedMealCount = selectedMealCount,
                    customSplits = customMealSplits,
                    ifType = ifType,
                    ifWindowStart = ifWindowStart,
                    workoutEnabled = workoutEnabled,
                    workoutTime = workoutTime,
                    onMealCountChanged = onMealPlanCountChanged,
                    onCustomSplitChanged = onCustomSplitsChanged,
                    onIFTypeChanged = onIFTypeChanged,
                    onIFWindowStartChanged = onIFWindowStartChanged,
                    onWorkoutEnabledChanged = onWorkoutEnabledChanged,
                    onWorkoutTimeChanged = onWorkoutTimeChanged
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Projected Outcome
        if (!result.isMaintenance) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000, 700)) + slideInVertically(tween(1000, 700))
            ) {
                ProjectedOutcomeCard(result)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Advanced Visuals (New from Prompt 82)
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1050, 750)) + slideInVertically(tween(1050, 750))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Target Progress Preview",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CalorieProgressRing(
                        target = result.safeGoalCalories.toInt(),
                        current = (result.safeGoalCalories * 0.3).toInt(), // Preview 30%
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Formula Details
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1100, 800)) + slideInVertically(tween(1100, 800))
        ) {
            FormulaDetailsCard(result)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Related Calculators Cross-Links
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1150, 850)) + slideInVertically(tween(1150, 850))
        ) {
            CalorieCalculatorCrossLinks(
                onNavigateToBMR = onNavigateToBMR,
                onNavigateToIBW = onNavigateToIBW,
                onNavigateToBMI = onNavigateToBMI
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1200, 900)) + slideInVertically(tween(1200, 900))
        ) {
            ActionButtons(isSaved, onSave, onRecalculate, onShare, onTrackToday, onViewHistory)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Educational Toggle Button
        OutlinedButton(
            onClick = onToggleEducational,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (showEducational)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
            )
        ) {
            Icon(Icons.Default.MenuBook, null, Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (showEducational) "Hide Calorie Guide" else "📚 Learn About Calories",
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Educational Content
        AnimatedVisibility(
            visible = showEducational,
            enter = expandVertically(tween(400)) + fadeIn(tween(400)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(200))
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                CalorieEducationalContent()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── NEW ANIMATED COMPONENTS ───────────────────────────────────────

@Composable
fun CalorieProgressRing(
    target: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (current.toFloat() / target).coerceIn(0f, 1.5f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )
    
    val color = when {
        progress <= 0.9f -> MaterialTheme.colorScheme.primary
        progress <= 1.0f -> Color(0xFF4CAF50)
        else -> Color(0xFFF44336)
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            // Track
            drawArc(
                color = color.copy(alpha = 0.1f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            // Progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$current",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "of $target kcal",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AnimatedMacroDonutChart(
    protein: Float,
    carbs: Float,
    fat: Float,
    modifier: Modifier = Modifier
) {
    val total = protein + carbs + fat
    if (total <= 0) return

    val pRatio = protein / total
    val cRatio = carbs / total
    val fRatio = fat / total

    val animatedP by animateFloatAsState(targetValue = pRatio, animationSpec = tween(1000), label = "p")
    val animatedC by animateFloatAsState(targetValue = cRatio, animationSpec = tween(1000, 200), label = "c")
    val animatedF by animateFloatAsState(targetValue = fRatio, animationSpec = tween(1000, 400), label = "f")

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 14.dp.toPx()
            val radius = size.minDimension / 2 - strokeW
            
            // Protein
            drawArc(
                color = Color(0xFF9C27B0),
                startAngle = -90f,
                sweepAngle = animatedP * 360f,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            // Carbs
            drawArc(
                color = Color(0xFF2196F3),
                startAngle = -90f + (animatedP * 360f),
                sweepAngle = animatedC * 360f,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            // Fat
            drawArc(
                color = Color(0xFFFF9800),
                startAngle = -90f + ((animatedP + animatedC) * 360f),
                sweepAngle = animatedF * 360f,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Macros", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("${total.toInt()}g", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnimatedFoodLogEntry(
    name: String,
    calories: Int,
    index: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn() + slideInHorizontally { it / 2 }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
                Spacer(modifier = Modifier.width(12.dp))
                Text(name, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "$calories kcal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SafetyWarningCard(result: CalorieResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "⚠️ Calorie Intake Below Recommended Minimum",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your goal of ${"%.0f".format(result.goalCalories)} kcal/day is below the recommended minimum of ${"%.0f".format(result.minimumCalories)} kcal/day for ${result.gender.lowercase()}s. We've adjusted your target to ${"%.0f".format(result.safeGoalCalories)} kcal/day for safety.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336).copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Consider a less aggressive approach or consult a healthcare provider for guidance on very low calorie intake.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336).copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PrimaryCalorieCard(result: CalorieResult) {
    val displayCalories = result.safeGoalCalories
    val goalColor = when {
        result.isDeficit -> Color(0xFFFF9800)
        result.isSurplus -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }

    val animatedCalories by animateFloatAsState(
        targetValue = displayCalories.toFloat(),
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "calories"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = goalColor.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = result.goalName,
                style = MaterialTheme.typography.titleMedium,
                color = goalColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Animated ring with calories
            Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                val trackColor = goalColor.copy(alpha = 0.15f)
                val sweepFraction = (displayCalories / result.tdee).toFloat().coerceIn(0f, 1.5f)
                val animatedSweep by animateFloatAsState(
                    targetValue = sweepFraction * 240f,
                    animationSpec = tween(1500, 300, easing = FastOutSlowInEasing),
                    label = "sweep"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 10.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = 150f, sweepAngle = 240f, useCenter = false,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = goalColor,
                        startAngle = 150f, sweepAngle = animatedSweep, useCenter = false,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", fontSize = 20.sp)
                    Text(
                        text = "${"%.0f".format(animatedCalories)}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold, fontSize = 40.sp
                        ),
                        color = goalColor
                    )
                    Text(
                        text = "kcal/day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = goalColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (result.isBelowMinimum) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF44336).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Adjusted from ${"%.0f".format(result.goalCalories)} to ${"%.0f".format(result.safeGoalCalories)} kcal (safety floor)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF44336),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp
                    )
                }
            }

            if (!result.isMaintenance) {
                Spacer(modifier = Modifier.height(4.dp))
                val adjText = if (result.isDeficit)
                    "${abs(result.goalAdjustment)} cal deficit from maintenance"
                else "${result.goalAdjustment} cal surplus over maintenance"
                Text(
                    text = adjText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun TdeeBmrCard(result: CalorieResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricColumn(
                label = "BMR",
                value = "${"%.0f".format(result.usedBmr)}",
                unit = "kcal/day",
                subtitle = "At complete rest",
                color = Color(0xFF9C27B0)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
            MetricColumn(
                label = "TDEE",
                value = "${"%.0f".format(result.tdee)}",
                unit = "kcal/day",
                subtitle = "Maintenance",
                color = Color(0xFF4CAF50)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
            MetricColumn(
                label = "Target",
                value = "${"%.0f".format(result.safeGoalCalories)}",
                unit = "kcal/day",
                subtitle = "Your goal",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    unit: String,
    subtitle: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f), fontSize = 9.sp)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 9.sp)
    }
}

@Composable
private fun EnergyBreakdownCard(result: CalorieResult) {
    val bmrAnim by animateFloatAsState(targetValue = result.bmrPercent, animationSpec = tween(1200, 200), label = "bmr")
    val actAnim by animateFloatAsState(targetValue = result.activityPercent, animationSpec = tween(1200, 400), label = "act")
    val tefAnim by animateFloatAsState(targetValue = result.tefPercent, animationSpec = tween(1200, 600), label = "tef")

    val bmrColor = Color(0xFF9C27B0)
    val actColor = Color(0xFF2196F3)
    val tefColor = Color(0xFFFF9800)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Energy Expenditure Breakdown",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Stacked bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Box(modifier = Modifier.fillMaxHeight().weight(bmrAnim.coerceAtLeast(1f)).background(bmrColor))
                Box(modifier = Modifier.fillMaxHeight().weight(actAnim.coerceAtLeast(1f)).background(actColor))
                Box(modifier = Modifier.fillMaxHeight().weight(tefAnim.coerceAtLeast(1f)).background(tefColor))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend items
            BreakdownLegendItem(
                color = bmrColor, label = "BMR (Basal Metabolic Rate)",
                value = "${"%.0f".format(result.usedBmr)} kcal",
                percent = "${"%.0f".format(result.bmrPercent)}%",
                description = "Energy for basic body functions"
            )
            Spacer(modifier = Modifier.height(8.dp))
            BreakdownLegendItem(
                color = actColor, label = "Physical Activity",
                value = "${"%.0f".format(result.activityCalories)} kcal",
                percent = "${"%.0f".format(result.activityPercent)}%",
                description = "Exercise and daily movement"
            )
            Spacer(modifier = Modifier.height(8.dp))
            BreakdownLegendItem(
                color = tefColor, label = "TEF (Thermic Effect of Food)",
                value = "${"%.0f".format(result.tef)} kcal",
                percent = "${"%.0f".format(result.tefPercent)}%",
                description = "Energy used to digest food"
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total TDEE", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("${"%.0f".format(result.tdee)} kcal", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun BreakdownLegendItem(
    color: Color, label: String, value: String, percent: String, description: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = color)
            Text(percent, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun ProjectedOutcomeCard(result: CalorieResult) {
    val action = if (result.isDeficit) "lose" else "gain"
    val projColor = if (result.isDeficit) Color(0xFFFF9800) else Color(0xFF2196F3)

    val weeklyKg = result.weeklyChangeDisplay
    val monthlyKg = result.monthlyChangeKg

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = projColor.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (result.isDeficit) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = null, tint = projColor, modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Projected Outcome", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = projColor.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "At ${"%.0f".format(result.safeGoalCalories)} kcal/day, you'll $action approximately:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ProjectionItem(value = "${"%.2f".format(weeklyKg)} kg", period = "per week", emoji = "📅")
                        ProjectionItem(value = "${"%.1f".format(monthlyKg)} kg", period = "per month", emoji = "📆")
                        ProjectionItem(value = "${"%.1f".format(monthlyKg * 3)} kg", period = "in 3 months", emoji = "🎯")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (result.isDeficit && abs(result.goalAdjustment) >= 1000) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.1f)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, Modifier.size(14.dp), tint = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Aggressive weight loss (>1 kg/week) may lead to muscle loss and metabolic slowdown. A moderate approach is recommended for long-term success.",
                            style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF9800).copy(alpha = 0.8f), fontSize = 10.sp, lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectionItem(value: String, period: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(period, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 9.sp)
    }
}

@Composable
private fun FormulaDetailsCard(result: CalorieResult) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Calculation Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, "Toggle")
                }
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Formula", result.bmrFormulaUsed)
                    DetailRow("BMR (Mifflin-St Jeor)", "${"%.0f".format(result.bmrMifflin)} kcal")
                    result.bmrKatchMcArdle?.let {
                        DetailRow("BMR (Katch-McArdle)", "${"%.0f".format(it)} kcal")
                    }
                    DetailRow("Activity Level", "${result.activityLevelName} (×${result.activityMultiplier})")
                    DetailRow("TDEE before TEF", "${"%.0f".format(result.tdee - result.tef)} kcal")
                    DetailRow("TEF (~10%)", "${"%.0f".format(result.tef)} kcal")
                    DetailRow("Total TDEE", "${"%.0f".format(result.tdee)} kcal")
                    DetailRow("Goal Adjustment", "${if (result.goalAdjustment >= 0) "+" else ""}${result.goalAdjustment} kcal")
                    DetailRow("Final Target", "${"%.0f".format(result.safeGoalCalories)} kcal")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DetailRow("Weight", "${"%.1f".format(result.weightKg)} kg")
                    DetailRow("Height", "${"%.1f".format(result.heightCm)} cm")
                    DetailRow("Age", "${result.age} years")
                    DetailRow("Gender", result.gender)
                    result.bodyFatPercent?.let { DetailRow("Body Fat", "${"%.1f".format(it)}%") }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), fontSize = 11.sp)
    }
}

@Composable
private fun ActionButtons(
    isSaved: Boolean,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit,
    onTrackToday: () -> Unit = {},
    onViewHistory: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSave, modifier = Modifier.weight(1f), enabled = !isSaved,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(if (isSaved) Icons.Default.Check else Icons.Default.Save, null, Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isSaved) "Saved ✓" else "Save")
            }
            OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share")
            }
        }

        Button(
            onClick = onTrackToday,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Icon(Icons.Default.Restaurant, null, Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Track Today's Food",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.BarChart, null, Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "View Calorie History",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(onClick = onRecalculate, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Recalculate")
        }
    }
}
