package com.health.calculator.bmi.tracker.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.local.AppDatabase
import com.health.calculator.bmi.tracker.data.model.DailyContent
import com.health.calculator.bmi.tracker.data.model.MotivationalQuote
import com.health.calculator.bmi.tracker.data.repository.DailyContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyContentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = DailyContentRepository(database.favoriteQuoteDao())
    
    val dailyContent: StateFlow<DailyContent?> = repository.dailyContent
    
    val favoriteQuoteIds: StateFlow<List<Int>> = repository.favoriteQuoteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        repository.refreshDailyContent()
    }

    fun toggleFavorite(quote: MotivationalQuote) {
        viewModelScope.launch {
            repository.toggleFavorite(quote)
        }
    }
    
    fun refresh() {
        repository.refreshDailyContent()
    }
}
