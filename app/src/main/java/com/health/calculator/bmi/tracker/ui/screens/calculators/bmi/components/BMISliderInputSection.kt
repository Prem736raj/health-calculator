package com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BMISliderInputSection(
    weightKg: Float,
    heightCm: Float,
    isUnitKg: Boolean,
    isUnitCm: Boolean,
    onWeightChange: (Float) -> Unit,
    onHeightChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Weight slider
            WeightSliderPicker(
                weightKg = weightKg,
                isUnitKg = isUnitKg,
                onWeightChange = onWeightChange
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Height slider
            HeightSliderPicker(
                heightCm = heightCm,
                isUnitCm = isUnitCm,
                onHeightChange = onHeightChange
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Real-time BMI Preview
    RealTimeBMIPreview(
        weightKg = weightKg,
        heightCm = heightCm
    )
}
