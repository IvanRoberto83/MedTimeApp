package com.example.medicineremindernew.ui

import BottomNavigationBar
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.medicineremindernew.ui.data.local.LocalDatabase
import com.example.medicineremindernew.ui.data.network.NetworkUtils
import com.example.medicineremindernew.ui.data.repository.*
import com.example.medicineremindernew.ui.ui.navigation.NavGraph
import com.example.medicineremindernew.ui.ui.screen.SplashScreen
import com.example.medicineremindernew.ui.ui.theme.BiruTua
import com.example.medicineremindernew.ui.ui.theme.MedicineReminderNewTheme
import com.example.medicineremindernew.ui.ui.viewmodel.*
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.medicineremindernew.R
import com.example.medicineremindernew.ui.alarm.AlarmPopupActivity

class MainActivity : AppCompatActivity() {

    private lateinit var localDatabase: LocalDatabase
    private lateinit var networkUtils: NetworkUtils

    private lateinit var hybridReminderRepository: HybridReminderRepository
    private lateinit var hybridLansiaRepository: HybridLansiaRepository
    private lateinit var hybridObatRepository: HybridObatRepository
    private lateinit var hybridKunjunganRepository: HybridKunjunganRepository
    private lateinit var hybridRiwayatRepository: HybridRiwayatRepository

    private val firestoreRepository = FirestoreRepository()

    private val lansiaRepository by lazy { LansiaRepository(firestoreRepository) }
    private val obatRepository by lazy { ObatRepository(firestoreRepository) }
    private val reminderRepository by lazy { ReminderRepository(firestoreRepository) }
    private val kunjunganRepository by lazy { KunjunganRepository(firestoreRepository) }
    private val riwayatRepository by lazy { RiwayatRepository() }

    private lateinit var hybridObatViewModel: HybridObatViewModel
    private lateinit var hybridLansiaViewModel: HybridLansiaViewModel
    private lateinit var hybridReminderViewModel: HybridReminderViewModel
    private lateinit var hybridKunjunganViewModel: HybridKunjunganViewModel
    private lateinit var hybridRiwayatViewModel: HybridRiwayatViewModel

    private val lansiaViewModel: LansiaViewModel by viewModels {
        LansiaViewModelFactory(lansiaRepository)
    }

    private val obatViewModel: ObatViewModel by viewModels {
        ObatViewModelFactory(obatRepository)
    }

    private val reminderViewModel: ReminderViewModel by viewModels {
        ReminderViewModelFactory(reminderRepository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainApp(navController: NavHostController) {

        var isRefreshing by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            Log.d("MainActivity", "Checking for active alarms on app start...")
            AlarmPopupActivity.checkAndShowActiveAlarms(this@MainActivity)
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.mipmap.logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MedTime")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (!isRefreshing) {
                                    lifecycleScope.launch {
                                        refreshAllData()
                                    }
                                }
                            },
                            enabled = !isRefreshing
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Data",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BiruTua.copy(alpha = 1.0f),
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = { BottomNavigationBar(navController) }
        ) { innerPadding ->
            NavGraph(
                navController = navController,
                obatViewModel = hybridObatViewModel,
                lansiaViewModel = hybridLansiaViewModel,
                reminderViewModel = hybridReminderViewModel,
                kunjunganViewModel = hybridKunjunganViewModel,
                riwayatViewModel = hybridRiwayatViewModel,
                hybridReminderRepository = hybridReminderRepository,
                hybridLansiaRepository = hybridLansiaRepository,
                hybridObatRepository = hybridObatRepository,
                modifier = Modifier.padding(innerPadding)
            )

        }

        suspend fun refreshAllData() {
            isRefreshing = true
            try {
                Log.d("MainActivity", "Memulai refresh semua data...")

                kotlinx.coroutines.coroutineScope {
                    launch { hybridReminderViewModel.syncPendingData() }
                    launch { hybridLansiaViewModel.syncPendingData() }
                    launch { hybridObatViewModel.syncPendingData() }
                    launch { hybridKunjunganViewModel.syncPendingData() }
                }

                Log.d("MainActivity", "Refresh semua data selesai")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error saat refresh data", e)
            } finally {
                isRefreshing = false
            }
        }
    }

    private suspend fun refreshAllData() {
        try {
            Log.d("MainActivity", "Memulai refresh semua data...")

            hybridReminderViewModel.syncPendingData()
            hybridLansiaViewModel.syncPendingData()
            hybridObatViewModel.syncPendingData()
            hybridKunjunganViewModel.syncPendingData()

            Log.d("MainActivity", "Refresh semua data selesai")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saat refresh data", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        localDatabase = LocalDatabase.getDatabase(this)
        networkUtils = NetworkUtils(this)

        hybridReminderRepository = HybridReminderRepository(
            context = this,
            reminderRepository = reminderRepository,
            localDao = localDatabase.reminderDao(),
            networkUtils = networkUtils
        )

        hybridLansiaRepository = HybridLansiaRepository(
            lansiaRepository,
            localDatabase.lansiaDao(),
            networkUtils
        )

        hybridObatRepository = HybridObatRepository(
            obatRepository,
            localDatabase.obatDao(),
            networkUtils
        )

        hybridKunjunganRepository = HybridKunjunganRepository(
            context = this,
            kunjunganRepository = kunjunganRepository,
            localDao = localDatabase.kunjunganDao(),
            networkUtils = networkUtils
        )
        hybridRiwayatRepository = HybridRiwayatRepository(
            context = this,
            riwayatRepository = riwayatRepository,
            localDao = localDatabase.riwayatDao(),
            networkUtils = networkUtils
        )

        hybridObatViewModel = HybridObatViewModelFactory(hybridObatRepository)
            .create(HybridObatViewModel::class.java)

        hybridLansiaViewModel = HybridLansiaViewModelFactory(hybridLansiaRepository)
            .create(HybridLansiaViewModel::class.java)

        hybridReminderViewModel = HybridReminderViewModelFactory(hybridReminderRepository)
            .create(HybridReminderViewModel::class.java)

        hybridKunjunganViewModel = HybridKunjunganViewModelFactory(this, firestoreRepository)
            .create(HybridKunjunganViewModel::class.java)

        hybridRiwayatViewModel = HybridRiwayatViewModelFactory(hybridRiwayatRepository)
            .create(HybridRiwayatViewModel::class.java)

        createNotificationChannel(this)

        checkActiveAlarmFromIntent()

        lifecycleScope.launch {
            networkUtils.observeNetworkChanges().collect { isConnected ->
                if (isConnected) {
                    Log.d("MainActivity", "Internet tersedia, mulai sinkronisasi...")
                    syncAllPendingData()
                } else {
                    Log.d("MainActivity", "Internet tidak tersedia, mode offline")
                }
            }
        }

        requestIgnoreBatteryOptimization()

        setContent {
            MedicineReminderNewTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    val navController = rememberNavController()

                    var fadeIn by remember { mutableStateOf(false) }
                    val alphaAnim by animateFloatAsState(
                        targetValue = if (fadeIn) 1f else 0f,
                        animationSpec = tween(durationMillis = 500)
                    )

                    LaunchedEffect(Unit) {
                        fadeIn = true
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BiruTua.copy(alpha = 1.0f))
                            .alpha(alphaAnim)
                    ) {
                        MainApp(navController)
                    }
                }
            }
        }
    }

    private fun checkActiveAlarmFromIntent() {

        val checkActiveAlarms = intent?.getBooleanExtra("check_active_alarms", false) ?: false
        val reminderIds = intent?.getStringArrayListExtra("reminderIds")

        val checkActiveAlarm = intent?.getBooleanExtra("check_active_alarm", false) ?: false
        val reminderId = intent?.getStringExtra("reminderId")

        when {
            checkActiveAlarms && !reminderIds.isNullOrEmpty() -> {
                Log.d("MainActivity", "Notification clicked with ${reminderIds.size} reminder IDs: $reminderIds")
                AlarmPopupActivity.checkAndShowActiveAlarms(this)
            }
            checkActiveAlarm && !reminderId.isNullOrBlank() -> {
                Log.d("MainActivity", "Notification clicked with single reminder ID: $reminderId")
                AlarmPopupActivity.checkAndShowActiveAlarms(this)
            }
            else -> {
                AlarmPopupActivity.checkAndShowActiveAlarms(this)
                Log.d("MainActivity", "Checked for stored active alarms...")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        AlarmPopupActivity.checkAndShowActiveAlarms(this)
        Log.d("MainActivity", "onResume - Checking for active alarms...")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        val checkActiveAlarms = intent?.getBooleanExtra("check_active_alarms", false) ?: false
        val reminderIds = intent?.getStringArrayListExtra("reminderIds")

        val checkActiveAlarm = intent?.getBooleanExtra("check_active_alarm", false) ?: false
        val singleReminderId = intent?.getStringExtra("reminderId")

        when {
            checkActiveAlarms && !reminderIds.isNullOrEmpty() -> {
                Log.d("MainActivity", "onNewIntent - Detected ${reminderIds.size} active reminders from notification: $reminderIds")
                AlarmPopupActivity.checkAndShowActiveAlarms(this)
            }
            checkActiveAlarm && !singleReminderId.isNullOrBlank() -> {
                Log.d("MainActivity", "onNewIntent - Detected single active reminder from notification: $singleReminderId")
                AlarmPopupActivity.checkAndShowActiveAlarms(this)
            }
            else -> {
                Log.d("MainActivity", "onNewIntent - Checking for any stored active alarms...")
                AlarmPopupActivity.checkAndShowActiveAlarms(this)
            }
        }
    }

    private suspend fun syncAllPendingData() {
        try {
            hybridReminderRepository.syncPendingData()
            hybridLansiaRepository.syncPendingData()
            hybridObatRepository.syncPendingData()
            Log.d("MainActivity", "Sinkronisasi selesai")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saat sinkronisasi", e)
        }
    }

    private fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Alarm Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk alarm pengingat obat"
                enableVibration(true)
                setSound(
                    android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM),
                    null
                )
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            Log.d("MainActivity", "Notification channel created successfully")
        }
    }
}