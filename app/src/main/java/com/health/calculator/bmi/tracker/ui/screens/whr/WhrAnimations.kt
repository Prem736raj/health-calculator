package com.health.calculator.bmi.tracker.ui.screens.whr

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.BodyShape
import com.health.calculator.bmi.tracker.data.model.WhrTrendDirection
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun AnimatedBodyShapeIcon(
    bodyShape: BodyShape,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "body_shape_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val enterScale = remember { Animatable(0f) }
    val enterAlpha = remember { Animatable(0f) }

    LaunchedEffect(bodyShape) {
        enterScale.snapTo(0.3f)
        enterAlpha.snapTo(0f)
        launch {
            enterScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        enterAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(400)
        )
    }

    val shapeColor = when (bodyShape) {
        BodyShape.APPLE -> Color(0xFFF44336)
        BodyShape.PEAR -> Color(0xFF4CAF50)
        BodyShape.BALANCED -> Color(0xFF2196F3)
    }

    Box(
        modifier = modifier
            .scale(enterScale.value * pulseScale)
            .alpha(enterAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Surface(
            shape = CircleShape,
            color = shapeColor.copy(alpha = 0.08f),
            modifier = Modifier.size(76.dp)
        ) {}

        Surface(
            shape = CircleShape,
            color = shapeColor.copy(alpha = 0.15f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    bodyShape.emoji,
                    fontSize = 32.sp
                )
            }
        }
    }
}

@Composable
fun AnimatedTrendArrow(
    direction: WhrTrendDirection,
    modifier: Modifier = Modifier
) {
    val enterOffset = remember { Animatable(20f) }
    val enterAlpha = remember { Animatable(0f) }

    LaunchedEffect(direction) {
        enterOffset.snapTo(20f)
        enterAlpha.snapTo(0f)
        launch {
            enterOffset.animateTo(
                0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        enterAlpha.animateTo(1f, tween(300))
    }

    val color = when (direction) {
        WhrTrendDirection.IMPROVING -> Color(0xFF4CAF50)
        WhrTrendDirection.WORSENING -> Color(0xFFF44336)
        WhrTrendDirection.STEADY -> Color(0xFF9E9E9E)
    }

    val arrowChar = when (direction) {
        WhrTrendDirection.IMPROVING -> "↓"
        WhrTrendDirection.WORSENING -> "↑"
        WhrTrendDirection.STEADY -> "→"
    }

    Box(
        modifier = modifier
            .offset(y = enterOffset.value.dp)
            .alpha(enterAlpha.value)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    arrowChar,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun AnimatedRiskGauge(
    progress: Float, // 0-1
    riskColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val strokeWidth = 10.dp.toPx()
        val padding = strokeWidth / 2 + 4.dp.toPx()

        // Track
        drawArc(
            color = trackColor,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(padding, padding),
            size = androidx.compose.ui.geometry.Size(
                size.width - padding * 2,
                size.height - padding * 2
            ),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress
        drawArc(
            color = riskColor,
            startAngle = 135f,
            sweepAngle = 270f * animatedProgress.value,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(padding, padding),
            size = androidx.compose.ui.geometry.Size(
                size.width - padding * 2,
                size.height - padding * 2
            ),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun AnimatedValueReveal(
    value: String,
    label: String,
    color: Color,
    delayMs: Int = 0,
    modifier: Modifier = Modifier
) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(16f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(value) {
        delay(delayMs.toLong())
        launch {
            alpha.animateTo(1f, tween(400, easing = EaseOutCubic))
        }
        launch {
            offsetY.animateTo(0f, tween(500, easing = EaseOutCubic))
        }
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }

    Column(
        modifier = modifier
            .alpha(alpha.value)
            .offset(y = offsetY.value.dp)
            .scale(scale.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun AnimatedCascadeColumn(
    itemCount: Int,
    delayPerItem: Int = 80,
    content: @Composable (index: Int, modifier: Modifier) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(itemCount) { index ->
            val alpha = remember { Animatable(0f) }
            val offsetX = remember { Animatable(30f) }

            LaunchedEffect(Unit) {
                delay((index * delayPerItem).toLong())
                launch {
                    alpha.animateTo(1f, tween(350))
                }
                offsetX.animateTo(0f, tween(400, easing = EaseOutCubic))
            }

            content(
                index,
                Modifier
                    .alpha(alpha.value)
                    .offset(x = offsetX.value.dp)
            )
        }
    }
}
