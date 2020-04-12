package com.android.example.thelanguagelion.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Sememe::class], version = 1, exportSchema = false)
abstract class SememeDatabase : RoomDatabase() {
    abstract val sememeDatabaseDao: SememeDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: SememeDatabase? = null

        fun getInstance(context: Context): SememeDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room
                        .databaseBuilder(context.applicationContext, SememeDatabase::class.java, "sememe_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}