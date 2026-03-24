package com.health.calculator.bmi.tracker.ui.components.history

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HistoryEmptyState(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (isFiltered) Icons.Default.SearchOff else Icons.Default.History,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .alpha(alpha),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Text(
                text = if (isFiltered)
                    "No matching results"
                else
                    "No calculations yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isFiltered)
                    "Try adjusting your filters to see more results."
                else
                    "Your calculation history will appear here.\nTry a calculator to get started!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isFiltered) {
                OutlinedButton(
                    onClick = onClearFilters,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear Filters")
                }
            } else {
                Button(
                    onClick = onNavigateHome,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go to Calculators")
                }
            }
        }
    }
}
