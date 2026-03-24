package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.local.dao.BloodPressureDao
import com.health.calculator.bmi.tracker.data.local.entity.BloodPressureEntity
import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class BloodPressureRepository(private val dao: BloodPressureDao) {

    val allMainReadings: Flow<List<BloodPressureEntity>> = dao.getMainReadings()
    val allReadings: Flow<List<BloodPressureEntity>> = dao.getAllReadings()
    val readingsCount: Flow<Int> = dao.getReadingsCount()

    suspend fun saveReading(
        reading: BloodPressureReading,
        note: String = "",
        isPartOfAverage: Boolean = false,
        averageGroupId: String? = null,
        isAveragedResult: Boolean = false,
        readingsInAverage: Int = 1,
        onMedication: Boolean = false,
        medicationName: String = ""
    ): Long {
        val entity = BloodPressureEntity(
            systolic = reading.systolic,
            diastolic = reading.diastolic,
            pulse = reading.pulse,
            category = reading.category.name,
            riskLevel = reading.riskLevel.name,
            arm = reading.arm?.name,
            position = reading.position?.name,
            timeOfDay = reading.timeOfDay?.name,
            measurementTimestamp = reading.measurementTime
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            note = note,
            isPartOfAverage = isPartOfAverage,
            averageGroupId = averageGroupId,
            isAveragedResult = isAveragedResult,
            readingsInAverage = readingsInAverage,
            pulsePressure = reading.pulsePressure,
            meanArterialPressure = reading.meanArterialPressure,
            onMedication = onMedication,
            medicationName = medicationName
        )
        return dao.insertReading(entity)
    }

    suspend fun saveAveragedReadings(
        individualReadings: List<BloodPressureReading>,
        averagedReading: BloodPressureReading,
        note: String = ""
    ) {
        val groupId = UUID.randomUUID().toString()

        // Save individual readings as part of average
        individualReadings.forEach { reading ->
            saveReading(
                reading = reading,
                note = "",
                isPartOfAverage = true,
                averageGroupId = groupId
            )
        }

        // Save the averaged result
        saveReading(
            reading = averagedReading,
            note = note.ifEmpty { "Average of ${individualReadings.size} readings" },
            isPartOfAverage = false,
            averageGroupId = groupId,
            isAveragedResult = true,
            readingsInAverage = individualReadings.size
        )
    }

    suspend fun getReadingById(id: Long): BloodPressureEntity? = dao.getReadingById(id)

    suspend fun getReadingsByGroupId(groupId: String): List<BloodPressureEntity> =
        dao.getReadingsByGroupId(groupId)

    suspend fun getLatestReading(): BloodPressureEntity? = dao.getLatestReading()

    suspend fun getRecentReadings(limit: Int): List<BloodPressureEntity> =
        dao.getRecentReadings(limit)

    suspend fun deleteReading(id: Long) = dao.deleteReadingById(id)

    suspend fun deleteReadingWithGroup(entity: BloodPressureEntity) {
        entity.averageGroupId?.let { groupId ->
            dao.deleteReadingsByGroupId(groupId)
        }
        dao.deleteReading(entity)
    }

    suspend fun updateNote(id: Long, note: String) = dao.updateNote(id, note)

    suspend fun deleteAll() = dao.deleteAll()

    companion object {
        fun entityToReading(entity: BloodPressureEntity): BloodPressureReading {
            val category = try {
                BpCategory.valueOf(entity.category)
            } catch (e: Exception) {
                BpCategory.OPTIMAL
            }
            val riskLevel = try {
                BpRiskLevel.valueOf(entity.riskLevel)
            } catch (e: Exception) {
                BpRiskLevel.LOW
            }

            return BloodPressureReading(
                systolic = entity.systolic,
                diastolic = entity.diastolic,
                pulse = entity.pulse,
                arm = entity.arm?.let {
                    try { BpArm.valueOf(it) } catch (e: Exception) { null }
                },
                position = entity.position?.let {
                    try { BpPosition.valueOf(it) } catch (e: Exception) { null }
                },
                timeOfDay = entity.timeOfDay?.let {
                    try { BpTimeOfDay.valueOf(it) } catch (e: Exception) { null }
                },
                measurementTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(entity.measurementTimestamp),
                    ZoneId.systemDefault()
                ),
                category = category,
                riskLevel = riskLevel
            )
        }
    }
}
