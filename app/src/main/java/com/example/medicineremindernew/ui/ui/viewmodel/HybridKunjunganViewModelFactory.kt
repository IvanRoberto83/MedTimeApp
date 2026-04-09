package com.example.medicineremindernew.ui.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicineremindernew.ui.data.local.LocalDatabase
import com.example.medicineremindernew.ui.data.network.NetworkUtils
import com.example.medicineremindernew.ui.data.repository.FirestoreRepository
import com.example.medicineremindernew.ui.data.repository.HybridKunjunganRepository
import com.example.medicineremindernew.ui.data.repository.KunjunganRepository

class HybridKunjunganViewModelFactory(
    private val context: Context,
    private val firestoreRepository: FirestoreRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HybridKunjunganViewModel::class.java)) {
            val dao = LocalDatabase.getDatabase(context).kunjunganDao()
            val firestoreRepo = KunjunganRepository(firestoreRepository)
            val networkUtils = NetworkUtils(context)

            val repository = HybridKunjunganRepository(
                kunjunganRepository = firestoreRepo,
                localDao = dao,
                networkUtils = networkUtils,
                context = context
            )
            return HybridKunjunganViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

