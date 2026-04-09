package com.example.medicineremindernew.ui.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicineremindernew.ui.data.model.Riwayat
import com.example.medicineremindernew.ui.data.repository.HybridRiwayatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HybridRiwayatViewModel(
    private val repository: HybridRiwayatRepository
) : ViewModel() {

    private val _riwayatList = MutableStateFlow<List<Riwayat>>(emptyList())
    val riwayatList: StateFlow<List<Riwayat>> = _riwayatList.asStateFlow()

    private val _riwayatDetail = MutableStateFlow<Riwayat?>(null)
    val riwayatDetail: StateFlow<Riwayat?> = _riwayatDetail.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    enum class SortCriteria {
        TIMESTAMP_ASC,
        TIMESTAMP_DESC,
    }

    private val _currentSortCriteria = MutableStateFlow(SortCriteria.TIMESTAMP_DESC)
    val currentSortCriteria: StateFlow<SortCriteria> = _currentSortCriteria.asStateFlow()

    val sortedRiwayatList: StateFlow<List<Riwayat>> = combine(
        _riwayatList,
        _currentSortCriteria
    ) { riwayatList, sortCriteria ->
        sortRiwayat(riwayatList, sortCriteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        observeRiwayat()
    }

    private fun sortRiwayat(riwayatList: List<Riwayat>, criteria: SortCriteria): List<Riwayat> {
        return when (criteria) {
            SortCriteria.TIMESTAMP_ASC -> {
                riwayatList.sortedBy { riwayat ->
                    getTimestampFromRiwayat(riwayat)
                }
            }
            SortCriteria.TIMESTAMP_DESC -> {
                riwayatList.sortedByDescending { riwayat ->
                    getTimestampFromRiwayat(riwayat)
                }
            }
        }
    }

    private fun getTimestampFromRiwayat(riwayat: Riwayat): Long {
        return try {
            val tanggalString = riwayat.tanggal
            val waktuString = riwayat.waktu

            if (tanggalString.isNotEmpty() && waktuString.isNotEmpty()) {
                val dateTimeString = "$tanggalString $waktuString"
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val dateTime = sdf.parse(dateTimeString)
                dateTime?.time ?: 0L
            } else if (tanggalString.isNotEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(tanggalString)
                date?.time ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e("HybridRiwayatVM", "Error parsing timestamp for riwayat ${riwayat.idRiwayat}", e)
            0L
        }
    }

    private fun observeRiwayat() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val list = repository.getAllRiwayatOnce()
                _riwayatList.value = list.distinctBy { it.idRiwayat }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error fetching riwayat"
                Log.e("HybridVM", "observeRiwayat error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun getRiwayatById(id: String) {
        val found = _riwayatList.value.find { it.idRiwayat == id }
        _riwayatDetail.value = found
    }

    fun addRiwayat(riwayat: Riwayat, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val success = repository.addRiwayat(riwayat)
                if (success) observeRiwayat()
                callback(success)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Add failed"
                callback(false)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateRiwayat(riwayat: Riwayat, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val success = repository.updateRiwayat(riwayat)
                if (success) {
                    observeRiwayat()
                    _riwayatDetail.value = riwayat
                }
                callback(success)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Update failed"
                callback(false)
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteRiwayat(id: String, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val success = repository.deleteRiwayat(id)
                if (success) {
                    observeRiwayat()
                    _riwayatDetail.value = null
                }
                callback(success)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Delete failed"
                callback(false)
            } finally {
                _loading.value = false
            }
        }
    }

    fun syncPendingData() {
        viewModelScope.launch {
            try {
                repository.syncPendingData()
                observeRiwayat()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Sync failed"
            }
        }
    }
}
