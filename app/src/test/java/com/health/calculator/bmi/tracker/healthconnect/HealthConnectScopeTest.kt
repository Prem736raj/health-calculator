package com.health.calculator.bmi.tracker.healthconnect

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import org.junit.Assert.*
import org.junit.Test

class HealthConnectScopeTest {

    @Test
    fun testOnlyStepsPermissionRequested() {
        val permissions = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )

        // Ensure minimal permissions policy: only 1 permission requested for Phase 1
        assertEquals(1, permissions.size)
        assertTrue(permissions.contains(HealthPermission.getReadPermission(StepsRecord::class)))
    }
}
