package com.example.medicineremindernew.ui.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicineremindernew.ui.data.entity.LocalKunjunganEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalKunjunganDao {
    @Query("""
        SELECT * FROM local_kunjungan
        WHERE datetime(tanggal || ' ' || waktu) >= datetime('now')
        ORDER BY createdAt DESC
    """)
    suspend fun getAllKunjungan(): List<LocalKunjunganEntity>

    @Query("""
        SELECT * FROM local_kunjungan
        WHERE datetime(tanggal || ' ' || waktu) >= datetime('now')
        ORDER BY createdAt DESC
    """)
    fun getAllKunjunganFlow(): Flow<List<LocalKunjunganEntity>>

    @Query("SELECT * FROM local_kunjungan WHERE isSynced = 0")
    suspend fun getUnsyncedKunjungan(): List<LocalKunjunganEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKunjungan(kunjungan: LocalKunjunganEntity)

    @Update
    suspend fun updateKunjungan(kunjungan: LocalKunjunganEntity)

    @Query("UPDATE local_kunjungan SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM local_kunjungan WHERE id = :id")
    suspend fun deleteKunjungan(id: String)
}
