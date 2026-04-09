package com.example.medicineremindernew.ui.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.medicineremindernew.ui.data.model.Reminder
import java.util.*

fun scheduleAlarm(context: Context, reminderId: String, timeInMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
            return
        }
    }

    Log.d("AlarmScheduler", "=== SUPER AGGRESSIVE CANCEL FOR $reminderId ===")

    val possibleRequestCodes = listOf(
        0,
        reminderId.hashCode(),
        reminderId.hashCode() and 0x7fffffff,
        Math.abs(reminderId.hashCode()),
        reminderId.toIntOrNull() ?: 0
    ).distinct()

    val possibleActions = listOf(
        "com.example.medicineremindernew.ALARM",
        "ALARM",
        null
    )

    val possibleDataUris = listOf(
        "reminder://$reminderId",
        "reminder:$reminderId",
        reminderId,
        null
    )

    var totalCancelled = 0

    possibleRequestCodes.forEach { reqCode ->
        possibleActions.forEach { action ->
            possibleDataUris.forEach { dataUri ->
                try {
                    val cancelIntent = Intent(context, AlarmReceiver::class.java).apply {
                        if (action != null) this.action = action
                        if (dataUri != null) data = Uri.parse(dataUri)
                        putExtra("reminderId", reminderId)
                    }

                    val cancelPendingIntent = PendingIntent.getBroadcast(
                        context,
                        reqCode,
                        cancelIntent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (cancelPendingIntent != null) {
                        alarmManager.cancel(cancelPendingIntent)
                        cancelPendingIntent.cancel()
                        totalCancelled++
                        Log.d("AlarmScheduler", "✅ CANCELLED: reqCode=$reqCode, action=$action, data=$dataUri")
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    Log.d("AlarmScheduler", "Total alarms cancelled: $totalCancelled")

    Thread.sleep(500)

    Log.d("AlarmScheduler", "=== CREATING NEW ALARM FOR $reminderId ===")

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "com.example.medicineremindernew.ALARM"
        data = Uri.parse("reminder://$reminderId")
        putExtra("reminderId", reminderId)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        timeInMillis,
        pendingIntent
    )

    Log.d("AlarmScheduler", "✅ NEW ALARM SCHEDULED: $reminderId at ${Date(timeInMillis)} with requestCode=${reminderId.hashCode()}")
}

fun cancelAlarm(context: Context, reminderId: String) {
    Log.d("AlarmScheduler", "Using aggressive cancel strategy...")

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val possibleRequestCodes = listOf(
        0,
        reminderId.hashCode(),
        reminderId.hashCode() and 0x7fffffff,
        Math.abs(reminderId.hashCode()),
        reminderId.toIntOrNull() ?: 0
    ).distinct()

    val possibleActions = listOf(
        "com.example.medicineremindernew.ALARM",
        "ALARM",
        null
    )

    val possibleDataUris = listOf(
        "reminder://$reminderId",
        "reminder:$reminderId",
        reminderId,
        null
    )

    var totalCancelled = 0

    possibleRequestCodes.forEach { reqCode ->
        possibleActions.forEach { action ->
            possibleDataUris.forEach { dataUri ->
                try {
                    val intent = Intent(context, AlarmReceiver::class.java).apply {
                        if (action != null) this.action = action
                        if (dataUri != null) data = Uri.parse(dataUri)
                        putExtra("reminderId", reminderId)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        reqCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                        totalCancelled++
                        Log.d("AlarmScheduler", "✅ CANCELLED: reqCode=$reqCode, action=$action, data=$dataUri")
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    if (totalCancelled > 0) {
        Log.d("AlarmScheduler", "✅ TOTAL CANCELLED: $totalCancelled alarms for $reminderId")
    } else {
        Log.d("AlarmScheduler", "❌ NO ACTIVE ALARMS FOUND for $reminderId")
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "alarm_channel",
            "Alarm Reminder",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel untuk alarm pengingat"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}