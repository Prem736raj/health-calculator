package com.health.calculator.bmi.tracker.export

import com.health.calculator.bmi.tracker.data.export.CsvExportSchema
import com.health.calculator.bmi.tracker.data.model.CalculatorType
import com.health.calculator.bmi.tracker.data.model.CategoryColor
import com.health.calculator.bmi.tracker.data.model.HistoryDisplayEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExportSchemaTest {
    @Test
    fun universalHeaderMatchesEveryExportRow() {
        val row = CsvExportSchema.buildRow(
            HistoryDisplayEntry(
                id = 1L,
                calculatorType = CalculatorType.BMI,
                primaryValue = "23.4",
                primaryLabel = "kg/m²",
                category = "Reference, range",
                categoryColor = CategoryColor.GREEN,
                timestamp = 0L,
                details = linkedMapOf(
                    "Weight" to "70 kg",
                    "Height" to "173 cm",
                    "Note" to "quoted, value"
                ),
                note = "keep \"quoted\""
            )
        )

        assertEquals(13, CsvExportSchema.UNIVERSAL_HEADER.split(',').size)
        assertEquals(13, row.splitCsvFields())
        assertTrue(row.contains("\"Reference, range\""))
        assertTrue(row.contains("\"keep \"\"quoted\"\"\""))
    }

    private fun String.splitCsvFields(): Int {
        var quoted = false
        var fields = 1
        var index = 0
        while (index < length) {
            when {
                this[index] == '"' && quoted && index + 1 < length && this[index + 1] == '"' -> index++
                this[index] == '"' -> quoted = !quoted
                this[index] == ',' && !quoted -> fields++
            }
            index++
        }
        return fields
    }
}
