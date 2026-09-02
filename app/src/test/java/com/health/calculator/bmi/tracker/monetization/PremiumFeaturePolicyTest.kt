package com.health.calculator.bmi.tracker.monetization

import com.health.calculator.bmi.tracker.domain.monetization.PremiumFeature
import com.health.calculator.bmi.tracker.domain.monetization.PremiumFeaturePolicy
import com.health.calculator.bmi.tracker.domain.monetization.SubscriptionTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumFeaturePolicyTest {
    @Test
    fun coreWellnessValueIsFreeAndNonEmpty() {
        assertTrue(PremiumFeaturePolicy.freeCoreFeatures.size >= 5)
        assertTrue(PremiumFeaturePolicy.freeCoreFeatures.any { it.contains("calculators", ignoreCase = true) })
        assertTrue(PremiumFeaturePolicy.freeCoreFeatures.any { it.contains("logging", ignoreCase = true) })
        assertTrue(PremiumFeaturePolicy.freeCoreFeatures.any { it.contains("safety", ignoreCase = true) || it.contains("Health Connect", ignoreCase = true) })
    }

    @Test
    fun plusCatalogHasStableStoreIdsAndUsefulDescriptions() {
        val features = PremiumFeaturePolicy.plusFeatures

        assertEquals(PremiumFeature.entries.toSet(), features.toSet())
        assertEquals(features.size, features.map { it.productId }.toSet().size)
        assertTrue(features.all { it.productId.startsWith("plus_") })
        assertTrue(features.all { it.title.isNotBlank() && it.description.isNotBlank() })
    }

    @Test
    fun freeUsersAreNeverGrantedPaidFeatures() {
        assertFalse(PremiumFeaturePolicy.isIncluded(PremiumFeature.EXTENDED_TRENDS, SubscriptionTier.FREE))
        assertTrue(PremiumFeaturePolicy.isIncluded(PremiumFeature.EXTENDED_TRENDS, SubscriptionTier.PLUS))
    }

    @Test
    fun copyDoesNotUsePressureOrSafetyGating() {
        val copy = "${PremiumFeaturePolicy.plannedUpgradeCopy()} ${PremiumFeaturePolicy.unavailableUntilStoreSetupCopy()}".lowercase()

        assertTrue(copy.contains("remain free"))
        assertTrue(copy.contains("nothing is required"))
        assertFalse(copy.contains("hurry"))
        assertFalse(copy.contains("limited time"))
        assertFalse(copy.contains("unlock your health"))
    }
}
