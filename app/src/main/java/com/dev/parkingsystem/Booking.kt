package com.dev.parkingsystem

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) var bookingId: Int = 0,
    var name: String,
    var phone: String,
    var vehicleNumber: String,
    var vehicleType: String,
    var slotId: Int,
    var startTime: Long,
    var requiredHours: Int,
    var price: Double,
    var status: String // Active / Completed / Cancelled
)
