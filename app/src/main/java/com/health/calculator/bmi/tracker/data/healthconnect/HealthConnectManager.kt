package com.health.calculator.bmi.tracker.data.healthconnect

import android.content.Context

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter

import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    /** Backwards-compatible alias for the steps feature used by Home and Settings. */
    val permissions: Set<String> = HealthConnectPermissionPolicy.stepsRead

    /** Weight is an optional, separately requested read-only feature. */
    val weightPermissions: Set<String> = HealthConnectPermissionPolicy.weightRead

    private val _isSupported = MutableStateFlow(
        HealthConnectClient.getSdkStatus(context) ==
            HealthConnectClient.SDK_AVAILABLE
    )

    val isSupported: StateFlow<Boolean> =
        _isSupported

    suspend fun hasAllPermissions(requiredPermissions: Set<String> = permissions): Boolean {

        if (!isSupported.value) {
            return false
        }

        return try {

            val granted =
                healthConnectClient
                    .permissionController
                    .getGrantedPermissions()

            granted.containsAll(requiredPermissions)

        } catch (_: Exception) {

            false
        }
    }

    suspend fun readDailySteps(): Long {
        val today = LocalDate.now()
        return readStepsHistory(days = 1)
            .firstOrNull { it.date == today }
            ?.steps
            ?: 0L
    }

    /**
     * Reads a bounded date-level step history for the visible tracking feature.
     * Only aggregated counts are returned; raw Health Connect records never
     * leave this manager. A missing permission or provider error is represented
     * by an empty list so the local dashboard can continue working offline.
     */
    suspend fun readStepsHistory(days: Int = 30): List<HealthConnectStepsDay> {
        if (!isSupported.value || !hasAllPermissions()) return emptyList()

        val safeDays = days.coerceIn(1, 90)
        val zone = ZoneId.systemDefault()
        val end = Instant.now()
        val start = ZonedDateTime.now(zone)
            .toLocalDate()
            .minusDays((safeDays - 1).toLong())
            .atStartOfDay(zone)
            .toInstant()

        return try {
            val result = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            result.records
                .asSequence()
                .filter { it.count >= 0L }
                .groupBy { it.startTime.atZone(zone).toLocalDate() }
                .map { (date, records) ->
                    HealthConnectStepsDay(date, records.sumOf { it.count })
                }
                .sortedBy { it.date }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Reads the most recent weight record without writing it back to Health Connect.
     * The result is intentionally a transient display value; users remain in control of
     * whether to save a copy in the local weight tracker.
     */
    suspend fun readLatestWeight(): HealthConnectWeight? {
        if (!isSupported.value || !hasAllPermissions(weightPermissions)) return null

        val end = Instant.now()
        val start = end.minus(365, ChronoUnit.DAYS)
        return try {
            val result = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end)
                )
            )
            result.records
                .maxByOrNull { it.time }
                ?.let { record ->
                    val kilograms = record.weight.inKilograms
                    if (kilograms.isFinite() && kilograms > 0.0) {
                        HealthConnectWeight(kilograms, record.time.toEpochMilli())
                    } else {
                        null
                    }
                }
        } catch (_: Exception) {
            null
        }
    }
}

data class HealthConnectWeight(
    val kilograms: Double,
    val timestampMillis: Long
)

data class HealthConnectStepsDay(
    val date: LocalDate,
    val steps: Long
)
