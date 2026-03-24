// ui/screens/waterintake/WaterHistoryViewModelFactory.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository

class WaterHistoryViewModelFactory(
    private val application: Application,
    private val repository: WaterIntakeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterHistoryViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
