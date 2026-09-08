package com.health.calculator.bmi.tracker.history

import com.health.calculator.bmi.tracker.data.model.HistoryEntry
import com.health.calculator.bmi.tracker.data.model.toDisplayEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryDetailsParsingTest {
    @Test
    fun jsonDetailsPreserveCommasColonsAndQuotes() {
        val display = HistoryEntry(
            calculatorKey = "bmi",
            resultValue = "23.4",
            resultLabel = "BMI",
            detailsJson = "{\"note\":\"steady, not a diagnosis\",\"ratio\":\"1:2\",\"quoted\":\"a \\\"value\\\"\"}"
        ).toDisplayEntry()

        assertEquals("steady, not a diagnosis", display.details["note"])
        assertEquals("1:2", display.details["ratio"])
        assertEquals("a \"value\"", display.details["quoted"])
    }

    @Test
    fun legacyPipeDetailsRemainReadable() {
        val display = HistoryEntry(
            calculatorKey = "bmi",
            resultValue = "23.4",
            resultLabel = "BMI",
            detailsJson = "secondary_value:23.4|secondary_label:kg/m²|note:legacy:format"
        ).toDisplayEntry()

        assertEquals("23.4", display.details["secondary_value"])
        assertEquals("kg/m²", display.details["secondary_label"])
        assertEquals("legacy:format", display.details["note"])
        assertTrue(display.details.keys.containsAll(listOf("secondary_value", "secondary_label", "note")))
    }
}
