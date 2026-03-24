package com.health.calculator.bmi.tracker.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ============================================================
// Shake Animation Modifier
// ============================================================
@Composable
fun rememberShakeController(): ShakeController {
    return remember { ShakeController() }
}

class ShakeController {
    var shakeState by mutableStateOf(ShakeState.Idle)
        private set

    suspend fun shake() {
        shakeState = ShakeState.Shaking
        delay(500)
        shakeState = ShakeState.Idle
    }
}

enum class ShakeState { Idle, Shaking }

@Composable
fun Modifier.shake(controller: ShakeController): Modifier {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(controller.shakeState) {
        if (controller.shakeState == ShakeState.Shaking) {
            // Rapid back-and-forth shake
            repeat(3) {
                shakeOffset.animateTo(
                    targetValue = 12f,
                    animationSpec = tween(durationMillis = 40, easing = LinearEasing)
                )
                shakeOffset.animateTo(
                    targetValue = -12f,
                    animationSpec = tween(durationMillis = 40, easing = LinearEasing)
                )
            }
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 60, easing = FastOutSlowInEasing)
            )
        }
    }

    return this.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
}

// ============================================================
// Cascade Reveal Animation
// ============================================================
@Composable
fun CascadeAnimatedItem(
    index: Int,
    baseDelay: Int = 80,
    initialDelay: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((initialDelay + index * baseDelay).toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        content()
    }
}

// ============================================================
// Stagger Entrance for screen load
// ============================================================
@Composable
fun StaggeredEntrance(
    index: Int,
    totalItems: Int,
    delayPerItem: Int = 60,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * delayPerItem).toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        )
    ) {
        content()
    }
}

// ============================================================
// Bounce Overshoot for gauge
// ============================================================
fun bounceOvershootSpec(durationMillis: Int = 1200): AnimationSpec<Float> {
    return spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}

fun smoothOvershootSpec(durationMillis: Int = 1000): AnimationSpec<Float> {
    return spring(
        dampingRatio = 0.55f,
        stiffness = 120f
    )
}

// ============================================================
// Pulse Animation
// ============================================================
@Composable
fun pulseModifier(
    enabled: Boolean = true,
    minScale: Float = 0.97f,
    maxScale: Float = 1.03f,
    durationMillis: Int = 1000
): Modifier {
    if (!enabled) return Modifier

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    return Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ============================================================
// Press Scale Animation
// ============================================================
@Composable
fun Modifier.pressScale(
    isPressed: Boolean,
    pressedScale: Float = 0.96f
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ============================================================
// Result Reveal Animation
// ============================================================
@Composable
fun ResultRevealAnimation(
    visible: Boolean,
    delayMillis: Int = 0,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            expandFrom = Alignment.Top
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(
                durationMillis = 500,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        content = content
    )
}
