// ui/screens/waterintake/WaterTrackingViewModelFactory.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository

class WaterTrackingViewModelFactory(
    private val application: Application,
    private val repository: WaterIntakeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterTrackingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterTrackingViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
