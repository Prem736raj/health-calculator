package com.health.calculator.bmi.tracker.data.provider

import android.content.Context
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.BpCategory
import com.health.calculator.bmi.tracker.data.model.BpHomeCardInfo
import com.health.calculator.bmi.tracker.data.preferences.BpReminderPreferences
import com.health.calculator.bmi.tracker.data.preferences.BpReminderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class BpHomeCardProvider(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val dao = database.bloodPressureDao()
    private val bpPreferences = BpReminderPreferences(context)

    fun getHomeCardInfo(): Flow<BpHomeCardInfo> {
        return combine(
            dao.getLatestReadingFlow(),
            bpPreferences.settingsFlow
        ) { latest, settings: BpReminderSettings ->
            if (latest == null) {
                BpHomeCardInfo()
            } else {
                val category = try {
                    BpCategory.valueOf(latest.category)
                } catch (e: Exception) {
                    BpCategory.OPTIMAL
                }

                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(latest.measurementTimestamp),
                    ZoneId.systemDefault()
                )

                val now = LocalDateTime.now()
                val hoursAgo = ChronoUnit.HOURS.between(dateTime, now)
                val daysAgo = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())

                val timeText = when {
                    hoursAgo < 1 -> "Just now"
                    hoursAgo < 24 -> "${hoursAgo}h ago"
                    daysAgo == 1L -> "Yesterday"
                    daysAgo < 7 -> "${daysAgo}d ago"
                    else -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd"))
                }

                val isConcerning = category.sortOrder >= BpCategory.GRADE_1_HYPERTENSION.sortOrder

                BpHomeCardInfo(
                    hasReading = true,
                    lastSystolic = latest.systolic,
                    lastDiastolic = latest.diastolic,
                    lastCategory = category,
                    lastReadingTime = timeText,
                    isConcerning = isConcerning,
                    streakDays = settings.currentStreak
                )
            }
        }
    }

    suspend fun getLatestCardInfo(): BpHomeCardInfo {
        val latest = dao.getLatestReading() ?: return BpHomeCardInfo()
        val category = try {
            BpCategory.valueOf(latest.category)
        } catch (e: Exception) {
            BpCategory.OPTIMAL
        }

        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(latest.measurementTimestamp),
            ZoneId.systemDefault()
        )
        val now = LocalDateTime.now()
        val hoursAgo = ChronoUnit.HOURS.between(dateTime, now)
        val daysAgo = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())

        val timeText = when {
            hoursAgo < 1 -> "Just now"
            hoursAgo < 24 -> "${hoursAgo}h ago"
            daysAgo == 1L -> "Yesterday"
            daysAgo < 7 -> "${daysAgo}d ago"
            else -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd"))
        }

        return BpHomeCardInfo(
            hasReading = true,
            lastSystolic = latest.systolic,
            lastDiastolic = latest.diastolic,
            lastCategory = category,
            lastReadingTime = timeText,
            isConcerning = category.sortOrder >= BpCategory.GRADE_1_HYPERTENSION.sortOrder,
            streakDays = 0
        )
    }
}
