package com.example.medicineremindernew.ui.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicineremindernew.ui.data.entity.LocalObatEntity

@Dao
interface LocalObatDao {
    @Query("SELECT * FROM local_obat ORDER BY nama ASC")
    suspend fun getAllObat(): List<LocalObatEntity>

    @Query("SELECT * FROM local_obat WHERE isSynced = 0")
    suspend fun getUnsyncedObat(): List<LocalObatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObat(obat: LocalObatEntity)

    @Update
    suspend fun updateObat(obat: LocalObatEntity)

    @Query("UPDATE local_obat SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM local_obat WHERE id = :id")
    suspend fun deleteObat(id: String)
}