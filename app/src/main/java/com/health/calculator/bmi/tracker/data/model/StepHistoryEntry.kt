package com.health.calculator.bmi.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One locally cached, date-level step total imported from Health Connect.
 *
 * The app never writes steps back to Health Connect. Keeping one row per local
 * day makes the dashboard resilient to process death and lets users see
 * explainable trends without retaining raw Health Connect records.
 */
@Entity(tableName = "step_history")
data class StepHistoryEntry(
    @PrimaryKey val dayStartMillis: Long,
    val steps: Long,
    val source: String = SOURCE_HEALTH_CONNECT,
    val syncedAtMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val SOURCE_HEALTH_CONNECT = "health_connect"
    }
}
