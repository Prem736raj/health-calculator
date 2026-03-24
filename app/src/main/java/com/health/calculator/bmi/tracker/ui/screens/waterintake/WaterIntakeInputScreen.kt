// ui/screens/waterintake/WaterIntakeInputScreen.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.health.calculator.bmi.tracker.data.model.ClimateType
import com.health.calculator.bmi.tracker.data.model.HealthStatus
import com.health.calculator.bmi.tracker.data.model.WaterActivityLevel

// Water-themed colors
private val WaterBlueLight = Color(0xFF64B5F6)
private val WaterBlueMedium = Color(0xFF2196F3)
private val WaterBlueDark = Color(0xFF1565C0)
private val WaterBlueSurface = Color(0xFFE3F2FD)
private val WaterBlueSurfaceDark = Color(0xFF0D2137)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterIntakeInputScreen(
    viewModel: WaterIntakeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    onNavigateToEducation: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // Water drop animation
    val infiniteTransition = rememberInfiniteTransition(label = "water_anim")
    val waterDropOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "water_drop"
    )

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💧", fontSize = 24.sp)
                        Text(
                            "Daily Water Intake",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.clearAll()
                    }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Clear")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Water-themed header card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
            ) {
                WaterHeaderCard(waterDropOffset)
            }

            // Profile data indicator
            AnimatedVisibility(
                visible = viewModel.isUsingProfileData,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ProfileDataBanner()
            }

            // Body Information Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 100)) + slideInVertically(tween(500, delayMillis = 100)) { 40 }
            ) {
                BodyInfoSection(viewModel, haptic)
            }

            // Activity Level Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(tween(500, delayMillis = 200)) { 40 }
            ) {
                ActivityLevelSection(viewModel, haptic)
            }

            // Climate Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 300)) + slideInVertically(tween(500, delayMillis = 300)) { 40 }
            ) {
                ClimateSection(viewModel, haptic)
            }

            // Health Status Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400)) { 40 }
            ) {
                HealthStatusSection(viewModel, haptic)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Education link card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 450)) + slideInVertically(tween(500, delayMillis = 450)) { 40 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEducation() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WaterBlueSurface.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📚", fontSize = 20.sp)
                        Text(
                            "Learn about hydration science →",
                            fontSize = 13.sp,
                            color = WaterBlueDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calculate Button
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500, delayMillis = 500)) + scaleIn(tween(500, delayMillis = 500))
            ) {
                CalculateButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.calculate()
                        if (viewModel.showResult) {
                            onNavigateToResult()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WaterHeaderCard(waterDropOffset: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            WaterBlueMedium,
                            WaterBlueDark
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Calculate Your Daily\nWater Needs",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "Proper hydration is essential for every body function.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(Modifier.width(16.dp))
                // Animated water drop
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.offset(y = waterDropOffset.dp)
                ) {
                    Text(
                        text = "💧",
                        fontSize = 52.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileDataBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WaterBlueSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = WaterBlueMedium,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Using profile data • You can override any value",
                fontSize = 13.sp,
                color = WaterBlueDark
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BodyInfoSection(
    viewModel: WaterIntakeViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    SectionCard(title = "Body Information", icon = "🏋️") {
        // Weight Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Weight",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = viewModel.weightValue,
                    onValueChange = { viewModel.updateWeight(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(if (viewModel.isMetric) "e.g., 70" else "e.g., 154")
                    },
                    suffix = {
                        Text(
                            if (viewModel.isMetric) "kg" else "lbs",
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = viewModel.weightError != null,
                    supportingText = viewModel.weightError?.let { error ->
                        { Text(error, color = MaterialTheme.colorScheme.error) }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                // Unit toggle
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleUnit()
                    },
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = WaterBlueSurface
                    )
                ) {
                    Text(
                        text = if (viewModel.isMetric) "→ lbs" else "→ kg",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Age Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Age",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = viewModel.ageValue,
                onValueChange = { viewModel.updateAge(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 30") },
                suffix = { Text("years", color = MaterialTheme.colorScheme.primary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = viewModel.ageError != null,
                supportingText = viewModel.ageError?.let { error ->
                    { Text(error, color = MaterialTheme.colorScheme.error) }
                },
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Gender Selection
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Gender",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Male" to "🚹", "Female" to "🚺").forEach { (gender, icon) ->
                    val isSelected = viewModel.selectedGender == gender
                    SelectableChipCard(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.updateGender(gender)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(icon, fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = gender,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WaterBlueDark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLevelSection(
    viewModel: WaterIntakeViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    SectionCard(title = "Activity Level", icon = "🏃") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WaterActivityLevel.entries.forEach { level ->
                val isSelected = viewModel.selectedActivityLevel == level
                SelectableOptionRow(
                    text = level.displayName,
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.updateActivityLevel(level)
                    }
                )
            }
        }
    }
}

@Composable
private fun ClimateSection(
    viewModel: WaterIntakeViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    SectionCard(title = "Climate / Environment", icon = "🌡️") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val climateIcons = mapOf(
                ClimateType.COLD to "❄️",
                ClimateType.TEMPERATE to "🌤️",
                ClimateType.HOT to "☀️",
                ClimateType.VERY_HOT to "🔥"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClimateType.entries.take(2).forEach { climate ->
                    val isSelected = viewModel.selectedClimate == climate
                    SelectableChipCard(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.updateClimate(climate)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(climateIcons[climate] ?: "", fontSize = 24.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = climate.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                color = if (isSelected) WaterBlueDark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClimateType.entries.drop(2).forEach { climate ->
                    val isSelected = viewModel.selectedClimate == climate
                    SelectableChipCard(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.updateClimate(climate)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(climateIcons[climate] ?: "", fontSize = 24.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = climate.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                color = if (isSelected) WaterBlueDark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthStatusSection(
    viewModel: WaterIntakeViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    SectionCard(title = "Health Status", icon = "🩺") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val statusIcons = mapOf(
                HealthStatus.NORMAL to "✅",
                HealthStatus.PREGNANT to "🤰",
                HealthStatus.BREASTFEEDING to "🤱",
                HealthStatus.ILLNESS to "🤒"
            )

            HealthStatus.entries.forEach { status ->
                val isSelected = viewModel.selectedHealthStatus == status
                SelectableOptionRow(
                    text = "${statusIcons[status] ?: ""} ${status.displayName}",
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.updateHealthStatus(status)
                    },
                    subtitle = when (status) {
                        HealthStatus.PREGNANT -> "+300ml recommended"
                        HealthStatus.BREASTFEEDING -> "+700ml recommended"
                        HealthStatus.ILLNESS -> "+500ml recommended"
                        else -> null
                    }
                )
            }
        }
    }
}

@Composable
private fun CalculateButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = WaterBlueMedium.copy(alpha = 0.3f),
                spotColor = WaterBlueMedium.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = WaterBlueMedium
        )
    ) {
        Text(
            text = "💧",
            fontSize = 20.sp
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Calculate My Water Needs",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Reusable Components ─────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(icon, fontSize = 20.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SelectableChipCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) WaterBlueMedium else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "border_color"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) WaterBlueSurface else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "bg_color"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun SelectableOptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) WaterBlueSurface else Color.Transparent,
        animationSpec = tween(300),
        label = "option_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) WaterBlueMedium else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        animationSpec = tween(300),
        label = "option_border"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) WaterBlueDark else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = WaterBlueMedium.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        AnimatedVisibility(
            visible = selected,
            enter = scaleIn(tween(200)) + fadeIn(tween(200)),
            exit = scaleOut(tween(200)) + fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(WaterBlueMedium, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
