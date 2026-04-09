import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.example.medicineremindernew.ui.ui.theme.BiruTua
import com.example.medicineremindernew.ui.ui.theme.Krem
import com.example.medicineremindernew.ui.ui.viewmodel.HybridObatViewModel

@Composable
fun ObatScreen(
    navController: NavController,
    obatViewModel: HybridObatViewModel
) {
    val obatList by obatViewModel.obatList.collectAsState(initial = emptyList())

    val warnaKrem = Krem.copy(alpha = 1.0f)
    val warnaBiru = BiruTua.copy(alpha = 1.0f)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var obatToDelete by remember { mutableStateOf<Obat?>(null) }

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
                text = "Daftar Obat",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = warnaBiru
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                if (obatList.isEmpty()) {
                    Text(
                        text = "Belum ada data obat",
                        color = warnaBiru,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    obatList.forEach { obat ->
                        ObatItem(
                            obat = obat,
                            onDeleteClick = {
                                obatToDelete = obat
                                showDeleteDialog = true
                            },
                            onItemClick = {
                                navController.navigate("detail_obat/${obat.id}")
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate("addObat") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp),
            containerColor = warnaBiru,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(30.dp))
        }

        if (showDeleteDialog && obatToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    obatToDelete = null
                },
                title = {
                    Text(
                        text = "Konfirmasi Hapus",
                        fontWeight = FontWeight.Bold,
                        color = BiruMuda.copy(alpha = 1.0f)
                    )
                },
                text = {
                    Text("Apakah Anda yakin ingin menghapus data obat \"${obatToDelete?.nama}\"?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            obatToDelete?.let { obat ->
                                obatViewModel.deleteObat(obat.id) { }
                            }
                            showDeleteDialog = false
                            obatToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Ya")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            obatToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = BiruMuda.copy(alpha = 1.0f))
                    ) {
                        Text("Tidak")
                    }
                }
            )
        }
    }
}

@Composable
fun ObatItem(
    obat: Obat,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = obat.nama, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "Deskripsi : ${obat.deskripsi}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "Jenis : ${obat.jenis}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "Dosis : ${obat.dosis}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "Waktu Minum : ${obat.waktuMinum}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "Stok : ${obat.stok}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "Keterangan : ${obat.catatan}", fontSize = 16.sp, color = Color.DarkGray)
        }


        IconButton(onClick = onDeleteClick) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
        }
    }
}

@Composable
fun AddObat(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = BiruTua.copy(alpha = 1.0f),
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Tambah",
            modifier = Modifier.size(30.dp)
        )
    }
}