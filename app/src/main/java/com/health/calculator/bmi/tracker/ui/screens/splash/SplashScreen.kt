package com.health.calculator.bmi.tracker.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─── Splash Colors ────────────────────────────────────────────────────────────

private val SplashTeal = Color(0xFF00ACC1)
private val SplashTealDark = Color(0xFF00838F)
private val SplashTealLight = Color(0xFF4DD0E1)

/**
 * Animated splash screen displayed on every app launch.
 * Shows the app logo with a heartbeat pulse animation for ~1.8 seconds
 * before calling [onSplashComplete].
 *
 * Animation sequence:
 * 1. Logo scales in with bounce (0-400ms)
 * 2. App name fades in (200-600ms)
 * 3. Heartbeat pulse loops on the medical cross (400ms+)
 * 4. Subtitle fades in (800-1200ms)
 * 5. Entire screen fades out (1600-1800ms)
 * 6. onSplashComplete is called
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    // ── Animation States ──────────────────────────────────────────────────
    val logoScale = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    var fadeOut by remember { mutableStateOf(false) }

    val screenAlpha by animateFloatAsState(
        targetValue = if (fadeOut) 0f else 1f,
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "screen_fade",
        finishedListener = {
            if (fadeOut) onSplashComplete()
        }
    )

    // Heartbeat pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val heartbeatScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Outer ring pulse
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    // ── Animation Sequence ────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // Step 1: Logo scales in with bounce
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = EaseOutBack)
        )

        // Step 2: Title fades in
        delay(100)
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )

        // Step 3: Subtitle fades in
        delay(200)
        subtitleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )

        // Step 4: Hold for a moment to let pulse animate
        delay(600)

        // Step 5: Fade out
        fadeOut = true
    }

    // ── UI ────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        SplashTeal.copy(alpha = 0.03f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo Section ──────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(logoScale.value)
            ) {
                // Outer pulsing ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ringScale)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = SplashTeal.copy(alpha = ringAlpha),
                            shape = CircleShape
                        )
                )

                // Middle ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SplashTeal.copy(alpha = 0.08f),
                                    SplashTeal.copy(alpha = 0.02f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Inner circle with medical cross
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(heartbeatScale)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SplashTeal.copy(alpha = 0.12f),
                                    SplashTealDark.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    SplashTealLight.copy(alpha = 0.4f),
                                    SplashTeal.copy(alpha = 0.2f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Medical cross made of two overlapping rounded rectangles
                    MedicalCross()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── App Name ──────────────────────────────────────────────
            Text(
                text = "Health Calculator",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Subtitle ──────────────────────────────────────────────
            Text(
                text = "Your personal health companion",
                style = MaterialTheme.typography.bodyLarge,
                color = SplashTeal.copy(alpha = 0.8f),
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Loading dots ──────────────────────────────────────────
            LoadingDots(
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }

        // ── Bottom disclaimer ─────────────────────────────────────────
        Text(
            text = "For educational purposes only",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(subtitleAlpha.value)
        )
    }
}

// ─── Medical Cross Icon ───────────────────────────────────────────────────────

@Composable
private fun MedicalCross() {
    Box(contentAlignment = Alignment.Center) {
        // Vertical bar
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SplashTealLight, SplashTeal)
                    )
                )
        )
        // Horizontal bar
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(SplashTealLight, SplashTeal)
                    )
                )
        )
    }
}

// ─── Loading Dots Animation ───────────────────────────────────────────────────

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .alpha(dotAlpha)
                    .background(SplashTeal)
            )

            if (index < 2) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}
