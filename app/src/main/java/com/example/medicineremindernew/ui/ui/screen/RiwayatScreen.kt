package com.example.medicineremindernew.ui.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medicineremindernew.ui.data.model.Obat
import com.example.medicineremindernew.ui.data.model.Lansia
import com.example.medicineremindernew.ui.data.model.Riwayat
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.example.medicineremindernew.ui.ui.theme.BiruTua
import com.example.medicineremindernew.ui.ui.theme.Krem
import com.example.medicineremindernew.ui.ui.viewmodel.HybridLansiaViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridObatViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridRiwayatViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*


private data class UiRiwayat(
    val id: String,
    val lansiaIds: List<String> = emptyList(),
    val obatIds: List<String> = emptyList(),
    val jenis: String? = null,
    val keterangan: String? = null,
    val tanggal: String? = null,
    val waktu: String? = null,
    val waktuDiminumMillis: Long? = null,
    val pertamaKonsumsi: String? = null
)

@Composable
fun RiwayatScreen(
    navController: NavController,
    riwayatViewModel: HybridRiwayatViewModel,
    lansiaViewModel: HybridLansiaViewModel,
    obatViewModel: HybridObatViewModel
) {
    val lansiaList by lansiaViewModel.lansiaList.collectAsState()
    val obatList by obatViewModel.obatList.collectAsState()

    var uiRiwayatList by remember { mutableStateOf<List<UiRiwayat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var riwayatToDelete by remember { mutableStateOf<String?>(null) }


    val lansiaMap = lansiaList.associate { it.id to (it.nama.ifBlank { "Nama tidak tersedia" }) }

    val obatMap by remember(obatList) {
        mutableStateOf(obatList.associate { it.id to (it.nama.ifBlank { "Nama tidak tersedia" }) })
    }

    val warnaKrem = Krem.copy(alpha = 1.0f)
    val warnaBiru = BiruTua.copy(alpha = 1.0f)

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("riwayat").get().await()
            val list = snap.documents.mapNotNull { doc ->
                try {

                    val lansiaIds = when (val x = doc.get("lansiaIds")) {
                        is List<*> -> x.mapNotNull { it?.toString() }
                        else -> {
                            val single = doc.getString("lansiaId")
                            if (single != null) listOf(single) else emptyList()
                        }
                    }

                    val obatIds = when (val x = doc.get("obatIds")) {
                        is List<*> -> x.mapNotNull { it?.toString() }
                        else -> {
                            val single = doc.getString("obatId")
                            if (single != null) listOf(single) else emptyList()
                        }
                    }

                    val waktuDiminumMillis: Long? = when (val x = doc.get("waktuDiminum")) {
                        is Long -> x
                        is Double -> x.toLong()
                        is Number -> x.toLong()
                        is String -> x.toLongOrNull()
                        else -> null
                    }

                    val tanggal = doc.getString("tanggal")
                    val waktu = doc.getString("waktu")

                    val jenis = doc.getString("jenis") ?: doc.getString("status")
                    val keterangan = doc.getString("keterangan") ?: doc.getString("catatan")

                    val obatData = obatList.find{it.id in obatIds}
                    val pertamaKonsumsi = obatData?.pertamaKonsumsi?.toDate()?.let{
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
                    }

                    UiRiwayat(
                        id = doc.id,
                        lansiaIds = lansiaIds,
                        obatIds = obatIds,
                        jenis = jenis,
                        keterangan = keterangan,
                        tanggal = tanggal,
                        waktu = waktu,
                        waktuDiminumMillis = waktuDiminumMillis,
                        pertamaKonsumsi = pertamaKonsumsi
                    )
                } catch (e: Exception) {
                    Log.e("RiwayatScreen", "Gagal parsing doc ${doc.id}: ${e.message}")
                    null
                }
            }
            uiRiwayatList = list.sortedWith(compareByDescending<UiRiwayat> {
                it.waktuDiminumMillis ?: run {
                    val t = try {
                        if (!it.tanggal.isNullOrBlank() && !it.waktu.isNullOrBlank()) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            sdf.parse("${it.tanggal} ${it.waktu}")?.time
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                    t
                }
            })
        } catch (e: Exception) {
            Log.e("RiwayatScreen", "Gagal ambil riwayat: ${e.message}", e)
            uiRiwayatList = emptyList()
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(warnaKrem)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Riwayat Minum Obat",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = warnaBiru
            )

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (uiRiwayatList.isEmpty()) {
                        Text("Belum ada riwayat.", modifier = Modifier.padding(8.dp), color = Color.Gray)
                    } else {
                        uiRiwayatList.forEach { r ->
                            val namaLansia = if (r.lansiaIds.isNotEmpty()) {
                                r.lansiaIds.map { id -> lansiaMap[id] ?: id }.joinToString(", ")
                            } else {
                                "(tidak ada lansia)"
                            }
                            val namaObat = if (r.obatIds.isNotEmpty()) {
                                r.obatIds.map { id -> obatMap[id] ?: id }.joinToString(", ")
                            } else {
                                "(tidak ada obat)"
                            }

                            Log.d("RiwayatDebug", "lansiaIds: ${r.lansiaIds}, lansiaMap keys: ${lansiaMap.keys}")

                            val waktuText = remember(r) {
                                r.waktuDiminumMillis?.let {
                                    try {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        sdf.format(Date(it))
                                    } catch (e: Exception) {
                                        "-"
                                    }
                                } ?: run {
                                    if (!r.tanggal.isNullOrBlank() && !r.waktu.isNullOrBlank()) {
                                        "${r.tanggal} ${r.waktu}"
                                    } else {
                                        "-"
                                    }
                                }
                            }

                            RiwayatRow(
                                riwayatId = r.id,
                                lansiaNama = namaLansia,
                                obatNama = namaObat,
                                waktuText = waktuText,
                                jenis = r.jenis ?: r.keterangan ?: "-",
                                pertamaKonsumsi = r.pertamaKonsumsi
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }




    }
}

@Composable
private fun RiwayatRow(
    riwayatId: String,
    lansiaNama: String,
    obatNama: String,
    waktuText: String,
    jenis: String,
    pertamaKonsumsi: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Lansia: $lansiaNama", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Obat: $obatNama", fontSize = 16.sp, color = Color.DarkGray)
            Text("Waktu: $waktuText", fontSize = 14.sp, color = Color.Gray)
            Text("Jenis: $jenis", fontSize = 14.sp, color = Color(0xFF555555))
            Text("Pertama Konsumsi: ${pertamaKonsumsi ?: "-"}", fontSize = 14.sp, color = Color(0xFF777777))
        }


    }
}
