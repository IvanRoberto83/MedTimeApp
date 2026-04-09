package com.example.medicineremindernew.ui.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.medicineremindernew.R
import com.example.medicineremindernew.ui.MainActivity
import com.example.medicineremindernew.ui.data.model.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm diterima!")

        if (intent?.action != "com.example.medicineremindernew.ALARM") {
            Log.d("AlarmReceiver", "Received intent with wrong action: ${intent?.action}")
            return
        }

        createNotificationChannel(context)

        val singleReminderId = intent.getStringExtra("reminderId")
            ?: intent.getStringExtra("reminder_id")
        val multipleReminderIds = intent.getStringArrayListExtra("reminderIds")

        val recurrenceType = intent.getStringExtra("recurrence_type")
        val originalTime = intent.getLongExtra("original_time", 0L)
        val intervalMillis = intent.getLongExtra("interval_millis", 0L)
        val isRecurring = intent.getBooleanExtra("is_recurring", false)
        val isSnooze = intent.getBooleanExtra("is_snooze", false)
        val snoozeTime = intent.getLongExtra("snooze_time", 0L)

        Log.d("AlarmReceiver", "Received - Single ID: $singleReminderId, Multiple: ${multipleReminderIds?.size}, Snooze: $isSnooze, SnoozeTime: $snoozeTime")

        val reminderIds = when {
            isSnooze && snoozeTime > 0L -> {
                val savedGroup = AlarmPopupActivity.getSnoozeGroup(context, snoozeTime)
                if (savedGroup.isNotEmpty()) {
                    Log.d("AlarmReceiver", "Retrieved snooze group: ${savedGroup.size} reminders")
                    savedGroup.toList()
                } else if (!multipleReminderIds.isNullOrEmpty()) {
                    multipleReminderIds
                } else if (!singleReminderId.isNullOrBlank()) {
                    listOf(singleReminderId)
                } else {
                    Log.e("AlarmReceiver", "No reminder IDs found for snooze alarm")
                    return
                }
            }
            !multipleReminderIds.isNullOrEmpty() -> multipleReminderIds
            !singleReminderId.isNullOrBlank() -> listOf(singleReminderId)
            else -> {
                Log.e("AlarmReceiver", "No reminder IDs provided")
                return
            }
        }

        Log.d("AlarmReceiver", "Processing ${reminderIds.size} reminder IDs: $reminderIds")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isSnooze) {
                    handleSnoozeAlarm(context, reminderIds, snoozeTime)
                } else {
                    handleRegularAlarm(context, reminderIds[0], recurrenceType, originalTime, intervalMillis, isRecurring)
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error handling alarm: ${e.message}")
                handleMultipleReminders(context, reminderIds, isSnooze, snoozeTime)
            }
        }
    }

    private suspend fun handleRegularAlarm(
        context: Context,
        currentReminderId: String,
        recurrenceType: String?,
        originalTime: Long,
        intervalMillis: Long,
        isRecurring: Boolean
    ) {
        try {
            val simultaneousReminderIds = findSimultaneousReminders(context, currentReminderId)
            Log.d("AlarmReceiver", "Found ${simultaneousReminderIds.size} simultaneous reminders: $simultaneousReminderIds")

            simultaneousReminderIds.forEach { id ->
                AlarmPopupActivity.addActiveReminder(context, id)
            }

            AlarmPopupActivity.playGlobalRingtone(context)

            showMultipleRemindersNotification(context, simultaneousReminderIds, false)

            val popupIntent = Intent(context, AlarmPopupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putStringArrayListExtra("reminderIds", ArrayList(simultaneousReminderIds))
                putExtra("is_snooze", false)
            }

            try {
                context.startActivity(popupIntent)
                Log.d("AlarmReceiver", "AlarmPopupActivity started successfully for ${simultaneousReminderIds.size} reminders")
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Failed to start AlarmPopupActivity: ${e.message}")
                showFallbackNotification(context, currentReminderId, false)
            }

            simultaneousReminderIds.forEach { reminderId ->
                triggerActiveAlarm(reminderId, false)
            }

            if (isRecurring && !recurrenceType.isNullOrEmpty()) {
                simultaneousReminderIds.forEach { reminderId ->
                    if (originalTime > 0) {
                        AlarmUtils.scheduleNextRecurringAlarm(
                            context,
                            reminderId,
                            originalTime,
                            recurrenceType
                        )
                    } else if (intervalMillis > 0) {
                        scheduleNextRecurringAlarmLegacy(context, reminderId, intervalMillis, recurrenceType)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error handling regular alarm: ${e.message}")
            handleSingleReminder(context, currentReminderId, false, 0L)
        }
    }

    private suspend fun handleSnoozeAlarm(context: Context, reminderIds: List<String>, snoozeTime: Long) {
        try {
            Log.d("AlarmReceiver", "Handling snooze for ${reminderIds.size} reminders")

            reminderIds.forEach { id ->
                AlarmPopupActivity.addActiveReminder(context, id)
            }

            AlarmPopupActivity.playGlobalRingtone(context)

            showMultipleRemindersNotification(context, reminderIds, true)

            val popupIntent = Intent(context, AlarmPopupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putStringArrayListExtra("reminderIds", ArrayList(reminderIds))
                putExtra("is_snooze", true)
                putExtra("snooze_time", snoozeTime)
            }

            try {
                context.startActivity(popupIntent)
                Log.d("AlarmReceiver", "AlarmPopupActivity started for ${reminderIds.size} snooze reminders")
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Failed to start AlarmPopupActivity for snooze: ${e.message}")
                reminderIds.forEach { id ->
                    showFallbackNotification(context, id, true)
                }
            }

            reminderIds.forEach { reminderId ->
                triggerActiveAlarm(reminderId, true)
            }

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error handling snooze alarm: ${e.message}")
            handleMultipleReminders(context, reminderIds, true, snoozeTime)
        }
    }

    private fun handleMultipleReminders(context: Context, reminderIds: List<String>, isSnooze: Boolean, snoozeTime: Long) {
        reminderIds.forEach { id ->
            AlarmPopupActivity.addActiveReminder(context, id)
        }

        AlarmPopupActivity.playGlobalRingtone(context)
        showMultipleRemindersNotification(context, reminderIds, isSnooze)

        reminderIds.forEach { reminderId ->
            triggerActiveAlarm(reminderId, isSnooze)
        }

        val popupIntent = Intent(context, AlarmPopupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putStringArrayListExtra("reminderIds", ArrayList(reminderIds))
            putExtra("is_snooze", isSnooze)
            putExtra("snooze_time", snoozeTime)
        }

        try {
            context.startActivity(popupIntent)
            Log.d("AlarmReceiver", "Multiple reminders popup started for ${reminderIds.size} reminders")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start multiple reminders popup: ${e.message}")
            reminderIds.forEach { id ->
                showFallbackNotification(context, id, isSnooze)
            }
        }
    }

    private fun handleSingleReminder(context: Context, reminderId: String, isSnooze: Boolean, snoozeTime: Long) {
        AlarmPopupActivity.addActiveReminder(context, reminderId)
        AlarmPopupActivity.playGlobalRingtone(context)
        showNotification(context, reminderId, if (isSnooze) "Snooze" else "Sekali", isSnooze)
        triggerActiveAlarm(reminderId, isSnooze)

        val popupIntent = Intent(context, AlarmPopupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putStringArrayListExtra("reminderIds", arrayListOf(reminderId))
            putExtra("is_snooze", isSnooze)
            putExtra("snooze_time", snoozeTime)
        }

        try {
            context.startActivity(popupIntent)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start single reminder popup: ${e.message}")
            showFallbackNotification(context, reminderId, isSnooze)
        }
    }

    private suspend fun findSimultaneousReminders(context: Context, currentReminderId: String): List<String> {
        val db = FirebaseFirestore.getInstance()
        val reminderIds = mutableSetOf<String>()

        try {
            val currentReminderSnap = db.collection("reminders").document(currentReminderId).get().await()
            if (!currentReminderSnap.exists()) {
                Log.w("AlarmReceiver", "Current reminder not found: $currentReminderId")
                return listOf(currentReminderId)
            }

            val currentReminder = currentReminderSnap.toObject(Reminder::class.java)
            if (currentReminder == null) {
                Log.w("AlarmReceiver", "Current reminder data is null: $currentReminderId")
                return listOf(currentReminderId)
            }

            val currentDateTime = "${currentReminder.tanggal} ${currentReminder.waktu}"
            Log.d("AlarmReceiver", "Looking for reminders at time: $currentDateTime")

            val allReminders = db.collection("reminders").get().await()
            for (document in allReminders.documents) {
                val reminder = document.toObject(Reminder::class.java)
                if (reminder != null && reminder.tanggal != null && reminder.waktu != null) {
                    val reminderDateTime = "${reminder.tanggal} ${reminder.waktu}"
                    if (reminderDateTime == currentDateTime) {
                        reminderIds.add(document.id)
                        Log.d("AlarmReceiver", "Found matching reminder: ${document.id}")
                    }
                }
            }

            Log.d("AlarmReceiver", "Total simultaneous reminders found: ${reminderIds.size}")
            return reminderIds.toList()

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error finding simultaneous reminders: ${e.message}")
            return listOf(currentReminderId)
        }
    }

    private fun showMultipleRemindersNotification(context: Context, reminderIds: List<String>, isSnooze: Boolean) {
        val notificationTitle = if (isSnooze) "Pengingat Obat (Snooze)" else "Pengingat Obat"
        val notificationText = if (reminderIds.size == 1) {
            "Saatnya minum obat!"
        } else {
            "Ada ${reminderIds.size} pengingat obat yang harus diminum!"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putStringArrayListExtra("reminderIds", ArrayList(reminderIds))
            putExtra("check_active_alarms", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderIds.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pill)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = if (isSnooze) {
            (reminderIds.hashCode() + 1000)
        } else {
            reminderIds.hashCode()
        }

        try {
            manager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "Permission denied for notification", e)
        }
    }

    private fun showNotification(context: Context, reminderId: String, recurrenceType: String, isSnooze: Boolean = false) {
        val notificationTitle = if (isSnooze) "Pengingat Obat (Snooze)" else "Pengingat Obat"
        val notificationText = if (isSnooze)
            "Waktunya minum obat! (Pengingat Snooze)"
        else
            "Saatnya minum obat - $recurrenceType"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminderId", reminderId)
            putExtra("check_active_alarm", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pill)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = if (isSnooze) {
            (reminderId.hashCode() + 1000)
        } else {
            reminderId.hashCode()
        }

        try {
            manager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "Permission denied for notification", e)
        }
    }

    private fun showFallbackNotification(context: Context, reminderId: String, isSnooze: Boolean) {
        val notificationTitle = "Pengingat Obat - Buka Aplikasi"
        val notificationText = "Ada alarm aktif. Buka aplikasi untuk melihat pengingat."

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminderId", reminderId)
            putExtra("check_active_alarm", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode() + 2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pill)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = reminderId.hashCode() + 2000

        try {
            manager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "Permission denied for fallback notification", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk alarm pengingat obat"
                enableVibration(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun triggerActiveAlarm(reminderId: String, isSnooze: Boolean = false) {
        try {
            val db = FirebaseFirestore.getInstance()

            val activeAlarmData = hashMapOf(
                "reminderId" to reminderId,
                "trigger" to true,
                "isSnooze" to isSnooze,
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("active_alarm")
                .document("current")
                .set(activeAlarmData)
                .addOnSuccessListener {
                    val alarmType = if (isSnooze) "snooze" else "regular"
                    Log.d("AlarmReceiver", "active_alarm triggered for reminderId: $reminderId (type: $alarmType)")
                }
                .addOnFailureListener { e ->
                    Log.e("AlarmReceiver", "Gagal menulis active_alarm: ${e.message}")
                }

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Exception saat trigger active_alarm: ${e.message}")
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNextRecurringAlarmLegacy(
        context: Context,
        reminderId: String,
        intervalMillis: Long,
        recurrenceType: String
    ) {
        try {
            val nextTimeInMillis = System.currentTimeMillis() + intervalMillis
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

            val nextIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("reminderId", reminderId)
                putExtra("reminder_id", reminderId)
                putExtra("recurrence_type", recurrenceType)
                putExtra("interval_millis", intervalMillis)
                putExtra("is_recurring", true)
                putExtra("is_snooze", false)
                action = "com.example.medicineremindernew.ALARM"
            }

            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                nextIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                nextTimeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                android.app.AlarmManager.RTC_WAKEUP,
                                nextTimeInMillis,
                                pendingIntent
                            )
                        }
                        Log.d("AlarmReceiver", "Next exact recurring alarm scheduled for: ${java.util.Date(nextTimeInMillis)}")
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            nextTimeInMillis,
                            pendingIntent
                        )
                        Log.w("AlarmReceiver", "Exact alarm permission not available, using inexact alarm")
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            nextTimeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            android.app.AlarmManager.RTC_WAKEUP,
                            nextTimeInMillis,
                            pendingIntent
                        )
                    }
                    Log.d("AlarmReceiver", "Next exact recurring alarm scheduled for: ${java.util.Date(nextTimeInMillis)}")
                }
            } catch (se: SecurityException) {
                Log.w("AlarmReceiver", "SecurityException saat set exact recurring alarm, fallback ke regular alarm: ${se.message}")
                alarmManager.set(
                    android.app.AlarmManager.RTC_WAKEUP,
                    nextTimeInMillis,
                    pendingIntent
                )
                Log.d("AlarmReceiver", "Fallback regular recurring alarm scheduled")
            }

        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to schedule next recurring alarm", e)
        }
    }
}

class MedicineTakenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminderId")
            ?: intent.getStringExtra("reminder_id")
        val reminderIds = intent.getStringArrayListExtra("reminderIds")
            ?: (if (reminderId != null) listOf(reminderId) else emptyList())

        if (reminderIds.isEmpty()) {
            Log.w("MedicineTakenReceiver", "No reminder IDs provided")
            return
        }

        val notificationManager = NotificationManagerCompat.from(context)
        reminderIds.forEach { id ->
            notificationManager.cancel(id.hashCode())
            notificationManager.cancel(id.hashCode() + 1000)
        }

        Log.d("MedicineTakenReceiver", "Medicine taken for ${reminderIds.size} reminders: $reminderIds")

        AlarmPopupActivity.clearAllActiveReminders(context)

        reminderIds.forEach { id ->
            updateMedicineTakenStatus(id)
        }
    }

    private fun updateMedicineTakenStatus(reminderId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val medicineStatus = hashMapOf(
                "reminderId" to reminderId,
                "status" to "taken",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("medicine_status")
                .document(reminderId)
                .set(medicineStatus)
                .addOnSuccessListener {
                    Log.d("MedicineTakenReceiver", "Medicine status updated for: $reminderId")
                }
                .addOnFailureListener { e ->
                    Log.e("MedicineTakenReceiver", "Failed to update medicine status: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("MedicineTakenReceiver", "Exception updating medicine status: ${e.message}")
        }
    }
}