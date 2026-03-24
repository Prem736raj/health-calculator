// ui/screens/waterintake/WaterIntakeViewModelFactory.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.health.calculator.bmi.tracker.data.repository.WaterIntakeRepository

class WaterIntakeViewModelFactory(
    private val application: Application,
    private val repository: WaterIntakeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterIntakeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterIntakeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
