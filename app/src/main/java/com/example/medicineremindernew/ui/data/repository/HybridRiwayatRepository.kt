package com.example.medicineremindernew.ui.data.repository

import android.content.Context
import android.util.Log
import com.example.medicineremindernew.ui.data.dao.LocalRiwayatDao
import com.example.medicineremindernew.ui.data.entity.LocalRiwayatEntity
import com.example.medicineremindernew.ui.data.model.Riwayat
import com.example.medicineremindernew.ui.data.network.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class HybridRiwayatRepository(
    private val riwayatRepository: RiwayatRepository,
    private val localDao: LocalRiwayatDao,
    private val networkUtils: NetworkUtils,
    private val context: Context
) {

    suspend fun addRiwayat(riwayat: Riwayat): Boolean {
        return try {
            val fixed = if (riwayat.idRiwayat.isEmpty()) {
                riwayat.copy(idRiwayat = java.util.UUID.randomUUID().toString())
            } else riwayat

            if (networkUtils.isNetworkAvailable()) {
                try {
                    riwayatRepository.addRiwayat(fixed)
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Firestore add failed: ${e.message}")
                }

                try {
                    localDao.insertRiwayat(fixed.toLocalEntity(isSynced = true))
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Room insert failed: ${e.message}")
                }
            } else {
                try {
                    localDao.insertRiwayat(fixed.toLocalEntity(isSynced = false))
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Room insert failed offline: ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e("HybridRiwayatRepo", "Add failed: ${e.message}", e)
            false
        }
    }

    suspend fun updateRiwayat(riwayat: Riwayat): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                try {
                    riwayatRepository.updateRiwayat(riwayat)
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Firestore update failed: ${e.message}")
                }

                try {
                    localDao.updateRiwayat(riwayat.toLocalEntity(isSynced = true))
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Room update failed: ${e.message}")
                }
            } else {
                try {
                    localDao.updateRiwayat(riwayat.toLocalEntity(isSynced = false))
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Room update failed offline: ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e("HybridRiwayatRepo", "Update failed: ${e.message}", e)
            false
        }
    }

    suspend fun deleteRiwayat(id: String): Boolean {
        return try {
            if (networkUtils.isNetworkAvailable()) {
                try {
                    riwayatRepository.deleteRiwayat(id)
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Firestore delete failed: ${e.message}")
                }

                try {
                    localDao.deleteRiwayatById(id)
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Room delete failed: ${e.message}")
                }
            } else {
                try {
                    localDao.deleteRiwayatById(id)
                } catch (e: Exception) {
                    Log.e("HybridRiwayatRepo", "Room delete failed offline: ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e("HybridRiwayatRepo", "Delete failed: ${e.message}")
            false
        }
    }

    suspend fun getAllRiwayatOnce(): List<Riwayat> {
        return if (networkUtils.isNetworkAvailable()) {
            try {
                riwayatRepository.getAllRiwayat()
            } catch (e: Exception) {
                Log.e("HybridRiwayatRepo", "Get remote failed: ${e.message}")
                getLocalRiwayatOnce()
            }
        } else {
            getLocalRiwayatOnce()
        }
    }

    fun getRiwayatByLansiaFlow(lansiaId: String): Flow<List<Riwayat>> {
        return localDao.getRiwayatByLansiaFlow(lansiaId)
            .map { list -> list.map { entity -> entity.toDomainModel() } }
    }

    private suspend fun getLocalRiwayatOnce(): List<Riwayat> {
        return localDao.getRiwayatByLansiaFlow("%")
            .map { list -> list.map { it.toDomainModel() } }
            .first()
    }

    suspend fun syncPendingData() {
        if (!networkUtils.isNetworkAvailable()) return

        val unsynced = localDao.getUnsyncedRiwayat()
        unsynced.forEach { entity ->
            val riwayat = entity.toDomainModel()
            try {
                riwayatRepository.addRiwayat(riwayat)
                localDao.updateRiwayat(entity.copy(isSynced = true))
            } catch (e: Exception) {
                Log.e("HybridRiwayatRepo", "Sync failed for ${entity.id}: ${e.message}")
            }
        }
    }

    private fun Riwayat.toLocalEntity(isSynced: Boolean): LocalRiwayatEntity {
        return LocalRiwayatEntity(
            id = this.idRiwayat,
            lansiaId = this.lansiaId,
            obatId = this.obatId ?: "",
            kunjunganId = this.kunjunganId ?: "",
            jenis = this.jenis,
            keterangan = this.keterangan,
            tanggal = this.tanggal,
            waktu = this.waktu,
            isSynced = isSynced
        )
    }

    private fun LocalRiwayatEntity.toDomainModel(): Riwayat {
        return Riwayat(
            idRiwayat = this.id,
            lansiaId = this.lansiaId,
            obatId = this.obatId?.ifEmpty { null },
            kunjunganId = this.kunjunganId?.ifEmpty { null },
            jenis = this.jenis,
            keterangan = this.keterangan,
            tanggal = this.tanggal,
            waktu = this.waktu
        )
    }

}
