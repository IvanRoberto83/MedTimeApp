package com.example.medicineremindernew.ui.data.repository

import android.content.Context
import android.util.Log
import com.example.medicineremindernew.ui.data.dao.LocalKunjunganDao
import com.example.medicineremindernew.ui.data.entity.LocalKunjunganEntity
import com.example.medicineremindernew.ui.data.model.Kunjungan
import com.example.medicineremindernew.ui.data.network.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HybridKunjunganRepository(
    private val kunjunganRepository: KunjunganRepository,
    private val localDao: LocalKunjunganDao,
    private val networkUtils: NetworkUtils,
    private val context: Context
) {

    suspend fun addKunjungan(kunjungan: Kunjungan): Boolean {
        return try {
            val fixed = if (kunjungan.idKunjungan.isEmpty()) {
                kunjungan.copy(idKunjungan = java.util.UUID.randomUUID().toString())
            } else kunjungan

            if (networkUtils.isNetworkAvailable()) {
                try { kunjunganRepository.addKunjungan(fixed) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Firestore add failed: ${e.message}") }

                try { localDao.insertKunjungan(fixed.toLocalEntity(isSynced = true)) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Room insert failed: ${e.message}") }
            } else {
                try { localDao.insertKunjungan(fixed.toLocalEntity(isSynced = false)) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Room insert failed offline: ${e.message}") }
            }

            true
        } catch (e: Exception) {
            Log.e("HybridKunjunganRepo", "Add failed: ${e.message}", e)
            false
        }
    }

    suspend fun updateKunjungan(kunjungan: Kunjungan): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                try { kunjunganRepository.updateKunjungan(kunjungan) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Firestore update failed: ${e.message}") }

                try { localDao.updateKunjungan(kunjungan.toLocalEntity(isSynced = true)) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Room update failed: ${e.message}") }
            } else {
                try { localDao.updateKunjungan(kunjungan.toLocalEntity(isSynced = false)) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Room update failed offline: ${e.message}") }
            }

            true
        } catch (e: Exception) {
            Log.e("HybridKunjunganRepo", "Update failed: ${e.message}", e)
            false
        }
    }

    suspend fun deleteKunjungan(id: String): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                try { kunjunganRepository.deleteKunjungan(id) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Firestore delete failed: ${e.message}") }

                try { localDao.deleteKunjungan(id) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Room delete failed: ${e.message}") }
            } else {
                try { localDao.deleteKunjungan(id) }
                catch (e: Exception) { Log.e("HybridKunjunganRepo", "Room delete failed offline: ${e.message}") }
            }

            true
        } catch (e: Exception) {
            Log.e("HybridKunjunganRepo", "Delete failed: ${e.message}")
            false
        }
    }

    suspend fun getAllKunjunganOnce(): List<Kunjungan> {
        return if (networkUtils.isNetworkAvailable()) {
            try {
                kunjunganRepository.getAllKunjungan()
            } catch (e: Exception) {
                Log.e("HybridKunjunganRepo", "Get remote failed: ${e.message}")
                getLocalKunjunganOnce()
            }
        } else {
            getLocalKunjunganOnce()
        }
    }

    fun getAllKunjunganFlow(): Flow<List<Kunjungan>> {
        return localDao.getAllKunjunganFlow()
            .map { list -> list.map { entity -> entity.toDomainModel() } }
    }

    private suspend fun getLocalKunjunganOnce(): List<Kunjungan> {
        return localDao.getAllKunjungan().map { it.toDomainModel() }
    }

    suspend fun syncPendingData() {
        if (!networkUtils.isNetworkAvailable()) return

        val unsynced = localDao.getUnsyncedKunjungan()
        unsynced.forEach { entity ->
            val kunjungan = entity.toDomainModel()
            try {
                kunjunganRepository.addKunjungan(kunjungan)
                localDao.markAsSynced(entity.id)
            } catch (e: Exception) {
                Log.e("HybridKunjunganRepo", "Sync failed for ${entity.id}: ${e.message}")
            }
        }
    }

    private fun Kunjungan.toLocalEntity(isSynced: Boolean): LocalKunjunganEntity {
        return LocalKunjunganEntity(
            id = this.idKunjungan,
            lansiaIds = this.lansiaIds.joinToString(","),
            tanggal = this.tanggal,
            waktu = this.waktu,
            jenisKunjungan = this.jenisKunjungan,
            isSynced = isSynced
        )
    }

    private fun LocalKunjunganEntity.toDomainModel(): Kunjungan {
        return Kunjungan(
            idKunjungan = this.id,
            lansiaIds = this.lansiaIds.split(","),
            tanggal = this.tanggal,
            waktu = this.waktu,
            jenisKunjungan = this.jenisKunjungan
        )
    }
}
