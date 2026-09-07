package com.health.calculator.bmi.tracker.ui.screens.onboarding

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Onboarding Page Data ─────────────────────────────────────────────────────

private data class OnboardingPage(
    val icon: ImageVector,
    val decorativeIcons: List<ImageVector>,
    val title: String,
    val subtitle: String,
    val description: String
)

/** The first useful destination a new user can choose without completing a profile. */
enum class OnboardingStartAction(
    val label: String,
    val description: String,
    val analyticsValue: String,
    val icon: ImageVector
) {
    WATER("Water", "Log a drink", "water", Icons.Outlined.WaterDrop),
    WEIGHT("Weight", "Start a trend", "weight", Icons.Outlined.MonitorWeight),
    STEPS("Steps", "Connect when ready", "steps", Icons.Outlined.DirectionsWalk),
    CALCULATORS("BMI calculator", "Get a quick estimate", "calculator", Icons.Outlined.Analytics)
}

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.FavoriteBorder,
        decorativeIcons = listOf(Icons.Outlined.MonitorHeart, Icons.Outlined.DirectionsWalk, Icons.Outlined.WaterDrop),
        title = "Welcome to\nHealth Metrics Tracker",
        subtitle = "Your Personal Health Companion",
        description = "Track and understand the metrics you choose with easy-to-use calculators. Your records stay private on this device."
    ),
    OnboardingPage(
        icon = Icons.Outlined.Analytics,
        decorativeIcons = listOf(Icons.Outlined.MonitorWeight, Icons.Outlined.Timeline, Icons.Outlined.Assignment),
        title = "10 practical\nhealth calculators",
        subtitle = "Evidence-informed estimates",
        description = "From BMI and BMR to blood pressure and heart-rate zones — each tool explains its method, sources, and limits."
    ),
    OnboardingPage(
        icon = Icons.Outlined.Timeline,
        decorativeIcons = listOf(Icons.Outlined.Analytics, Icons.Outlined.Flag, Icons.Outlined.DirectionsWalk),
        title = "Start with one\nsmall step",
        subtitle = "Choose what feels useful today",
        description = "Pick one quick action now. You can change it later — nothing is required, and missing a day does not erase your progress."
    )
)

@Composable
private fun onboardingAccentColor(pageIndex: Int): Color {
    return when (pageIndex % 3) {
        0 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }
}

// ─── Main Onboarding Screen ──────────────────────────────────────────────────

/**
 * Full-screen onboarding flow shown only on first app launch.
 * Contains three swipeable pages with page indicators and a first-action choice.
 *
 * @param onComplete Called when user taps "Get Started" / "Skip" — navigates to Home
 * @param onSetUpProfile Optional legacy shortcut for users who want to personalize first.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSetUpProfile: () -> Unit,
    onStartAction: (OnboardingStartAction) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1
    var selectedStartAction by remember { mutableStateOf<OnboardingStartAction?>(null) }

    // Entrance animation
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
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
                                text = stringResource(R.string.txt_skip),
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
                        isCurrentPage = pagerState.currentPage == pageIndex,
                        selectedAction = selectedStartAction,
                        onSelectAction = { selectedStartAction = it }
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
                    onSetUpProfile = onSetUpProfile,
                    selectedAction = selectedStartAction,
                    onStartAction = onStartAction
                )
            }
        }
        }
    }
}

// ─── Onboarding Page Content ──────────────────────────────────────────────────

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int,
    isCurrentPage: Boolean,
    selectedAction: OnboardingStartAction?,
    onSelectAction: (OnboardingStartAction) -> Unit
) {
    val accentColor = onboardingAccentColor(pageIndex)
    val isActionPage = pageIndex == onboardingPages.lastIndex
    val illustrationBoxSize = if (isActionPage) 148.dp else 200.dp
    val illustrationRadius = if (isActionPage) 58.dp else 80.dp
    val outerIllustrationSize = if (isActionPage) 120.dp else 160.dp
    val innerIllustrationSize = if (isActionPage) 84.dp else 110.dp
    val illustrationIconSize = if (isActionPage) 38.dp else 48.dp

    // Icon entrance animation
    val iconScale = remember { Animatable(0.5f) }
    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            iconScale.snapTo(0.5f)
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
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
                .size(illustrationBoxSize)
                .scale(iconScale.value)
        ) {
            // Decorative vector icons reinforce the page without relying on
            // emoji glyphs that vary by device and theme.
            page.decorativeIcons.forEachIndexed { index, icon ->
                val angle = (360f / page.decorativeIcons.size) * index
                val radius = illustrationRadius
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

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.65f),
                    modifier = Modifier
                        .size(22.dp)
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
                    .size(outerIllustrationSize)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.15f),
                                accentColor.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner circle
            Box(
                modifier = Modifier
                    .size(innerIllustrationSize)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.1f),
                                accentColor.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = accentColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .size(illustrationIconSize)
                        .padding(bottom = floatY.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isActionPage) 16.dp else 40.dp))

        // ── Title ─────────────────────────────────────────────────────
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.7f)
                    )
                )
            ),
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
            color = accentColor,
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

        if (isActionPage) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Choose a starting point",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OnboardingActionPicker(
                selectedAction = selectedAction,
                onSelectAction = onSelectAction
            )
        }
    }
}

@Composable
private fun OnboardingActionPicker(
    selectedAction: OnboardingStartAction?,
    onSelectAction: (OnboardingStartAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OnboardingStartAction.entries.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { action ->
                    val isSelected = selectedAction == action
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .selectable(
                                selected = isSelected,
                                onClick = { onSelectAction(action) },
                                role = Role.RadioButton
                            )
                            .semantics {
                                contentDescription = "${action.label}. ${action.description}"
                                selected = isSelected
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(
                                    text = action.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = action.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
                if (rowActions.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── Bottom Section ───────────────────────────────────────────────────────────

@Composable
private fun BottomSection(
    pagerState: PagerState,
    isLastPage: Boolean,
    onNext: () -> Unit,
    onComplete: () -> Unit,
    onSetUpProfile: () -> Unit,
    selectedAction: OnboardingStartAction?,
    onStartAction: (OnboardingStartAction) -> Unit
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
                // Primary: take one useful action without requiring profile setup.
                Button(
                    onClick = {
                        selectedAction?.let(onStartAction) ?: onComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = selectedAction?.icon ?: Icons.Outlined.Timeline,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selectedAction?.let { "Start with ${it.label}" } ?: "Explore Home",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                TextButton(onClick = onComplete) {
                    Text("I’ll explore home first")
                }
                if (selectedAction == null) {
                    TextButton(onClick = onSetUpProfile) {
                        Text("Set up a profile instead")
                    }
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
                    containerColor = onboardingAccentColor(pagerState.currentPage),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.txt_next),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
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

            val pageColor = onboardingAccentColor(currentPage)

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
