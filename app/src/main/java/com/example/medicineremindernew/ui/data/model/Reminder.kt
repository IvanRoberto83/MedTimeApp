package com.example.medicineremindernew.ui.data.model



import com.google.firebase.Timestamp
import java.util.Date


data class Reminder(
    val id: String = "",
    val lansiaIds: List<String> = emptyList(),
    val obatIds: List<String> = emptyList(),
    val waktu: String = "",
    val tanggal: String = "",
    val pengulangan: String = ""
)




