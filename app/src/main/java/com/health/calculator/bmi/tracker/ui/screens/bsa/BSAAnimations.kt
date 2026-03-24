package com.health.calculator.bmi.tracker.ui.screens.bsa

import androidx.compose.animation.core.*
import androidx.compose.runtime.*

@Composable
fun animateSequentialVisibility(
    count: Int,
    trigger: Boolean,
    baseDelay: Long = 200,
    stagger: Long = 120
): List<Boolean> {
    val states = remember { MutableList(count) { mutableStateOf(false) } }

    LaunchedEffect(trigger) {
        if (trigger) {
            states.forEachIndexed { index, state ->
                kotlinx.coroutines.delay(baseDelay + (index * stagger))
                state.value = true
            }
        } else {
            states.forEach { it.value = false }
        }
    }

    return states.map { it.value }
}

@Composable
fun animateBSAValue(targetValue: Float): Float {
    val animated by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bsa_value_anim"
    )
    return animated
}

@Composable
fun animateBodyFill(trigger: Boolean): Float {
    val progress by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "body_fill_anim"
    )
    return progress
}

@Composable
fun animateBarProgress(
    targetProgress: Float,
    index: Int,
    baseDelay: Int = 400
): Float {
    val animated by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = baseDelay + (index * 100),
            easing = FastOutSlowInEasing
        ),
        label = "bar_${index}_anim"
    )
    return animated
}
