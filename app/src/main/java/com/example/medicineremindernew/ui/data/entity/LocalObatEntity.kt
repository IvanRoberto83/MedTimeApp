package com.example.medicineremindernew.ui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "local_obat")
data class LocalObatEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val jenis: String,
    val nama: String,
    val deskripsi: String,
    val takaranDosis: String,
    val dosis: String,
    val waktuMinum: String,
    val catatan: String,
    val stok: Int = 0,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)