package com.example.medicineremindernew.ui.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.medicineremindernew.R

class AlarmService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private val CHANNEL_ID = "alarm_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification())

        mediaPlayer = MediaPlayer.create(this, R.raw.nada1)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        Handler(mainLooper).postDelayed({
            mediaPlayer.stop()
            mediaPlayer.release()
            stopSelf()
        }, 10_000)

        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Obat")
            .setContentText("Waktunya minum obat!")
            .setSmallIcon(R.mipmap.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alarm", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
