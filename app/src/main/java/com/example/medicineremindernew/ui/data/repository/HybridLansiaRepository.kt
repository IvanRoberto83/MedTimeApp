package com.example.medicineremindernew.ui.data.repository

import android.util.Log
import com.example.medicineremindernew.ui.data.dao.LocalLansiaDao
import com.example.medicineremindernew.ui.data.entity.LocalLansiaEntity
import com.example.medicineremindernew.ui.data.model.Lansia
import com.example.medicineremindernew.ui.data.network.NetworkUtils
import com.google.firebase.Timestamp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HybridLansiaRepository(
    private val lansiaRepository: LansiaRepository,
    private val localDao: LocalLansiaDao,
    private val networkUtils: NetworkUtils
) {
    suspend fun addLansia(lansia: Lansia): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                lansiaRepository.addLansia(lansia) { /* callback optional */ }
                true
            } else {
                val localEntity = LocalLansiaEntity(
                    id = lansia.id,
                    nama = lansia.nama,
                    goldar = lansia.goldar,
                    gender = lansia.gender,
                    lahir = lansia.lahir?.toDate().toString(),
                    penyakit = lansia.penyakit,
                    isSynced = false
                )
                localDao.insertLansia(localEntity)
                true
            }
        } catch (e: Exception) {
            Log.e("HybridLansiaRepo", "Add failed: ${e.message}")
            false
        }
    }

    suspend fun updateLansia(lansia: Lansia): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                lansiaRepository.updateLansia(lansia) { /* callback optional */ }
                true
            } else {
                val localEntity = LocalLansiaEntity(
                    id = lansia.id,
                    nama = lansia.nama,
                    goldar = lansia.goldar,
                    gender = lansia.gender,
                    lahir = lansia.lahir?.toDate().toString(),
                    penyakit = lansia.penyakit,
                    isSynced = false
                )
                localDao.updateLansia(localEntity)
                true
            }
        } catch (e: Exception) {
            Log.e("HybridLansiaRepo", "Update failed: ${e.message}")
            false
        }
    }

    suspend fun deleteLansia(id: String): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                lansiaRepository.deleteLansia(id)
            } else {
                localDao.deleteLansia(id)
            }
            true
        } catch (e: Exception) {
            Log.e("HybridLansiaRepo", "Delete failed: ${e.message}")
            false
        }
    }

    suspend fun getAllLansia(): List<Lansia> {
        return if (networkUtils.isNetworkAvailable()) {
            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                var isResumed = false
                lansiaRepository.getAllLansia { list ->
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(list) {}
                    }
                }
            }
        } else {
            getLocalLansia()
        }
    }

    private suspend fun getLocalLansia(): List<Lansia> {
        return localDao.getAllLansia().map {
            Lansia(
                id = it.id,
                nama = it.nama,
                goldar = it.goldar,
                gender = it.gender,
                lahir = parseDateToTimestamp(it.lahir),
                penyakit = it.penyakit
            )
        }
    }

    suspend fun syncPendingData() {
        if (!networkUtils.isNetworkAvailable()) return

        val unsynced = localDao.getUnsyncedLansia()
        unsynced.forEach { entity ->
            val lansia = Lansia(
                id = entity.id,
                nama = entity.nama,
                goldar = entity.goldar,
                gender = entity.gender,
                lahir = parseDateToTimestamp(entity.lahir),
                penyakit = entity.penyakit
            )

            suspendCoroutine<Unit> { continuation ->
                lansiaRepository.addLansia(lansia) { success ->
                    if (success) {
                        runBlocking {
                            localDao.markAsSynced(entity.id)
                            Log.d("HybridLansiaRepo", "Synced lansia: ${entity.id}")
                        }
                    }
                    continuation.resume(Unit)
                }
            }
        }
    }

    private fun parseDateToTimestamp(dateString: String): Timestamp {
        return try {
            val format = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            val date = format.parse(dateString)
            Timestamp(date!!)
        } catch (e: Exception) {
            Log.e("HybridLansiaRepo", "Date parsing failed: ${e.message}")
            Timestamp.now()
        }
    }
}
