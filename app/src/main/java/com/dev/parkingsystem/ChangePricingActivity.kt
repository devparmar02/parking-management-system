package com.dev.parkingsystem

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class ChangePricingActivity : AppCompatActivity() {
    private lateinit var repo: Repository
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var etTwoWheelerHourly: EditText
    private lateinit var etTwoWheelerDaily: EditText
    private lateinit var etLmvHourly: EditText
    private lateinit var etLmvDaily: EditText
    private lateinit var etHmvHourly: EditText
    private lateinit var etHmvDaily: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pricing)
        repo = Repository.getInstance(applicationContext)

        etTwoWheelerHourly = findViewById(R.id.etTwoWheelerHourly)
        etTwoWheelerDaily = findViewById(R.id.etTwoWheelerDaily)
        etLmvHourly = findViewById(R.id.etLmvHourly)
        etLmvDaily = findViewById(R.id.etLmvDaily)
        etHmvHourly = findViewById(R.id.etHmvHourly)
        etHmvDaily = findViewById(R.id.etHmvDaily)

        val btnEdit = findViewById<Button>(R.id.btnEdit)
        val btnSave = findViewById<Button>(R.id.btnSave)

        setFieldsEnabled(false)
        loadPrices()

        btnEdit.setOnClickListener {
            setFieldsEnabled(true)
        }

        btnSave.setOnClickListener {
            savePrices()
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        etTwoWheelerHourly.isEnabled = enabled
        etTwoWheelerDaily.isEnabled = enabled
        etLmvHourly.isEnabled = enabled
        etLmvDaily.isEnabled = enabled
        etHmvHourly.isEnabled = enabled
        etHmvDaily.isEnabled = enabled
    }

    private fun loadPrices() {
        mainScope.launch {
            val pTwo = withContext(Dispatchers.IO) { repo.getPricing("TwoWheeler") }
            val pLmv = withContext(Dispatchers.IO) { repo.getPricing("LMV") }
            val pHmv = withContext(Dispatchers.IO) { repo.getPricing("HMV") }

            etTwoWheelerHourly.setText(pTwo?.hourlyRate?.toString() ?: "10")
            etTwoWheelerDaily.setText(pTwo?.dailyRate?.toString() ?: "100")
            etLmvHourly.setText(pLmv?.hourlyRate?.toString() ?: "20")
            etLmvDaily.setText(pLmv?.dailyRate?.toString() ?: "200")
            etHmvHourly.setText(pHmv?.hourlyRate?.toString() ?: "40")
            etHmvDaily.setText(pHmv?.dailyRate?.toString() ?: "400")
        }
    }

    private fun savePrices() {
        val twoHourly = etTwoWheelerHourly.text.toString().toDoubleOrNull() ?: 0.0
        val twoDaily = etTwoWheelerDaily.text.toString().toDoubleOrNull() ?: 0.0
        val lmvHourly = etLmvHourly.text.toString().toDoubleOrNull() ?: 0.0
        val lmvDaily = etLmvDaily.text.toString().toDoubleOrNull() ?: 0.0
        val hmvHourly = etHmvHourly.text.toString().toDoubleOrNull() ?: 0.0
        val hmvDaily = etHmvDaily.text.toString().toDoubleOrNull() ?: 0.0

        mainScope.launch(Dispatchers.IO) {
            repo.insertOrUpdatePricing(Pricing("TwoWheeler", twoHourly, twoDaily))
            repo.insertOrUpdatePricing(Pricing("LMV", lmvHourly, lmvDaily))
            repo.insertOrUpdatePricing(Pricing("HMV", hmvHourly, hmvDaily))
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ChangePricingActivity, "Pricing Saved", Toast.LENGTH_SHORT).show()
                setFieldsEnabled(false)
            }
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}
