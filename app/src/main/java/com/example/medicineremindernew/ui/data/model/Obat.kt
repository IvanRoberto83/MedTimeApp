package com.example.medicineremindernew.ui.data.model

import com.google.firebase.Timestamp

data class Obat(
    val id: String = "",
    val jenis: String = "",
    val nama: String = "",
    val deskripsi: String = "",
    val takaranDosis: String = "",
    val dosis: String = "",
    val waktuMinum: String = "",
    val catatan: String = "",
    val pertamaKonsumsi: Timestamp? = null,
    val stok: Int = 0
)

