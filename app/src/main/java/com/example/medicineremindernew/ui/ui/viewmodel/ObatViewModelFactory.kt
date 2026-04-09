package com.example.medicineremindernew.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicineremindernew.ui.data.repository.ObatRepository

class ObatViewModelFactory(
    private val repository: ObatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ObatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ObatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
