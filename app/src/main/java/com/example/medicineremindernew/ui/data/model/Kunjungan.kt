package com.example.medicineremindernew.ui.data.model



import com.google.firebase.Timestamp
import java.util.Date


data class Kunjungan(
    val idKunjungan: String = "",
    val lansiaIds: List<String> = emptyList(),
    val waktu: String = "",
    val tanggal: String = "" ,
    val jenisKunjungan: String = ""


)




