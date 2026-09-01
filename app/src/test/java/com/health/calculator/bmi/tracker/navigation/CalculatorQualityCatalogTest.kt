package com.health.calculator.bmi.tracker.navigation

import com.health.calculator.bmi.tracker.presentation.navigation.CalculatorDestination
import com.health.calculator.bmi.tracker.presentation.navigation.CalculatorQualityCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorQualityCatalogTest {

    @Test
    fun `catalog covers every calculator destination exactly once`() {
        assertEquals(CalculatorDestination.entries.toSet(), CalculatorQualityCatalog.all.map { it.id }.toSet())
        assertEquals(CalculatorQualityCatalog.all.size, CalculatorQualityCatalog.all.map { it.id }.toSet().size)
    }

    @Test
    fun `every calculator explains inputs method limits and sources`() {
        CalculatorQualityCatalog.all.forEach { info ->
            assertTrue(info.id.name, info.inputs.isNotBlank())
            assertTrue(info.id.name, info.method.isNotBlank())
            assertTrue(info.id.name, info.limitations.isNotBlank())
            assertFalse(info.id.name, info.sources.isEmpty())
        }
    }

    @Test
    fun `related calculator links resolve to known catalog entries`() {
        val ids = CalculatorQualityCatalog.all.map { it.id }.toSet()

        CalculatorQualityCatalog.all.forEach { info ->
            assertTrue(info.id.name, info.related.all(ids::contains))
        }
    }
}
