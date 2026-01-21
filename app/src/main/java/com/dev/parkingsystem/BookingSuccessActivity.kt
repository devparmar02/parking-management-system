package com.dev.parkingsystem

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BookingSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_success)

        val tvAssignedSlot = findViewById<TextView>(R.id.tvAssignedSlot)
        val tvBookingPrice = findViewById<TextView>(R.id.tvBookingPrice)
        val tvBookingDuration = findViewById<TextView>(R.id.tvBookingDuration)
        val tvBookingVehicleDetails = findViewById<TextView>(R.id.tvBookingVehicleDetails)
        val btnDone = findViewById<Button>(R.id.btnDone)

        val slotNumber = intent.getStringExtra("slotNumber")
        val price = intent.getDoubleExtra("price", 0.0)
        val duration = intent.getIntExtra("duration", 0)
        val vehicleNumber = intent.getStringExtra("vehicleNumber")
        val vehicleType = intent.getStringExtra("vehicleType")

        tvAssignedSlot.text = "Assigned Slot: $slotNumber"
        tvBookingPrice.text = "Price: ₹$price"
        tvBookingDuration.text = "Duration: $duration hours"
        tvBookingVehicleDetails.text = "Vehicle: $vehicleNumber ($vehicleType)"

        btnDone.setOnClickListener {
            finish()
        }
    }
}
