// app/src/main/java/com/health/calculator/bmi/tracker/presentation/components/HealthSnackbar.kt

package com.health.calculator.bmi.tracker.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.ui.theme.HealthCalculatorTheme

/**
 * Custom styled snackbar that matches the app's design system.
 */
@Composable
fun HealthSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            HealthSnackbar(snackbarData = snackbarData)
        }
    )
}

@Composable
fun HealthSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        actionContentColor = MaterialTheme.colorScheme.inversePrimary,
        dismissActionContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        action = snackbarData.visuals.actionLabel?.let { actionLabel ->
            {
                TextButton(onClick = { snackbarData.performAction() }) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                }
            }
        }
    ) {
        Text(
            text = snackbarData.visuals.message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
