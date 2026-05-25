package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WashProvider::class, Booking::class], version = 1, exportSchema = false)
abstract class UgaWashDatabase : RoomDatabase() {
    abstract fun dao(): UgaWashDao

    companion object {
        @Volatile
        private var INSTANCE: UgaWashDatabase? = null

        fun getDatabase(context: Context): UgaWashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UgaWashDatabase::class.java,
                    "ugawash_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
