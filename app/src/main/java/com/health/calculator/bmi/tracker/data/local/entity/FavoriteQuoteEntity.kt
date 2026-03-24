package com.health.calculator.bmi.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_quotes")
data class FavoriteQuoteEntity(
    @PrimaryKey
    val quoteId: Int,
    val quote: String,
    val author: String,
    val savedAt: Long = System.currentTimeMillis()
)
