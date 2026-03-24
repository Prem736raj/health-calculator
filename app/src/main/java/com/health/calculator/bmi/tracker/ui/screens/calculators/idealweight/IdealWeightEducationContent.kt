package com.health.calculator.bmi.tracker.ui.screens.calculators.idealweight

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IdealWeightEducationContent() {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        EducationSection(
            title = "What is Ideal Body Weight?",
            content = "Ideal Body Weight (IBW) is a target weight derived from scientific formulas based on height and gender. It was originally developed to determine dosage for medications but has since become a popular benchmark for assessing weight health.",
            icon = Icons.Outlined.Lightbulb
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        EducationSection(
            title = "Which Formula is Best?",
            content = "While four formulas are provided (Devine, Robinson, Miller, and Hamwi), the Devine Formula is the most widely used in clinical settings. Robinson and Miller are considered more modern updates, and Hamwi is often used in social settings. Most people find that all formulas yield results within a few kilograms of each other.",
            icon = Icons.Outlined.Functions
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        EducationSection(
            title = "BMI Healthy Range",
            content = "The World Health Organization (WHO) defines a healthy BMI as being between 18.5 and 25.0. Our calculator also provides this range as a broader alternative to fixed-point IBW formulas, giving you more flexibility in your fitness goals.",
            icon = Icons.Outlined.CheckCircle
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        EducationSection(
            title = "Limitations of IBW",
            content = "IBW formulas only consider height and gender. They do not account for muscle mass, bone density, or body fat percentage. Athletes and people with high muscle mass may find the results suggest a weight that is too low for their actual healthy state.",
            icon = Icons.Outlined.Warning
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "This calculator is for informational purposes only. Please consult with a healthcare professional before making significant changes to your diet or exercise routine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun EducationSection(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
