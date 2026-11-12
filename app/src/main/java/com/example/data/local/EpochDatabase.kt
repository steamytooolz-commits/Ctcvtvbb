package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ActiveLifeEntity::class, LegacyHallEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EpochDatabase : RoomDatabase() {
    abstract fun epochDao(): EpochDao

    companion object {
        @Volatile
        private var INSTANCE: EpochDatabase? = null

        fun getInstance(context: Context): EpochDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EpochDatabase::class.java,
                    "epoch_life_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
