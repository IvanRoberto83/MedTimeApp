package com.example.medicineremindernew.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicineremindernew.ui.data.repository.HybridObatRepository

class HybridObatViewModelFactory(
    private val repository: HybridObatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HybridObatViewModel(repository) as T
    }
}