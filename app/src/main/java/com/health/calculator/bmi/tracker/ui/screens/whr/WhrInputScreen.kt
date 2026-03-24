package com.health.calculator.bmi.tracker.ui.screens.whr

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.health.calculator.bmi.tracker.data.model.Gender
import com.health.calculator.bmi.tracker.data.model.WhrGuideData
import com.health.calculator.bmi.tracker.ui.screens.whr.WhrEdgeCaseHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhrInputScreen(
    onNavigateBack: () -> Unit,
    onCalculate: (waistCm: Float, hipCm: Float, gender: Gender, age: Int) -> Unit,
    onNavigateToEducation: () -> Unit = {},
    viewModel: WhrViewModel = viewModel()
) {
    val inputState by viewModel.inputState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Waist-to-Hip Ratio",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.clearAll()
                    }) {
                        Text("Clear All")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            WhrHeaderCard()

            // Profile Data Indicator
            AnimatedVisibility(visible = inputState.isProfileDataLoaded) {
                ProfileDataBanner()
            }

            // Body Measurement Guide Visual
            MeasurementGuideVisual()

            // Measurement Tip Note
            MeasurementTipCard()

            // Unit Toggle
            UnitToggleSection(
                useMetric = inputState.useMetric,
                onToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleUnit()
                }
            )

            // Waist Input
            MeasurementInputField(
                label = "Waist Circumference",
                value = inputState.waistValue,
                onValueChange = { viewModel.updateWaist(it) },
                unit = if (inputState.useMetric) "cm" else "inches",
                icon = Icons.Outlined.Straighten,
                error = inputState.waistError,
                placeholder = if (inputState.useMetric) "e.g., 80" else "e.g., 31.5",
                helperText = "Measure at the narrowest point above the belly button",
                onInfoClick = { viewModel.toggleWaistGuide() }
            )

            // Waist Guide Expandable
            AnimatedVisibility(
                visible = inputState.showWaistGuide,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                MeasurementGuideCard(
                    guide = WhrGuideData.waistGuide,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            // Hip Input
            MeasurementInputField(
                label = "Hip Circumference",
                value = inputState.hipValue,
                onValueChange = { viewModel.updateHip(it) },
                unit = if (inputState.useMetric) "cm" else "inches",
                icon = Icons.Outlined.Straighten,
                error = inputState.hipError,
                placeholder = if (inputState.useMetric) "e.g., 100" else "e.g., 39.5",
                helperText = "Measure at the widest point of the buttocks",
                onInfoClick = { viewModel.toggleHipGuide() }
            )

            // Hip Guide Expandable
            AnimatedVisibility(
                visible = inputState.showHipGuide,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                MeasurementGuideCard(
                    guide = WhrGuideData.hipGuide,
                    accentColor = MaterialTheme.colorScheme.secondary
                )
            }

            // Waist > Hip Warning
            AnimatedVisibility(
                visible = inputState.waistWarning != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                inputState.waistWarning?.let { warning ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = warning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Gender Selection
            GenderSelectionSection(
                selectedGender = inputState.gender,
                onGenderSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.updateGender(it)
                }
            )

            // Age Input
            OutlinedTextField(
                value = inputState.age,
                onValueChange = { viewModel.updateAge(it) },
                label = { Text("Age") },
                leadingIcon = {
                    Icon(Icons.Outlined.Cake, contentDescription = null)
                },
                suffix = { Text("years") },
                placeholder = { Text("e.g., 30") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = inputState.ageError != null,
                supportingText = inputState.ageError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // How to Measure Expandable Section
            HowToMeasureExpandable(
                expanded = inputState.showMeasurementGuide,
                onToggle = { viewModel.toggleMeasurementGuide() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calculate Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (viewModel.validate()) {
                        val validationResult = WhrEdgeCaseHandler.validateInputs(
                            waistValue = inputState.waistValue,
                            hipValue = inputState.hipValue,
                            ageValue = inputState.age,
                            useMetric = inputState.useMetric
                        )
                        if (validationResult.isValid) {
                            val age = inputState.age.toIntOrNull() ?: 25
                            onCalculate(
                                viewModel.getWaistInCm(),
                                viewModel.getHipInCm(),
                                inputState.gender,
                                age
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Filled.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Calculate WHR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Educational Content Link
            OutlinedButton(
                onClick = onNavigateToEducation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Learn About WHR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WhrHeaderCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📐", fontSize = 24.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Waist-to-Hip Ratio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Measures body fat distribution and helps assess health risks related to your body shape",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ProfileDataBanner() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Using profile data for gender and age",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun MeasurementGuideVisual() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Measurement Points",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Body illustration using Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                BodyMeasurementIllustration()
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MeasurementLegendItem(
                    color = MaterialTheme.colorScheme.primary,
                    label = "Waist",
                    description = "Narrowest point"
                )
                MeasurementLegendItem(
                    color = MaterialTheme.colorScheme.secondary,
                    label = "Hip",
                    description = "Widest point"
                )
            }
        }
    }
}

@Composable
private fun BodyMeasurementIllustration() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Canvas(
        modifier = Modifier
            .width(120.dp)
            .height(200.dp)
    ) {
        val centerX = size.width / 2
        val topY = size.height * 0.05f
        val bottomY = size.height * 0.95f

        // Head
        drawCircle(
            color = outlineColor,
            radius = 18f,
            center = Offset(centerX, topY + 20f),
            style = Stroke(width = 2.5f)
        )

        // Neck
        drawLine(
            color = outlineColor,
            start = Offset(centerX, topY + 38f),
            end = Offset(centerX, topY + 50f),
            strokeWidth = 2.5f
        )

        // Shoulders
        drawLine(
            color = outlineColor,
            start = Offset(centerX - 40f, topY + 55f),
            end = Offset(centerX + 40f, topY + 55f),
            strokeWidth = 2.5f
        )

        // Body outline - left side
        val bodyPath = Path().apply {
            moveTo(centerX - 40f, topY + 55f)
            // Narrow to waist
            cubicTo(
                centerX - 42f, topY + 70f,
                centerX - 28f, topY + 95f,
                centerX - 25f, topY + 105f // Waist
            )
            // Widen to hip
            cubicTo(
                centerX - 22f, topY + 115f,
                centerX - 38f, topY + 130f,
                centerX - 40f, topY + 145f // Hip
            )
            // Legs
            lineTo(centerX - 35f, bottomY)
        }
        drawPath(bodyPath, color = outlineColor, style = Stroke(width = 2.5f))

        // Body outline - right side
        val bodyPathRight = Path().apply {
            moveTo(centerX + 40f, topY + 55f)
            cubicTo(
                centerX + 42f, topY + 70f,
                centerX + 28f, topY + 95f,
                centerX + 25f, topY + 105f
            )
            cubicTo(
                centerX + 22f, topY + 115f,
                centerX + 38f, topY + 130f,
                centerX + 40f, topY + 145f
            )
            lineTo(centerX + 35f, bottomY)
        }
        drawPath(bodyPathRight, color = outlineColor, style = Stroke(width = 2.5f))

        // Inner leg line
        drawLine(
            color = outlineColor,
            start = Offset(centerX, topY + 145f),
            end = Offset(centerX - 5f, bottomY),
            strokeWidth = 2f
        )
        drawLine(
            color = outlineColor,
            start = Offset(centerX, topY + 145f),
            end = Offset(centerX + 5f, bottomY),
            strokeWidth = 2f
        )

        // Waist measurement line (dashed)
        val waistY = topY + 105f
        drawLine(
            color = primaryColor,
            start = Offset(centerX - 55f, waistY),
            end = Offset(centerX - 27f, waistY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = primaryColor,
            start = Offset(centerX + 27f, waistY),
            end = Offset(centerX + 55f, waistY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        // Waist arrows
        drawCircle(
            color = primaryColor,
            radius = 4f,
            center = Offset(centerX - 55f, waistY)
        )
        drawCircle(
            color = primaryColor,
            radius = 4f,
            center = Offset(centerX + 55f, waistY)
        )

        // Hip measurement line
        val hipY = topY + 145f
        drawLine(
            color = secondaryColor,
            start = Offset(centerX - 65f, hipY),
            end = Offset(centerX - 42f, hipY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = secondaryColor,
            start = Offset(centerX + 42f, hipY),
            end = Offset(centerX + 65f, hipY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        // Hip arrows
        drawCircle(
            color = secondaryColor,
            radius = 4f,
            center = Offset(centerX - 65f, hipY)
        )
        drawCircle(
            color = secondaryColor,
            radius = 4f,
            center = Offset(centerX + 65f, hipY)
        )
    }
}

@Composable
private fun MeasurementLegendItem(
    color: Color,
    label: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun MeasurementTipCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Use a flexible tape measure. Stand straight and relaxed. Don't hold your breath while measuring.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun UnitToggleSection(
    useMetric: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Measurement Unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (useMetric)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        modifier = Modifier.clickable { if (!useMetric) onToggle() }
                    ) {
                        Text(
                            "cm",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = if (useMetric)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!useMetric)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        modifier = Modifier.clickable { if (useMetric) onToggle() }
                    ) {
                        Text(
                            "inches",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = if (!useMetric)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    error: String?,
    placeholder: String,
    helperText: String,
    onInfoClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "Measurement guide",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onValueChange(newValue)
                }
            },
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            suffix = { Text(unit) },
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MeasurementGuideCard(
    guide: com.health.calculator.bmi.tracker.data.model.WhrMeasurementGuide,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                guide.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                guide.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            HorizontalDivider(
                color = accentColor.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            guide.steps.forEachIndexed { index, step ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        step,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GenderSelectionSection(
    selectedGender: Gender,
    onGenderSelected: (Gender) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Gender",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderOption(
                label = "Male",
                emoji = "👨",
                isSelected = selectedGender == Gender.MALE,
                onClick = { onGenderSelected(Gender.MALE) },
                modifier = Modifier.weight(1f)
            )
            GenderOption(
                label = "Female",
                emoji = "👩",
                isSelected = selectedGender == Gender.FEMALE,
                onClick = { onGenderSelected(Gender.FEMALE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GenderOption(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outlineVariant,
        label = "gender_border"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surface,
        label = "gender_bg"
    )

    Card(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 28.sp)
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HowToMeasureExpandable(
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "How to Measure Correctly",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HowToStep(
                        number = 1,
                        title = "Get a flexible tape measure",
                        description = "Use a soft, flexible measuring tape — not a metal one"
                    )
                    HowToStep(
                        number = 2,
                        title = "Stand straight",
                        description = "Stand upright with feet hip-width apart, arms relaxed at sides"
                    )
                    HowToStep(
                        number = 3,
                        title = "Measure your waist",
                        description = "Find the narrowest point of your torso (usually above the belly button). Wrap the tape snugly around."
                    )
                    HowToStep(
                        number = 4,
                        title = "Measure your hips",
                        description = "Find the widest part of your buttocks. Keep the tape parallel to the floor."
                    )
                    HowToStep(
                        number = 5,
                        title = "Read after exhale",
                        description = "Breathe normally. Read the measurement after a normal exhale — don't suck in your stomach."
                    )
                    HowToStep(
                        number = 6,
                        title = "Take multiple readings",
                        description = "Measure 2-3 times and use the average for best accuracy."
                    )
                }
            }
        }
    }
}

@Composable
private fun HowToStep(
    number: Int,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "$number",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 18.sp
            )
        }
    }
}
