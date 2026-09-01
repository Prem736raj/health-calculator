package com.health.calculator.bmi.tracker.data.healthconnect

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord

/**
 * The smallest read-only permission sets for features currently visible in the app.
 * Keep each feature independent so a user can connect steps without granting weight access.
 */
enum class HealthConnectFeature {
    STEPS,
    WEIGHT
}

object HealthConnectPermissionPolicy {
    val stepsRead: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class)
    )

    val weightRead: Set<String> = setOf(
        HealthPermission.getReadPermission(WeightRecord::class)
    )

    fun permissionsFor(feature: HealthConnectFeature): Set<String> = when (feature) {
        HealthConnectFeature.STEPS -> stepsRead
        HealthConnectFeature.WEIGHT -> weightRead
    }

    val allReadOnly: Set<String> = stepsRead + weightRead
}
