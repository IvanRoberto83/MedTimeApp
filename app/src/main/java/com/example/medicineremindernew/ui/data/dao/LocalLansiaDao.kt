package com.example.medicineremindernew.ui.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicineremindernew.ui.data.entity.LocalLansiaEntity

@Dao
interface LocalLansiaDao {
    @Query("SELECT * FROM local_lansia ORDER BY nama ASC")
    suspend fun getAllLansia(): List<LocalLansiaEntity>

    @Query("SELECT * FROM local_lansia WHERE isSynced = 0")
    suspend fun getUnsyncedLansia(): List<LocalLansiaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLansia(lansia: LocalLansiaEntity)

    @Update
    suspend fun updateLansia(lansia: LocalLansiaEntity)

    @Query("UPDATE local_lansia SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM local_lansia WHERE id = :id")
    suspend fun deleteLansia(id: String)
}