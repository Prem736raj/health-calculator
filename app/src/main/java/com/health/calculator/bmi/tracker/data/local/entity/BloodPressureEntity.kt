package com.health.calculator.bmi.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_pressure_readings")
data class BloodPressureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int? = null,
    val category: String,
    val riskLevel: String,
    val arm: String? = null,
    val position: String? = null,
    val timeOfDay: String? = null,
    val measurementTimestamp: Long,
    val note: String = "",
    val isPartOfAverage: Boolean = false,
    val averageGroupId: String? = null,
    val isAveragedResult: Boolean = false,
    val readingsInAverage: Int = 1,
    val pulsePressure: Int,
    val meanArterialPressure: Double,
    val onMedication: Boolean = false,
    val medicationName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
