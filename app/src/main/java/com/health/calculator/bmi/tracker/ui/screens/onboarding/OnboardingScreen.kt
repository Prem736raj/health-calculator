package com.health.calculator.bmi.tracker.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Accent Colors ────────────────────────────────────────────────────────────

private val OnboardingTeal = Color(0xFF00ACC1)
private val OnboardingTealDark = Color(0xFF00838F)
private val OnboardingTealLight = Color(0xFF4DD0E1)

// ─── Onboarding Page Data ─────────────────────────────────────────────────────

private data class OnboardingPage(
    val emoji: String,
    val decorativeEmojis: List<String>,
    val title: String,
    val subtitle: String,
    val description: String,
    val accentColor: Color
)

private val onboardingPages = listOf(
    OnboardingPage(
        emoji = "💚",
        decorativeEmojis = listOf("🫀", "🩺", "💪", "🧘"),
        title = "Welcome to\nHealth Calculator",
        subtitle = "Your Personal Health Companion",
        description = "Track and understand your health metrics with easy-to-use calculators. All your data stays private on your device.",
        accentColor = Color(0xFF43A047)
    ),
    OnboardingPage(
        emoji = "🏥",
        decorativeEmojis = listOf("⚖️", "🔥", "❤️", "💧", "📊", "🎯", "💓", "🩺", "📐", "🍎"),
        title = "10 WHO-Standard\nCalculators",
        subtitle = "Medical-Grade Accuracy",
        description = "From BMI and BMR to Blood Pressure and Heart Rate Zones — all calculations follow World Health Organization standards.",
        accentColor = Color(0xFF1E88E5)
    ),
    OnboardingPage(
        emoji = "📈",
        decorativeEmojis = listOf("📊", "📉", "🏆", "⭐"),
        title = "Track Your\nProgress",
        subtitle = "Beautiful Charts & History",
        description = "Every calculation is saved automatically. Watch your health trends over time with beautiful charts and detailed history.",
        accentColor = Color(0xFF7B1FA2)
    ),
    OnboardingPage(
        emoji = "👤",
        decorativeEmojis = listOf("📋", "✨", "🎯", "❤️"),
        title = "Set Up Your\nProfile",
        subtitle = "Personalized Results",
        description = "Enter your details once and they'll be used across all calculators. Get personalized, accurate health insights tailored to you.",
        accentColor = OnboardingTeal
    )
)

// ─── Main Onboarding Screen ──────────────────────────────────────────────────

/**
 * Full-screen onboarding flow shown only on first app launch.
 * Contains 4 swipeable pages with page indicators and navigation controls.
 *
 * @param onComplete Called when user taps "Get Started" / "Skip" — navigates to Home
 * @param onSetUpProfile Called when user taps "Set Up Now" on the last page — navigates to Profile
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSetUpProfile: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    // Entrance animation
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(500))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // ── Skip Button ───────────────────────────────────────────
                AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(onClick = onComplete) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // ── Pager Content ─────────────────────────────────────────
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    OnboardingPageContent(
                        page = onboardingPages[pageIndex],
                        pageIndex = pageIndex,
                        isCurrentPage = pagerState.currentPage == pageIndex
                    )
                }

                // ── Bottom Section ────────────────────────────────────────
                BottomSection(
                    pagerState = pagerState,
                    isLastPage = isLastPage,
                    onNext = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                page = pagerState.currentPage + 1,
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            )
                        }
                    },
                    onComplete = onComplete,
                    onSetUpProfile = onSetUpProfile
                )
            }
        }
    }
}

// ─── Onboarding Page Content ──────────────────────────────────────────────────

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int,
    isCurrentPage: Boolean
) {
    // Icon entrance animation
    val iconScale = remember { Animatable(0.5f) }
    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            iconScale.snapTo(0.5f)
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = EaseOutBack)
            )
        }
    }

    // Subtle floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "float_$pageIndex")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y_$pageIndex"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Illustration Area ─────────────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .scale(iconScale.value)
        ) {
            // Decorative floating emojis around the main icon
            page.decorativeEmojis.forEachIndexed { index, emoji ->
                val angle = (360f / page.decorativeEmojis.size) * index
                val radius = 80.dp
                val radians = Math.toRadians(angle.toDouble())
                val x = (radius.value * Math.cos(radians)).toFloat()
                val y = (radius.value * Math.sin(radians)).toFloat()

                val decorFloat by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            2000 + (index * 300),
                            easing = EaseInOut
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "decor_${pageIndex}_$index"
                )

                Text(
                    text = emoji,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(
                            x = x.dp,
                            y = (y - decorFloat).dp
                        )
                        .alpha(0.5f)
                )
            }

            // Outer glow
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                page.accentColor.copy(alpha = 0.15f),
                                page.accentColor.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner circle
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                page.accentColor.copy(alpha = 0.1f),
                                page.accentColor.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = page.accentColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.emoji,
                    fontSize = 48.sp,
                    modifier = Modifier
                        .padding(bottom = floatY.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ── Title ─────────────────────────────────────────────────────
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ── Subtitle ──────────────────────────────────────────────────
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = page.accentColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Description ───────────────────────────────────────────────
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

// ─── Bottom Section ───────────────────────────────────────────────────────────

@Composable
private fun BottomSection(
    pagerState: PagerState,
    isLastPage: Boolean,
    onNext: () -> Unit,
    onComplete: () -> Unit,
    onSetUpProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Page Indicators ───────────────────────────────────────────
        PageIndicators(
            pageCount = onboardingPages.size,
            currentPage = pagerState.currentPage
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Action Buttons ────────────────────────────────────────────
        AnimatedVisibility(
            visible = isLastPage,
            enter = fadeIn(tween(300)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(300, easing = EaseOutCubic)
            ),
            exit = fadeOut(tween(200))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary: Set Up Profile
                Button(
                    onClick = onSetUpProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnboardingTeal
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Set Up My Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Secondary: Skip to Home
                OutlinedButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "Skip for Now",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !isLastPage,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(200))
        ) {
            // Next button (circular)
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = onboardingPages[pagerState.currentPage].accentColor
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Page Indicators ──────────────────────────────────────────────────────────

@Composable
private fun PageIndicators(
    pageCount: Int,
    currentPage: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateFloatAsState(
                targetValue = if (isSelected) 28f else 8f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "indicator_width_$index"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                animationSpec = tween(300),
                label = "indicator_alpha_$index"
            )

            val pageColor = onboardingPages[currentPage].accentColor

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .alpha(alpha)
                    .background(
                        if (isSelected) pageColor
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
            )
        }
    }
}
