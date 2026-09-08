package com.health.calculator.bmi.tracker.calculator

import com.health.calculator.bmi.tracker.domain.usecase.IBWCalculatorUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IBWCalculatorUseCaseTest {
    private val calculator = IBWCalculatorUseCase()

    @Test
    fun shortAdultDoesNotReceiveInventedHistoricalFormulaValues() {
        val result = calculator.calculate(
            heightCm = 150.0,
            gender = "Female",
            frameSize = "Medium",
            currentWeightKg = 50.0,
            age = 30
        )

        assertFalse(result.hasHistoricalFormulaResults)
        assertFalse(result.devineKg.isFinite())
        assertFalse(result.robinsonKg.isFinite())
        assertFalse(result.brocaKg.isFinite())
        assertTrue(result.bmiLowerKg.isFinite())
        assertTrue(result.bmiUpperKg.isFinite())
        assertTrue(result.heightWarning.orEmpty().contains("below 60 inches", ignoreCase = true))
    }

    @Test
    fun boundaryAtSixtyInchesUsesHistoricalEquations() {
        val result = calculator.calculate(
            heightCm = 152.41,
            gender = "Male",
            frameSize = "Medium"
        )

        assertTrue(result.hasHistoricalFormulaResults)
        assertTrue(result.devineKg.isFinite())
        assertTrue(result.frameAdjustedDevineKg.isFinite())
    }
}
