package com.example.medicineremindernew.ui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "local_riwayat")
data class LocalRiwayatEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val lansiaId: String,
    val obatId: String? = null,
    val kunjunganId: String? = null,

    val jenis: String,
    val keterangan: String = "",

    val tanggal: String,
    val waktu: String,

    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
