package com.example.medicineremindernew.ui.data.repository

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.medicineremindernew.ui.alarm.AlarmReceiver
import com.example.medicineremindernew.ui.alarm.cancelAlarm
import com.example.medicineremindernew.ui.alarm.scheduleAlarm
import com.example.medicineremindernew.ui.data.dao.LocalReminderDao
import com.example.medicineremindernew.ui.data.entity.LocalReminderEntity
import com.example.medicineremindernew.ui.data.model.Reminder
import com.example.medicineremindernew.ui.data.network.NetworkUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import kotlin.coroutines.resume

class HybridReminderRepository(
    private val reminderRepository: ReminderRepository,
    private val localDao: LocalReminderDao,
    private val networkUtils: NetworkUtils,
    private val context: Context
) {

    suspend fun addReminder(reminder: Reminder): Boolean {
        return try {
            val fixedReminder = if (reminder.id.isEmpty()) {
                reminder.copy(id = java.util.UUID.randomUUID().toString())
            } else {
                reminder
            }

            if (fixedReminder.lansiaIds.isEmpty()) {
                Log.e("HybridReminderRepo", "LansiaIds kosong! Reminder id=${fixedReminder.id}")
                return false
            }
            if (fixedReminder.obatIds.isEmpty()) {
                Log.e("HybridReminderRepo", "ObatIds kosong! Reminder id=${fixedReminder.id}")
                return false
            }

            if (networkUtils.isNetworkAvailable()) {
                reminderRepository.addReminder(fixedReminder)
                try {
                    localDao.insertReminder(fixedReminder.toLocalEntity(isSynced = true))
                } catch (e: Exception) {
                    Log.e("HybridReminderRepo", "Room insert failed: ${e.message}")
                }
            } else {
                try {
                    localDao.insertReminder(fixedReminder.toLocalEntity(isSynced = false))
                } catch (e: Exception) {
                    Log.e("HybridReminderRepo", "Room insert failed (offline): ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e("HybridReminderRepo", "Add failed: ${e.message}", e)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateReminder(reminder: Reminder): Boolean {
        return try {
            Log.d("HybridReminderRepo", "=== STARTING UPDATE FOR ${reminder.id} ===")
            Log.d("HybridReminderRepo", "New time: ${reminder.tanggal} ${reminder.waktu}")

            if (reminder.tanggal.isEmpty() || reminder.waktu.isEmpty()) {
                Log.e("HybridReminderRepo", "Tanggal atau Waktu kosong!")
                return false
            }

            try { cancelAlarm(context, reminder.id) } catch(e: Exception) { Log.e("HybridReminderRepo", "Cancel alarm failed: ${e.message}") }

            if (networkUtils.isNetworkAvailable()) {
                try { reminderRepository.updateReminder(reminder) } catch(e: Exception) { Log.e("HybridReminderRepo", "Firestore update failed: ${e.message}") }
                try { localDao.updateReminder(reminder.toLocalEntity(isSynced = true)) } catch(e: Exception) { Log.e("HybridReminderRepo", "Room update failed: ${e.message}") }
            } else {
                val localData = localDao.getAllReminders().find { it.id == reminder.id }
                if (localData != null) {
                    try { localDao.updateReminder(reminder.toLocalEntity(isSynced = false)) } catch(e: Exception) { Log.e("HybridReminderRepo", "Room update failed offline: ${e.message}") }
                }
            }

            try {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val localDateTime = java.time.LocalDateTime.parse("${reminder.tanggal} ${reminder.waktu}", formatter)
                val timeMillis = localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                if (timeMillis > System.currentTimeMillis()) {
                    scheduleAlarm(context, reminder.id, timeMillis)
                    Log.d("HybridReminderRepo", "✅ NEW ALARM SCHEDULED for ${Date(timeMillis)}")
                } else {
                    Log.d("HybridReminderRepo", "⚠️ ALARM TIME IN PAST - NOT SCHEDULED")
                }
            } catch(e: Exception) {
                Log.e("HybridReminderRepo", "Schedule alarm failed: ${e.message}")
            }

            Log.d("HybridReminderRepo", "=== UPDATE COMPLETED SUCCESSFULLY ===")
            true
        } catch (e: Exception) {
            Log.e("HybridReminderRepo", "=== UPDATE FAILED ===", e)
            false
        }
    }

    suspend fun deleteReminder(id: String): Boolean {
        return try {
            try { cancelAlarm(context, id) } catch(e: Exception) { Log.e("HybridReminderRepo", "Cancel alarm failed: ${e.message}") }

            if (networkUtils.isNetworkAvailable()) {
                try { reminderRepository.deleteReminder(id) } catch(e: Exception) { Log.e("HybridReminderRepo", "Firestore delete failed: ${e.message}") }
                try { localDao.deleteReminder(id) } catch(e: Exception) { Log.e("HybridReminderRepo", "Room delete failed: ${e.message}") }
            } else {
                try { localDao.deleteReminder(id) } catch(e: Exception) { Log.e("HybridReminderRepo", "Room delete failed offline: ${e.message}") }
            }

            true
        } catch (e: Exception) {
            Log.e("HybridReminderRepo", "Delete failed: ${e.message}")
            false
        }
    }

    fun cancelAlarm(context: Context, reminderId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val mainIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("reminderId", reminderId)
            }
            val mainPendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(mainPendingIntent)
            Log.d("CancelAlarm", "Alarm utama dibatalkan untuk reminder $reminderId")

            val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("reminderId", reminderId)
                putExtra("is_snooze", true)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode() + 1000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(snoozePendingIntent)
            Log.d("CancelAlarm", "Snooze alarm dibatalkan untuk reminder $reminderId")

        } catch (e: Exception) {
            Log.e("CancelAlarm", "Gagal membatalkan alarm: ${e.message}")
        }
    }

    suspend fun getAllReminders(): List<Reminder> {
        return if (networkUtils.isNetworkAvailable()) {
            try {
                reminderRepository.getAllReminders()
            } catch (e: Exception) {
                Log.e("HybridReminderRepo", "Get remote failed: ${e.message}")
                getLocalReminders()
            }
        } else {
            getLocalReminders()
        }
    }

    private suspend fun getLocalReminders(): List<Reminder> {
        return localDao.getAllReminders().map { it.toDomainModel() }
    }

    suspend fun syncPendingData() {
        if (!networkUtils.isNetworkAvailable()) return

        val unsynced = localDao.getUnsyncedReminders()
        unsynced.forEach { entity ->
            val reminder = entity.toDomainModel()
            try {
                reminderRepository.addReminder(reminder)
                localDao.markAsSynced(entity.id)
            } catch (e: Exception) {
                Log.e("HybridReminderRepo", "Sync failed for ${entity.id}: ${e.message}")
            }
        }
    }

    private fun Reminder.toLocalEntity(isSynced: Boolean): LocalReminderEntity {
        return LocalReminderEntity(
            id = this.id,
            obatId = this.obatIds.joinToString(","),
            lansiaId = this.lansiaIds.joinToString(","),
            tanggal = this.tanggal,
            waktu = this.waktu,
            pengulangan = this.pengulangan,
            isSynced = isSynced
        )
    }

    private fun LocalReminderEntity.toDomainModel(): Reminder {
        return Reminder(
            id = this.id,
            obatIds = this.obatId.split(","),
            lansiaIds = this.lansiaId.split(","),
            tanggal = this.tanggal,
            waktu = this.waktu,
            pengulangan = this.pengulangan
        )
    }
}

