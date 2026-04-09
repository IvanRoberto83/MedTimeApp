package com.example.medicineremindernew.ui.ui.screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicineremindernew.R
import com.example.medicineremindernew.ui.data.local.LocalDatabase
import com.example.medicineremindernew.ui.data.model.Kunjungan
import com.example.medicineremindernew.ui.data.network.NetworkUtils
import com.example.medicineremindernew.ui.data.repository.FirestoreRepository
import com.example.medicineremindernew.ui.data.repository.HybridLansiaRepository
import com.example.medicineremindernew.ui.data.repository.LansiaRepository
import com.example.medicineremindernew.ui.ui.theme.BiruAgakTua
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.example.medicineremindernew.ui.ui.theme.BiruTua
import com.example.medicineremindernew.ui.ui.theme.Krem
import com.example.medicineremindernew.ui.ui.viewmodel.HybridKunjunganViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridKunjunganViewModelFactory
import com.example.medicineremindernew.ui.ui.viewmodel.HybridLansiaViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridLansiaViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun AddKunjunganScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val firestoreRepo = remember { FirestoreRepository() }

    val warnaKrem = Krem.copy(alpha = 1.0f)
    val warnaBiru = BiruTua.copy(alpha = 1.0f)

    val lansiaRepository = remember {
        val firestoreLansiaRepo = LansiaRepository(firestoreRepository = firestoreRepo)
        HybridLansiaRepository(
            lansiaRepository = firestoreLansiaRepo,
            localDao = LocalDatabase.getDatabase(context).lansiaDao(),
            networkUtils = NetworkUtils(context)
        )
    }

    val lansiaViewModel: HybridLansiaViewModel = viewModel(
        factory = HybridLansiaViewModelFactory(lansiaRepository)
    )

    val lansiaList by lansiaViewModel.lansiaList.collectAsStateWithLifecycle()

    val kunjunganViewModel: HybridKunjunganViewModel = viewModel(
        factory = HybridKunjunganViewModelFactory(
            context = context,
            firestoreRepository = FirestoreRepository()
        )
    )

    var selectedLansia by remember { mutableStateOf<String?>(null) }
    var tanggal by remember { mutableStateOf("") }
    var waktu by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedJenisKunjungan by remember { mutableStateOf("") }
    var expandedJenis by remember { mutableStateOf(false) }

    val jenisOptions = listOf("Konsultasi ke Rumah Sakit", "Kegiatan Bersama")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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
                Text("Tambah Kunjungan", color = Color.White, fontSize = 20.sp)
            }

            val biru = BiruMuda.copy(alpha = 1.0f)

            SectionTitle("Waktu Kunjungan")
            CardSection {
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

                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, biru, shape = RoundedCornerShape(30.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiruMuda.copy(alpha = 0.0f),
                        contentColor = biru
                    ),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(text = if (tanggal.isEmpty()) "Pilih Tanggal" else "Tanggal: $tanggal")
                }

                Spacer(modifier = Modifier.height(8.dp))

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

                Button(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, biru, shape = RoundedCornerShape(30.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiruMuda.copy(alpha = 0.0f),
                        contentColor = biru
                    ),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(text = if (waktu.isEmpty()) "Pilih Waktu" else "Waktu: $waktu")
                }
            }

            SectionWithAddButton("Lansia", navController = navController)
            CardSection {
                if (lansiaList.isEmpty()) {
                    Text("Belum ada data lansia")
                } else {
                    lansiaList.forEach { lansia ->
                        ReminderButton(
                            text = lansia.nama,
                            onClick = { selectedLansia = if (selectedLansia == lansia.id) null else lansia.id },
                            isSelected = selectedLansia == lansia.id
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Jenis Kunjungan")
            CardSection {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, biru, shape = RoundedCornerShape(30.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { expandedJenis = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedJenisKunjungan.isEmpty()) "Pilih Jenis Kunjungan" else selectedJenisKunjungan,
                        color = if (selectedJenisKunjungan.isEmpty()) biru else biru,
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
                                selectedJenisKunjungan = option
                                expandedJenis = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (!selectedLansia.isNullOrEmpty() && tanggal.isNotEmpty() && waktu.isNotEmpty()&& selectedJenisKunjungan.isNotEmpty()) {
                            val kunjunganId = UUID.randomUUID().toString()
                            val kunjungan = Kunjungan(
                                idKunjungan = kunjunganId,
                                lansiaIds = listOf(selectedLansia!!),
                                tanggal = tanggal,
                                waktu = waktu,
                                jenisKunjungan = selectedJenisKunjungan

                            )
                            kunjunganViewModel.addKunjungan(kunjungan) { success ->
                                coroutineScope.launch {
                                    if (success) {
                                        snackbarHostState.showSnackbar("Kunjungan berhasil disimpan")
                                    } else {
                                        snackbarHostState.showSnackbar("Gagal menyimpan kunjungan")
                                    }
                                }
                            }

                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Harap lengkapi semua data")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .border(1.dp, biru, RoundedCornerShape(35.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiruMuda.copy(alpha = 0.0f),
                        contentColor = biru
                    )
                ) {
                    Text("Save")
                }

                Button(
                    onClick = {
                        selectedLansia = null
                        tanggal = ""
                        waktu = ""
                        selectedJenisKunjungan = ""
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Form telah direset")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .border(1.dp, biru, RoundedCornerShape(35.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BiruMuda.copy(alpha = 0.0f),
                        contentColor = biru
                    )
                ) {
                    Text("Clear")
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp)
            )
        }
    }
}
