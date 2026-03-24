// ui/screens/waterintake/components/WaterCelebration.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.util.WaterShareHelper
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun WaterGoalCelebration(
    currentMl: Int,
    goalMl: Int,
    streakDays: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val percentage = if (goalMl > 0) (currentMl.toFloat() / goalMl * 100).coerceAtMost(200f) else 0f

    // Haptic feedback on appear
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(300)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Auto dismiss after 6 seconds
    LaunchedEffect(Unit) {
        delay(6000)
        onDismiss()
    }

    // Card scale animation
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "celebration")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Confetti particles
        ConfettiAnimation()

        // Floating water drops
        FloatingWaterDrops(infiniteTransition)

        // Main celebration card
        Card(
            modifier = Modifier
                .padding(32.dp)
                .scale(scale),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pulsing trophy
                val trophyScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "trophy_pulse"
                )

                Text(
                    "🎉🏆🎉",
                    fontSize = 48.sp,
                    modifier = Modifier.scale(trophyScale)
                )

                Text(
                    if (percentage >= 150) "INCREDIBLE!" else "GOAL ACHIEVED!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = Color(0xFF4CAF50)
                )

                // Stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CelebrationStat("💧", "${String.format("%.1f", currentMl / 1000f)}L", "Drank")
                    CelebrationStat("📊", "${percentage.toInt()}%", "Progress")
                    if (streakDays > 0) {
                        CelebrationStat("🔥", "$streakDays", "Streak")
                    }
                }

                // Motivational message
                val message = when {
                    percentage >= 150 -> "You're a hydration superstar! 🌟"
                    percentage >= 125 -> "Above and beyond! Amazing!"
                    streakDays >= 30 -> "A whole month of perfect hydration!"
                    streakDays >= 7 -> "Week streak! You're on fire!"
                    else -> "Your body thanks you!"
                }

                Text(
                    message,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            WaterShareHelper.shareWaterAchievement(
                                context = context,
                                currentMl = currentMl,
                                goalMl = goalMl,
                                streakDays = streakDays,
                                percentage = percentage
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Share 🎉", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CelebrationStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ConfettiAnimation() {
    val particles = remember {
        (0 until 30).map {
            ConfettiParticle(
                x = Random.nextFloat(),
                startY = Random.nextFloat() * -0.5f,
                speedY = 0.5f + Random.nextFloat() * 0.5f,
                color = listOf(
                    Color(0xFFFF6B6B),
                    Color(0xFF4ECDC4),
                    Color(0xFFFFE66D),
                    Color(0xFF95E1D3),
                    Color(0xFFF38181),
                    Color(0xFF3EDBF0),
                    Color(0xFFFF9FF3)
                ).random(),
                size = 8f + Random.nextFloat() * 12f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val currentY = (particle.startY + time * particle.speedY * 1.5f) % 1.5f - 0.25f
            val x = particle.x + kotlin.math.sin(currentY * kotlin.math.PI * 2).toFloat() * 0.05f

            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(x * size.width, currentY * size.height)
            )
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val speedY: Float,
    val color: Color,
    val size: Float
)

@Composable
private fun FloatingWaterDrops(infiniteTransition: InfiniteTransition) {
    val emojis = listOf("💧", "🌊", "💦", "🫧")

    emojis.forEachIndexed { index, emoji ->
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 600f,
            targetValue = -100f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2500 + index * 300,
                    delayMillis = index * 200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "drop_$index"
        )

        val offsetX = ((index * 80 + 50) % 300) - 150

        Text(
            text = emoji,
            fontSize = 28.sp,
            modifier = Modifier.offset(x = offsetX.dp, y = offsetY.dp)
        )
    }
}
