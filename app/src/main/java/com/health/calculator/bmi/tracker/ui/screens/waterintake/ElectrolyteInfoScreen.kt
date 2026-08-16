// ui/screens/waterintake/ElectrolyteInfoScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Electrolyte colors
private val SodiumColor = Color(0xFFFF7043)
private val PotassiumColor = Color(0xFFFFCA28)
private val MagnesiumColor = Color(0xFF66BB6A)
private val CalciumColor = Color(0xFF42A5F5)
private val WaterBlueMedium = Color(0xFF2196F3)
private val WaterBlueDark = Color(0xFF1565C0)
private val WaterBlueSurface = Color(0xFFE3F2FD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectrolyteInfoScreen(
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(R.string.txt_text_placeholder_33), fontSize = 22.sp)
                        Text(stringResource(R.string.txt_electrolytes), fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
        ) {
            // Header
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
                ) {
                    ElectrolyteHeaderCard()
                }
            }

            // Basic Info
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 40 }
                ) {
                    BasicElectrolyteInfoCard()
                }
            }

            // The 4 Key Electrolytes
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 40 }
                ) {
                    KeyElectrolytesCard()
                }
            }

            // When to Consider Electrolytes
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 40 }
                ) {
                    WhenToConsiderCard()
                }
            }

            // Natural Food Sources
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 40 }
                ) {
                    NaturalSourcesCard()
                }
            }

            // DIY Electrolyte Drinks
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 500)) + slideInVertically(tween(500, 500)) { 40 }
                ) {
                    DIYElectrolyteDrinksCard()
                }
            }

            // WHO ORS Formula
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 600)) + slideInVertically(tween(500, 600)) { 40 }
                ) {
                    WHOORSCard()
                }
            }

            // Warning Signs
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 700)) + slideInVertically(tween(500, 700)) { 40 }
                ) {
                    ElectrolyteImbalanceWarningsCard()
                }
            }

            // Disclaimer
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 800))
                ) {
                    ElectrolyteDisclaimerCard()
                }
            }
        }
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun ElectrolyteHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF6F00),
                            Color(0xFFFF8F00),
                            Color(0xFFFFA726)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.txt_beyond_water),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        stringResource(R.string.txt_understanding_electrolytes),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        lineHeight = 26.sp
                    )
                    Text(
                        stringResource(R.string.txt_learn_when_and_why_your_body_n),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_33), fontSize = 42.sp)
                    Text(stringResource(R.string.txt_text_placeholder_71), fontSize = 24.sp)
                }
            }
        }
    }
}

// ─── Basic Info ──────────────────────────────────────────────────────────────

@Composable
private fun BasicElectrolyteInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_23), fontSize = 20.sp)
                Text(
                    stringResource(R.string.txt_what_are_electrolytes),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Text(
                stringResource(R.string.txt_electrolytes_are_minerals_that) +
                        "They're essential for many body functions and are lost through sweat, urine, and other bodily fluids.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // Key point callout
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_1), fontSize = 18.sp)
                    Text(
                        buildString {
                            append("Water alone isn't always enough. ")
                            append("When you sweat heavily or lose fluids through illness, ")
                            append("you also lose electrolytes that need to be replaced for optimal hydration and body function.")
                        },
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }

            // What they do
            Text(stringResource(R.string.txt_what_electrolytes_do), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            val functions = listOf(
                "⚡" to "Conduct nerve impulses and muscle contractions",
                "💧" to "Regulate fluid balance between cells",
                "🫀" to "Maintain heart rhythm and blood pressure",
                "🧠" to "Support brain function and cognitive performance",
                "⚖️" to "Balance pH levels in your body"
            )

            functions.forEach { (icon, text) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(icon, fontSize = 14.sp)
                    Text(
                        text,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ─── Key Electrolytes ────────────────────────────────────────────────────────

@Composable
private fun KeyElectrolytesCard() {
    val electrolytes = listOf(
        ElectrolyteData(
            name = "Sodium",
            symbol = "Na⁺",
            emoji = "🧂",
            color = SodiumColor,
            dailyNeed = "1,500-2,300mg",
            function = "Regulates fluid balance, nerve signaling, and muscle function. Most commonly lost through sweat.",
            deficiencySign = "Muscle cramps, headache, fatigue, confusion"
        ),
        ElectrolyteData(
            name = "Potassium",
            symbol = "K⁺",
            emoji = "🍌",
            color = PotassiumColor,
            dailyNeed = "2,600-3,400mg",
            function = "Essential for heart function, muscle contractions, and maintaining cellular fluid balance.",
            deficiencySign = "Muscle weakness, cramps, irregular heartbeat"
        ),
        ElectrolyteData(
            name = "Magnesium",
            symbol = "Mg²⁺",
            emoji = "🥜",
            color = MagnesiumColor,
            dailyNeed = "310-420mg",
            function = "Involved in 300+ enzyme reactions, muscle relaxation, energy production, and bone health.",
            deficiencySign = "Muscle twitches, fatigue, mood changes"
        ),
        ElectrolyteData(
            name = "Calcium",
            symbol = "Ca²⁺",
            emoji = "🦴",
            color = CalciumColor,
            dailyNeed = "1,000-1,200mg",
            function = "Critical for bone health, muscle contraction, blood clotting, and nerve function.",
            deficiencySign = "Muscle spasms, numbness, weak bones"
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_33), fontSize = 20.sp)
                Text(
                    stringResource(R.string.txt_the_4_key_electrolytes),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            electrolytes.forEach { electrolyte ->
                ElectrolyteDetailCard(electrolyte)
            }
        }
    }
}

data class ElectrolyteData(
    val name: String,
    val symbol: String,
    val emoji: String,
    val color: Color,
    val dailyNeed: String,
    val function: String,
    val deficiencySign: String
)

@Composable
private fun ElectrolyteDetailCard(data: ElectrolyteData) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = data.color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(data.color.copy(alpha = 0.15f), CircleShape)
                        .border(2.dp, data.color.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(data.emoji, fontSize = 24.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            data.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = data.color
                        )
                        Card(
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = data.color.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                data.symbol,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = data.color
                            )
                        }
                    }
                    Text(
                        "Daily need: ${data.dailyNeed}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "expand_${data.name}"
                )
                Icon(
                    Icons.Default.ExpandMore,
                    null,
                    modifier = Modifier.rotate(rotation),
                    tint = data.color.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = data.color.copy(alpha = 0.15f))

                    Text(
                        stringResource(R.string.txt_function),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = data.color
                    )
                    Text(
                        data.function,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Text(
                        stringResource(R.string.txt_deficiency_signs),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        data.deficiencySign,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ─── When to Consider ────────────────────────────────────────────────────────

@Composable
private fun WhenToConsiderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_70), fontSize = 20.sp)
                Text(
                    stringResource(R.string.txt_when_do_you_need_electrolytes),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Text(
                stringResource(R.string.txt_for_everyday_hydration_plain_w) +
                        "you may need to replace electrolytes as well:",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            val situations = listOf(
                SituationData(
                    emoji = "🏃",
                    title = "Intense Exercise (60+ minutes)",
                    description = "During prolonged physical activity, you lose significant sodium and other electrolytes through sweat. " +
                            "After about an hour, water alone may not be enough to maintain performance.",
                    priority = "High"
                ),
                SituationData(
                    emoji = "☀️",
                    title = "Hot Weather & Heavy Sweating",
                    description = "Heat increases sweat rate dramatically. If you're sweating profusely (wet clothes, dripping), " +
                            "you're losing electrolytes faster than normal.",
                    priority = "High"
                ),
                SituationData(
                    emoji = "🤒",
                    title = "Illness (Vomiting/Diarrhea)",
                    description = "Gastrointestinal illness rapidly depletes fluids AND electrolytes. Oral rehydration solutions " +
                            "are specifically designed for these situations.",
                    priority = "Critical"
                ),
                SituationData(
                    emoji = "💧",
                    title = "Drinking Large Quantities of Plain Water",
                    description = "If you're drinking a lot of water (3+ liters) without food, you may dilute your electrolyte levels. " +
                            "This is especially relevant during exercise or in hot weather.",
                    priority = "Moderate"
                ),
                SituationData(
                    emoji = "🏔️",
                    title = "High Altitude Activities",
                    description = "At altitude, increased respiration and urination lead to higher fluid and electrolyte losses. " +
                            "Hikers and climbers should pay extra attention.",
                    priority = "Moderate"
                ),
                SituationData(
                    emoji = "🍺",
                    title = "After Alcohol Consumption",
                    description = "Alcohol is a diuretic that increases urine output, leading to electrolyte loss. " +
                            "Rehydrating with electrolytes can help reduce hangover symptoms.",
                    priority = "Low"
                )
            )

            situations.forEach { situation ->
                SituationCard(situation)
            }
        }
    }
}

data class SituationData(
    val emoji: String,
    val title: String,
    val description: String,
    val priority: String
)

@Composable
private fun SituationCard(data: SituationData) {
    val priorityColor = when (data.priority) {
        "Critical" -> Color(0xFFF44336)
        "High" -> Color(0xFFFF9800)
        "Moderate" -> Color(0xFFFFC107)
        else -> Color(0xFF8BC34A)
    }

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                priorityColor.copy(alpha = 0.06f),
                RoundedCornerShape(10.dp)
            )
            .clickable { expanded = !expanded }
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(data.emoji, fontSize = 24.sp)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    data.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Card(
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = priorityColor.copy(alpha = 0.15f)
                    )
                ) {
                    Text(
                        data.priority,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
            ) {
                Text(
                    data.description,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (!expanded) {
                Text(
                    stringResource(R.string.txt_tap_for_details_1),
                    fontSize = 11.sp,
                    color = priorityColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ─── Natural Food Sources ────────────────────────────────────────────────────

@Composable
private fun NaturalSourcesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_30), fontSize = 20.sp)
                Text(
                    stringResource(R.string.txt_natural_electrolyte_sources),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Text(
                stringResource(R.string.txt_the_best_way_to_maintain_elect) +
                        "Here are great food sources for each electrolyte:",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // Sodium
            FoodSourceRow(
                electrolyte = "Sodium",
                color = SodiumColor,
                foods = listOf(
                    FoodItem("🧂", "Table Salt", "390mg/pinch"),
                    FoodItem("🥒", "Pickles", "785mg/pickle"),
                    FoodItem("🫒", "Olives", "735mg/10 olives"),
                    FoodItem("🧀", "Cheese", "174mg/slice"),
                    FoodItem("🥓", "Bacon", "137mg/slice"),
                    FoodItem("🥫", "Canned soup", "800-1000mg/cup")
                )
            )

            // Potassium
            FoodSourceRow(
                electrolyte = "Potassium",
                color = PotassiumColor,
                foods = listOf(
                    FoodItem("🍌", "Banana", "422mg/medium"),
                    FoodItem("🥥", "Coconut Water", "600mg/cup"),
                    FoodItem("🍊", "Orange", "237mg/medium"),
                    FoodItem("🥑", "Avocado", "975mg/avocado"),
                    FoodItem("🥔", "Potato", "926mg/medium"),
                    FoodItem("🍠", "Sweet Potato", "542mg/medium")
                )
            )

            // Magnesium
            FoodSourceRow(
                electrolyte = "Magnesium",
                color = MagnesiumColor,
                foods = listOf(
                    FoodItem("🥜", "Almonds", "80mg/oz"),
                    FoodItem("🌻", "Pumpkin Seeds", "156mg/oz"),
                    FoodItem("🥬", "Spinach", "157mg/cup"),
                    FoodItem("🍫", "Dark Chocolate", "65mg/oz"),
                    FoodItem("🥑", "Avocado", "58mg/avocado"),
                    FoodItem("🫘", "Black Beans", "120mg/cup")
                )
            )

            // Calcium
            FoodSourceRow(
                electrolyte = "Calcium",
                color = CalciumColor,
                foods = listOf(
                    FoodItem("🥛", "Milk", "300mg/cup"),
                    FoodItem("🧀", "Cheese", "200mg/oz"),
                    FoodItem("🥬", "Kale", "180mg/cup"),
                    FoodItem("🐟", "Sardines", "325mg/can"),
                    FoodItem("🥦", "Broccoli", "43mg/cup"),
                    FoodItem("🫘", "Tofu", "250mg/½ cup")
                )
            )

            // Pro tip
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = WaterBlueSurface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_1), fontSize = 16.sp)
                    Text(
                        stringResource(R.string.txt_pro_tip_coconut_water_is_a_nat) +
                                "It's a great post-workout option!",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = WaterBlueDark
                    )
                }
            }
        }
    }
}

data class FoodItem(val emoji: String, val name: String, val amount: String)

@Composable
private fun FoodSourceRow(
    electrolyte: String,
    color: Color,
    foods: List<FoodItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Text(
                electrolyte,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
        }

        // Scrollable food items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            foods.forEach { food ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = color.copy(alpha = 0.06f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(food.emoji, fontSize = 24.sp)
                        Text(
                            food.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            food.amount,
                            fontSize = 9.sp,
                            color = color.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── DIY Electrolyte Drinks ──────────────────────────────────────────────────

@Composable
private fun DIYElectrolyteDrinksCard() {
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var showCopied by remember { mutableStateOf(false) }

    LaunchedEffect(showCopied) {
        if (showCopied) {
            delay(2000)
            showCopied = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF26A69A), Color(0xFF00897B))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_69), fontSize = 22.sp)
                    Text(
                        stringResource(R.string.txt_diy_electrolyte_drink),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }

                Text(
                    stringResource(R.string.txt_make_your_own_natural_electrol),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                // Recipe card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.txt_homemade_sports_drink),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        val ingredients = listOf(
                            "💧" to "1 liter (4 cups) water",
                            "🧂" to "¼ teaspoon salt (sodium)",
                            "🍋" to "¼ cup fresh lemon or orange juice (potassium)",
                            "🍯" to "2 tablespoons honey or maple syrup (energy)"
                        )

                        ingredients.forEach { (icon, text) ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(icon, fontSize = 16.sp)
                                Text(
                                    text,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        Text(
                            stringResource(R.string.txt_mix_all_ingredients_until_diss),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                // Copy button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val recipe = "DIY Electrolyte Drink:\n- 1 liter water\n- ¼ tsp salt\n- ¼ cup lemon/orange juice\n- 2 tbsp honey\nMix and chill!"
                            clipboardManager.setText(AnnotatedString(recipe))
                            showCopied = true
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (showCopied) "Copied! ✓" else "Copy Recipe", fontSize = 13.sp)
                    }
                }

                // Variations
                Text(
                    stringResource(R.string.txt_variations_add_coconut_water_f) +
                            "For a sugar-free version, use a small amount of stevia instead of honey.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ─── WHO ORS Formula ─────────────────────────────────────────────────────────

@Composable
private fun WHOORSCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_64), fontSize = 20.sp)
                Text(
                    stringResource(R.string.txt_who_oral_rehydration_solution),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Critical banner
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_21), fontSize = 18.sp)
                    Text(
                        stringResource(R.string.txt_for_cases_of_severe_dehydratio) +
                                "the WHO recommends a specific oral rehydration solution (ORS):",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFC62828)
                    )
                }
            }

            // Formula
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3E5F5)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        stringResource(R.string.txt_who_ors_formula_per_1_liter_wa),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF7B1FA2)
                    )

                    HorizontalDivider(color = Color(0xFF7B1FA2).copy(alpha = 0.1f))

                    val ingredients = listOf(
                        "🧂" to "½ teaspoon (2.6g)" to "Table salt (sodium chloride)",
                        "🍚" to "6 teaspoons (13.5g)" to "Sugar (glucose)",
                        "💧" to "1 liter" to "Clean drinking water"
                    )

                    ingredients.forEach { (iconAmount, desc) ->
                        val (icon, amount) = iconAmount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(icon, fontSize = 18.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    amount,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF7B1FA2).copy(alpha = 0.1f))

                    Text(
                        stringResource(R.string.txt_sip_small_amounts_frequently_f) +
                                "For adults: frequent small sips or about 200-400ml after each loose stool.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Important notes
            val notes = listOf(
                "🔬" to "The glucose in ORS helps the intestine absorb sodium and water more effectively",
                "❌" to "Do NOT add extra sugar or salt — precise proportions are important",
                "🏪" to "Pre-made ORS packets are available at pharmacies and are recommended for accuracy",
                "🏥" to "Seek medical attention if dehydration is severe or symptoms persist"
            )

            notes.forEach { (icon, text) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(icon, fontSize = 14.sp)
                    Text(
                        text,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ─── Warning Signs ───────────────────────────────────────────────────────────

@Composable
private fun ElectrolyteImbalanceWarningsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.txt_text_placeholder_21), fontSize = 20.sp)
                Text(
                    stringResource(R.string.txt_signs_of_electrolyte_imbalance),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE65100)
                )
            }

            HorizontalDivider(color = Color(0xFFE65100).copy(alpha = 0.1f))

            Text(
                stringResource(R.string.txt_watch_for_these_warning_signs_),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            val warnings = listOf(
                "💪 Muscle cramps or spasms" to "Especially during or after exercise",
                "😵💫 Dizziness or lightheadedness" to "Particularly when standing up",
                "🤕 Persistent headache" to "Not relieved by plain water",
                "😴 Unusual fatigue or weakness" to "Beyond normal tiredness",
                "🤢 Nausea or stomach upset" to "Without other obvious cause",
                "💓 Irregular heartbeat" to "Seek medical attention if severe",
                "🧠 Confusion or brain fog" to "Difficulty concentrating",
                "🤲 Numbness or tingling" to "In hands, feet, or limbs"
            )

            warnings.forEach { (symptom, detail) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFFFE0B2).copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(symptom.substring(0, 2), fontSize = 16.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            symptom.substring(3),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            detail,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Emergency note
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFCDD2)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(stringResource(R.string.txt_text_placeholder_57), fontSize = 18.sp)
                    Text(
                        stringResource(R.string.txt_seek_immediate_medical_attenti) +
                                "loss of consciousness, severe confusion, or significant cardiac irregularities.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Disclaimer ──────────────────────────────────────────────────────────────

@Composable
private fun ElectrolyteDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(R.string.txt_medical_disclaimer),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    stringResource(R.string.txt_this_information_is_for_educat_2) +
                            "based on individual health conditions, medications, and other factors. " +
                            "People with kidney disease, heart conditions, or on certain medications should consult " +
                            "a healthcare provider before significantly changing electrolyte intake. " +
                            "For severe dehydration, always seek professional medical care.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
