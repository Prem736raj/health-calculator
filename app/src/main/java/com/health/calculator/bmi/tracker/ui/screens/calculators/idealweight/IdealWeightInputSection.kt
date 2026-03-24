package com.health.calculator.bmi.tracker.ui.screens.calculators.idealweight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedInputField
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedCalculateButton
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedClearButton
import com.health.calculator.bmi.tracker.ui.utils.ShakeController

@Composable
fun IdealWeightInputSection(
    inputState: IdealWeightInputState,
    validationState: IdealWeightValidationState,
    isCalculating: Boolean,
    onWeightUpdate: (String) -> Unit = {}, // Not actually used in IBW but for consistency if I were to use a shared component
    onHeightUpdate: (String) -> Unit,
    onHeightFeetUpdate: (String) -> Unit,
    onHeightInchesUpdate: (String) -> Unit,
    onAgeUpdate: (String) -> Unit,
    onGenderUpdate: (Boolean) -> Unit,
    onToggleHeightUnit: () -> Unit,
    onCalculate: () -> Unit,
    onClearAll: () -> Unit,
    weightShakeController: ShakeController, // Placeholders for shake
    heightShakeController: ShakeController,
    ageShakeController: ShakeController
) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Personal Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Height Input
                if (inputState.isUnitCm) {
                    AnimatedInputField(
                        value = inputState.heightText,
                        onValueChange = onHeightUpdate,
                        label = "Height (cm)",
                        icon = Icons.Outlined.Height,
                        errorMessage = if (validationState.hasAttemptedCalculation)
                            validationState.heightError else null,
                        shakeController = heightShakeController,
                        suffix = "cm",
                        trailingContent = {
                            UnitToggleButton(
                                isFirstOption = true,
                                firstLabel = "cm",
                                secondLabel = "ft-in",
                                onClick = onToggleHeightUnit
                            )
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedInputField(
                            value = inputState.heightFeetText,
                            onValueChange = onHeightFeetUpdate,
                            label = "Feet",
                            icon = Icons.Outlined.Height,
                            errorMessage = null,
                            shakeController = heightShakeController,
                            suffix = "ft",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedInputField(
                            value = inputState.heightInchesText,
                            onValueChange = onHeightInchesUpdate,
                            label = "Inches",
                            icon = Icons.Outlined.Straighten,
                            errorMessage = if (validationState.hasAttemptedCalculation)
                                validationState.heightError else null,
                            shakeController = heightShakeController,
                            suffix = "in",
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                            trailingContent = {
                                UnitToggleButton(
                                    isFirstOption = false,
                                    firstLabel = "cm",
                                    secondLabel = "ft-in",
                                    onClick = onToggleHeightUnit
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Age Input
                AnimatedInputField(
                    value = inputState.ageText,
                    onValueChange = onAgeUpdate,
                    label = "Age",
                    icon = Icons.Outlined.Cake,
                    errorMessage = if (validationState.hasAttemptedCalculation)
                        validationState.ageError else null,
                    shakeController = ageShakeController,
                    suffix = "years",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender Selection
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GenderChip(
                        label = "Male",
                        icon = Icons.Outlined.Male,
                        isSelected = inputState.isMale,
                        onClick = { onGenderUpdate(true) },
                        modifier = Modifier.weight(1f)
                    )
                    GenderChip(
                        label = "Female",
                        icon = Icons.Outlined.Female,
                        isSelected = !inputState.isMale,
                        onClick = { onGenderUpdate(false) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        AnimatedCalculateButton(
            onClick = onCalculate,
            isLoading = isCalculating,
            enabled = !isCalculating
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedClearButton(onClick = onClearAll)
        }
    }
}

@Composable
private fun UnitToggleButton(
    isFirstOption: Boolean,
    firstLabel: String,
    secondLabel: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(
            text = if (isFirstOption) firstLabel else secondLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Icon(
            Icons.Outlined.SwapHoriz,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}
