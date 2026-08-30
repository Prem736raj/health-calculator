package com.health.calculator.bmi.tracker.data.healthconnect

import android.content.Context

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter

import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import java.time.Instant
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

    /*
     * PHASE 1:
     *
     * Request ONLY data that is actually consumed by
     * a current user-facing app feature.
     */
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(
            StepsRecord::class
        )
    )

    private val _isSupported = MutableStateFlow(
        HealthConnectClient.getSdkStatus(context) ==
            HealthConnectClient.SDK_AVAILABLE
    )

    val isSupported: StateFlow<Boolean> =
        _isSupported

    suspend fun hasAllPermissions(): Boolean {

        if (!isSupported.value) {
            return false
        }

        return try {

            val granted =
                healthConnectClient
                    .permissionController
                    .getGrantedPermissions()

            granted.containsAll(permissions)

        } catch (_: Exception) {

            false
        }
    }

    suspend fun readDailySteps(): Long {

        if (!isSupported.value) {
            return 0L
        }

        if (!hasAllPermissions()) {
            return 0L
        }

        val start =
            ZonedDateTime
                .now()
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant()

        val end = Instant.now()

        return try {

            val result =
                healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter =
                            TimeRangeFilter.between(
                                start,
                                end
                            )
                    )
                )

            result.records.sumOf {
                it.count
            }

        } catch (_: Exception) {

            0L
        }
    }
}
