package com.dev.parkingsystem

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.util.*

class BookingsActivity : AppCompatActivity() {
    private lateinit var repo: Repository
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var bookings: List<Booking>
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)
        repo = Repository.getInstance(applicationContext)
        val list = findViewById<ListView>(R.id.listBookings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mainScope.launch {
            bookings = withContext(Dispatchers.IO) { repo.getTodaysBookings() }
            adapter = BookingAdapter(this@BookingsActivity, bookings)
            list.adapter = adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bookings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_date -> {
                showDatePicker()
                true
            }
            R.id.action_download_report -> {
                downloadBookingsReport()
                true
            }
            R.id.action_clear_history -> {
                showClearHistoryConfirmationDialog()
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
            loadBookingsForDate(selectedCalendar.timeInMillis)
        }, year, month, day).show()
    }

    private fun loadBookingsForDate(date: Long) {
        mainScope.launch {
            bookings = withContext(Dispatchers.IO) { repo.getBookingsForDay(date) }
            adapter = BookingAdapter(this@BookingsActivity, bookings)
            findViewById<ListView>(R.id.listBookings).adapter = adapter
        }
    }

    private fun showClearHistoryConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_history_confirmation_title))
            .setMessage(getString(R.string.clear_history_confirmation_message))
            .setPositiveButton(getString(R.string.clear_history)) { _, _ ->
                mainScope.launch {
                    withContext(Dispatchers.IO) {
                        repo.deleteAllBookings()
                        repo.freeAllSlots()
                    }
                    loadBookingsForDate(System.currentTimeMillis())
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun downloadBookingsReport() {
        mainScope.launch {
            val report = StringBuilder()
            report.append("Bookings Report\n\n")
            for (booking in bookings) {
                report.append("Vehicle: ${booking.vehicleNumber}\n")
                report.append("Slot: ${booking.slotId}\n")
                report.append("Timing: ${booking.requiredHours} hours\n")
                report.append("Revenue: ₹${booking.price}\n")
                report.append("Status: ${booking.status}\n\n")
            }

            try {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BookingsReport.txt")
                val writer = FileWriter(file)
                writer.append(report.toString())
                writer.flush()
                writer.close()

                AlertDialog.Builder(this@BookingsActivity)
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
