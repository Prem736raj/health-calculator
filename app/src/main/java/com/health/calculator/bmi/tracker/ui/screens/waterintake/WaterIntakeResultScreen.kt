// ui/screens/waterintake/WaterIntakeResultScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.WaterIntakeCalculation
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

// Water-themed colors (shared with input screen)
private val WaterBlueLight = Color(0xFF64B5F6)
private val WaterBlueMedium = Color(0xFF2196F3)
private val WaterBlueDark = Color(0xFF1565C0)
private val WaterBluePale = Color(0xFFBBDEFB)
private val WaterBlueSurface = Color(0xFFE3F2FD)
private val WaterCyan = Color(0xFF00BCD4)
private val BottleOutline = Color(0xFF90CAF9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterIntakeResultScreen(
    viewModel: WaterIntakeViewModel,
    onNavigateBack: () -> Unit,
    onRecalculate: () -> Unit,
    onStartTracking: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val result = viewModel.calculationResult ?: return
    val scrollState = rememberScrollState()

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Haptic on result appear
    LaunchedEffect(Unit) {
        delay(400)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Save confirmation
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💧", fontSize = 22.sp)
                        Text("Your Water Goal", fontWeight = FontWeight.Bold)
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Water Bottle Visual with primary result
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.8f)
                ) {
                    WaterBottleResultCard(result)
                }

                // Multiple unit display
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 40 }
                ) {
                    UnitConversionsCard(result)
                }

                // Glasses visualization
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 40 }
                ) {
                    GlassesVisualizationCard(result)
                }

                // Hourly breakdown
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 400)) + slideInVertically(tween(500, 400)) { 40 }
                ) {
                    HourlyBreakdownCard(result)
                }

                // Factor breakdown
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 500)) + slideInVertically(tween(500, 500)) { 40 }
                ) {
                    FactorBreakdownCard(viewModel)
                }

                // Hydration tips
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(500, 600)) + slideInVertically(tween(500, 600)) { 40 }
                ) {
                    HydrationTipsCard()
                }

                Spacer(Modifier.height(16.dp))
            }

            // Bottom action bar
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(tween(500, 400)) { it } + fadeIn(tween(500, 400)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BottomActionBar(
                    isSaved = isSaved,
                    onSave = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveToHistory()
                        isSaved = true
                        showSaveConfirmation = true
                    },
                    onRecalculate = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRecalculate()
                    },
                    onShare = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        shareWaterResult(context, result)
                    },
                    onStartTracking = onStartTracking
                )
            }

            // Save confirmation snackbar
            AnimatedVisibility(
                visible = showSaveConfirmation,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            ) {
                LaunchedEffect(showSaveConfirmation) {
                    if (showSaveConfirmation) {
                        delay(2500)
                        showSaveConfirmation = false
                    }
                }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text("Goal saved successfully!", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─── Water Bottle Visual ─────────────────────────────────────────────────────

@Composable
private fun WaterBottleResultCard(result: WaterIntakeCalculation) {
    val fillFraction = (result.recommendedIntakeMl.toFloat() / 4000f).coerceIn(0.2f, 1f)

    // Animate fill level
    val animatedFill by animateFloatAsState(
        targetValue = fillFraction,
        animationSpec = tween(1500, easing = EaseOutCubic),
        label = "bottle_fill"
    )

    // Wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Liters display animate
    val animatedLiters by animateFloatAsState(
        targetValue = result.recommendedIntakeMl / 1000f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "liters"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WaterBlueDark,
                            WaterBlueMedium,
                            WaterCyan
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Your Daily Water Goal",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Water Bottle Canvas
                Box(
                    modifier = Modifier
                        .size(width = 140.dp, height = 220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawWaterBottle(
                            fillLevel = animatedFill,
                            wavePhase = wavePhase
                        )
                    }

                    // Overlay text on bottle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = 20.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", animatedLiters),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Liters",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Subtitle
                Text(
                    text = "${result.recommendedIntakeMl} ml per day",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Motivational message
                val motivationText = when {
                    result.recommendedIntakeMl >= 3500 -> "💪 You need extra hydration! Stay on top of it."
                    result.recommendedIntakeMl >= 2500 -> "🌊 A solid water goal — your body will thank you!"
                    else -> "✨ A healthy daily target — easy to achieve!"
                }
                Text(
                    text = motivationText,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun DrawScope.drawWaterBottle(fillLevel: Float, wavePhase: Float) {
    val width = size.width
    val height = size.height

    // Bottle dimensions
    val bottleLeft = width * 0.2f
    val bottleRight = width * 0.8f
    val bottleTop = height * 0.15f
    val bottleBottom = height * 0.92f
    val neckLeft = width * 0.35f
    val neckRight = width * 0.65f
    val neckTop = height * 0.02f
    val shoulderY = height * 0.18f

    // Bottle outline path
    val bottlePath = Path().apply {
        // Cap top
        moveTo(neckLeft + 4, neckTop)
        lineTo(neckRight - 4, neckTop)

        // Right side of neck
        lineTo(neckRight, neckTop + 8)
        lineTo(neckRight, shoulderY - 20)

        // Right shoulder curve
        cubicTo(
            neckRight, shoulderY,
            bottleRight - 20, shoulderY,
            bottleRight, shoulderY + 20
        )

        // Right body
        lineTo(bottleRight, bottleBottom - 16)

        // Bottom right curve
        cubicTo(
            bottleRight, bottleBottom,
            bottleRight - 16, bottleBottom,
            bottleRight - 16, bottleBottom
        )

        // Bottom
        lineTo(bottleLeft + 16, bottleBottom)

        // Bottom left curve
        cubicTo(
            bottleLeft, bottleBottom,
            bottleLeft, bottleBottom,
            bottleLeft, bottleBottom - 16
        )

        // Left body
        lineTo(bottleLeft, shoulderY + 20)

        // Left shoulder curve
        cubicTo(
            bottleLeft, shoulderY,
            neckLeft + 20, shoulderY,
            neckLeft, shoulderY - 20
        )

        // Left side of neck
        lineTo(neckLeft, neckTop + 8)
        lineTo(neckLeft + 4, neckTop)

        close()
    }

    // Draw bottle outline
    drawPath(
        path = bottlePath,
        color = Color.White.copy(alpha = 0.3f),
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )

    // Fill area calculation
    val fillableHeight = bottleBottom - shoulderY
    val waterTop = bottleBottom - (fillableHeight * fillLevel)

    // Water fill with wave
    val waterPath = Path().apply {
        moveTo(bottleLeft + 4, bottleBottom - 16)

        // Bottom
        lineTo(bottleRight - 4, bottleBottom - 16)

        // Right side up
        lineTo(bottleRight - 2, waterTop)

        // Wave top
        val waveAmplitude = 6f
        val steps = 30
        for (i in steps downTo 0) {
            val x = bottleLeft + 2 + (bottleRight - bottleLeft - 4) * (i.toFloat() / steps)
            val waveY = waterTop + sin(wavePhase + (i.toFloat() / steps) * 2 * PI.toFloat()) * waveAmplitude
            lineTo(x, waveY)
        }

        // Left side down
        lineTo(bottleLeft + 2, bottleBottom - 16)

        close()
    }

    // Clip water to bottle shape
    clipPath(bottlePath) {
        // Water gradient fill
        drawPath(
            path = waterPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF42A5F5).copy(alpha = 0.6f),
                    Color(0xFF1E88E5).copy(alpha = 0.8f),
                    Color(0xFF1565C0).copy(alpha = 0.9f)
                ),
                startY = waterTop,
                endY = bottleBottom
            )
        )

        // Water highlight
        drawPath(
            path = waterPath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.05f)
                )
            )
        )
    }

    // Bottle glass reflection
    drawLine(
        color = Color.White.copy(alpha = 0.2f),
        start = Offset(bottleLeft + 12, shoulderY + 30),
        end = Offset(bottleLeft + 12, bottleBottom - 40),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // Cap
    drawRoundRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(neckLeft + 2, neckTop - 2),
        size = Size(neckRight - neckLeft - 4, 12f),
        cornerRadius = CornerRadius(4f),
        style = Fill
    )
}

// ─── Unit Conversions Card ───────────────────────────────────────────────────

@Composable
private fun UnitConversionsCard(result: WaterIntakeCalculation) {
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
                Text("📐", fontSize = 18.sp)
                Text(
                    "Your Goal in Different Units",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            val liters = result.recommendedIntakeMl / 1000f
            val flOz = result.recommendedIntakeOz
            val cups = result.recommendedIntakeMl / 250f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UnitItem(
                    value = "${result.recommendedIntakeMl}",
                    unit = "ml",
                    icon = "💧"
                )
                UnitDivider()
                UnitItem(
                    value = String.format("%.1f", liters),
                    unit = "Liters",
                    icon = "🫗"
                )
                UnitDivider()
                UnitItem(
                    value = String.format("%.0f", flOz),
                    unit = "fl oz",
                    icon = "🥤"
                )
                UnitDivider()
                UnitItem(
                    value = String.format("%.1f", cups),
                    unit = "Cups",
                    icon = "☕"
                )
            }
        }
    }
}

@Composable
private fun UnitItem(value: String, unit: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 22.sp)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = WaterBlueDark
        )
        Text(
            text = unit,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun UnitDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(50.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    )
}

// ─── Glasses Visualization ───────────────────────────────────────────────────

@Composable
private fun GlassesVisualizationCard(result: WaterIntakeCalculation) {
    val glasses = result.recommendedGlasses

    // Animate glasses appearing
    var animatedCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(glasses) {
        animatedCount = 0
        for (i in 1..glasses) {
            delay(80)
            animatedCount = i
        }
    }

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
                Text("🥛", fontSize = 18.sp)
                Text(
                    "That's $glasses glasses of water!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Text(
                text = "Each glass = 250 ml (8.5 fl oz)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            // Glass grid
            val columns = 5
            val rows = (glasses + columns - 1) / columns

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val startIndex = row * columns
                        for (col in 0 until columns) {
                            val glassIndex = startIndex + col
                            if (glassIndex < glasses) {
                                val isFilled = glassIndex < animatedCount
                                AnimatedVisibility(
                                    visible = true,
                                    enter = scaleIn(tween(200)) + fadeIn(tween(200))
                                ) {
                                    GlassIcon(
                                        filled = isFilled,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassIcon(filled: Boolean, modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(
        targetValue = if (filled) 1f else 0.25f,
        animationSpec = tween(300),
        label = "glass_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (filled) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "glass_scale"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🥛",
            fontSize = (28 * scale).sp,
            modifier = Modifier.alpha(alpha)
        )
    }
}

// ─── Hourly Breakdown ────────────────────────────────────────────────────────

@Composable
private fun HourlyBreakdownCard(result: WaterIntakeCalculation) {
    val wakingHours = 16
    val hourlyMl = result.recommendedIntakeMl / wakingHours
    val hourlyOz = result.recommendedIntakeOz / wakingHours

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
                        colors = listOf(
                            WaterBlueSurface,
                            WaterBluePale.copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⏰", fontSize = 20.sp)
                    Text(
                        "Hourly Recommendation",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = WaterBlueDark
                    )
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(WaterBlueMedium.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💧", fontSize = 24.sp)
                        }
                        Column {
                            Text(
                                text = "Drink ~${hourlyMl} ml every hour",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = WaterBlueDark
                            )
                            Text(
                                text = "(~${String.format("%.1f", hourlyOz)} fl oz) during your ${wakingHours} waking hours",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Timeline visualization
                HourlyTimeline(hourlyMl, wakingHours)
            }
        }
    }
}

@Composable
private fun HourlyTimeline(hourlyMl: Int, wakingHours: Int) {
    val sampleHours = listOf(
        "7 AM" to "☀️",
        "9 AM" to "🏢",
        "12 PM" to "🍽️",
        "3 PM" to "☕",
        "6 PM" to "🏃",
        "9 PM" to "🌙"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        sampleHours.forEach { (time, icon) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(icon, fontSize = 16.sp)
                Text(
                    text = time,
                    fontSize = 9.sp,
                    color = WaterBlueDark.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${hourlyMl}ml",
                    fontSize = 8.sp,
                    color = WaterBlueMedium.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Factor Breakdown ────────────────────────────────────────────────────────

@Composable
private fun FactorBreakdownCard(viewModel: WaterIntakeViewModel) {
    val result = viewModel.calculationResult ?: return

    val weightKg = result.weightKg
    val baseMl = (weightKg * 35).toInt()

    // Recalculate individual adjustments for display
    val ageAdjusted = calculateAgeAdjustedBase(baseMl, result.age, weightKg)
    val genderAdjustment = if (result.gender == "Female" && result.age >= 18) {
        -(ageAdjusted * 0.1f).toInt()
    } else 0

    val afterGender = ageAdjusted + genderAdjustment

    val activityLevel = try {
        com.health.calculator.bmi.tracker.data.model.WaterActivityLevel.valueOf(result.activityLevel)
    } catch (e: Exception) {
        com.health.calculator.bmi.tracker.data.model.WaterActivityLevel.SEDENTARY
    }
    val activityAdjustment = (afterGender * (activityLevel.multiplier - 1f)).toInt()

    val climate = try {
        com.health.calculator.bmi.tracker.data.model.ClimateType.valueOf(result.climate)
    } catch (e: Exception) {
        com.health.calculator.bmi.tracker.data.model.ClimateType.TEMPERATE
    }
    val climateAdjustment = (afterGender * (climate.multiplier - 1f)).toInt()

    val healthStatus = try {
        com.health.calculator.bmi.tracker.data.model.HealthStatus.valueOf(result.healthStatus)
    } catch (e: Exception) {
        com.health.calculator.bmi.tracker.data.model.HealthStatus.NORMAL
    }
    val healthAdjustment = healthStatus.additionalMl

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
                Text("📊", fontSize = 18.sp)
                Text(
                    "How We Calculated This",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Base requirement
            BreakdownRow(
                label = "Base requirement",
                detail = "${String.format("%.1f", weightKg)} kg × 35 ml/kg",
                value = "$baseMl ml",
                color = WaterBlueMedium
            )

            // Age adjustment
            if (ageAdjusted != baseMl) {
                val ageSign = if (ageAdjusted - baseMl >= 0) "+" else ""
                BreakdownRow(
                    label = "Age adjustment",
                    detail = "Age ${result.age}",
                    value = "$ageSign${ageAdjusted - baseMl} ml",
                    color = Color(0xFFFF9800)
                )
            }

            // Gender adjustment
            if (genderAdjustment != 0) {
                BreakdownRow(
                    label = "Gender adjustment",
                    detail = result.gender,
                    value = "${genderAdjustment} ml",
                    color = Color(0xFFE91E63)
                )
            }

            // Activity adjustment
            if (activityAdjustment > 0) {
                BreakdownRow(
                    label = "Activity level",
                    detail = activityLevel.displayName.substringBefore("(").trim(),
                    value = "+$activityAdjustment ml",
                    color = Color(0xFF4CAF50)
                )
            }

            // Climate adjustment
            if (climateAdjustment > 0) {
                BreakdownRow(
                    label = "Climate",
                    detail = climate.displayName,
                    value = "+$climateAdjustment ml",
                    color = Color(0xFFFF5722)
                )
            }

            // Health adjustment
            if (healthAdjustment > 0) {
                BreakdownRow(
                    label = "Health status",
                    detail = healthStatus.displayName,
                    value = "+$healthAdjustment ml",
                    color = Color(0xFF9C27B0)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = WaterBlueMedium.copy(alpha = 0.3f),
                thickness = 2.dp
            )

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Daily Goal",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = WaterBlueDark
                )
                Text(
                    text = "${result.recommendedIntakeMl} ml",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = WaterBlueDark
                )
            }

            // Visual bar
            FactorBar(
                baseFraction = baseMl.toFloat() / result.recommendedIntakeMl,
                activityFraction = activityAdjustment.toFloat() / result.recommendedIntakeMl,
                climateFraction = climateAdjustment.toFloat() / result.recommendedIntakeMl,
                healthFraction = healthAdjustment.toFloat() / result.recommendedIntakeMl
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    detail: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
            )
            Column {
                Text(
                    text = label,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = detail,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = color
        )
    }
}

@Composable
private fun FactorBar(
    baseFraction: Float,
    activityFraction: Float,
    climateFraction: Float,
    healthFraction: Float
) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "bar_progress"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
    ) {
        if (baseFraction > 0) {
            Box(
                modifier = Modifier
                    .weight(baseFraction.coerceAtLeast(0.01f) * animProgress.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(WaterBlueMedium)
            )
        }
        if (activityFraction > 0) {
            Box(
                modifier = Modifier
                    .weight(activityFraction.coerceAtLeast(0.01f) * animProgress.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(0xFF4CAF50))
            )
        }
        if (climateFraction > 0) {
            Box(
                modifier = Modifier
                    .weight(climateFraction.coerceAtLeast(0.01f) * animProgress.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(0xFFFF5722))
            )
        }
        if (healthFraction > 0) {
            Box(
                modifier = Modifier
                    .weight(healthFraction.coerceAtLeast(0.01f) * animProgress.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(0xFF9C27B0))
            )
        }
        // Remainder for adjustments not separately tracked
        val remaining = 1f - baseFraction - activityFraction - climateFraction - healthFraction
        if (remaining > 0.01f) {
            Box(
                modifier = Modifier
                    .weight(remaining * animProgress.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(0xFFFF9800))
            )
        }
    }
}

private fun calculateAgeAdjustedBase(baseMl: Int, age: Int, weightKg: Float): Int {
    return when {
        age < 4 -> (weightKg * 100).toInt().coerceAtMost(1300)
        age < 9 -> 1400
        age < 14 -> 1800
        age < 18 -> 2200
        age in 18..30 -> baseMl
        age in 31..55 -> (baseMl * 0.95f).toInt()
        age in 56..75 -> (baseMl * 0.90f).toInt()
        else -> (baseMl * 0.85f).toInt()
    }
}

// ─── Hydration Tips ──────────────────────────────────────────────────────────

@Composable
private fun HydrationTipsCard() {
    val tips = listOf(
        "🌅" to "Start your day with a glass of water right after waking up",
        "⏰" to "Set regular reminders to drink throughout the day",
        "🍋" to "Add lemon, cucumber, or mint for flavor variety",
        "🍽️" to "Drink a glass of water 30 minutes before each meal",
        "📱" to "Keep a water bottle at your desk or workspace",
        "🏃" to "Drink extra before, during, and after exercise"
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
                Text("💡", fontSize = 18.sp)
                Text(
                    "Hydration Tips",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            tips.forEach { (icon, tip) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(icon, fontSize = 16.sp)
                    Text(
                        text = tip,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ─── Bottom Action Bar ───────────────────────────────────────────────────────

@Composable
private fun BottomActionBar(
    isSaved: Boolean,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit,
    onStartTracking: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recalculate
            OutlinedButton(
                onClick = onRecalculate,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Recalculate", fontSize = 13.sp)
            }

            // Save / Saved
            Button(
                onClick = { if (!isSaved) onSave() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) Color(0xFF4CAF50) else WaterBlueMedium
                )
            ) {
                Icon(
                    if (isSaved) Icons.Default.Check else Icons.Default.Check,
                    null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (isSaved) "Saved ✓" else "Save Goal",
                    fontSize = 13.sp
                )
            }

            // Share
            FilledTonalButton(
                onClick = onShare,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
            }

            // Start Tracking / Track
            AnimatedVisibility(visible = isSaved) {
                Button(
                    onClick = onStartTracking,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Track 💧", fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── Share Helper ────────────────────────────────────────────────────────────

private fun shareWaterResult(context: android.content.Context, result: WaterIntakeCalculation) {
    val liters = result.recommendedIntakeMl / 1000f
    val text = buildString {
        appendLine("💧 My Daily Water Intake Goal")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━")
        appendLine("Total: ${String.format("%.1f", liters)} L (${result.recommendedIntakeMl} ml)")
        appendLine("Glasses: ${result.recommendedGlasses} × 250ml")
        appendLine("Hourly: ~${result.recommendedIntakeMl / 16} ml")
        appendLine()
        appendLine("Calculated using Health Calculator: BMI Tracker")
    }

    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(
        android.content.Intent.createChooser(intent, "Share Water Goal")
    )
}

// Helper for alpha modifier
@Composable
private fun Modifier.alpha(alpha: Float): Modifier {
    return this.then(Modifier.graphicsLayer { this.alpha = alpha })
}
