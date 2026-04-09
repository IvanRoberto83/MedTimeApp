package com.example.medicineremindernew.ui.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicineremindernew.ui.data.model.Lansia
import com.example.medicineremindernew.ui.data.repository.LansiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LansiaViewModel(private val repository: LansiaRepository) : ViewModel() {

    private val _lansiaList = MutableStateFlow<List<Lansia>>(emptyList())
    val lansiaList: StateFlow<List<Lansia>> = _lansiaList

    private val _lansiaDetail = MutableStateFlow<Lansia?>(null)
    val lansiaDetail: StateFlow<Lansia?> = _lansiaDetail

    init {
        repository.getAllLansia { lansiaList ->
            _lansiaList.value = lansiaList
        }
    }


    fun loadLansia() {
        repository.getAllLansia { list ->
            _lansiaList.value = list
        }
    }

    fun getLansiaById(id: String) {
        viewModelScope.launch {
            _lansiaDetail.value = repository.getLansiaById(id)
        }
    }

    fun updateLansia(lansia: Lansia, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.updateLansiaSuspend(lansia)
            onResult(success)
        }
    }

    fun addLansia(lansia: Lansia, onResult: (Boolean) -> Unit) {
        repository.addLansia(lansia, onResult)
    }

    fun deleteLansia(id: String) {
        viewModelScope.launch {
            repository.deleteLansia(id)
        }
    }

}
