package com.dev.parkingsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val adminEmail = "admin@parking.com"
    private val adminPassword = "admin123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bootstrap()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email == adminEmail && pass == adminPassword) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bootstrap() {
        val repo = Repository.getInstance(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            // insert slots and default pricing only if DB is empty
            if (repo.countSlots() == 0) {
                // A1..A10 -> TwoWheeler
                for (i in 1..10) repo.insertSlot(Slot(slotNumber = "A-$i", category = "TwoWheeler"))
                for (i in 1..10) repo.insertSlot(Slot(slotNumber = "B-$i", category = "LMV"))
                for (i in 1..10) repo.insertSlot(Slot(slotNumber = "C-$i", category = "HMV"))

                // default pricing
                repo.insertOrUpdatePricing(Pricing("TwoWheeler", 10.0, 100.0))
                repo.insertOrUpdatePricing(Pricing("LMV", 20.0, 200.0))
                repo.insertOrUpdatePricing(Pricing("HMV", 40.0, 400.0))
            }
        }
    }
}
