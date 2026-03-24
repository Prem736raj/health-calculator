package com.health.calculator.bmi.tracker.data.repository

import com.health.calculator.bmi.tracker.data.local.DailyContentData
import com.health.calculator.bmi.tracker.data.local.dao.FavoriteQuoteDao
import com.health.calculator.bmi.tracker.data.local.entity.FavoriteQuoteEntity
import com.health.calculator.bmi.tracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class DailyContentRepository(
    private val favoriteQuoteDao: FavoriteQuoteDao
) {
    private val _dailyContent = MutableStateFlow<DailyContent?>(null)
    val dailyContent: StateFlow<DailyContent?> = _dailyContent.asStateFlow()

    fun refreshDailyContent() {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        
        // Seed based on date to ensure same content for all users on same day
        val seed = (year * 1000 + dayOfYear).toLong()
        val tipIdx = (seed % DailyContentData.healthTips.size).toInt()
        val quoteIdx = (seed % DailyContentData.motivationalQuotes.size).toInt()

        _dailyContent.value = DailyContent(
            tip = DailyContentData.healthTips[tipIdx],
            quote = DailyContentData.motivationalQuotes[quoteIdx],
            date = calendar.timeInMillis
        )
    }

    fun getFavoriteQuotes(): Flow<List<FavoriteQuoteEntity>> = favoriteQuoteDao.getAllFavorites()

    fun isQuoteFavorite(quoteId: Int): Flow<Boolean> = kotlinx.coroutines.flow.flow {
        // Since isFavorite is a suspend function, we emit from a flow
        // Or we could observe getFavoriteIds and map it
    }
    
    // Better way to observe favorited IDs
    val favoriteQuoteIds: Flow<List<Int>> = favoriteQuoteDao.getFavoriteIds()

    suspend fun toggleFavorite(quote: MotivationalQuote) {
        if (favoriteQuoteDao.isFavorite(quote.id)) {
            favoriteQuoteDao.removeFavorite(quote.id)
        } else {
            favoriteQuoteDao.addFavorite(
                FavoriteQuoteEntity(
                    quoteId = quote.id,
                    quote = quote.quote,
                    author = quote.author
                )
            )
        }
    }
}
