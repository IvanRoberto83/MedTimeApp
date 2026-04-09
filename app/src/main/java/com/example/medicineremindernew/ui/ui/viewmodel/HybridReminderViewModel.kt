package com.example.medicineremindernew.ui.ui.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicineremindernew.ui.alarm.scheduleAlarm
import com.example.medicineremindernew.ui.alarm.cancelAlarm as cancelAlarmScheduler
import com.example.medicineremindernew.ui.data.model.Reminder
import com.example.medicineremindernew.ui.data.repository.HybridReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HybridReminderViewModel(
    private val hybridRepository: HybridReminderRepository
) : ViewModel() {

    private val _reminderList = MutableStateFlow<List<Reminder>>(emptyList())
    val reminderList: StateFlow<List<Reminder>> = _reminderList

    private val _reminderDetail = MutableStateFlow<Reminder?>(null)
    val reminderDetail: StateFlow<Reminder?> = _reminderDetail

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            _reminderList.value = hybridRepository.getAllReminders()
        }
    }

    fun addReminder(reminder: Reminder, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = hybridRepository.addReminder(reminder)
            loadReminders()
            onResult(success)
        }
    }

    fun getReminderById(id: String) {
        viewModelScope.launch {
            _reminderDetail.value = hybridRepository.getAllReminders().find { it.id == id }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateReminder(reminder: Reminder, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("HybridReminderViewModel", "Updating reminder ${reminder.id} dengan waktu ${reminder.tanggal} ${reminder.waktu}")

                val success = hybridRepository.updateReminder(reminder)

                if (success) {
                    loadReminders()
                    Log.d("HybridReminderViewModel", "Reminder ${reminder.id} berhasil diupdate")
                } else {
                    Log.e("HybridReminderViewModel", "Gagal update reminder ${reminder.id}")
                }

                onResult(success)
            } catch (e: Exception) {
                Log.e("HybridReminderViewModel", "Error updating reminder: ${e.message}", e)
                onResult(false)
            }
        }
    }

    fun deleteReminder(id: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = hybridRepository.deleteReminder(id)
            if (success) loadReminders()
            onResult(success)
        }
    }

    private fun observeReminder() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val list = hybridRepository.getAllReminders()
                _reminderList.value = list.distinctBy { it.id }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error fetching kunjungan"
                Log.e("HybridVM", "observeKunjungan error", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun syncPendingData() {
        viewModelScope.launch {
            try {
                hybridRepository.syncPendingData()
                observeReminder()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Sync failed"
            }
        }
    }
}
