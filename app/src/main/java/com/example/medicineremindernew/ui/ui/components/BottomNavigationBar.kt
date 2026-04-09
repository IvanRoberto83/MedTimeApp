
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.medicineremindernew.ui.ui.navigation.BottomNavItem
import com.example.medicineremindernew.ui.ui.theme.BiruMuda
import com.example.medicineremindernew.ui.ui.theme.BiruTua

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Lansia,
        BottomNavItem.Obat,
        BottomNavItem.Kunjungan,
        BottomNavItem.Riwayat
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier, // Transparansi 50%
        containerColor = BiruTua.copy(alpha = 1.0f),
        tonalElevation = 7.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) Color.White else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 11.sp
                    )
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(item.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = BiruMuda.copy(alpha = 0.7f), // indikator juga transparan
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White
                )
            )
        }
    }
}
