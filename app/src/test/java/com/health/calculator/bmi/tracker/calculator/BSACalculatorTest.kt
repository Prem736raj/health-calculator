package com.health.calculator.bmi.tracker.calculator

import org.junit.Assert.*
import org.junit.Test

class BSACalculatorTest {

    private val delta = 0.01f

    @Test
    fun testMostellerFormula() {
        // Height 180cm, Weight 75kg
        val bsa = BSACalculator.calculateSingle(weightKg = 75f, heightCm = 180f, formulaId = "mosteller")
        // sqrt(75 * 180 / 3600) = sqrt(3.75) ≈ 1.936
        assertEquals(1.936f, bsa, delta)
    }

    @Test
    fun testDuBoisFormula() {
        val bsa = BSACalculator.calculateSingle(weightKg = 75f, heightCm = 180f, formulaId = "dubois")
        // 0.007184 * 75^0.425 * 180^0.725 ≈ 1.942
        assertEquals(1.942f, bsa, delta)
    }

    @Test
    fun testHaycockFormula() {
        val bsa = BSACalculator.calculateSingle(weightKg = 75f, heightCm = 180f, formulaId = "haycock")
        // 0.024265 * 75^0.5378 * 180^0.3964 ≈ 1.938
        assertEquals(1.938f, bsa, delta)
    }

    @Test
    fun testGehanGeorgeFormula() {
        val bsa = BSACalculator.calculateSingle(weightKg = 75f, heightCm = 180f, formulaId = "gehan")
        // 0.0235 * 75^0.51456 * 180^0.42246 ≈ 1.944
        assertEquals(1.944f, bsa, delta)
    }

    @Test
    fun testBoydFormula() {
        val bsa = BSACalculator.calculateSingle(weightKg = 75f, heightCm = 180f, formulaId = "boyd")
        // 0.0003207 * (180^0.3) * 75000^(0.7285 - 0.0188 log10(75000)) ≈ 1.938
        assertEquals(1.938f, bsa, delta)
    }
    
    @Test
    fun testEdgeCases() {
        val validation = BSAEdgeCaseValidator.validate(weightKg = 0f, heightCm = 180f, formulaId = "dubois")
        assertFalse(validation.isValid)
        assertNotNull(validation.weightError)
        
        val valid = BSAEdgeCaseValidator.validate(weightKg = 75f, heightCm = 180f, formulaId = "dubois")
        assertTrue(valid.isValid)
    }
}
