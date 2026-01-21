package com.dev.parkingsystem

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Slot::class, Booking::class, Pricing::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parkingDao(): ParkingDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                val inst = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "parking_db")
                    .fallbackToDestructiveMigration()
                    .build()
                instance = inst
                inst
            }
    }
}
