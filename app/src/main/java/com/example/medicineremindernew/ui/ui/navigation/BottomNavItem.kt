package com.example.medicineremindernew.ui.ui.navigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Lansia : BottomNavItem("lansia", "Lansia", Icons.Default.Person)
    object Obat : BottomNavItem("obat", "Obat", Icons.Default.Medication)
    object Kunjungan : BottomNavItem("kunjungan", "Aktivitas", Icons.Default.DriveEta)
    object Riwayat : BottomNavItem("riwayat", "Riwayat", Icons.Default.History)

}