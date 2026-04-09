package com.example.medicineremindernew.ui.data.dao

import androidx.room.*
import com.example.medicineremindernew.ui.data.entity.LocalRiwayatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalRiwayatDao {

    @Query("SELECT * FROM local_riwayat WHERE lansiaId = :lansiaId ORDER BY tanggal DESC, waktu DESC")
    fun getRiwayatByLansiaFlow(lansiaId: String): Flow<List<LocalRiwayatEntity>>

    @Query("SELECT * FROM local_riwayat WHERE isSynced = 0")
    suspend fun getUnsyncedRiwayat(): List<LocalRiwayatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiwayat(riwayat: LocalRiwayatEntity)

    @Update
    suspend fun updateRiwayat(riwayat: LocalRiwayatEntity)

    @Query("DELETE FROM local_riwayat WHERE id = :id")
    suspend fun deleteRiwayatById(id: String)
}
