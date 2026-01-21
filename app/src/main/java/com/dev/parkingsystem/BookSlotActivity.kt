package com.dev.parkingsystem

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.util.*

class BookSlotActivity : AppCompatActivity() {
    private lateinit var repo: Repository
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_slot)
        repo = Repository.getInstance(applicationContext)

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etVehicleNo = findViewById<EditText>(R.id.etVehicleNo)
        val spinnerType = findViewById<Spinner>(R.id.spinnerVehicleType)
        val tvEntryTime = findViewById<TextView>(R.id.tvEntryTime)
        val etHours = findViewById<EditText>(R.id.etRequiredHours)
        val tvTotalAmt = findViewById<TextView>(R.id.tvTotalAmount)
        val btnBook = findViewById<Button>(R.id.btnBook)

        val types = listOf("TwoWheeler","LMV","HMV")
        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)

        var entryTimeMillis = System.currentTimeMillis()

        tvEntryTime.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, m)
                entryTimeMillis = cal.timeInMillis
                tvEntryTime.text = String.format("%02d:%02d", h, m)
            }, hour, minute, true).show()
        }

        // calculate price on hours change
        etHours.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) calculatePrice(spinnerType.selectedItem.toString(), etHours, tvTotalAmt)
        }

        btnBook.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val vehicleNo = etVehicleNo.text.toString().trim()
            val type = spinnerType.selectedItem.toString()
            val hours = etHours.text.toString().toIntOrNull() ?: 0

            if (name.isEmpty() || vehicleNo.isEmpty() || hours <= 0) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mainScope.launch {
                // assign first available slot for the category
                val slot = withContext(Dispatchers.IO) { repo.findFirstAvailableSlot(type) }
                if (slot == null) {
                    Toast.makeText(this@BookSlotActivity, "No available slots for $type", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val price = withContext(Dispatchers.IO) {
                    if (hours >= 24) {
                        val dailyRate = repo.getDailyRate(type)
                        val days = hours / 24
                        val remainingHours = hours % 24
                        val hourlyRate = repo.getHourlyRate(type)
                        (days * dailyRate) + (remainingHours * hourlyRate)
                    } else {
                        val hourlyRate = repo.getHourlyRate(type)
                        hourlyRate * hours
                    }
                }
                val booking = Booking(
                    name = name,
                    phone = phone,
                    vehicleNumber = vehicleNo,
                    vehicleType = type,
                    slotId = slot.slotId,
                    startTime = entryTimeMillis,
                    requiredHours = hours,
                    price = price,
                    status = "Active"
                )
                withContext(Dispatchers.IO) {
                    val id = repo.insertBooking(booking)
                    repo.occupySlot(slot.slotId, id.toInt())
                }
                
                val intent = Intent(this@BookSlotActivity, BookingSuccessActivity::class.java)
                intent.putExtra("slotNumber", slot.slotNumber)
                intent.putExtra("price", price)
                intent.putExtra("duration", hours)
                intent.putExtra("vehicleNumber", vehicleNo)
                intent.putExtra("vehicleType", type)
                startActivity(intent)
                
                finish()
            }
        }
    }

    private fun calculatePrice(type: String, etHours: EditText, tvTotal: TextView) {
        val hours = etHours.text.toString().toIntOrNull() ?: 0
        if (hours <= 0) {
            tvTotal.text = "Total Amount: ₹0"
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val price = withContext(Dispatchers.IO) {
                val repo = Repository.getInstance(applicationContext)
                if (hours >= 24) {
                    val dailyRate = repo.getDailyRate(type)
                    val days = hours / 24
                    val remainingHours = hours % 24
                    val hourlyRate = repo.getHourlyRate(type)
                    (days * dailyRate) + (remainingHours * hourlyRate)
                } else {
                    val hourlyRate = repo.getHourlyRate(type)
                    hourlyRate * hours
                }
            }
            tvTotal.text = "Total Amount: ₹$price"
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}
