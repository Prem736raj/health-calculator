package com.health.calculator.bmi.tracker.data.model




data class BSARecord(
    val timestamp: Long,
    val dateTime: String,
    val bsaValue: Float,
    val formulaId: String,
    val formulaName: String,
    val weightKg: Float,
    val heightCm: Float
)

data class BSAStatistics(
    val totalReadings: Int,
    val currentBSA: Float,
    val averageBSA: Float,
    val firstBSA: Float,
    val changeFromFirst: Float,
    val changePercent: Float,
    val mostUsedFormula: String
)
