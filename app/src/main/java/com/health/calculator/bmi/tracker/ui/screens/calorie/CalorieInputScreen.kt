package com.health.calculator.bmi.tracker.ui.screens.calorie

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalorieInputScreen(
    uiState: CalorieUiState,
    activityLevels: List<ActivityLevelOption>,
    goalOptions: List<GoalOption>,
    onUpdateWeight: (String) -> Unit,
    onToggleWeightUnit: () -> Unit,
    onUpdateHeight: (String) -> Unit,
    onUpdateHeightFeet: (String) -> Unit,
    onUpdateHeightInches: (String) -> Unit,
    onToggleHeightUnit: () -> Unit,
    onUpdateAge: (String) -> Unit,
    onUpdateGender: (String) -> Unit,
    onUpdateBodyFat: (String) -> Unit,
    onToggleBodyFat: () -> Unit,
    onSelectActivity: (String) -> Unit,
    onSelectGoal: (String) -> Unit,
    onCalculate: () -> Unit,
    onClear: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header note
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Daily Calorie Calculator",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Find out exactly how many calories you need to reach your goals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Profile data indicator
        AnimatedVisibility(visible = uiState.isProfileDataUsed) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Using profile data • You can adjust values below",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // ─── SECTION 1: Body Measurements ───
        SectionHeader(icon = Icons.Default.Straighten, title = "Body Measurements")

        // Gender
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Male" to "♂️", "Female" to "♀️").forEach { (gender, emoji) ->
                FilterChip(
                    selected = uiState.gender == gender,
                    onClick = { onUpdateGender(gender) },
                    label = {
                        Text("$emoji $gender")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        if (uiState.gender == gender) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        }
                    }
                )
            }
        }

        // Weight
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = uiState.weightValue,
                onValueChange = onUpdateWeight,
                label = { Text(if (uiState.isMetricWeight) "Weight (kg)" else "Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.MonitorWeight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            FilledTonalIconButton(
                onClick = onToggleWeightUnit,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    if (uiState.isMetricWeight) "lbs" else "kg",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Height
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (uiState.isMetricHeight) {
                OutlinedTextField(
                    value = uiState.heightValue,
                    onValueChange = onUpdateHeight,
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Height,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            } else {
                OutlinedTextField(
                    value = uiState.heightFeet,
                    onValueChange = onUpdateHeightFeet,
                    label = { Text("Feet") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.heightInches,
                    onValueChange = onUpdateHeightInches,
                    label = { Text("Inches") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            FilledTonalIconButton(
                onClick = onToggleHeightUnit,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    if (uiState.isMetricHeight) "ft" else "cm",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Age
        OutlinedTextField(
            value = uiState.age,
            onValueChange = onUpdateAge,
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            suffix = { Text("years") }
        )

        // Body Fat Percentage (optional)
        BodyFatSection(
            showBodyFatField = uiState.showBodyFatField,
            bodyFatPercent = uiState.bodyFatPercent,
            onToggle = onToggleBodyFat,
            onUpdate = onUpdateBodyFat
        )

        // ─── SECTION 2: Activity Level ───
        SectionHeader(icon = Icons.Default.DirectionsRun, title = "Activity Level")

        ActivityLevelSelector(
            options = activityLevels,
            selectedId = uiState.selectedActivityLevel,
            onSelect = onSelectActivity
        )

        // ─── SECTION 3: Goal ───
        SectionHeader(icon = Icons.Default.Flag, title = "Your Goal")

        GoalSelector(
            options = goalOptions,
            selectedId = uiState.selectedGoal,
            onSelect = onSelectGoal
        )

        // Error Message
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Calculate Button
        Button(
            onClick = onCalculate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Calculate My Calories",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Clear button
        TextButton(
            onClick = onClear,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Clear, contentDescription = null, Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear All")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BodyFatSection(
    showBodyFatField: Boolean,
    bodyFatPercent: String,
    onToggle: () -> Unit,
    onUpdate: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Body Fat % (Optional)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "For more accurate calculation using Katch-McArdle formula",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = showBodyFatField,
                    onCheckedChange = { onToggle() }
                )
            }

            AnimatedVisibility(
                visible = showBodyFatField,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = bodyFatPercent,
                        onValueChange = onUpdate,
                        label = { Text("Body Fat Percentage") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        suffix = { Text("%") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Percent,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Don't know? Skip this — we'll use standard formulas instead. Typical ranges: Men 10-25%, Women 18-35%.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLevelSelector(
    options: List<ActivityLevelOption>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selectedId == option.id

            val animatedElevation by animateDpAsState(
                targetValue = if (isSelected) 4.dp else 0.dp,
                animationSpec = tween(300),
                label = "elevation"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(option.id) },
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
                border = if (isSelected) BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary
                ) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(option.id) },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = option.emoji,
                        fontSize = 22.sp,
                        modifier = Modifier.width(32.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "×${option.multiplier}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = option.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            lineHeight = 16.sp,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalSelector(
    options: List<GoalOption>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Group labels
        Text(
            text = "Weight Loss",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )

        options.filter { it.weeklyChangeKg < 0 }.forEach { option ->
            GoalOptionCard(option, selectedId == option.id) { onSelect(option.id) }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Maintenance",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )

        options.filter { it.weeklyChangeKg == 0.0 }.forEach { option ->
            GoalOptionCard(option, selectedId == option.id) { onSelect(option.id) }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Weight Gain",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )

        options.filter { it.weeklyChangeKg > 0 }.forEach { option ->
            GoalOptionCard(option, selectedId == option.id) { onSelect(option.id) }
        }
    }
}

@Composable
private fun GoalOptionCard(
    option: GoalOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val goalColor = Color(option.color)

    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = tween(300),
        label = "goalElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        border = if (isSelected) BorderStroke(2.dp, goalColor) else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                goalColor.copy(alpha = 0.06f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.size(20.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = goalColor
                )
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = option.emoji,
                fontSize = 18.sp,
                modifier = Modifier.width(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = if (isSelected) goalColor
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            // Calorie adjustment badge
            if (option.calorieAdjustment != 0) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = goalColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${if (option.calorieAdjustment > 0) "+" else ""}${option.calorieAdjustment}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = goalColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
