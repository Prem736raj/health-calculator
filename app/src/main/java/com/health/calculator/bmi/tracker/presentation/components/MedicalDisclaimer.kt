// app/src/main/java/com/health/calculator/bmi/tracker/presentation/components/MedicalDisclaimer.kt

package com.health.calculator.bmi.tracker.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.core.constants.AppConstants
import com.health.calculator.bmi.tracker.ui.theme.HealthCalculatorTheme

/**
 * Short inline medical disclaimer used at the bottom of calculator screens.
 */
@Composable
fun MedicalDisclaimerShort(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Medical disclaimer",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = AppConstants.MEDICAL_DISCLAIMER_SHORT,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Start
        )
    }
}

/**
 * Full medical disclaimer card used in settings and first-launch screens.
 */
@Composable
fun MedicalDisclaimerFull(
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.MedicalServices,
                    contentDescription = "Medical disclaimer",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = AppConstants.MEDICAL_DISCLAIMER_FULL,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true)
@Composable
private fun MedicalDisclaimerShortPreview() {
    HealthCalculatorTheme {
        MedicalDisclaimerShort()
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicalDisclaimerFullPreview() {
    HealthCalculatorTheme {
        MedicalDisclaimerFull()
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicalDisclaimerFullDarkPreview() {
    HealthCalculatorTheme(themeMode = com.health.calculator.bmi.tracker.data.model.ThemeMode.DARK) {
        MedicalDisclaimerFull()
    }
}
