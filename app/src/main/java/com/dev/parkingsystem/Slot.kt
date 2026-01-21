package com.dev.parkingsystem

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slots")
data class Slot(
    @PrimaryKey(autoGenerate = true) val slotId: Int = 0,
    val slotNumber: String,
    val category: String, // TwoWheeler, LMV, HMV
    var status: String = "Available", // Available / Occupied
    var currentBookingId: Int? = null
)
