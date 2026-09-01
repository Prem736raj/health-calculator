package com.health.calculator.bmi.tracker.healthconnect

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import com.health.calculator.bmi.tracker.data.healthconnect.HealthConnectFeature
import com.health.calculator.bmi.tracker.data.healthconnect.HealthConnectPermissionPolicy
import org.junit.Assert.*
import org.junit.Test

class HealthConnectScopeTest {

    @Test
    fun testOnlyStepsPermissionRequested() {
        val permissions = HealthConnectPermissionPolicy.permissionsFor(HealthConnectFeature.STEPS)

        // Steps stays a one-permission feature and does not ask for weight access.
        assertEquals(1, permissions.size)
        assertTrue(permissions.contains(HealthPermission.getReadPermission(StepsRecord::class)))
        assertTrue(permissions.none { it.contains("WEIGHT") })
    }

    @Test
    fun weightPermissionIsSeparateAndReadOnly() {
        val permissions = HealthConnectPermissionPolicy.permissionsFor(HealthConnectFeature.WEIGHT)

        assertEquals(setOf(HealthPermission.getReadPermission(WeightRecord::class)), permissions)
        assertTrue(HealthConnectPermissionPolicy.allReadOnly.none { it.contains("WRITE") })
        assertTrue(HealthConnectPermissionPolicy.allReadOnly.containsAll(permissions))
    }
}
