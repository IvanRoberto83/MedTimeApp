package com.example.medicineremindernew.ui.data.model

import java.util.*
import com.google.firebase.Timestamp
import java.util.Date

data class Riwayat(
    val idRiwayat: String = "",
    val lansiaId: String = "",
    val obatId: String? = null,
    val kunjunganId: String? = null,

    val jenis: String = "",
    val keterangan: String = "",

    val tanggal: String = "",
    val waktu: String = ""
)
