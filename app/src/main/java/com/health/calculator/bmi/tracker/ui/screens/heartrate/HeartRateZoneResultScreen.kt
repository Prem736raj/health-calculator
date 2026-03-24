package com.health.calculator.bmi.tracker.ui.screens.heartrate

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.health.calculator.bmi.tracker.util.HeartRateZone
import com.health.calculator.bmi.tracker.util.HeartRateZoneResult
import com.health.calculator.bmi.tracker.util.HeartRateZoneRecommendationEngine
import com.health.calculator.bmi.tracker.util.WHOExerciseGuidelinesEngine
import com.health.calculator.bmi.tracker.util.ActivityHeartRateReferenceEngine
import com.health.calculator.bmi.tracker.util.VO2MaxCalculator
import com.health.calculator.bmi.tracker.ui.components.HeartRateFormula
import com.health.calculator.bmi.tracker.ui.components.FitnessLevel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateZoneResultScreen(
    result: HeartRateZoneResult,
    weightKg: Float = 70f,
    onNavigateBack: () -> Unit,
    onSaveToHistory: (HeartRateZoneResult) -> Unit,
    onRecalculate: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var showResults by remember { mutableStateOf(false) }
    var savedToHistory by remember { mutableStateOf(false) }
    var expandedZoneIndex by remember { mutableIntStateOf(-1) }
    var selectedGoalName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        delay(200)
        showResults = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Heart Rate Zones", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        shareHeartRateZonesText(context, result)
                    }) {
                        Icon(Icons.Default.Share, "Share as Text")
                    }
                    IconButton(onClick = {
                        shareHeartRateZonesAsImage(context, result)
                    }) {
                        Icon(Icons.Default.Image, "Share as Image")
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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Max HR Card
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn()
                ) {
                    MaxHeartRateCard(result = result)
                }
            }

            // Edge Case Warnings
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = fadeIn(tween(300, delayMillis = 150)) +
                            expandVertically(tween(300, delayMillis = 150))
                ) {
                    HeartRateEdgeCaseWarnings(
                        age = result.age,
                        restingHR = result.restingHeartRate,
                        maxHR = result.maxHeartRate
                    )
                }
            }

            // HRR info for Karvonen
            if (result.heartRateReserve != null) {
                item {
                    AnimatedVisibility(
                        visible = showResults,
                        enter = expandVertically(
                            animationSpec = tween(400, delayMillis = 200)
                        ) + fadeIn(tween(400, delayMillis = 200))
                    ) {
                        KarvonenInfoCard(result = result)
                    }
                }
            }

            // Formula Comparison
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = expandVertically(tween(400, delayMillis = 250)) +
                            fadeIn(tween(400, delayMillis = 250))
                ) {
                    FormulaComparisonCard(
                        age = result.age,
                        selectedFormula = result.formulaUsed,
                        gender = result.gender,
                        customMaxHR = if (result.formulaUsed == HeartRateFormula.CUSTOM) result.maxHeartRate else null
                    )
                }
            }

            // Visual Zone Chart
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = expandVertically(
                        animationSpec = tween(500, delayMillis = 300)
                    ) + fadeIn(tween(500, delayMillis = 300))
                ) {
                    ZoneVisualizationChart(zones = result.zones, maxHR = result.maxHeartRate)
                }
            }

            // Zone Cards
            itemsIndexed(result.zones) { index, zone ->
                val delayMs = 400 + (index * 100)
                AnimatedVisibility(
                    visible = showResults,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(450, delayMillis = delayMs, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(450, delayMillis = delayMs))
                ) {
                    ZoneDetailCard(
                        zone = zone,
                        isExpanded = expandedZoneIndex == index,
                        onToggleExpand = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            expandedZoneIndex = if (expandedZoneIndex == index) -1 else index
                        }
                    )
                }
            }

            // Action Buttons
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = fadeIn(tween(400, delayMillis = 900))
                ) {
                    ActionButtonsSection(
                        savedToHistory = savedToHistory,
                        onSave = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSaveToHistory(result)
                            savedToHistory = true
                        },
                        onRecalculate = onRecalculate,
                        result = result
                    )
                }
            }

            // Recommendations Section
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = fadeIn(tween(400, delayMillis = 1000)) +
                            expandVertically(tween(400, delayMillis = 1000))
                ) {
                    val recommendations = remember(result) {
                        HeartRateZoneRecommendationEngine.generateRecommendations(
                            fitnessLevel = result.fitnessLevel,
                            zones = result.zones
                        )
                    }

                    val calorieBurns = remember(result, weightKg) {
                        HeartRateZoneRecommendationEngine.estimateCaloriesPerZone(
                            weightKg = weightKg,
                            zones = result.zones
                        )
                    }

                    ZoneRecommendationsSection(
                        recommendations = recommendations,
                        calorieBurns = calorieBurns,
                        fitnessLevel = result.fitnessLevel,
                        onGoalSelected = { goalName ->
                            selectedGoalName = goalName
                        }
                    )
                }
            }

            // WHO Guidelines Section
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = fadeIn(tween(400, delayMillis = 1200)) +
                            expandVertically(tween(400, delayMillis = 1200))
                ) {
                    WHOGuidelinesSection(
                        age = result.age,
                        fitnessLevel = result.fitnessLevel,
                        zones = result.zones,
                        selectedGoalName = selectedGoalName.ifEmpty {
                            // Default based on fitness level
                            when (result.fitnessLevel) {
                                FitnessLevel.BEGINNER -> "Beginner Safe Start"
                                FitnessLevel.INTERMEDIATE -> "Cardiovascular Fitness"
                                FitnessLevel.ADVANCED -> "Performance & Speed"
                            }
                        }
                    )
                }
            }

            // Activity Reference Section
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = fadeIn(tween(400, delayMillis = 1400)) +
                            expandVertically(tween(400, delayMillis = 1400))
                ) {
                    ActivityReferenceSection(
                        zones = result.zones,
                        weightKg = weightKg,
                        restingHR = result.restingHeartRate
                    )
                }
            }

            // VO2 Max & Fitness Age Section
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = fadeIn(tween(400, delayMillis = 1600)) +
                            expandVertically(tween(400, delayMillis = 1600))
                ) {
                    VO2MaxSection(
                        maxHR = result.maxHeartRate,
                        restingHR = result.restingHeartRate,
                        age = result.age,
                        gender = result.gender
                    )
                }
            }

            // Educational Content Section
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = fadeIn(tween(400, delayMillis = 1800)) +
                            expandVertically(tween(400, delayMillis = 1800))
                ) {
                    HeartRateEducationalSection()
                }
            }

            // Resting HR Trend Tracker
            item {
                AnimatedVisibility(
                    visible = showResults,
                    enter = fadeIn(tween(400, delayMillis = 2000)) +
                            expandVertically(tween(400, delayMillis = 2000))
                ) {
                    // Note: In a real app, this would come from the ViewModel's history flow
                    val sampleRestingHRHistory = remember {
                        listOf<Pair<String, Int>>() // Empty initially
                    }

                    RestingHRTrendCard(
                        restingHRHistory = sampleRestingHRHistory
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ============================================================
// MAX HR CARD
// ============================================================

@Composable
private fun MaxHeartRateCard(result: HeartRateZoneResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE53935).copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Pulsing Heart with MHR
            AnimatedHeartWithBPM(
                bpm = result.maxHeartRate,
                label = "Max Heart Rate"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Formula used
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = "${result.formulaUsed.label}: ${result.formulaUsed.formulaText}",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )

            if (result.restingHeartRate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(
                        icon = "💤",
                        label = "Resting",
                        value = "${result.restingHeartRate} BPM"
                    )
                    InfoChip(
                        icon = "📊",
                        label = "Reserve",
                        value = "${result.heartRateReserve} BPM"
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedHeartWithBPM(bpm: Int, label: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "mhr_heartbeat")

    // Pulsing scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                1f at 0
                1.12f at 100 using FastOutSlowInEasing
                1f at 250
                1.08f at 350 using FastOutSlowInEasing
                1f at 500
                1f at 800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "mhr_scale"
    )

    // Glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 800
                0.1f at 0
                0.3f at 100
                0.1f at 250
                0.25f at 350
                0.1f at 500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "mhr_glow"
    )

    // Animated BPM counter
    val animatedBpm by animateIntAsState(
        targetValue = bpm,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "bpm_counter"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // Glow ring
            Canvas(modifier = Modifier.size((90 * scale).dp)) {
                drawCircle(
                    color = Color(0xFFE53935).copy(alpha = glowAlpha),
                    radius = size.minDimension / 2
                )
            }
            // Heart
            Text(
                text = "❤️",
                fontSize = (42 * scale).sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$animatedBpm",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE53935)
        )
        Text(
            text = "BPM",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE53935).copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun InfoChip(icon: String, label: String, value: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================
// KARVONEN INFO CARD
// ============================================================

@Composable
private fun KarvonenInfoCard(result: HeartRateZoneResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("📐", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Karvonen Method",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2196F3)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your zones are personalized using Heart Rate Reserve (HRR).\n" +
                            "HRR = Max HR (${result.maxHeartRate}) − Resting HR (${result.restingHeartRate}) = ${result.heartRateReserve} BPM\n" +
                            "Target HR = (HRR × %Intensity) + Resting HR",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ============================================================
// ZONE VISUALIZATION CHART
// ============================================================

@Composable
private fun ZoneVisualizationChart(
    zones: List<HeartRateZone>,
    maxHR: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Zone Overview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Stacked horizontal bars
            zones.reversed().forEach { zone ->
                ZoneBar(zone = zone, maxBPM = maxHR)
                if (zone.zoneNumber > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BPM scale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                zones.firstOrNull()?.let {
                    Text(
                        text = "${it.bpmLow}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                Text(
                    text = "$maxHR BPM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun ZoneBar(zone: HeartRateZone, maxBPM: Int) {
    val animatedWidth by animateFloatAsState(
        targetValue = (zone.bpmHigh.toFloat() / maxBPM).coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = zone.zoneNumber * 100,
            easing = FastOutSlowInEasing
        ),
        label = "zone_bar_${zone.zoneNumber}"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Zone label
        Text(
            text = "Z${zone.zoneNumber}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = zone.color,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
        ) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(zone.color.copy(alpha = 0.1f))
            )

            // Filled bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                zone.color.copy(alpha = 0.4f),
                                zone.color.copy(alpha = 0.8f)
                            )
                        )
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "${zone.bpmLow}-${zone.bpmHigh}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = zone.icon,
            fontSize = 16.sp
        )
    }
}

// ============================================================
// ZONE DETAIL CARD
// ============================================================

@Composable
private fun ZoneDetailCard(
    zone: HeartRateZone,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = zone.color.copy(alpha = 0.06f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = zone.color.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zone number circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(zone.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing heart at zone speed
                    ZonePulsingHeart(zone = zone)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Zone ${zone.zoneNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = zone.color
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = zone.icon,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "${zone.zoneName} · ${zone.subtitle}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // BPM range pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = zone.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${zone.bpmLow}-${zone.bpmHigh}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = zone.color,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Percentage range
            Text(
                text = "${zone.percentLow}% - ${zone.percentHigh}% of Max HR",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            // Expanded details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = zone.color.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ZoneDetailRow(
                        icon = "🎯",
                        title = "Purpose",
                        content = zone.purpose
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ZoneDetailRow(
                        icon = "💬",
                        title = "Talk Test",
                        content = zone.talkTest
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ZoneDetailRow(
                        icon = "🏋️",
                        title = "Effort",
                        content = zone.effortDescription
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ZoneDetailRow(
                        icon = "⏱️",
                        title = "Recommended Duration",
                        content = zone.recommendedDuration
                    )
                }
            }

            // Tap hint
            if (!isExpanded) {
                Text(
                    text = "Tap for details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ZoneDetailRow(icon: String, title: String, content: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 17.sp
            )
        }
    }
}

// ============================================================
// ZONE-SPECIFIC PULSING HEART
// ============================================================

@Composable
private fun ZonePulsingHeart(zone: HeartRateZone) {
    // Different pulse speeds for each zone
    val pulseDuration = when (zone.zoneNumber) {
        1 -> 1500   // Slow pulse
        2 -> 1200
        3 -> 900
        4 -> 650
        5 -> 400    // Rapid pulse
        else -> 1000
    }

    val infiniteTransition = rememberInfiniteTransition(label = "zone_${zone.zoneNumber}_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = pulseDuration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zone_${zone.zoneNumber}_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = pulseDuration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zone_${zone.zoneNumber}_alpha"
    )

    Text(
        text = "❤️",
        fontSize = (20 * scale).sp,
        modifier = Modifier.offset(y = (-1).dp),
        color = Color.Unspecified.copy(alpha = alpha)
    )
}

// ============================================================
// ACTION BUTTONS
// ============================================================

@Composable
private fun ActionButtonsSection(
    savedToHistory: Boolean,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    result: HeartRateZoneResult
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !savedToHistory,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (savedToHistory) Color(0xFF4CAF50)
                    else MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = if (savedToHistory) Icons.Default.Check
                    else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (savedToHistory) "Saved to History ✓" else "Save to History",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRecalculate,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recalculate", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { shareHeartRateZonesText(context, result) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Text", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = { shareHeartRateZonesAsImage(context, result) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Image", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
