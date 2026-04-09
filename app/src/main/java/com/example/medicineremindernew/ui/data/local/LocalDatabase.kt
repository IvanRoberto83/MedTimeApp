package com.example.medicineremindernew.ui.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.medicineremindernew.ui.data.dao.LocalLansiaDao
import com.example.medicineremindernew.ui.data.dao.LocalObatDao
import com.example.medicineremindernew.ui.data.dao.LocalReminderDao
import com.example.medicineremindernew.ui.data.dao.LocalKunjunganDao
import com.example.medicineremindernew.ui.data.dao.LocalRiwayatDao
import com.example.medicineremindernew.ui.data.entity.LocalReminderEntity
import com.example.medicineremindernew.ui.data.entity.LocalLansiaEntity
import com.example.medicineremindernew.ui.data.entity.LocalRiwayatEntity
import com.example.medicineremindernew.ui.data.entity.LocalObatEntity
import com.example.medicineremindernew.ui.data.entity.LocalKunjunganEntity // ✅ tambahkan


@Database(
    entities = [
        LocalReminderEntity::class,
        LocalLansiaEntity::class,
        LocalObatEntity::class,
        LocalKunjunganEntity::class,
        LocalRiwayatEntity::class
    ],
    version = 2,
    exportSchema = false
)

abstract class LocalDatabase : RoomDatabase() {

    abstract fun reminderDao(): LocalReminderDao
    abstract fun lansiaDao(): LocalLansiaDao
    abstract fun obatDao(): LocalObatDao
    abstract fun kunjunganDao(): LocalKunjunganDao
    abstract fun riwayatDao(): LocalRiwayatDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE local_kunjungan ADD COLUMN jenisKunjungan TEXT NOT NULL DEFAULT ''"
                )
            }
        }



        fun getDatabase(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "medicine_local_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}