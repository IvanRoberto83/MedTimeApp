package com.example.medicineremindernew.ui.ui.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicineremindernew.ui.data.model.Kunjungan
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.example.medicineremindernew.ui.ui.theme.BiruTua
import com.example.medicineremindernew.ui.ui.theme.Krem
import com.example.medicineremindernew.ui.ui.viewmodel.HybridKunjunganViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridLansiaViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun KunjunganScreen(
    navController: NavController,
    kunjunganViewModel: HybridKunjunganViewModel,
    lansiaViewModel: HybridLansiaViewModel
) {


    LaunchedEffect(Unit) {
        kunjunganViewModel.syncPendingData()
    }

    val kunjunganList by kunjunganViewModel.kunjunganList.collectAsState()
    val lansiaList by lansiaViewModel.lansiaList.collectAsState()
    val now = remember { LocalDateTime.now() }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val filteredKunjungan = kunjunganList
        .mapNotNull { k ->
            try {
                val dateTime = LocalDateTime.parse("${k.tanggal} ${k.waktu}", formatter)
                Pair(k, dateTime)
            } catch (e: Exception) {
                Log.e("KunjunganParse", "Gagal parsing: ${k.tanggal} ${k.waktu}")
                null
            }
        }
        .filter { (_, dateTime) -> dateTime.isAfter(now) || dateTime.isEqual(now) }
        .sortedBy { (_, dateTime) -> dateTime }
        .map { (k, _) -> k }

    val kunjunganTerdekat = filteredKunjungan.firstOrNull()

    val warnaKrem = Krem.copy(alpha = 1.0f)
    val warnaBiru = BiruTua.copy(alpha = 1.0f)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var kunjunganToDelete by remember { mutableStateOf<String?>(null) }

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
                text = "Daftar Aktivitas",
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
                Spacer(modifier = Modifier.height(5.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Aktivitas Terdekat",
                            color = warnaBiru,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        if (kunjunganTerdekat != null) {
                            val lansiaName = lansiaList
                                .filter { it.id in kunjunganTerdekat.lansiaIds }
                                .joinToString(", ") { it.nama }

                            Text(
                                text = lansiaName,
                                color = Color.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 5.dp)
                            )

                            Text(
                                text = "${kunjunganTerdekat.waktu} - ${kunjunganTerdekat.tanggal}",
                                color = Color(0xFF666666),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = {
                                    kunjunganToDelete = kunjunganTerdekat.idKunjungan
                                    showDeleteDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = Color.Red
                                    )
                                }

                                IconButton(onClick = {
                                    navController.navigate("detail_kunjungan/${kunjunganTerdekat.idKunjungan}")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = warnaBiru
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Tidak ada Aktivitas terdekat",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
                            )
                        }
                    }
                }

                filteredKunjungan.forEach { kunjungan ->
                    val lansiaName = lansiaList
                        .filter { it.id in kunjungan.lansiaIds }
                        .joinToString(", ") { it.nama }

                    KunjunganItem(
                        kunjunganId = kunjungan.idKunjungan,
                        lansia = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Lansia : ")
                            }
                            append(lansiaName)
                        },
                        time = "${kunjungan.tanggal} - ${kunjungan.waktu}",
                        jenis = kunjungan.jenisKunjungan,
                        onClick = { id ->
                            navController.navigate("detail_kunjungan/$id")
                        },
                        onDelete = {
                            kunjunganToDelete = kunjungan.idKunjungan
                            showDeleteDialog = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("add_kunjungan") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp),
            containerColor = BiruTua.copy(alpha = 1.0f),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(30.dp))
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Konfirmasi Hapus", fontWeight = FontWeight.Bold, color = BiruMuda.copy(alpha = 1.0f)) },
                text = { Text("Apakah Anda yakin ingin menghapus Aktivitas ini?") },
                confirmButton = {
                    TextButton(onClick = {
                        kunjunganToDelete?.let { id ->
                            kunjunganViewModel.deleteKunjungan(id)
                        }
                        showDeleteDialog = false
                    }) { Text("Ya", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Tidak", color = BiruMuda.copy(alpha = 1.0f))
                    }
                }
            )
        }
    }
}

@Composable
fun KunjunganItem(
    kunjunganId: String,
    lansia: AnnotatedString,
    time: String,
    jenis: String,
    onClick: (String) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick(kunjunganId) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = lansia, fontSize = 20.sp, color = Color.Black)
            Row {
                Text("Waktu : ", fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                Text(time, fontSize = 16.sp, color = Color.DarkGray)
            }
            Text(
                text = "Jenis : $jenis",
                fontSize = 14.sp,
                color = Color(0xFF555555),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
        }
    }
}

