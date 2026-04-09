package com.example.medicineremindernew.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicineremindernew.ui.data.model.Obat
import com.example.medicineremindernew.ui.data.repository.ObatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ObatViewModel(private val repository: ObatRepository) : ViewModel() {

    private val _obatList = MutableStateFlow<List<Obat>>(emptyList())
    val obatList: StateFlow<List<Obat>> = _obatList

    private val _obatDetail = MutableStateFlow<Obat?>(null)
    val obatDetail: StateFlow<Obat?> = _obatDetail

    init {
        repository.getAllObat { obatList ->
            _obatList.value = obatList
        }
    }


    fun addObat(obat: Obat, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.addObat(obat) { success ->
                onResult(success)
            }
        }
    }

    fun updateObat(obat: Obat, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.updateObat(obat)
            onResult(success)
        }
    }

    fun getObatById(id: String) {
        viewModelScope.launch {
            _obatDetail.value = repository.getObatById(id)
        }
    }

    fun deleteObat(id: String) {
        viewModelScope.launch {
            repository.deleteObat(id)
        }
    }
}
