package com.example.medicineremindernew.ui.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicineremindernew.ui.data.entity.LocalReminderEntity

@Dao
interface LocalReminderDao {
    @Query("SELECT * FROM local_reminders ORDER BY createdAt DESC")
    suspend fun getAllReminders(): List<LocalReminderEntity>

    @Query("SELECT * FROM local_reminders WHERE isSynced = 0")
    suspend fun getUnsyncedReminders(): List<LocalReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: LocalReminderEntity)

    @Update
    suspend fun updateReminder(reminder: LocalReminderEntity)

    @Query("UPDATE local_reminders SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM local_reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)
}