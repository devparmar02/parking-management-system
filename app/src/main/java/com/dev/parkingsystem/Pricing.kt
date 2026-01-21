package com.dev.parkingsystem

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pricing")
data class Pricing(
    @PrimaryKey var vehicleType: String,
    var hourlyRate: Double = 0.0,
    var dailyRate: Double = 0.0
)
