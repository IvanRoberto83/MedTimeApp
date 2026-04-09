package com.example.medicineremindernew.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicineremindernew.ui.data.repository.HybridLansiaRepository

class HybridLansiaViewModelFactory(
    private val repository: HybridLansiaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HybridLansiaViewModel::class.java)) {
            return HybridLansiaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

}