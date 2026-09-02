package com.health.calculator.bmi.tracker.growth

import com.health.calculator.bmi.tracker.domain.growth.StoreListingPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreListingPolicyTest {
    @Test
    fun plannedTitleAndShortDescriptionFitPlayLimits() {
        assertTrue(StoreListingPolicy.isTitleCompliant())
        assertTrue(StoreListingPolicy.isShortDescriptionCompliant())
        assertTrue(StoreListingPolicy.APP_TITLE.length <= 30)
        assertTrue(StoreListingPolicy.SHORT_DESCRIPTION.length <= 80)
    }

    @Test
    fun metadataGuardRejectsPromotionalOrMedicalCertaintyCopy() {
        assertFalse(StoreListingPolicy.isTitleCompliant("Best BMI app #1"))
        assertFalse(StoreListingPolicy.isShortDescriptionCompliant("Download now: guaranteed accurate diagnosis"))
    }

    @Test
    fun screenshotPlanIsCompleteAndDoesNotUseRawHealthValues() {
        assertTrue(StoreListingPolicy.screenshotOverlays.size in 2..8)
        assertTrue(StoreListingPolicy.screenshotOverlays.all { it.isNotBlank() })
        assertTrue(StoreListingPolicy.screenshotOverlays.none { overlay ->
            overlay.matches(Regex(".*\\d+([./:]\\d+)?(\\s?(kg|lb|ml|mmHg|steps|%))?.*", RegexOption.IGNORE_CASE))
        })
    }
}
