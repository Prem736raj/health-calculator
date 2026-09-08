package com.health.calculator.bmi.tracker.core.brand

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandVoiceTest {

    @Test
    fun `positioning stays calm optional and privacy conscious`() {
        val copy = listOf(
            BrandVoice.TAGLINE,
            BrandVoice.POSITIONING,
            BrandVoice.PRIVACY_PROMISE,
            BrandVoice.WELLNESS_BOUNDARY
        ).joinToString(" ").lowercase()

        assertTrue(copy.contains("calm"))
        assertTrue(copy.contains("control"))
        assertTrue(copy.contains("wellness"))
        assertFalse(copy.contains("clinically proven"))
        assertFalse(copy.contains("diagnose"))
        assertFalse(copy.contains("cure"))
    }
}
