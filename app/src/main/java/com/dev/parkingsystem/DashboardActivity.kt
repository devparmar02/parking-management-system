package com.dev.parkingsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class DashboardActivity : AppCompatActivity() {
    private lateinit var repo: Repository
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        repo = Repository.getInstance(applicationContext)

        findViewById<Button>(R.id.btnBookSlot).setOnClickListener {
            startActivity(Intent(this, BookSlotActivity::class.java))
        }
        findViewById<Button>(R.id.btnSlotMap).setOnClickListener {
            startActivity(Intent(this, SlotMapActivity::class.java))
        }
        findViewById<Button>(R.id.btnChangePricing).setOnClickListener {
            startActivity(Intent(this, ChangePricingActivity::class.java))
        }
        findViewById<Button>(R.id.btnAllBookings).setOnClickListener {
            startActivity(Intent(this, BookingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnRevenueReport).setOnClickListener {
            startActivity(Intent(this, RevenueActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStats()
    }

    private fun refreshStats() {
        val tvTotal = findViewById<TextView>(R.id.tvTotalSlots)
        val tvAvailable = findViewById<TextView>(R.id.tvAvailableSlots)
        val tvOccupied = findViewById<TextView>(R.id.tvOccupiedSlots)
        val tvRevenue = findViewById<TextView>(R.id.tvTodayRevenue)

        mainScope.launch {
            val total = withContext(Dispatchers.IO) { repo.countSlots() }
            val available = withContext(Dispatchers.IO) { repo.countAvailableSlots() }
            val occupied = total - available
            val todayRevenue = withContext(Dispatchers.IO) { repo.getTodayRevenue() }

            tvTotal.text = "Total Slots : $total"
            tvAvailable.text = "Available Slots: $available"
            tvOccupied.text = "Occupied Slots: $occupied"
            tvRevenue.text = "Today\'s Revenue: ₹$todayRevenue"
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}
