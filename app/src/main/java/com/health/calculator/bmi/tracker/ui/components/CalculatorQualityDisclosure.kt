package com.health.calculator.bmi.tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.health.calculator.bmi.tracker.presentation.navigation.CalculatorDestination
import com.health.calculator.bmi.tracker.presentation.navigation.CalculatorQualityCatalog
import com.health.calculator.bmi.tracker.presentation.navigation.CalculatorQualityInfo

/**
 * A consistent entry point for the evidence and limitations of every estimate.
 * Keeping it beside the calculation—not only in a separate Learn route—makes
 * the method available at the moment a user is deciding how to use a result.
 */
@Composable
fun CalculatorQualityAction(
    destination: CalculatorDestination,
    modifier: Modifier = Modifier
) {
    var isOpen by rememberSaveable(destination) { mutableStateOf(false) }
    val info = CalculatorQualityCatalog.get(destination)

    IconButton(
        onClick = { isOpen = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "How ${info.title} works"
        )
    }

    if (isOpen) {
        CalculatorQualitySheet(
            info = info,
            onDismiss = { isOpen = false }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CalculatorQualitySheet(
    info: CalculatorQualityInfo,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "About ${info.title}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                info.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CalculatorQualitySection("What you enter", info.inputs)
            CalculatorQualitySection("Method", info.method)
            CalculatorQualitySection("How to read it", info.interpretation)
            CalculatorQualitySection("Limitations", info.limitations)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Text("Sources", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            info.sources.forEach { source ->
                Text(
                    text = "• $source",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (info.related.isNotEmpty()) {
                val labels = info.related.joinToString(" · ") { CalculatorQualityCatalog.get(it).title }
                CalculatorQualitySection("Related calculators", labels)
            }
            Text(
                "This is an informational wellness estimate. It is not a diagnosis, treatment plan, or a substitute for professional care.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("Done")
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CalculatorQualitySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
