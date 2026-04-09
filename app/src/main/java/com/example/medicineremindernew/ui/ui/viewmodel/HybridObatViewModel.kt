package com.example.medicineremindernew.ui.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicineremindernew.ui.data.model.Obat
import com.example.medicineremindernew.ui.data.repository.HybridObatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HybridObatViewModel(
    private val hybridRepository: HybridObatRepository
) : ViewModel() {

    private val _obatList = MutableStateFlow<List<Obat>>(emptyList())
    val obatList: StateFlow<List<Obat>> = _obatList

    private val _obatDetail = MutableStateFlow<Obat?>(null)
    val obatDetail: StateFlow<Obat?> = _obatDetail

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    enum class SortCriteria {
        NAME_ASC,
        NAME_DESC,
        DATE_ASC,
        DATE_DESC,
        STOCK_ASC,
        STOCK_DESC
    }

    private val _currentSortCriteria = MutableStateFlow(SortCriteria.NAME_ASC)
    val currentSortCriteria: StateFlow<SortCriteria> = _currentSortCriteria.asStateFlow()

    init {
        loadObat()
    }

    private fun sortObat(obatList: List<Obat>, criteria: SortCriteria): List<Obat> {
        return when (criteria) {
            SortCriteria.NAME_ASC -> obatList.sortedBy { it.nama.lowercase() }
            SortCriteria.NAME_DESC -> obatList.sortedByDescending { it.nama.lowercase() }
            SortCriteria.DATE_ASC -> obatList.sortedBy { it.pertamaKonsumsi?.toDate() }
            SortCriteria.DATE_DESC -> obatList.sortedByDescending { it.pertamaKonsumsi?.toDate() }
            SortCriteria.STOCK_ASC -> obatList.sortedBy { it.stok ?: 0 }
            SortCriteria.STOCK_DESC -> obatList.sortedByDescending { it.stok ?: 0 }
        }
    }

    fun loadObat() {
        viewModelScope.launch {
            val rawList = hybridRepository.getAllObat()
            val sortedList = sortObat(rawList, _currentSortCriteria.value)
            _obatList.value = sortedList
        }
    }

    fun setSortCriteria(criteria: SortCriteria) {
        _currentSortCriteria.value = criteria
        refreshObatWithCurrentSort()
    }

    private fun refreshObatWithCurrentSort() {
        viewModelScope.launch {
            try {
                val rawList = hybridRepository.getAllObat()
                val sortedList = sortObat(rawList, _currentSortCriteria.value)
                _obatList.value = sortedList
            } catch (e: Exception) {
                _error.value = e.message ?: "Error refreshing sorted obat"
                Log.e("HybridObatViewModel", "Error refreshing sorted obat: ${e.message}")
            }
        }
    }

    fun addObat(obat: Obat, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = hybridRepository.addObat(obat)
            loadObat()
            onResult(success)
        }
    }

    fun getObatById(id: String) {
        viewModelScope.launch {
            _obatDetail.value = hybridRepository.getAllObat().find { it.id == id }
        }
    }

    fun updateObat(obat: Obat, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = hybridRepository.updateObat(obat)
            if (success) loadObat()
            onResult(success)
        }
    }

    fun deleteObat(id: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = hybridRepository.deleteObat(id)
            if (success) loadObat()
            onResult(success)
        }
    }

    private fun observeObat() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val list = hybridRepository.getAllObat()
                val distinctList = list.distinctBy { it.id }
                val sortedList = sortObat(distinctList, _currentSortCriteria.value)
                _obatList.value = sortedList
            } catch (e: Exception) {
                _error.value = e.message ?: "Error fetching obat"
                Log.e("HybridVM", "observeObat error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun syncPendingData() {
        viewModelScope.launch {
            try {
                hybridRepository.syncPendingData()
                observeObat()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Sync failed"
            }
        }
    }
}
