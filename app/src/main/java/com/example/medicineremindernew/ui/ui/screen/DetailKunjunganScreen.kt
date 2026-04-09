package com.example.medicineremindernew.ui.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.medicineremindernew.R
import com.example.medicineremindernew.ui.data.model.Kunjungan
import com.example.medicineremindernew.ui.data.model.Lansia
import com.example.medicineremindernew.ui.ui.theme.BiruAgakTua
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.example.medicineremindernew.ui.ui.viewmodel.HybridKunjunganViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridLansiaViewModel
import kotlinx.coroutines.launch
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailKunjunganScreen(
    kunjunganId: String,
    navController: NavController,
    kunjunganViewModel: HybridKunjunganViewModel,
    lansiaViewModel: HybridLansiaViewModel,
    onBackClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val kunjungan by kunjunganViewModel.kunjunganDetail.collectAsStateWithLifecycle(initialValue = null)
    val lansiaList by lansiaViewModel.lansiaList.collectAsStateWithLifecycle(initialValue = emptyList())

    var jenisKunjungan by remember { mutableStateOf("") }
    var expandedJenis by remember { mutableStateOf(false) }
    val jenisOptions = listOf("Konsultasi ke Rumah Sakit", "Kegiatan Bersama")


    val biru = BiruMuda.copy(alpha = 1.0f)

    LaunchedEffect(kunjungan) {
        jenisKunjungan = kunjungan?.jenisKunjungan ?: ""
    }

    LaunchedEffect(kunjunganId) {
        kunjunganViewModel.getKunjunganById(kunjunganId)
    }

    if (kunjungan == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedLansia by remember { mutableStateOf(kunjungan?.lansiaIds?.firstOrNull()) }
    var tanggal by remember { mutableStateOf(kunjungan?.tanggal ?: "") }
    var waktu by remember { mutableStateOf(kunjungan?.waktu ?: "") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                tanggal = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                waktu = String.format("%02d:%02d", hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(16.dp)
                .background(BiruAgakTua.copy(alpha = 1.0f)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(
                    painter = painterResource(id = R.drawable.back_white),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text("Update Kunjungan", color = Color.White, fontSize = 20.sp)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, BiruMuda.copy(alpha = 1.0f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = biru
                    )
                ) {
                    Text(if (tanggal.isEmpty()) "Pilih Tanggal" else "Tanggal: $tanggal")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, BiruMuda.copy(alpha = 1.0f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = biru
                    )
                ) {
                    Text(if (waktu.isEmpty()) "Pilih Waktu" else "Waktu: $waktu")
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pilih Lansia", fontWeight = FontWeight.Bold)
                if (lansiaList.isEmpty()) {
                    Text("Belum ada data lansia")
                } else {
                    lansiaList.forEach { lansia: Lansia ->
                        ReminderButton(
                            text = lansia.nama,
                            onClick = { selectedLansia = lansia.id },
                            isSelected = selectedLansia == lansia.id
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Jenis Kunjungan", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, biru, shape = RoundedCornerShape(30.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { expandedJenis = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (jenisKunjungan.isEmpty()) "Pilih Jenis Kunjungan" else jenisKunjungan,
                        color = if (jenisKunjungan.isEmpty()) biru else biru,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                DropdownMenu(
                    expanded = expandedJenis,
                    onDismissRequest = { expandedJenis = false },
                ) {
                    jenisOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                jenisKunjungan = option
                                expandedJenis = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = {
                    if (!selectedLansia.isNullOrEmpty() && tanggal.isNotEmpty() && waktu.isNotEmpty()) {
                        val updatedKunjungan = kunjungan!!.copy(
                            lansiaIds = listOf(selectedLansia!!),
                            tanggal = tanggal,
                            waktu = waktu,
                            jenisKunjungan = jenisKunjungan

                        )
                        kunjunganViewModel.updateKunjungan(updatedKunjungan) { success ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Data kunjungan berhasil diperbarui"
                                    else "Gagal memperbarui kunjungan"
                                )
                                if (success) onBackClick()
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Harap lengkapi semua data")
                        }
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BiruMuda.copy(alpha = 1.0f))
            ) {
                Text("Update")
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BiruMuda.copy(alpha = 1.0f))
            ) {
                Text("Cancel")
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(8.dp))
    }
}
