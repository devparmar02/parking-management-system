package com.dev.parkingsystem

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.util.*

class RevenueActivity : AppCompatActivity() {
    private lateinit var repo: Repository
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_revenue)
        repo = Repository.getInstance(applicationContext)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val tvTotal = findViewById<TextView>(R.id.tvTotalRevenue)
        val tvToday = findViewById<TextView>(R.id.tvTodayRevenue)
        val tvThisWeek = findViewById<TextView>(R.id.tvThisWeekRevenue)
        val tvThisMonth = findViewById<TextView>(R.id.tvThisMonthRevenue)
        val btnDownload = findViewById<Button>(R.id.btnDownloadReport)
        val btnReset = findViewById<Button>(R.id.btnResetRevenue)

        loadRevenue()

        btnDownload.setOnClickListener {
            downloadRevenueReport()
        }

        btnReset.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun loadRevenue() {
        mainScope.launch {
            val total = withContext(Dispatchers.IO) { repo.getTotalRevenue() }
            val today = withContext(Dispatchers.IO) { repo.getTodayRevenue() }
            val thisWeek = withContext(Dispatchers.IO) { repo.getThisWeekRevenue() }
            val thisMonth = withContext(Dispatchers.IO) { repo.getThisMonthRevenue() }

            findViewById<TextView>(R.id.tvTotalRevenue).text = "Total Revenue: ₹$total"
            findViewById<TextView>(R.id.tvTodayRevenue).text = "Today: ₹$today"
            findViewById<TextView>(R.id.tvThisWeekRevenue).text = "This Week: ₹$thisWeek"
            findViewById<TextView>(R.id.tvThisMonthRevenue).text = "This Month: ₹$thisMonth"
        }
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reset_confirmation_title))
            .setMessage(getString(R.string.reset_confirmation_message))
            .setPositiveButton(getString(R.string.reset)) { _, _ ->
                mainScope.launch {
                    withContext(Dispatchers.IO) { repo.resetAllBookingPrices() }
                    loadRevenue()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.revenue_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_date -> {
                showDatePicker()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            loadRevenueForDate(selectedCalendar.timeInMillis)
        }, year, month, day).show()
    }

    private fun loadRevenueForDate(date: Long) {
        mainScope.launch {
            val revenue = withContext(Dispatchers.IO) { repo.getRevenueForDay(date) }
            findViewById<TextView>(R.id.tvTodayRevenue).text = "Revenue for selected date: ₹$revenue"
        }
    }

    private fun downloadRevenueReport() {
        mainScope.launch {
            val total = withContext(Dispatchers.IO) { repo.getTotalRevenue() }
            val today = withContext(Dispatchers.IO) { repo.getTodayRevenue() }
            val thisWeek = withContext(Dispatchers.IO) { repo.getThisWeekRevenue() }
            val thisMonth = withContext(Dispatchers.IO) { repo.getThisMonthRevenue() }

            val report = "Revenue Report\n"
                .plus("Total Revenue: ₹$total\n")
                .plus("Today: ₹$today\n")
                .plus("This Week: ₹$thisWeek\n")
                .plus("This Month: ₹$thisMonth\n")

            try {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RevenueReport.txt")
                val writer = FileWriter(file)
                writer.append(report)
                writer.flush()
                writer.close()

                AlertDialog.Builder(this@RevenueActivity)
                    .setTitle("Download Complete")
                    .setMessage("PDF downloaded to ${file.absolutePath}")
                    .setPositiveButton("OK", null)
                    .show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}
