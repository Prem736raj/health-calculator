package com.health.calculator.bmi.tracker.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.health.calculator.bmi.tracker.data.local.SearchPreferences
import com.health.calculator.bmi.tracker.data.model.QuickAction
import com.health.calculator.bmi.tracker.data.model.SearchResult
import com.health.calculator.bmi.tracker.data.repository.QuickActionRepository
import com.health.calculator.bmi.tracker.data.repository.SearchRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val searchPrefs = SearchPreferences(application)
    private val searchRepository = SearchRepository()
    private val quickActionRepository = QuickActionRepository(searchPrefs)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val searchResults: StateFlow<List<SearchResult>> = _searchQuery
        .debounce(300)
        .map { query ->
            if (query.isBlank()) emptyList()
            else searchRepository.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<String>> = searchPrefs.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quickActions: StateFlow<List<QuickAction>> = quickActionRepository.getQuickActions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onSearchResultClick(result: SearchResult) {
        viewModelScope.launch {
            searchPrefs.addRecentSearch(result.title)
            // Track usage if it's a calculator or feature
            val featureKey = when (result.id) {
                "bmi" -> "bmi_calc"
                "bmr" -> "bmr_calc"
                "bp" -> "bp_check"
                "water", "log_water" -> "water_log"
                "calories" -> "calorie_log"
                "whr" -> "whr_calc"
                "hr" -> "hr_calc"
                "ibw" -> "ibw_calc"
                "bsa" -> "bsa_calc"
                "met" -> "met_calc"
                else -> null
            }
            featureKey?.let { quickActionRepository.trackUsage(it) }
        }
        _searchQuery.value = ""
    }

    fun onQuickActionClick(action: QuickAction) {
        viewModelScope.launch {
            val featureKey = when (action.id) {
                "quick_water" -> "water_log"
                "quick_bp" -> "bp_check"
                "quick_calorie" -> "calorie_log"
                "quick_bmi" -> "quick_bmi"
                "quick_bmr" -> "bmr_calc"
                "quick_whr" -> "whr_calc"
                "quick_hr" -> "hr_calc"
                else -> null
            }
            featureKey?.let { quickActionRepository.trackUsage(it) }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            searchPrefs.clearRecentSearches()
        }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            searchPrefs.removeRecentSearch(query)
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Simulate refresh delay
            kotlinx.coroutines.delay(1000)
            _isRefreshing.value = false
        }
    }
}
