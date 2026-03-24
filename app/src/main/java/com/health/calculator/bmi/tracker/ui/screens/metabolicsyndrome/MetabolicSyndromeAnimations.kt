package com.health.calculator.bmi.tracker.ui.screens.metabolicsyndrome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.health.calculator.bmi.tracker.ui.theme.*

@Composable
fun animateCountIncrement(targetCount: Int): Int {
    var displayedCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetCount) {
        if (targetCount > displayedCount) {
            for (i in displayedCount + 1..targetCount) {
                kotlinx.coroutines.delay(250)
                displayedCount = i
            }
        } else {
            displayedCount = targetCount
        }
    }

    return displayedCount
}

@Composable
fun animateRiskColor(
    criteriaMet: Int,
    animationSpec: AnimationSpec<Color> = tween(800)
): Color {
    val targetColor = when {
        criteriaMet >= 4 -> Color(0xFFB71C1C)
        criteriaMet >= 3 -> HealthRed
        criteriaMet >= 2 -> HealthOrange
        criteriaMet >= 1 -> HealthYellow
        else -> HealthGreen
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = animationSpec,
        label = "risk_color"
    )

    return animatedColor
}

@Composable
fun animateGaugeProgress(
    targetProgress: Float,
    durationMillis: Int = 1200
): Float {
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gauge_progress"
    )

    return animatedProgress
}

data class CriterionAnimState(
    val isVisible: Boolean = false,
    val delay: Long = 0
)

@Composable
fun rememberCriterionAnimStates(
    count: Int,
    baseDelay: Long = 300,
    stagger: Long = 150,
    trigger: Boolean = true
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
