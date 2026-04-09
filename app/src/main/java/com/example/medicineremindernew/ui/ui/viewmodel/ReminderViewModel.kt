package com.example.medicineremindernew.ui.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicineremindernew.ui.alarm.AlarmReceiver
import com.example.medicineremindernew.ui.alarm.cancelAlarm
import com.example.medicineremindernew.ui.data.model.Reminder
import com.example.medicineremindernew.ui.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    private val _reminderList = MutableStateFlow<List<Reminder>>(emptyList())
    val reminderList: StateFlow<List<Reminder>> = _reminderList

    private val _reminderDetail = MutableStateFlow<Reminder?>(null)
    val reminderDetail: StateFlow<Reminder?> = _reminderDetail

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            _reminderList.value = repository.getAllReminders()
        }
    }

    fun addReminder(reminder: Reminder, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addReminder(reminder)
                loadReminders()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun getReminderById(id: String) {
        viewModelScope.launch {
            _reminderDetail.value = repository.getReminderById(id)
        }
    }

    fun updateReminder(reminder: Reminder, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.updateReminder(reminder)
            onResult(success)
        }
    }

    fun deleteReminder(context: Context, id: String) {
        viewModelScope.launch {
            cancelAlarm(context, id)
            repository.deleteReminder(id)
            loadReminders()
            Log.d("ReminderViewModel", "Reminder $id dihapus beserta alarmnya")
        }
    }












}
