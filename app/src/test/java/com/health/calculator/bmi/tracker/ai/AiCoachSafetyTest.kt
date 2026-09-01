package com.health.calculator.bmi.tracker.ai

import com.health.calculator.bmi.tracker.data.ai.AiCoachException
import com.health.calculator.bmi.tracker.data.ai.AiCoachFailureReason
import org.junit.Assert.*
import org.junit.Test

class AiCoachSafetyTest {

    @Test
    fun testAiCoachExceptionCreation() {
        val rootCause = RuntimeException("Network timeout")
        val exception = AiCoachException(
            message = "AI service is temporarily unavailable.",
            cause = rootCause
        )

        assertEquals("AI service is temporarily unavailable.", exception.message)
        assertEquals(rootCause, exception.cause)
    }

    @Test
    fun failureReasonClassifiesSafeUserFacingStates() {
        assertEquals(
            AiCoachFailureReason.NETWORK,
            AiCoachFailureReason.from(RuntimeException("network timeout"))
        )
        assertEquals(
            AiCoachFailureReason.RATE_LIMITED,
            AiCoachFailureReason.from(RuntimeException("HTTP 429 quota exceeded"))
        )
    }
}
