package com.health.calculator.bmi.tracker.domain.tracking

import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

/** Shared validation and comparison rules for daily wellness tracking. */
object TrackingQualityPolicy {
    const val MIN_WEIGHT_KG = 20.0
    const val MAX_WEIGHT_KG = 350.0
    const val MIN_WATER_ML = 1
    const val MAX_WATER_ML = 5_000
    const val MAX_NOTE_LENGTH = 280
    const val DEFAULT_STEP_GOAL = 10_000
    const val FUTURE_DATE_TOLERANCE_MILLIS = 5 * 60 * 1_000L

    fun validateWeightKg(weightKg: Double): String? = when {
        !weightKg.isFinite() -> "Enter a valid weight"
        weightKg !in MIN_WEIGHT_KG..MAX_WEIGHT_KG -> "Weight must be between 20 and 350 kg"
        else -> null
    }

    fun validateWaterMl(amountMl: Int): String? =
        if (amountMl in MIN_WATER_ML..MAX_WATER_ML) null
        else "Water entries must be between 1 and 5,000 ml"

    fun validateNote(note: String): String? =
        if (note.length <= MAX_NOTE_LENGTH) null else "Notes can be up to 280 characters"

    fun validateTimestamp(millis: Long, nowMillis: Long = System.currentTimeMillis()): String? =
        if (millis <= nowMillis + FUTURE_DATE_TOLERANCE_MILLIS) null
        else "A log date cannot be in the future"

    fun percentChange(current: Double, previous: Double): Double? {
        if (!current.isFinite() || !previous.isFinite() || previous == 0.0) return null
        return ((current - previous) / abs(previous)) * 100.0
    }

    fun average(values: Iterable<Double>): Double? {
        val finite = values.filter { it.isFinite() }
        return finite.takeIf { it.isNotEmpty() }?.average()
    }

    /** A streak counts consecutive completed logging days, including today. */
    fun currentLoggingStreak(loggedDates: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
        var cursor = today
        var streak = 0
        while (loggedDates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    fun localDateAt(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        java.time.Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
}

enum class TrackingTrend { UP, DOWN, STEADY }

data class TrackingComparison(
    val currentAverage: Double,
    val previousAverage: Double,
    val percentChange: Double,
    val trend: TrackingTrend
)

fun buildTrackingComparison(
    currentValues: Iterable<Double>,
    previousValues: Iterable<Double>,
    steadyThresholdPercent: Double = 1.0
): TrackingComparison? {
    val current = TrackingQualityPolicy.average(currentValues) ?: return null
    val previous = TrackingQualityPolicy.average(previousValues) ?: return null
    val change = TrackingQualityPolicy.percentChange(current, previous) ?: return null
    val trend = when {
        change > steadyThresholdPercent -> TrackingTrend.UP
        change < -steadyThresholdPercent -> TrackingTrend.DOWN
        else -> TrackingTrend.STEADY
    }
    return TrackingComparison(current, previous, change, trend)
}
