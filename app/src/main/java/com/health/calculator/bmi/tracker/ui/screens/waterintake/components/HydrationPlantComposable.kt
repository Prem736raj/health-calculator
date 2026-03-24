// ui/screens/waterintake/components/HydrationPlantComposable.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.PlantMood
import com.health.calculator.bmi.tracker.data.model.PlantStage
import com.health.calculator.bmi.tracker.data.model.PlantState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Plant colors
private val StemGreen = Color(0xFF4CAF50)
private val LeafGreen = Color(0xFF66BB6A)
private val LeafDarkGreen = Color(0xFF388E3C)
private val WiltYellow = Color(0xFFC8B900)
private val WiltBrown = Color(0xFF8D6E63)
private val FlowerPink = Color(0xFFEC407A)
private val FlowerYellow = Color(0xFFFFEB3B)
private val FlowerPurple = Color(0xFFAB47BC)
private val FruitRed = Color(0xFFF44336)
private val FruitOrange = Color(0xFFFF9800)
private val SoilBrown = Color(0xFF795548)
private val PotColor = Color(0xFFBCAAA4)
private val PotDarkColor = Color(0xFF8D6E63)
private val SkyBlue = Color(0xFFE3F2FD)
private val SparkleGold = Color(0xFFFFD700)

@Composable
fun HydrationPlantCard(
    plantState: PlantState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "plant")

    // Gentle sway
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Wilt droop
    val wiltDroop by animateFloatAsState(
        targetValue = when (plantState.mood) {
            PlantMood.WILTING -> 15f
            PlantMood.THIRSTY -> 8f
            PlantMood.NEUTRAL -> 3f
            else -> 0f
        },
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label = "wilt"
    )

    // Water splash animation
    var showSplash by remember { mutableStateOf(false) }
    LaunchedEffect(plantState.justWatered) {
        if (plantState.justWatered) {
            showSplash = true
            delay(1200)
            showSplash = false
        }
    }

    // Goal bloom animation
    var showBloom by remember { mutableStateOf(false) }
    LaunchedEffect(plantState.goalReachedToday) {
        if (plantState.goalReachedToday) {
            showBloom = true
        }
    }

    // Sparkle for streaks
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (plantState.currentStreak >= 7) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle"
    )

    // Perk up when watered
    val perkScale by animateFloatAsState(
        targetValue = if (showSplash) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "perk"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SkyBlue.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .padding(8.dp)
        ) {
            // Plant Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(perkScale)
            ) {
                drawPlant(
                    stage = plantState.stage,
                    mood = plantState.mood,
                    swayAngle = swayAngle,
                    wiltDroop = wiltDroop,
                    showBloom = showBloom,
                    sparkleAlpha = sparkleAlpha
                )
            }

            // Water splash overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = showSplash,
                enter = fadeIn(tween(200)) + scaleIn(tween(300)),
                exit = fadeOut(tween(500)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                WaterSplashEffect()
            }

            // Plant mood speech bubble
            PlantMoodBubble(
                mood = plantState.mood,
                stage = plantState.stage,
                goalReached = plantState.goalReachedToday,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )

            // Stage label
            Text(
                text = "${plantState.stage.displayName} 🌱",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )

            // Streak sparkle indicator
            if (plantState.currentStreak >= 7) {
                Text(
                    text = "✨",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .alpha(sparkleAlpha)
                )
            }
        }
    }
}

private fun DrawScope.drawPlant(
    stage: PlantStage,
    mood: PlantMood,
    swayAngle: Float,
    wiltDroop: Float,
    showBloom: Boolean,
    sparkleAlpha: Float
) {
    val centerX = size.width / 2
    val bottomY = size.height * 0.88f

    // Draw pot
    drawPot(centerX, bottomY)

    // Draw soil
    drawSoil(centerX, bottomY)

    // Draw plant based on stage
    rotate(degrees = swayAngle, pivot = Offset(centerX, bottomY - 20)) {
        when (stage) {
            PlantStage.SPROUT -> drawSprout(centerX, bottomY, wiltDroop, mood)
            PlantStage.SMALL_PLANT -> drawSmallPlant(centerX, bottomY, wiltDroop, mood)
            PlantStage.GROWING_PLANT -> drawGrowingPlant(centerX, bottomY, wiltDroop, mood)
            PlantStage.FLOWERING -> drawFloweringPlant(centerX, bottomY, wiltDroop, mood, showBloom)
            PlantStage.FULL_BLOOM -> drawFullBloom(centerX, bottomY, wiltDroop, mood, showBloom, sparkleAlpha)
        }
    }
}

private fun DrawScope.drawPot(centerX: Float, bottomY: Float) {
    val potWidth = size.width * 0.3f
    val potHeight = size.height * 0.18f
    val rimHeight = potHeight * 0.2f

    // Pot rim
    drawRoundRect(
        color = PotColor,
        topLeft = Offset(centerX - potWidth / 2 - 5, bottomY - potHeight),
        size = Size(potWidth + 10, rimHeight),
        cornerRadius = CornerRadius(4f)
    )

    // Pot body (trapezoid approximated as rounded rect)
    val bodyPath = Path().apply {
        moveTo(centerX - potWidth / 2, bottomY - potHeight + rimHeight)
        lineTo(centerX - potWidth / 2 + 8, bottomY)
        lineTo(centerX + potWidth / 2 - 8, bottomY)
        lineTo(centerX + potWidth / 2, bottomY - potHeight + rimHeight)
        close()
    }
    drawPath(bodyPath, PotDarkColor)

    // Pot highlight
    drawLine(
        color = Color.White.copy(alpha = 0.15f),
        start = Offset(centerX - potWidth / 4, bottomY - potHeight + rimHeight + 4),
        end = Offset(centerX - potWidth / 4 + 3, bottomY - 4),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSoil(centerX: Float, bottomY: Float) {
    val potWidth = size.width * 0.3f
    val soilTop = bottomY - size.height * 0.18f

    drawOval(
        color = SoilBrown,
        topLeft = Offset(centerX - potWidth / 2 + 3, soilTop - 4),
        size = Size(potWidth - 6, 14f)
    )
}

private fun DrawScope.drawSprout(centerX: Float, bottomY: Float, droop: Float, mood: PlantMood) {
    val stemTop = bottomY - size.height * 0.35f
    val stemColor = if (mood == PlantMood.WILTING) WiltYellow else StemGreen

    // Stem
    drawLine(
        color = stemColor,
        start = Offset(centerX, bottomY - size.height * 0.2f),
        end = Offset(centerX, stemTop + droop),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // Two tiny leaves
    val leafColor = if (mood == PlantMood.WILTING) WiltYellow else LeafGreen

    // Left leaf
    val leftLeafPath = Path().apply {
        moveTo(centerX, stemTop + droop + 10)
        cubicTo(
            centerX - 20, stemTop + droop - 5,
            centerX - 25, stemTop + droop + 10,
            centerX - 10, stemTop + droop + 18 + droop * 0.5f
        )
        close()
    }
    drawPath(leftLeafPath, leafColor)

    // Right leaf
    val rightLeafPath = Path().apply {
        moveTo(centerX, stemTop + droop + 10)
        cubicTo(
            centerX + 20, stemTop + droop - 5,
            centerX + 25, stemTop + droop + 10,
            centerX + 10, stemTop + droop + 18 + droop * 0.5f
        )
        close()
    }
    drawPath(rightLeafPath, leafColor)
}

private fun DrawScope.drawSmallPlant(centerX: Float, bottomY: Float, droop: Float, mood: PlantMood) {
    val stemBase = bottomY - size.height * 0.2f
    val stemTop = bottomY - size.height * 0.48f
    val stemColor = if (mood == PlantMood.WILTING) WiltYellow else StemGreen
    val leafColor = if (mood == PlantMood.WILTING) WiltYellow else LeafGreen

    // Stem
    drawLine(
        color = stemColor,
        start = Offset(centerX, stemBase),
        end = Offset(centerX, stemTop + droop),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    // Leaves at different heights
    drawLeafPair(centerX, stemTop + droop + 15, leafColor, 30f, droop)
    drawLeafPair(centerX, stemTop + droop + 35, leafColor, 22f, droop)

    // Top bud
    drawCircle(
        color = if (mood == PlantMood.THRIVING) LeafGreen else leafColor,
        radius = 6f,
        center = Offset(centerX, stemTop + droop)
    )
}

private fun DrawScope.drawGrowingPlant(centerX: Float, bottomY: Float, droop: Float, mood: PlantMood) {
    val stemBase = bottomY - size.height * 0.2f
    val stemTop = bottomY - size.height * 0.58f
    val stemColor = if (mood == PlantMood.WILTING) WiltBrown else StemGreen
    val leafColor = if (mood == PlantMood.WILTING) WiltYellow else LeafGreen
    val darkLeaf = if (mood == PlantMood.WILTING) WiltBrown else LeafDarkGreen

    // Main stem
    drawLine(
        color = stemColor,
        start = Offset(centerX, stemBase),
        end = Offset(centerX, stemTop + droop),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    // Multiple leaf pairs
    drawLeafPair(centerX, stemTop + droop + 10, leafColor, 35f, droop)
    drawLeafPair(centerX, stemTop + droop + 30, darkLeaf, 30f, droop * 0.7f)
    drawLeafPair(centerX, stemTop + droop + 50, leafColor, 25f, droop * 0.5f)

    // Small branch left
    val branchEndX = centerX - 20
    val branchEndY = stemTop + droop + 20
    drawLine(
        color = stemColor,
        start = Offset(centerX, stemTop + droop + 25),
        end = Offset(branchEndX, branchEndY),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Branch leaf
    drawLeaf(branchEndX, branchEndY, leafColor, -20f, 20f, droop * 0.3f)

    // Top cluster
    drawCircle(color = LeafGreen, radius = 8f, center = Offset(centerX, stemTop + droop))
    drawCircle(color = leafColor, radius = 5f, center = Offset(centerX - 6, stemTop + droop - 4))
    drawCircle(color = leafColor, radius = 5f, center = Offset(centerX + 6, stemTop + droop - 4))
}

private fun DrawScope.drawFloweringPlant(
    centerX: Float, bottomY: Float, droop: Float,
    mood: PlantMood, showBloom: Boolean
) {
    // Draw the growing plant base
    drawGrowingPlant(centerX, bottomY, droop, mood)

    val stemTop = bottomY - size.height * 0.58f

    // Flowers
    if (mood != PlantMood.WILTING) {
        // Main flower
        drawFlower(
            centerX, stemTop + droop - 8,
            if (showBloom) 14f else 8f,
            FlowerPink, FlowerYellow
        )

        // Side flower
        if (showBloom) {
            drawFlower(
                centerX - 22, stemTop + droop + 15,
                10f, FlowerPurple, FlowerYellow
            )
        }
    }
}

private fun DrawScope.drawFullBloom(
    centerX: Float, bottomY: Float, droop: Float,
    mood: PlantMood, showBloom: Boolean, sparkleAlpha: Float
) {
    drawFloweringPlant(centerX, bottomY, droop, mood, showBloom)

    val stemTop = bottomY - size.height * 0.58f

    if (mood != PlantMood.WILTING) {
        // Additional flowers
        drawFlower(
            centerX + 24, stemTop + droop + 18,
            9f, FlowerPink.copy(alpha = 0.8f), FlowerYellow
        )

        // Fruits
        if (showBloom) {
            drawCircle(
                color = FruitRed,
                radius = 7f,
                center = Offset(centerX - 15, stemTop + droop + 38)
            )
            drawCircle(
                color = FruitOrange,
                radius = 6f,
                center = Offset(centerX + 18, stemTop + droop + 42)
            )
            // Fruit highlights
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 2.5f,
                center = Offset(centerX - 17, stemTop + droop + 36)
            )
        }

        // Sparkles for long streaks
        if (sparkleAlpha > 0f) {
            val sparklePositions = listOf(
                Offset(centerX - 30, stemTop + droop - 15),
                Offset(centerX + 28, stemTop + droop + 5),
                Offset(centerX - 10, stemTop + droop - 25),
                Offset(centerX + 15, stemTop + droop + 30)
            )
            sparklePositions.forEach { pos ->
                drawCircle(
                    color = SparkleGold.copy(alpha = sparkleAlpha * 0.8f),
                    radius = 3f,
                    center = pos
                )
                drawCircle(
                    color = Color.White.copy(alpha = sparkleAlpha * 0.6f),
                    radius = 1.5f,
                    center = pos
                )
            }
        }
    }
}

private fun DrawScope.drawFlower(cx: Float, cy: Float, radius: Float, petalColor: Color, centerColor: Color) {
    val petalCount = 5
    for (i in 0 until petalCount) {
        val angle = (i * 360f / petalCount) * (PI.toFloat() / 180f)
        val petalX = cx + cos(angle) * radius * 0.8f
        val petalY = cy + sin(angle) * radius * 0.8f
        drawCircle(
            color = petalColor,
            radius = radius * 0.55f,
            center = Offset(petalX, petalY)
        )
    }
    // Center
    drawCircle(color = centerColor, radius = radius * 0.35f, center = Offset(cx, cy))
}

private fun DrawScope.drawLeafPair(cx: Float, cy: Float, color: Color, size: Float, droop: Float) {
    drawLeaf(cx, cy, color, -size, size * 0.6f, droop)
    drawLeaf(cx, cy, color, size, size * 0.6f, droop)
}

private fun DrawScope.drawLeaf(cx: Float, cy: Float, color: Color, offsetX: Float, height: Float, droop: Float) {
    val leafPath = Path().apply {
        moveTo(cx, cy)
        cubicTo(
            cx + offsetX * 0.6f, cy - height * 0.8f + droop * 0.3f,
            cx + offsetX, cy - height * 0.3f + droop * 0.5f,
            cx + offsetX * 0.5f, cy + height * 0.2f + droop * 0.8f
        )
        close()
    }
    drawPath(leafPath, color)
}

@Composable
private fun WaterSplashEffect() {
    val particles = remember {
        (0 until 8).map {
            val angle = it * 45f * (PI.toFloat() / 180f)
            Offset(cos(angle), sin(angle))
        }
    }

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "splash"
    )

    Box(modifier = Modifier.size(80.dp)) {
        particles.forEachIndexed { index, direction ->
            val distance = animProgress * 35f
            val alpha = (1f - animProgress).coerceAtLeast(0f)

            Text(
                text = "💧",
                fontSize = (12 * (1f - animProgress * 0.5f)).sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = (direction.x * distance).dp,
                        y = (direction.y * distance).dp
                    )
                    .alpha(alpha)
            )
        }
    }
}

@Composable
private fun PlantMoodBubble(
    mood: PlantMood,
    stage: PlantStage,
    goalReached: Boolean,
    modifier: Modifier = Modifier
) {
    val (emoji, message) = when {
        goalReached -> "🌟" to "I'm thriving!"
        mood == PlantMood.THRIVING -> "😊" to "So hydrated!"
        mood == PlantMood.HAPPY -> "🙂" to "Feeling good!"
        mood == PlantMood.NEUTRAL -> "😐" to "Need water..."
        mood == PlantMood.THIRSTY -> "😟" to "I'm thirsty!"
        mood == PlantMood.WILTING -> "😢" to "Help me..."
        else -> "🌱" to "Water me!"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bubble")
    val bubbleScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubble_scale"
    )

    Card(
        modifier = modifier.scale(bubbleScale),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                text = message,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// ─── Detailed Plant View (tapped) ────────────────────────────────────────────

@Composable
fun PlantDetailDialog(
    plantState: PlantState,
    plantName: String,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onHide: () -> Unit
) {
    var editingName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(plantName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = plantState.stage.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = plantState.stage.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Plant name
                if (editingName) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it.take(20) },
                        label = { Text("Plant Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { editingName = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            onNameChange(nameInput)
                            editingName = false
                        }) { Text("Save") }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🌱", fontSize = 20.sp)
                        Text(
                            plantName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = { editingName = true }) {
                            Text("✏️", fontSize = 14.sp)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Stats
                val statsItems = listOf(
                    "📅" to "Days tracked: ${plantState.totalDaysTracked}",
                    "🔥" to "Current streak: ${plantState.currentStreak}",
                    "💧" to "Today: ${(plantState.todayPercentage * 100).toInt()}%",
                    "🌱" to "Stage: ${plantState.stage.displayName}"
                )

                statsItems.forEach { (icon, text) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(icon, fontSize = 16.sp)
                        Text(text, fontSize = 13.sp)
                    }
                }

                // Growth progress
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                Text("Growth Progress", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                PlantStage.entries.forEach { stage ->
                    val isCompleted = plantState.totalDaysTracked >= stage.daysRequired
                    val isCurrent = plantState.stage == stage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isCurrent) StemGreen.copy(alpha = 0.1f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (isCompleted) "✅" else "⬜",
                            fontSize = 14.sp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stage.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) StemGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (stage.daysRequired == 0) "Start" else "${stage.daysRequired} days",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        if (isCurrent) {
                            Text("← You", fontSize = 10.sp, color = StemGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = onHide) {
                Text("Hide Plant", color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    )
}
