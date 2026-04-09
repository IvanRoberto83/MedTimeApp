    package com.example.medicineremindernew.ui.ui.navigation

    import ObatScreen
    import android.os.Build
    import androidx.annotation.RequiresApi
    import androidx.compose.runtime.Composable
    import androidx.compose.ui.Modifier
    import androidx.navigation.NavHostController
    import androidx.navigation.NavType
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.navArgument
    import com.example.medicineremindernew.ui.data.repository.HybridLansiaRepository
    import com.example.medicineremindernew.ui.data.repository.HybridObatRepository
    import com.example.medicineremindernew.ui.data.repository.HybridReminderRepository
    import com.example.medicineremindernew.ui.ui.screen.AddKunjunganScreen
    import com.example.medicineremindernew.ui.ui.screen.AddLansiaScreen
    import com.example.medicineremindernew.ui.ui.screen.AddObatScreen
    import com.example.medicineremindernew.ui.ui.screen.AddReminderScreen
    import com.example.medicineremindernew.ui.ui.screen.DetailKunjunganScreen
    import com.example.medicineremindernew.ui.ui.screen.DetailLansiaScreen
    import com.example.medicineremindernew.ui.ui.screen.DetailObatScreen
    import com.example.medicineremindernew.ui.ui.screen.DetailReminderScreen
    import com.example.medicineremindernew.ui.ui.screen.RiwayatScreen
    import com.example.medicineremindernew.ui.ui.screen.HomeScreen
    import com.example.medicineremindernew.ui.ui.screen.KunjunganScreen
    import com.example.medicineremindernew.ui.ui.screen.LansiaScreen
    import com.example.medicineremindernew.ui.ui.viewmodel.HybridKunjunganViewModel
    import com.example.medicineremindernew.ui.ui.viewmodel.HybridLansiaViewModel
    import com.example.medicineremindernew.ui.ui.viewmodel.HybridObatViewModel
    import com.example.medicineremindernew.ui.ui.viewmodel.HybridReminderViewModel
    import com.example.medicineremindernew.ui.ui.viewmodel.HybridRiwayatViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun NavGraph(
        navController: NavHostController,
        obatViewModel: HybridObatViewModel,
        lansiaViewModel: HybridLansiaViewModel,
        reminderViewModel: HybridReminderViewModel,
        kunjunganViewModel : HybridKunjunganViewModel,
        riwayatViewModel : HybridRiwayatViewModel,
        modifier: Modifier = Modifier,
        hybridReminderRepository: HybridReminderRepository,
        hybridLansiaRepository: HybridLansiaRepository,
        hybridObatRepository: HybridObatRepository
    ) {
        NavHost(navController, startDestination = "home",modifier = modifier) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    navController = navController,
                    reminderViewModel = reminderViewModel,
                    lansiaViewModel = lansiaViewModel,
                    obatViewModel = obatViewModel
                )
            }

            composable("add_reminder") {
                AddReminderScreen(
                    onBackClick = { navController.popBackStack() },
                    navController = navController,
                    obatViewModel = obatViewModel,
                    lansiaViewModel = lansiaViewModel,
                    reminderViewModel = reminderViewModel,
                )
            }

            composable("addObat") {
                AddObatScreen(
                    viewModel = obatViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("add_kunjungan"){
                AddKunjunganScreen(
                    navController = navController,
                    modifier = modifier,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("addlansia") {
                AddLansiaScreen(
                    viewModel = lansiaViewModel,
                    obatViewModel = obatViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("lansia") {
                LansiaScreen(
                    navController = navController,
                    lansiaViewModel = lansiaViewModel,
                    obatViewModel = obatViewModel
                )
            }

            composable(BottomNavItem.Obat.route) { ObatScreen(navController, obatViewModel) }
            composable(BottomNavItem.Riwayat.route) { RiwayatScreen(navController,riwayatViewModel,lansiaViewModel,obatViewModel,) }
            composable(BottomNavItem.Kunjungan.route) { KunjunganScreen(navController, kunjunganViewModel, lansiaViewModel) }

            composable(
                "detail_lansia/{lansiaId}",
                arguments = listOf(navArgument("lansiaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val lansiaId = backStackEntry.arguments?.getString("lansiaId") ?: return@composable
                DetailLansiaScreen(
                    lansiaId = lansiaId,
                    viewModel = lansiaViewModel,
                    obatViewModel = obatViewModel,
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "detail_kunjungan/{kunjunganId}",
                arguments = listOf(navArgument("kunjunganId") { type = NavType.StringType })
            ) { backStackEntry ->
                val kunjunganId = backStackEntry.arguments?.getString("kunjunganId") ?: return@composable
                DetailKunjunganScreen(
                    kunjunganId = kunjunganId,
                    navController = navController,
                    kunjunganViewModel = kunjunganViewModel,
                    lansiaViewModel = lansiaViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("detail_obat/{obatId}") { backStackEntry ->
                val obatId = backStackEntry.arguments?.getString("obatId") ?: return@composable
                DetailObatScreen(
                    obatId = obatId,
                    viewModel = obatViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "detail_reminder/{reminderId}",
                arguments = listOf(navArgument("reminderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reminderId = backStackEntry.arguments?.getString("reminderId") ?: return@composable
                DetailReminderScreen(
                    reminderId = reminderId,
                    navController = navController,
                    reminderViewModel = reminderViewModel,
                    lansiaViewModel = lansiaViewModel,
                    obatViewModel = obatViewModel,
                    onBackClick = { navController.popBackStack() },
                    onUpdateClick = { navController.popBackStack() }
                )
            }
        }
    }

