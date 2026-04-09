package com.example.medicineremindernew.ui.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.medicineremindernew.ui.ui.theme.MedicineReminderNewTheme
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.medicineremindernew.ui.data.model.Lansia
import com.example.medicineremindernew.ui.data.model.Obat
import com.example.medicineremindernew.ui.data.model.Reminder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class AlarmPopupActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null

    companion object {
        private const val PREF_NAME = "AlarmPrefs"
        private const val KEY_ACTIVE_REMINDER_IDS = "active_reminder_ids"
        private const val KEY_SNOOZE_GROUP_PREFIX = "snooze_group_"

        private var globalRingtone: Ringtone? = null

        fun isRingtoneCurrentlyPlaying(): Boolean {
            return globalRingtone?.isPlaying == true
        }

        fun addActiveReminder(context: Context, reminderId: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val currentIds = getActiveReminderIds(context).toMutableSet()
            currentIds.add(reminderId)
            prefs.edit().putStringSet(KEY_ACTIVE_REMINDER_IDS, currentIds).apply()
            Log.d("AlarmPopup", "Added active reminder: $reminderId, total: ${currentIds.size}")
        }

        fun getActiveReminderIds(context: Context): Set<String> {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getStringSet(KEY_ACTIVE_REMINDER_IDS, emptySet()) ?: emptySet()
        }

        fun removeActiveReminder(context: Context, reminderId: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val currentIds = getActiveReminderIds(context).toMutableSet()
            currentIds.remove(reminderId)
            if (currentIds.isEmpty()) {
                prefs.edit().remove(KEY_ACTIVE_REMINDER_IDS).apply()
            } else {
                prefs.edit().putStringSet(KEY_ACTIVE_REMINDER_IDS, currentIds).apply()
            }
            Log.d("AlarmPopup", "Removed active reminder: $reminderId, remaining: ${currentIds.size}")
        }

        fun clearAllActiveReminders(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_ACTIVE_REMINDER_IDS).apply()
            Log.d("AlarmPopup", "Cleared all active reminders")
        }

        fun saveSnoozeGroup(context: Context, reminderIds: List<String>, snoozeTime: Long) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val groupKey = "$KEY_SNOOZE_GROUP_PREFIX$snoozeTime"
            prefs.edit().putStringSet(groupKey, reminderIds.toSet()).apply()
            Log.d("AlarmPopup", "Saved snooze group with ${reminderIds.size} reminders for time: $snoozeTime")
        }

        fun getSnoozeGroup(context: Context, snoozeTime: Long): Set<String> {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val groupKey = "$KEY_SNOOZE_GROUP_PREFIX$snoozeTime"
            return prefs.getStringSet(groupKey, emptySet()) ?: emptySet()
        }

        fun removeSnoozeGroup(context: Context, snoozeTime: Long) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val groupKey = "$KEY_SNOOZE_GROUP_PREFIX$snoozeTime"
            prefs.edit().remove(groupKey).apply()
            Log.d("AlarmPopup", "Removed snooze group for time: $snoozeTime")
        }

        suspend fun findSimultaneousReminders(context: Context, currentReminderId: String): List<String> {
            val db = FirebaseFirestore.getInstance()
            val reminderIds = mutableSetOf<String>()

            try {
                val currentReminderSnap = db.collection("reminders").document(currentReminderId).get().await()
                if (!currentReminderSnap.exists()) return listOf(currentReminderId)

                val currentReminder = currentReminderSnap.toObject(Reminder::class.java) ?: return listOf(currentReminderId)
                val currentTime = "${currentReminder.tanggal} ${currentReminder.waktu}"

                val allReminders = db.collection("reminders").get().await()
                for (document in allReminders.documents) {
                    val reminder = document.toObject(Reminder::class.java)
                    if (reminder != null) {
                        val reminderTime = "${reminder.tanggal} ${reminder.waktu}"
                        if (reminderTime == currentTime) {
                            reminderIds.add(document.id)
                        }
                    }
                }

                Log.d("AlarmPopup", "Found ${reminderIds.size} reminders at time: $currentTime")
                return reminderIds.toList()

            } catch (e: Exception) {
                Log.e("AlarmPopup", "Error finding simultaneous reminders: ${e.message}")
                return listOf(currentReminderId)
            }
        }

        fun stopGlobalRingtone() {
            try {
                if (globalRingtone?.isPlaying == true) {
                    globalRingtone?.stop()
                    Log.d("AlarmPopup", "Global ringtone stopped")
                }
                globalRingtone = null
            } catch (e: Exception) {
                Log.e("AlarmPopup", "Error stopping global ringtone: ${e.message}")
            }
        }

        fun playGlobalRingtone(context: Context) {
            try {
                stopGlobalRingtone()

                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                globalRingtone = RingtoneManager.getRingtone(context, alarmUri)
                globalRingtone?.play()
                Log.d("AlarmPopup", "Global ringtone started")
            } catch (e: Exception) {
                Log.e("AlarmPopup", "Error playing global ringtone: ${e.message}")
            }
        }

        fun setActiveReminders(context: Context, reminderId: String) {
            addActiveReminder(context, reminderId)
        }

        fun checkAndShowActiveAlarms(context: Context) {
            val activeReminderIds = getActiveReminderIds(context)
            if (activeReminderIds.isNotEmpty()) {
                Log.d("AlarmPopup", "Found ${activeReminderIds.size} active alarms, showing popup")
                val intent = Intent(context, AlarmPopupActivity::class.java).apply {
                    putStringArrayListExtra("reminderIds", ArrayList(activeReminderIds))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AlarmPopup", "onCreate: AlarmPopupActivity dimulai")

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val singleReminderId = intent.getStringExtra("reminderId") ?: intent.getStringExtra("reminder_id")
        val multipleReminderIds = intent.getStringArrayListExtra("reminderIds")
        val isSnooze = intent.getBooleanExtra("is_snooze", false)
        val snoozeTime = intent.getLongExtra("snooze_time", 0L)

        val reminderIds = when {
            isSnooze && snoozeTime > 0L -> {
                val savedGroup = getSnoozeGroup(this, snoozeTime)
                if (savedGroup.isNotEmpty()) {
                    Log.d("AlarmPopup", "Retrieved snooze group with ${savedGroup.size} reminders")
                    savedGroup.toList()
                } else {
                    listOf(singleReminderId ?: "")
                }
            }
            !multipleReminderIds.isNullOrEmpty() -> multipleReminderIds
            !singleReminderId.isNullOrBlank() -> {
                lifecycleScope.launch {
                    val simultaneousIds = findSimultaneousReminders(this@AlarmPopupActivity, singleReminderId)
                    simultaneousIds.forEach { id ->
                        addActiveReminder(this@AlarmPopupActivity, id)
                    }
                }
                listOf(singleReminderId)
            }
            else -> emptyList()
        }

        Log.d("AlarmPopup", "Processing ${reminderIds.size} reminder IDs: $reminderIds")

        reminderIds.forEach { id ->
            addActiveReminder(this, id)
        }

        if (globalRingtone?.isPlaying != true) {
            playGlobalRingtone(this)
        }

        setContent {
            MedicineReminderNewTheme {
                AlarmPopupScreen(
                    reminderIds = reminderIds,
                    onDismiss = { reminders, allLansia, allObat ->
                        Log.d("AlarmPopup", "Tombol 'Sudah Diminum' ditekan untuk ${reminders.size} reminders")

                        if (isSnooze && snoozeTime > 0L) {
                            removeSnoozeGroup(this@AlarmPopupActivity, snoozeTime)
                        }

                        reminders.forEach { reminder ->
                            matikanIoT(reminder.id ?: "")

                            val reminderLansia = allLansia.filter { it.id in (reminder.lansiaIds ?: emptyList()) }
                            val reminderObat = allObat.filter { it.id in (reminder.obatIds ?: emptyList()) }
                            simpanRiwayat(reminder.id ?: "", reminderLansia, reminderObat, jenisRiwayat = "minum obat")

                            AlarmUtils.updateToNextAlarmTime(
                                context = this@AlarmPopupActivity,
                                reminderId = reminder.id ?: "",
                                recurrenceType = reminder.pengulangan ?: "Sekali",
                                currentDateStr = reminder.tanggal ?: "",
                                currentTimeStr = reminder.waktu ?: ""
                            )

                            cancelSnoozeAlarm(this@AlarmPopupActivity, reminder.id ?: "")
                        }

                        stopRingtone()

                        clearAllActiveReminders(this@AlarmPopupActivity)

                        finish()
                    },
                    onSnooze = { reminderIds ->
                        stopRingtone()

                        Log.d("AlarmPopup", "Tombol 'Bunyikan 5 Menit Lagi' ditekan untuk ${reminderIds.size} reminders")

                        val snoozeTime = System.currentTimeMillis() + (5 * 60 * 1000)
                        saveSnoozeGroup(this@AlarmPopupActivity, reminderIds, snoozeTime)

                        reminderIds.forEach { reminderId ->
                            matikanIoT(reminderId)
                        }

                        setSnoozeAlarmForGroup(this@AlarmPopupActivity, reminderIds, snoozeTime)

                        clearAllActiveReminders(this@AlarmPopupActivity)

                        finish()
                    }
                )
            }
        }
    }

    private fun stopRingtone() {
        try {
            Companion.stopGlobalRingtone()

            if (ringtone?.isPlaying == true) {
                ringtone?.stop()
                Log.d("AlarmPopup", "Local ringtone stopped")
            }
            ringtone = null

            Log.d("AlarmPopup", "All ringtones stopped")
        } catch (e: Exception) {
            Log.e("AlarmPopup", "Error stopping all ringtones: ${e.message}")
        }
    }

    private fun cancelSnoozeAlarm(context: Context, reminderId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.medicineremindernew.ALARM"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode() + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("AlarmPopup", "Cancelled snooze alarm for reminder: $reminderId")
        } catch (e: Exception) {
            Log.e("AlarmPopup", "Error cancelling snooze alarm: ${e.message}")
        }
    }

    private fun matikanIoT(reminderId: String) {
        if (reminderId.isBlank()) {
            Log.e("Firestore", "Reminder ID tidak valid")
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("reminders").document(reminderId)
            .update("statusIoT", "OFF")
            .addOnSuccessListener {
                Log.d("Firestore", "statusIoT diupdate ke OFF untuk reminder $reminderId")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Gagal update statusIoT", e)
            }
    }

    private fun setSnoozeAlarmForGroup(context: Context, reminderIds: List<String>, snoozeTime: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putStringArrayListExtra("reminderIds", ArrayList(reminderIds))
                putExtra("is_snooze", true)
                putExtra("snooze_time", snoozeTime)
                putExtra("is_recurring", false)
                action = "com.example.medicineremindernew.ALARM"
            }

            val requestCode = (snoozeTime / 1000).toInt()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            snoozeTime,
                            pendingIntent
                        )
                        Log.d("AlarmPopup", "Exact snooze alarm scheduled for ${reminderIds.size} reminders at: ${java.util.Date(snoozeTime)}")
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            snoozeTime,
                            pendingIntent
                        )
                        Log.w("AlarmPopup", "Exact alarm permission not available, using inexact snooze alarm")
                    }
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime,
                        pendingIntent
                    )
                    Log.d("AlarmPopup", "Exact snooze alarm scheduled (API 23+)")
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime,
                        pendingIntent
                    )
                    Log.d("AlarmPopup", "Exact snooze alarm scheduled (API 19+)")
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime,
                        pendingIntent
                    )
                    Log.d("AlarmPopup", "Basic snooze alarm scheduled")
                }
            } catch (se: SecurityException) {
                Log.w("AlarmPopup", "SecurityException, fallback to inexact snooze alarm: ${se.message}")
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            }

        } catch (e: Exception) {
            Log.e("AlarmPopup", "Gagal mengatur snooze alarm untuk group: ${e.message}")
        }
    }

    private fun simpanRiwayat(reminderId: String, lansiaList: List<Lansia>, obatList: List<Obat>, jenisRiwayat: String) {
        val db = FirebaseFirestore.getInstance()
        val riwayatId = db.collection("riwayat").document().id

        val riwayatData = hashMapOf(
            "idRiwayat" to riwayatId,
            "reminderId" to reminderId,
            "lansiaIds" to lansiaList.map { it.id },
            "obatIds" to obatList.map { it.id },
            "waktuDiminum" to System.currentTimeMillis(),
            "status" to "SUDAH",
            "jenis" to jenisRiwayat
        )

        db.collection("riwayat")
            .document(riwayatId)
            .set(riwayatData)
            .addOnSuccessListener {
                Log.d("Riwayat", "Riwayat berhasil disimpan untuk reminder: $reminderId")
            }
            .addOnFailureListener { e ->
                Log.e("Riwayat", "Gagal menyimpan riwayat untuk reminder: $reminderId", e)
            }
    }

    override fun onDestroy() {
        stopRingtone()
        super.onDestroy()
    }
}

data class ReminderGroup(
    val reminder: Reminder,
    val lansiaList: List<Lansia>,
    val obatList: List<Obat>
)

@Composable
fun AlarmPopupScreen(
    reminderIds: List<String>,
    onDismiss: (reminders: List<Reminder>, allLansia: List<Lansia>, allObat: List<Obat>) -> Unit,
    onSnooze: (reminderIds: List<String>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var reminderGroups by remember { mutableStateOf<List<ReminderGroup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reminderIds) {
        Log.d("AlarmPopup", "Memulai fetch ${reminderIds.size} reminders")
        try {
            val groups = mutableListOf<ReminderGroup>()

            for (reminderId in reminderIds) {
                try {
                    val reminderSnap = db.collection("reminders").document(reminderId).get().await()
                    if (reminderSnap.exists()) {
                        val reminder = reminderSnap.toObject(Reminder::class.java)
                        if (reminder != null) {
                            val obatTempList = mutableListOf<Obat>()
                            reminder.obatIds?.forEach { obatId ->
                                try {
                                    val obatSnap = db.collection("obat").document(obatId).get().await()
                                    obatSnap.toObject(Obat::class.java)?.let { obatTempList.add(it) }
                                } catch (e: Exception) {
                                    Log.w("AlarmPopup", "Failed to fetch obat $obatId: ${e.message}")
                                }
                            }

                            val lansiaTempList = mutableListOf<Lansia>()
                            reminder.lansiaIds?.forEach { lansiaId ->
                                try {
                                    val lansiaSnap = db.collection("lansia").document(lansiaId).get().await()
                                    lansiaSnap.toObject(Lansia::class.java)?.let { lansiaTempList.add(it) }
                                } catch (e: Exception) {
                                    Log.w("AlarmPopup", "Failed to fetch lansia $lansiaId: ${e.message}")
                                }
                            }

                            groups.add(ReminderGroup(reminder, lansiaTempList, obatTempList))
                            Log.d("AlarmPopup", "Successfully loaded reminder $reminderId with ${lansiaTempList.size} lansia and ${obatTempList.size} obat")
                        }
                    } else {
                        Log.w("AlarmPopup", "Reminder $reminderId not found in Firestore")
                    }
                } catch (e: Exception) {
                    Log.e("AlarmPopup", "Error fetching reminder $reminderId: ${e.message}")
                }
            }

            reminderGroups = groups
            Log.d("AlarmPopup", "Successfully loaded ${groups.size} reminder groups")

        } catch (e: Exception) {
            Log.e("AlarmPopup", "Fatal error loading reminders: ${e.message}")
            errorMessage = "Gagal memuat data reminder: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(16.dp))
                .border(4.dp, Color(0xFF027A7E), RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Saatnya Minum Obat!",
                fontSize = 22.sp,
                color = Color(0xFF011A27)
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Memuat data...", fontSize = 14.sp, color = Color(0xFF011A27))
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }

                reminderGroups.isNotEmpty() -> {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        reminderGroups.forEachIndexed { index, group ->
                            if (group.lansiaList.isNotEmpty()) {
                                Text(
                                    text = "Lansia: ${group.lansiaList.joinToString(", ") { it.nama ?: "Unknown" }}",
                                    fontSize = 16.sp,
                                    color = Color(0xFF011A27)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (group.obatList.isNotEmpty()) {
                                group.obatList.forEach { obat ->
                                    Text(
                                        text = "Obat: ${obat.nama ?: "Unknown"}",
                                        fontSize = 14.sp,
                                        color = Color(0xFF011A27)
                                    )
                                    Text(
                                        text = "Dosis: ${obat.dosis ?: "Unknown"}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF666666)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

                            if (index < reminderGroups.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 1.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }

                else -> {
                    Text(
                        text = "Data reminder tidak tersedia",
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val allReminders = reminderGroups.map { it.reminder }
                        val allLansia = reminderGroups.flatMap { it.lansiaList }.distinctBy { it.id }
                        val allObat = reminderGroups.flatMap { it.obatList }.distinctBy { it.id }
                        onDismiss(allReminders, allLansia, allObat)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF027A7E),
                        contentColor = Color.White
                    ),
                    enabled = reminderGroups.isNotEmpty()
                ) {
                    Text("Sudah Diminum", fontSize = 12.sp)
                }

                Button(
                    onClick = { onSnooze(reminderIds) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White
                    ),
                    enabled = reminderGroups.isNotEmpty()
                ) {
                    Text("5 Menit Lagi", fontSize = 12.sp)
                }
            }
        }
    }
}