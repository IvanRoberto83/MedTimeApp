package com.example.medicineremindernew.ui.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicineremindernew.ui.data.model.Lansia
import com.example.medicineremindernew.ui.data.repository.HybridLansiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HybridLansiaViewModel(
    private val hybridRepository: HybridLansiaRepository
) : ViewModel() {

    private val _lansiaList = MutableStateFlow<List<Lansia>>(emptyList())
    val lansiaList: StateFlow<List<Lansia>> = _lansiaList

    private val _lansiaDetail = MutableStateFlow<Lansia?>(null)
    val lansiaDetail: StateFlow<Lansia?> = _lansiaDetail

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    enum class SortCriteria {
        NAME_ASC,
        NAME_DESC
    }

    private val _currentSortCriteria = MutableStateFlow(SortCriteria.NAME_ASC)
    val currentSortCriteria: StateFlow<SortCriteria> = _currentSortCriteria.asStateFlow()


    init {
        loadLansia()
    }

    private fun sortLansia(lansiaList: List<Lansia>, criteria: SortCriteria): List<Lansia> {
        return when (criteria) {
            SortCriteria.NAME_ASC -> lansiaList.sortedBy { it.nama.lowercase() }
            SortCriteria.NAME_DESC -> lansiaList.sortedByDescending { it.nama.lowercase() }
        }
    }

    fun loadLansia() {
        viewModelScope.launch {
            val rawList = hybridRepository.getAllLansia()
            val sortedList = sortLansia(rawList, _currentSortCriteria.value)
            _lansiaList.value = sortedList
        }
    }

    fun addLansia(lansia: Lansia, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = hybridRepository.addLansia(lansia)
            loadLansia()
            onResult(success)
        }
    }

    fun getLansiaById(id: String) {
        viewModelScope.launch {
            _lansiaDetail.value = hybridRepository.getAllLansia().find { it.id == id }
        }
    }

    fun updateLansia(lansia: Lansia, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = hybridRepository.updateLansia(lansia)
            if (success) loadLansia()
            onResult(success)
        }
    }

    fun deleteLansia(id: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = hybridRepository.deleteLansia(id)
            if (success) loadLansia()
            onResult(success)
        }
    }

    private fun observeLansia() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val list = hybridRepository.getAllLansia()
                val distinctList = list.distinctBy { it.id }
                val sortedList = sortLansia(distinctList, _currentSortCriteria.value)
                _lansiaList.value = sortedList
            } catch (e: Exception) {
                _error.value = e.message ?: "Error fetching lansia"
                Log.e("HybridVM", "observeLansia error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun syncPendingData() {
        viewModelScope.launch {
            try {
                hybridRepository.syncPendingData()
                observeLansia()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Sync failed"
            }
        }
    }
}
