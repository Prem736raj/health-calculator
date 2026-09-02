package com.health.calculator.bmi.tracker.domain.growth

/** Guardrails for the Play Store copy prepared in ASO_LAUNCH_PLAN.md. */
object StoreListingPolicy {
    const val APP_TITLE = "Health Metrics Tracker"
    const val SHORT_DESCRIPTION = "Track weight, water, steps and wellness insights with helpful calculators"
    const val CATEGORY = "Health & Fitness"

    val screenshotOverlays: List<String> = listOf(
        "See today's wellness snapshot",
        "Log a weigh-in in seconds",
        "Build a hydration rhythm",
        "Understand the method and limits",
        "Spot changes without diagnosis",
        "Connect steps only when you choose",
        "Review your week and share selected sections",
        "Ask general wellness questions — context is optional"
    )

    private val titleAndShortDescriptionBannedPhrases = listOf(
        "best",
        "#1",
        "free",
        "sale",
        "download now",
        "medical device",
        "diagnosis",
        "guaranteed accurate",
        "who standard"
    )

    fun isTitleCompliant(title: String = APP_TITLE): Boolean =
        title.isNotBlank() &&
            title.length <= 30 &&
            titleAndShortDescriptionBannedPhrases.none { title.contains(it, ignoreCase = true) }

    fun isShortDescriptionCompliant(description: String = SHORT_DESCRIPTION): Boolean =
        description.isNotBlank() &&
            description.length <= 80 &&
            descriptionAndTitleHasSafePunctuation(description) &&
            titleAndShortDescriptionBannedPhrases.none { description.contains(it, ignoreCase = true) }

    private fun descriptionAndTitleHasSafePunctuation(description: String): Boolean =
        description.none { it == '\n' || it == '\r' } &&
            !description.contains("!!") &&
            !description.contains("??")
}
