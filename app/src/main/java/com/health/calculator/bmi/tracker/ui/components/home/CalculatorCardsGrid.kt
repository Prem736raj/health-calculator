package com.health.calculator.bmi.tracker.ui.components.home

import androidx.compose.ui.res.stringResource
import com.health.calculator.bmi.tracker.R

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Grid of all calculator cards with dynamic data
 */
@Composable
fun CalculatorCardsGrid(
    state: CalculatorCardsState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: BMI & BMR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BMICalculatorCard(
                lastBMI = state.lastBMI,
                lastCategory = state.lastBMICategory,
                onClick = { onNavigate("bmi_calculator") },
                modifier = Modifier.weight(1f)
            )
            BMRCalculatorCard(
                lastBMR = state.lastBMR,
                lastTDEE = state.lastTDEE,
                onClick = { onNavigate("bmr_calculator") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: BP & WHR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BloodPressureCard(
                lastSystolic = state.lastBPSystolic,
                lastDiastolic = state.lastBPDiastolic,
                lastCategory = state.lastBPCategory,
                onClick = { onNavigate("blood_pressure_checker") },
                modifier = Modifier.weight(1f)
            )
            WHRCalculatorCard(
                lastWHR = state.lastWHR,
                lastCategory = state.lastWHRCategory,
                onClick = { onNavigate("whr_calculator") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Water & Calories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WaterIntakeCard(
                currentIntake = state.waterIntakeToday,
                goalIntake = state.waterGoalToday,
                onClick = { onNavigate("water_intake_calculator") },
                modifier = Modifier.weight(1f)
            )
            CalorieCalculatorCard(
                consumedCalories = state.caloriesConsumedToday,
                targetCalories = state.calorieTargetToday,
                onClick = { onNavigate("calorie_calculator") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: Metabolic & BSA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetabolicSyndromeCard(
                criteriaMet = state.metabolicCriteriaMet,
                onClick = { onNavigate("metabolic_syndrome_checker") },
                modifier = Modifier.weight(1f)
            )
            BSACalculatorCard(
                lastBSA = state.lastBSA,
                onClick = { onNavigate("bsa_calculator") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: IBW & Heart Rate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IBWCalculatorCard(
                idealWeight = state.idealBodyWeight,
                currentWeight = state.currentWeight,
                onClick = { onNavigate("ibw_calculator") },
                modifier = Modifier.weight(1f)
            )
            HeartRateZonesCard(
                maxHR = state.maxHeartRate,
                restingHR = state.restingHeartRate,
                onClick = { onNavigate("heart_rate_zone_calculator") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Section header for calculator cards
 */
@Composable
fun CalculatorsSectionHeader(
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Text(
        text = stringResource(R.string.txt_health_calculators),
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = modifier.padding(vertical = 8.dp)
    )
}
