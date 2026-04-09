package com.example.medicineremindernew.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicineremindernew.ui.data.repository.HybridRiwayatRepository

class HybridRiwayatViewModelFactory(
    private val repository: HybridRiwayatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HybridRiwayatViewModel::class.java)) {
            return HybridRiwayatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

