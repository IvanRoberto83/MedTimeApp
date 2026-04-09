package com.example.medicineremindernew.ui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "local_reminders")
data class LocalReminderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val obatId: String,
    val lansiaId: String,
    val tanggal: String,
    val waktu: String,
    val pengulangan: String,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)