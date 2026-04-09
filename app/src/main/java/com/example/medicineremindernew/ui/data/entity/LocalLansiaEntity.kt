package com.example.medicineremindernew.ui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "local_lansia")
data class LocalLansiaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nama: String,
    val goldar: String,
    val gender: String,
    val lahir: String,
    val penyakit: String,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)