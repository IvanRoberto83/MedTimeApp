package com.example.medicineremindernew.ui.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicineremindernew.ui.data.model.Kunjungan
import com.example.medicineremindernew.ui.data.model.Obat
import com.example.medicineremindernew.ui.data.repository.HybridKunjunganRepository
import com.example.medicineremindernew.ui.ui.viewmodel.HybridObatViewModel.SortCriteria
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HybridKunjunganViewModel(
    private val repository: HybridKunjunganRepository
) : ViewModel() {

    private val _kunjunganList = MutableStateFlow<List<Kunjungan>>(emptyList())
    val kunjunganList: StateFlow<List<Kunjungan>> = _kunjunganList.asStateFlow()

    private val _kunjunganDetail = MutableStateFlow<Kunjungan?>(null)
    val kunjunganDetail: StateFlow<Kunjungan?> = _kunjunganDetail.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    enum class SortCriteria {
        TIMESTAMP_ASC,
        TIMESTAMP_DESC,
        DATE_ASC,
        DATE_DESC
    }

    private val _currentSortCriteria = MutableStateFlow(SortCriteria.TIMESTAMP_DESC)
    val currentSortCriteria: StateFlow<SortCriteria> = _currentSortCriteria.asStateFlow()

    val sortedKunjunganList: StateFlow<List<Kunjungan>> = combine(
        _kunjunganList,
        _currentSortCriteria
    ) { kunjunganList, sortCriteria ->
        sortKunjungan(kunjunganList, sortCriteria)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        observeKunjungan()
    }

    private fun sortKunjungan(kunjunganList: List<Kunjungan>, criteria: SortCriteria): List<Kunjungan> {
        return when (criteria) {
            SortCriteria.TIMESTAMP_ASC -> {
                kunjunganList.sortedBy { kunjungan ->
                    getTimestampFromKunjungan(kunjungan)
                }
            }
            SortCriteria.TIMESTAMP_DESC -> {
                kunjunganList.sortedByDescending { kunjungan ->
                    getTimestampFromKunjungan(kunjungan)
                }
            }
            SortCriteria.DATE_ASC -> {
                kunjunganList.sortedBy { kunjungan ->
                    if (kunjungan.tanggal.isNotEmpty()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.parse(kunjungan.tanggal)?.time ?: 0L
                    } else {
                        0L
                    }
                }
            }
            SortCriteria.DATE_DESC -> {
                kunjunganList.sortedByDescending { kunjungan ->
                    if (kunjungan.tanggal.isNotEmpty()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.parse(kunjungan.tanggal)?.time ?: 0L
                    } else {
                        0L
                    }
                }
            }
        }
    }

    private fun getTimestampFromKunjungan(kunjungan: Kunjungan): Long {
        return try {
            val tanggalString = kunjungan.tanggal
            val waktuString = kunjungan.waktu

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
            Log.e("HybridKunjunganVM", "Error parsing timestamp for kunjungan ${kunjungan.idKunjungan}", e)
            0L
        }
    }

    private fun observeKunjungan() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val list = repository.getAllKunjunganOnce()
                _kunjunganList.value = list.distinctBy { it.idKunjungan }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error fetching kunjungan"
                Log.e("HybridVM", "observeKunjungan error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun getKunjunganById(id: String) {
        val found = _kunjunganList.value.find { it.idKunjungan == id }
        _kunjunganDetail.value = found
    }

    fun addKunjungan(kunjungan: Kunjungan, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val success = repository.addKunjungan(kunjungan)
                if (success) observeKunjungan()
                callback(success)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Add failed"
                callback(false)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateKunjungan(kunjungan: Kunjungan, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val success = repository.updateKunjungan(kunjungan)
                if (success) {
                    observeKunjungan()
                    _kunjunganDetail.value = kunjungan
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

    fun deleteKunjungan(id: String, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val success = repository.deleteKunjungan(id)
                if (success) {
                    observeKunjungan()
                    _kunjunganDetail.value = null
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

    /**
     * 🔹 Sync dulu (biar data offline masuk Firestore), lalu reload dari Firestore
     */
    fun syncPendingData() {
        viewModelScope.launch {
            try {
                repository.syncPendingData()
                observeKunjungan()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Sync failed"
            }
        }
    }
}
