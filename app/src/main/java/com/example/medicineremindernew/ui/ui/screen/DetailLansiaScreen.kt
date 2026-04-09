package com.example.medicineremindernew.ui.ui.screen

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.navigation.NavHostController
import com.example.medicineremindernew.R
import com.example.medicineremindernew.ui.ui.theme.BiruAgakTua
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.example.medicineremindernew.ui.ui.viewmodel.HybridLansiaViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridObatViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailLansiaScreen(
    lansiaId: String,
    viewModel: HybridLansiaViewModel,
    obatViewModel: HybridObatViewModel,
    navController: NavHostController,
    onBackClick: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val lansia by viewModel.lansiaDetail.collectAsStateWithLifecycle()
    val obatList by obatViewModel.obatList.collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(lansiaId) {
        viewModel.getLansiaById(lansiaId)
        obatViewModel.loadObat()
    }

    if (lansia == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedObat by remember { mutableStateOf(listOf<String>()) }
    var namaLansia by remember { mutableStateOf("") }
    var golonganDarah by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var penyakit by remember { mutableStateOf("") }
    var tanggalLahir by remember { mutableStateOf(Date()) }

    LaunchedEffect(lansia) {
        lansia?.let {
            selectedObat = it.obatIds.toMutableList()
            namaLansia = it.nama
            golonganDarah = it.goldar
            gender = it.gender
            penyakit = it.penyakit
            tanggalLahir = it.lahir?.toDate() ?: Date()
        }
    }

    var expandedGolongan by remember { mutableStateOf(false) }
    var expandedGender by remember { mutableStateOf(false) }

    val golonganOptions = listOf("A", "B", "AB", "O")
    val genderOptions = listOf("Laki-Laki", "Perempuan")

    val context = LocalContext.current
    val blueColor = BiruMuda.copy(alpha = 1.0f)

    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        calendar.time = tanggalLahir
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                tanggalLahir = calendar.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
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
            Text("Update Lansia", color = Color.White, fontSize = 20.sp)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = namaLansia,
                    onValueChange = { namaLansia = it },
                    label = { Text("Nama Lansia") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text("Golongan Darah", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expandedGolongan,
                    onExpandedChange = { expandedGolongan = !expandedGolongan }) {
                    TextField(
                        readOnly = true,
                        value = golonganDarah,
                        onValueChange = {},
                        label = { Text("Pilih golongan darah") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGolongan) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGolongan,
                        onDismissRequest = { expandedGolongan = false }) {
                        golonganOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                golonganDarah = option
                                expandedGolongan = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Jenis Kelamin", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expandedGender,
                    onExpandedChange = { expandedGender = !expandedGender }) {
                    TextField(
                        readOnly = true,
                        value = if (gender == "L") "Laki-Laki" else "Perempuan",
                        onValueChange = {},
                        label = { Text("Pilih jenis kelamin") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGender,
                        onDismissRequest = { expandedGender = false }) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                gender = if (option == "Laki-Laki") "L" else "P"
                                expandedGender = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = penyakit,
                    onValueChange = { penyakit = it },
                    label = { Text("Penyakit") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text("Tanggal Lahir", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = blueColor)
                ) {
                    Text(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(tanggalLahir))
                }

                Spacer(modifier = Modifier.height(16.dp))
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
                Text("Obat untuk Lansia", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (obatList.isEmpty()) {
                    Text("Belum ada data obat", color = Color.Gray)
                } else {
                    obatList.forEach { obat ->
                        ReminderButton(
                            text = obat.nama,
                            onClick = {
                                selectedObat = if (selectedObat.contains(obat.id)) {
                                    (selectedObat - obat.id).toMutableList()
                                } else {
                                    (selectedObat + obat.id).toMutableList()
                                }
                            },
                            isSelected = selectedObat.contains(obat.id)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = {
                    if (namaLansia.isNotBlank()) {
                        val updatedLansia = lansia!!.copy(
                            nama = namaLansia,
                            goldar = golonganDarah,
                            gender = gender,
                            penyakit = penyakit,
                            lahir = Timestamp(tanggalLahir),
                            obatIds = selectedObat
                        )
                        viewModel.updateLansia(updatedLansia) { success ->
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Data berhasil diperbarui")
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar("Gagal memperbarui data")
                                }
                            }
                        }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Lengkapi semua data!") }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = blueColor)
            ) {
                Text("Update")
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = blueColor)
            ) {
                Text("Cancel")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
