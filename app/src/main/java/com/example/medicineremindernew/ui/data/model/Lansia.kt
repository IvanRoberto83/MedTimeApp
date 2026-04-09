package com.example.medicineremindernew.ui.data.model

import com.google.firebase.Timestamp

data class Lansia(
    val id: String = "",
    val nama: String = "",
    val goldar: String = "",
    val gender: String = "",
    val lahir: Timestamp? = null,
    val penyakit: String = "",
    val obatIds: List<String> = emptyList()

)
