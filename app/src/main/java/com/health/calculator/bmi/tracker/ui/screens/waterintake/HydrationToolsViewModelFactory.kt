// ui/screens/waterintake/HydrationToolsViewModelFactory.kt
package com.health.calculator.bmi.tracker.ui.screens.waterintake

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.health.calculator.bmi.tracker.data.dao.UrineColorDao

class HydrationToolsViewModelFactory(
    private val application: Application,
    private val urineColorDao: UrineColorDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HydrationToolsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HydrationToolsViewModel(application, urineColorDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
