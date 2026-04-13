// data/repository/WaterIntakeRepository.kt
package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.dao.WaterIntakeDao
import com.health.calculator.bmi.tracker.data.model.WaterIntakeCalculation
import com.health.calculator.bmi.tracker.data.model.WaterIntakeLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class WaterIntakeRepository(private val waterIntakeDao: WaterIntakeDao) {

    // Calculations
    suspend fun saveCalculation(calculation: WaterIntakeCalculation): Long {
        return waterIntakeDao.insertCalculation(calculation)
    }

    fun getAllCalculations(): Flow<List<WaterIntakeCalculation>> {
        return waterIntakeDao.getAllCalculations()
    }

    suspend fun getLatestCalculation(): WaterIntakeCalculation? {
        return waterIntakeDao.getLatestCalculation()
    }

    suspend fun deleteCalculation(calculation: WaterIntakeCalculation) {
        waterIntakeDao.deleteCalculation(calculation)
    }

    // Water Logs
    suspend fun logWater(amountMl: Int, note: String = ""): Long {
        return waterIntakeDao.insertWaterLog(
            WaterIntakeLog(amountMl = amountMl, note = note)
        )
    }

    fun getWaterLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<WaterIntakeLog>> {
        return waterIntakeDao.getWaterLogsForDay(startOfDay, endOfDay)
    }

    fun getTotalWaterForDay(startOfDay: Long, endOfDay: Long): Flow<Int?> {
        // Defensive: during development DB version/table changes can temporarily
        // produce "no such table" crashes. Treat that as "no water logged".
        return waterIntakeDao.getTotalWaterForDay(startOfDay, endOfDay)
            .catch { emit(null) }
    }

    suspend fun deleteWaterLog(log: WaterIntakeLog) {
        waterIntakeDao.deleteWaterLog(log)
    }

    suspend fun deleteAllWaterLogs() {
        waterIntakeDao.deleteAllWaterLogs()
    }

    suspend fun getWaterLogById(id: Long): WaterIntakeLog? {
        return waterIntakeDao.getWaterLogById(id)
    }

    fun getAllWaterLogs(): Flow<List<WaterIntakeLog>> {
        return waterIntakeDao.getAllWaterLogs()
    }

    fun getWaterLogsBetween(startTime: Long, endTime: Long): Flow<List<WaterIntakeLog>> {
        return waterIntakeDao.getWaterLogsBetween(startTime, endTime)
    }

    suspend fun getTotalWaterBetweenSync(startTime: Long, endTime: Long): Int? {
        return waterIntakeDao.getTotalWaterBetweenSync(startTime, endTime)
    }

    suspend fun getDaysGoalMetInRange(startMillis: Long, endMillis: Long): Int {
        val goal = getLatestCalculation()?.recommendedIntakeMl ?: 2500
        var daysMet = 0
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = startMillis
        
        while (cal.timeInMillis <= endMillis) {
            val dayStart = cal.timeInMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            cal.set(java.util.Calendar.MINUTE, 59)
            val dayEnd = cal.timeInMillis
            
            val total = getTotalWaterBetweenSync(dayStart, dayEnd) ?: 0
            if (total >= goal) daysMet++
            
            cal.timeInMillis = dayStart
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return daysMet
    }

    suspend fun getDaysTrackedInRange(startMillis: Long, endMillis: Long): Int {
        var daysTracked = 0
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = startMillis
        
        while (cal.timeInMillis <= endMillis) {
            val dayStart = cal.timeInMillis
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            cal.set(java.util.Calendar.MINUTE, 59)
            val dayEnd = cal.timeInMillis
            
            val total = getTotalWaterBetweenSync(dayStart, dayEnd) ?: 0
            if (total > 0) daysTracked++
            
            cal.timeInMillis = dayStart
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return daysTracked
    }

    suspend fun getAverageIntakeInRange(startMillis: Long, endMillis: Long): Int {
        val total = getTotalWaterBetweenSync(startMillis, endMillis) ?: 0
        val days = ((endMillis - startMillis) / (24 * 60 * 60 * 1000)).toInt() + 1
        return total / days.coerceAtLeast(1)
    }

    fun getDailyGoal(): kotlinx.coroutines.flow.Flow<Int> = kotlinx.coroutines.flow.flow {
        emit(getLatestCalculation()?.recommendedIntakeMl ?: 2500)
    }
}
