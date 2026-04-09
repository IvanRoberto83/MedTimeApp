package com.example.medicineremindernew.ui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "local_kunjungan")
data class LocalKunjunganEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val lansiaIds: String,
    val tanggal: String,
    val waktu: String,
    val jenisKunjungan: String,


    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
