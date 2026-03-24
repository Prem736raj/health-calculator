// File: com/health/calculator/bmi/tracker/ui/screens/bmr/components/BMRActionButtons.kt
package com.health.calculator.bmi.tracker.ui.screens.bmr.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.ui.screens.calculators.bmi.components.AnimatedActionButton

@Composable
fun BMRActionButtons(
    isSaved: Boolean,
    onSave: () -> Unit,
    onRecalculate: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedActionButton(
            text = if (isSaved) "Saved ✓" else "Save",
            icon = Icons.Filled.Save,
            onClick = onSave,
            enabled = !isSaved,
            modifier = Modifier.weight(1f),
            containerColor = if (isSaved)
                MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (isSaved)
                MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onPrimaryContainer
        )

        AnimatedActionButton(
            text = "Recalculate",
            icon = Icons.Filled.Refresh,
            onClick = onRecalculate,
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        AnimatedActionButton(
            text = "Share",
            icon = Icons.Filled.Share,
            onClick = onShare,
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}
