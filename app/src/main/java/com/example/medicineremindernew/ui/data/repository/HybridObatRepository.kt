package com.example.medicineremindernew.ui.data.repository

import android.util.Log
import com.example.medicineremindernew.ui.data.dao.LocalObatDao
import com.example.medicineremindernew.ui.data.entity.LocalObatEntity
import com.example.medicineremindernew.ui.data.model.Lansia
import com.example.medicineremindernew.ui.data.model.Obat
import com.example.medicineremindernew.ui.data.network.NetworkUtils
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HybridObatRepository(
    private val obatRepository: ObatRepository,
    private val localDao: LocalObatDao,
    private val networkUtils: NetworkUtils
) {
    suspend fun addObat(obat: Obat): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                obatRepository.addObat(obat) { /* callback optional */ }
                true
            } else {
                val localEntity = LocalObatEntity(
                    id = obat.id,
                    nama = obat.nama,
                    deskripsi = obat.deskripsi,
                    jenis = obat.jenis,
                    takaranDosis = obat.takaranDosis,
                    dosis = obat.dosis,
                    waktuMinum = obat.waktuMinum,
                    catatan = obat.catatan,
                    stok = obat.stok,
                    isSynced = false
                )
                localDao.insertObat(localEntity)
                true
            }
        } catch (e: Exception) {
            Log.e("HybridObatRepo", "Add failed: ${e.message}")
            false
        }
    }

    suspend fun updateObat(obat: Obat): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                val success = obatRepository.updateObat(obat)
                success
            } else {
                val id = obat.id ?: return false

                val existing = localDao.getAllObat().find { it.id == id }
                if (existing == null) {
                    Log.e("HybridObatRepo", "Obat ID $id tidak ditemukan di lokal")
                    return false
                }

                val localEntity = LocalObatEntity(
                    id = id,
                    nama = obat.nama,
                    deskripsi = obat.deskripsi,
                    jenis = obat.jenis,
                    takaranDosis = obat.takaranDosis,
                    dosis = obat.dosis,
                    waktuMinum = obat.waktuMinum,
                    catatan = obat.catatan,
                    stok = obat.stok,
                    isSynced = false
                )
                localDao.updateObat(localEntity)
                true
            }
        } catch (e: Exception) {
            Log.e("HybridObatRepo", "Update failed: ${e.message}", e)
            false
        }
    }

    suspend fun deleteObat(id: String): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                obatRepository.deleteObat(id)
            } else {
                localDao.deleteObat(id)
            }
            true
        } catch (e: Exception) {
            Log.e("HybridObatRepo", "Delete failed: ${e.message}")
            false
        }
    }

    suspend fun getAllObat(): List<Obat> {
        return if (networkUtils.isNetworkAvailable()) {
            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                var isResumed = false
                obatRepository.getAllObat { list ->
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(list) {}
                    }
                }
            }
        } else {
            getLocalObat()
        }
    }

    private suspend fun getLocalObat(): List<Obat> {
        return localDao.getAllObat().map {
            Obat(
                id = it.id,
                nama = it.nama,
                deskripsi = it.deskripsi,
                jenis = it.jenis,
                takaranDosis = it.takaranDosis,
                dosis = it.dosis,
                waktuMinum = it.waktuMinum,
                catatan = it.catatan,
                stok = it.stok
            )
        }
    }

    suspend fun syncPendingData() {
        if (!networkUtils.isNetworkAvailable()) return

        val unsynced = localDao.getUnsyncedObat()
        unsynced.forEach { entity ->
            val obat = Obat(
                id = entity.id,
                nama = entity.nama,
                deskripsi = entity.deskripsi,
                jenis = entity.jenis,
                takaranDosis = entity.takaranDosis,
                dosis = entity.dosis,
                waktuMinum = entity.waktuMinum,
                catatan = entity.catatan,
                stok = entity.stok
            )

            suspendCoroutine<Unit> { continuation ->
                obatRepository.addObat(obat) { success ->
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
}
