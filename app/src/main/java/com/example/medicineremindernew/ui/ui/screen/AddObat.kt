package com.example.medicineremindernew.ui.ui.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicineremindernew.R.drawable.back_white
import com.example.medicineremindernew.ui.data.model.Obat
import com.example.medicineremindernew.ui.ui.theme.BiruAgakTua
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.example.medicineremindernew.ui.ui.viewmodel.HybridObatViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AddObatScreen(
    viewModel: HybridObatViewModel,
    onBackClick: () -> Unit = {}
) {
    var namaObat by remember { mutableStateOf("") }
    var jenisObat by remember { mutableStateOf("Tablet") }
    var satuanDosis by remember { mutableStateOf("mg") }
    var waktuMinum by remember { mutableStateOf("Sebelum Makan") }
    var notes by remember { mutableStateOf("") }
    var pertamaKonsumsi by remember { mutableStateOf<Date?>(null) }
    var stok by remember { mutableStateOf("") }
    var takaranDosis by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("satuan_dosis", Context.MODE_PRIVATE)

    val defaultSatuan = listOf("mg", "ml", "IU", "Tetes")
    val savedSatuan = sharedPrefs.getStringSet("custom_satuan", emptySet())?.toList() ?: emptyList()
    var listSatuanDosis by remember { mutableStateOf((defaultSatuan + savedSatuan).distinct().toMutableList()) }
    var inputSatuanBaru by remember { mutableStateOf(false) }
    var satuanBaru by remember { mutableStateOf("") }

    val blueColor = BiruMuda.copy(alpha = 1.0f)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            pertamaKonsumsi = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun saveSatuanToPrefs(newSatuan: String) {
        val currentSaved = sharedPrefs.getStringSet("custom_satuan", emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSaved.add(newSatuan)
        sharedPrefs.edit().putStringSet("custom_satuan", currentSaved).apply()
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
                    painter = painterResource(id = back_white),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text("Tambah Obat", color = Color.White, fontSize = 20.sp)
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
                    value = namaObat,
                    onValueChange = { namaObat = it },
                    placeholder = { Text("Nama Obat") },
                    label = { Text("Masukkan Nama Obat") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    singleLine = true
                )

                Text("Jenis Obat", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                DropdownMenuField(
                    options = listOf("Tablet", "Sirup", "Salep", "Tetes", "Kapsul"),
                    selectedOption = jenisObat,
                    onOptionSelected = { jenisObat = it }
                )

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    placeholder = { Text("Deskripsi Obat") },
                    label = { Text("Masukkan Deskripsi Obat") },
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text("Dosis Obat", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedTextField(
                        value = takaranDosis,
                        onValueChange = {takaranDosis = it},
                        placeholder = {Text ("Takaran")},
                        modifier = Modifier.weight(1f).height(56.dp),
                        singleLine = true
                    )

                    DropdownMenuField(
                        options = listSatuanDosis,
                        selectedOption = satuanDosis,
                        onOptionSelected = { selected ->
                            satuanDosis = selected
                            inputSatuanBaru = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .aspectRatio(1f)
                            .border(width = 1.dp, color = BiruMuda.copy(alpha = 1.0f), shape = RoundedCornerShape(12.dp))
                    ) {
                        OutlinedIconButton(
                            onClick = {
                                inputSatuanBaru = !inputSatuanBaru
                                satuanBaru = ""
                            },
                            modifier = Modifier.size(56.dp),
                            border = BorderStroke(1.dp, BiruMuda.copy(alpha = 1.0f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Satuan Baru",
                                tint = BiruMuda.copy(alpha = 1.0f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (inputSatuanBaru) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = satuanBaru,
                            onValueChange = { satuanBaru = it },
                            placeholder = { Text("Satuan baru") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (satuanBaru.isNotBlank() && !listSatuanDosis.contains(satuanBaru)) {
                                    listSatuanDosis = (listSatuanDosis + satuanBaru).toMutableList()
                                    saveSatuanToPrefs(satuanBaru)
                                    satuanDosis = satuanBaru
                                    satuanBaru = ""
                                    inputSatuanBaru = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Satuan '$satuanDosis' berhasil ditambahkan!")
                                    }
                                } else if (listSatuanDosis.contains(satuanBaru)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Satuan sudah ada!")
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Satuan tidak boleh kosong!")
                                    }
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = BiruMuda.copy(alpha = 1.0f)
                            ),
                            border = BorderStroke(1.dp, BiruMuda.copy(alpha = 1.0f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(
                                "Add",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text("Waktu Minum Obat", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                DropdownMenuField(
                    options = listOf("Sebelum Makan", "Sesudah Makan", "Bersamaan Makan"),
                    selectedOption = waktuMinum,
                    onOptionSelected = { waktuMinum = it }
                )

                Spacer(modifier = Modifier.height(15.dp))

                Text("Tanggal Pertama Konsumsi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                val biru = BiruMuda.copy(alpha = 1.0f)
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = biru),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(biru)
                    )
                ) {
                    Text(
                        pertamaKonsumsi?.let {
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(it)
                        } ?: "Tanggal Pertama Konsumsi"
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Catatan Tambahan") },
                    label = { Text("Masukkan Catatan Tambahan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(bottom = 2.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    value = stok,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) stok = it
                    },
                    placeholder = { Text("Stok Obat") },
                    label = { Text("Masukkan Stok Obat") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    singleLine = true
                )

            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val biru = BiruMuda.copy(alpha = 1.0f)

            OutlinedButton(
                onClick = {
                    if (namaObat.isBlank() || jenisObat.isBlank() || satuanDosis.isBlank() || waktuMinum.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Data obat tidak boleh kosong!")
                        }
                    } else {
                        val newObat = Obat(
                            id = UUID.randomUUID().toString(),
                            nama = namaObat,
                            jenis = jenisObat,
                            deskripsi = deskripsi,
                            dosis = satuanDosis,
                            waktuMinum = waktuMinum,
                            pertamaKonsumsi = pertamaKonsumsi?.let { Timestamp(it) },
                            catatan = notes,
                            stok = stok.toIntOrNull() ?: 0

                        )

                        viewModel.addObat(newObat) { success ->
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Data Obat berhasil disimpan")
                                    namaObat = ""
                                    jenisObat = "Tablet"
                                    deskripsi = ""
                                    satuanDosis = "mg"
                                    waktuMinum = "Sebelum Makan"
                                    notes = ""
                                    stok = ""
                                    pertamaKonsumsi = null
                                    inputSatuanBaru = false
                                    satuanBaru = ""
                                } else {
                                    snackbarHostState.showSnackbar("Gagal menyimpan data")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = biru),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(biru)
                )
            ) {
                Text("Save")
            }

            OutlinedButton(
                onClick = {
                    namaObat = ""
                    jenisObat = "Tablet"
                    deskripsi = ""
                    satuanDosis = "mg"
                    waktuMinum = "Sebelum Makan"
                    notes = ""
                    stok = ""
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = biru),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(biru)
                )
            ) {
                Text("Clear")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuField(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}