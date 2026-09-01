package com.health.calculator.bmi.tracker.ai

import com.health.calculator.bmi.tracker.data.ai.AiResponseSafety
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiResponseSafetyTest {
    @Test
    fun replacesMedicationInstructionsAndDiagnosticCertainty() {
        val medication = AiResponseSafety.sanitize("Stop your medication today and double the dose tomorrow.")
        val diagnosis = AiResponseSafety.sanitize("You may have hypertension because this result is high.")

        assertFalse(medication.contains("Stop your medication", ignoreCase = true))
        assertFalse(diagnosis.contains("You may have hypertension", ignoreCase = true))
        assertTrue(medication.contains("general wellness", ignoreCase = true))
    }

    @Test
    fun urgentPromptAddsEscalationWhenReplyOmitsIt() {
        val response = AiResponseSafety.sanitize("I can share general information.", potentiallyUrgent = true)

        assertTrue(response.contains("local emergency services", ignoreCase = true))
    }

    @Test
    fun existingEscalationIsNotDuplicated() {
        val response = AiResponseSafety.sanitize(
            "Please seek medical care now or contact local emergency services.",
            potentiallyUrgent = true
        )

        assertTrue(response.lowercase().indexOf("emergency services") == response.lowercase().lastIndexOf("emergency services"))
    }
}
