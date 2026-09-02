package com.health.calculator.bmi.tracker.domain.monetization

/**
 * Product-level monetization boundaries.
 *
 * This policy is deliberately independent of Play Billing. It lets the UI and
 * future billing adapter agree on what may be paid for without creating a
 * pretend entitlement or blocking safety information while store products
 * are not configured.
 */
enum class SubscriptionTier {
    FREE,
    PLUS
}

enum class PremiumFeature(
    val productId: String,
    val title: String,
    val description: String
) {
    EXTENDED_TRENDS(
        productId = "plus_extended_trends",
        title = "Extended trends",
        description = "Longer comparisons and more ways to review your recorded patterns."
    ),
    DETAILED_REPORTS(
        productId = "plus_detailed_reports",
        title = "Detailed reports",
        description = "Expanded report layouts and additional export customization."
    ),
    WIDGET_CUSTOMIZATION(
        productId = "plus_widget_customization",
        title = "Widget customization",
        description = "More choices for arranging the wellness information shown in widgets."
    ),
    AI_WELLNESS_MESSAGES(
        productId = "plus_ai_wellness_messages",
        title = "Additional AI Wellness Assistant messages",
        description = "A higher usage allowance for optional, safety-screened wellness conversations."
    )
}

object PremiumFeaturePolicy {
    const val FREE_PLAN_NAME = "Free"
    const val PLUS_PLAN_NAME = "Plus"

    /** Core value must remain useful without a purchase. */
    val freeCoreFeatures: List<String> = listOf(
        "All informational calculators",
        "Basic weight, water, food and blood-pressure logging",
        "Local history and deterministic insights",
        "Weekly wellness summaries",
        "CSV and JSON wellness-data exports",
        "Optional read-only Health Connect connections"
    )

    val plusFeatures: List<PremiumFeature> = PremiumFeature.entries.toList()

    /**
     * A feature is included for Plus only when a future store-backed
     * entitlement says so. Free users never lose access to core features.
     */
    fun isIncluded(feature: PremiumFeature, tier: SubscriptionTier): Boolean =
        tier == SubscriptionTier.PLUS

    fun plannedUpgradeCopy(): String =
        "Optional Plus features are planned for advanced personalization. Core calculators, tracking and safety information remain free."

    fun unavailableUntilStoreSetupCopy(): String =
        "Premium purchases are not enabled in this build. Nothing is required to keep using the free wellness experience."
}
