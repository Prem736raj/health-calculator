package com.health.calculator.bmi.tracker.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
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
    @ApplicationContext private val context: Context
) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class)
    )

    private val _isSupported = MutableStateFlow(
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    )
    val isSupported: StateFlow<Boolean> = _isSupported

    suspend fun hasAllPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readDailySteps(): Long {
        if (!hasAllPermissions()) return 0L
        
        val todayStart = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant()
        val now = Instant.now()
        
        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(todayStart, now)
        )
        
        val response = healthConnectClient.readRecords(request)
        return response.records.sumOf { it.count }
    }
}
