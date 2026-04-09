package com.example.medicineremindernew.ui.ui.screen

import android.app.Application
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicineremindernew.R
import com.example.medicineremindernew.R.drawable.back_white
import com.example.medicineremindernew.ui.data.model.Reminder
import com.example.medicineremindernew.ui.ui.theme.AbuMenu
import com.example.medicineremindernew.ui.ui.viewmodel.HybridLansiaViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridObatViewModel
import com.example.medicineremindernew.ui.ui.viewmodel.HybridReminderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.medicineremindernew.ui.ui.theme.BiruAgakTua
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

@Composable
fun AddReminderScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    obatViewModel: HybridObatViewModel = viewModel(),
    lansiaViewModel: HybridLansiaViewModel = viewModel(),
    reminderViewModel: HybridReminderViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val lansiaList by lansiaViewModel.lansiaList.collectAsStateWithLifecycle()
    val obatList by obatViewModel.obatList.collectAsStateWithLifecycle()

    var selectedLansia by remember { mutableStateOf<String?>(null) }
    var selectedObat by remember { mutableStateOf(setOf<String>()) }
    var tanggal by remember { mutableStateOf("") }
    var waktu by remember { mutableStateOf("") }

    val pengulanganOptions = listOf("Harian", "Mingguan", "Satu Kali")
    var selectedPengulangan by remember { mutableStateOf(pengulanganOptions.first()) }

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedLansiaId by remember { mutableStateOf<String?>(null) }
    var selectedObatId by remember { mutableStateOf<String?>(null) }




    Box(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        painter = painterResource(id = back_white),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text("Tambah Pengingat", color = Color.White, fontSize = 20.sp)
            }

            val biru = BiruMuda.copy(alpha = 1.0f)

            SectionTitle("Pengingat")
            CardSection {
                val calendar = remember { Calendar.getInstance() }

                val datePickerDialog = remember {
                    android.app.DatePickerDialog(
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
                    android.app.TimePickerDialog(
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

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Pengulangan", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                DropdownMenuField(pengulanganOptions, selectedPengulangan) {
                    selectedPengulangan = it
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
                            onClick = {
                                selectedLansia = if (selectedLansia == lansia.id) null else lansia.id
                                selectedObat = emptySet()
                            },
                            isSelected = selectedLansia == lansia.id
                        )
                    }
                }
            }

            SectionWithAddButton("Obat", navController = navController)
            CardSection {
                if (selectedLansia == null) {
                    Text(
                        "Silahkan pilih lansia terlebih dahulu",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    val lansia = lansiaList.find { it.id == selectedLansia }
                    val obatFiltered = obatList.filter { lansia?.obatIds?.contains(it.id) == true }

                    if (obatFiltered.isEmpty()) {
                        Text(
                            "Lansia ini belum memiliki obat yang ditandai",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        obatFiltered.forEach { obat ->
                            ReminderButton(
                                text = obat.nama,
                                onClick = {
                                    selectedObat = if (selectedObat.contains(obat.id)) {
                                        selectedObat - obat.id
                                    } else {
                                        selectedObat + obat.id
                                    }
                                },
                                isSelected = selectedObat.contains(obat.id)
                            )
                        }
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
                        if (!selectedLansia.isNullOrEmpty() && selectedObat.isNotEmpty() &&
                            tanggal.isNotEmpty() && waktu.isNotEmpty()) {

                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            val dateTimeString = "$tanggal $waktu"
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val date = sdf.parse(dateTimeString)
                            val timeInMillis = date?.time ?: 0L

                            coroutineScope.launch {

                                val reminderId = UUID.randomUUID().toString()
                                val reminder = Reminder(
                                    id = reminderId,
                                    obatIds = selectedObat.toList(),
                                    lansiaIds = listOf(selectedLansia!!),
                                    tanggal = tanggal,
                                    waktu = waktu,
                                    pengulangan = selectedPengulangan
                                )



                                reminderViewModel.addReminder(reminder) { success ->
                                    if (success) {
                                        coroutineScope.launch {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                                                if (!alarmManager.canScheduleExactAlarms()) {
                                                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                                    context.startActivity(intent)
                                                    return@launch
                                                }
                                            }

                                            com.example.medicineremindernew.ui.alarm.AlarmUtils.scheduleRecurringReminder(
                                                context = context,
                                                reminderId = reminderId,
                                                timeInMillis = timeInMillis,
                                                recurrenceType = selectedPengulangan
                                            )

                                            val oneHourBefore = timeInMillis - 60 * 60 * 1000
                                            if (oneHourBefore > System.currentTimeMillis()) {
                                                com.example.medicineremindernew.ui.alarm.AlarmUtils.scheduleRecurringReminder(
                                                    context = context,
                                                    reminderId = reminderId + "_before",
                                                    timeInMillis = oneHourBefore,
                                                    recurrenceType = selectedPengulangan
                                                )
                                            }

                                            snackbarHostState.showSnackbar("Reminder berhasil disimpan & alarm berulang dijadwalkan")
                                            navController.popBackStack()
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Gagal menyimpan reminder")
                                        }
                                    }
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Harap lengkapi semua data")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp).border(1.dp, biru, shape = RoundedCornerShape(35.dp)),
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
                        selectedObat = emptySet()
                        tanggal = ""
                        waktu = ""
                        selectedPengulangan = pengulanganOptions.first()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Form telah direset")
                        }
                        onClearClick()
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp).border(1.dp, biru, shape = RoundedCornerShape(35.dp)),
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
                modifier = Modifier.fillMaxWidth().padding(50.dp)
            )
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AbuMenu)
            .padding(16.dp)
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionWithAddButton(
    title: String,
    navController: NavController
) {
    val route = when (title) {
        "Obat" -> "addObat"
        "Lansia" -> "addlansia"
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AbuMenu)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Button(
            onClick = { navController.navigate(route) },
            colors = ButtonDefaults.buttonColors(
                containerColor = BiruAgakTua.copy(alpha = 1.0f),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.plus_black),
                contentDescription = "Tambah $title",
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun CardSection(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun DropdownMenuField(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedOption.ifEmpty { "Pilih" }, color = BiruMuda.copy(alpha = 1.0f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun ReminderButton(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    val backgroundColor = if (isSelected) BiruMuda.copy(alpha = 1.0f) else BiruMuda.copy(alpha = 0.0f)
    val contentColor = if (isSelected) Color.Black else Color.White

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text)
            if (isSelected) {
                Icon(
                    painter = painterResource(id = android.R.drawable.checkbox_on_background),
                    contentDescription = "Selected",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

