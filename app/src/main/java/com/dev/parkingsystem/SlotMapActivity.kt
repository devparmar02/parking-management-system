package com.dev.parkingsystem

import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.format.DateFormat
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.*
import java.util.*

class SlotMapActivity : AppCompatActivity() {
    private lateinit var repo: Repository
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var gridSlots: GridLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slot_map)
        repo = Repository.getInstance(applicationContext)
        gridSlots = findViewById(R.id.gridSlots)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        populateGrid()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.slot_map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_slot -> {
                showAddSlotDialog()
                true
            }
            R.id.action_delete_slot -> {
                showDeleteSlotDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddSlotDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_slot, null)
        val etSlotName = dialogView.findViewById<EditText>(R.id.etSlotName)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        val categories = listOf("TwoWheeler", "LMV", "HMV")
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_slot))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val slotName = etSlotName.text.toString().trim()
                if (slotName.isNotEmpty()) {
                    val category = spinnerCategory.selectedItem.toString()
                    mainScope.launch(Dispatchers.IO) {
                        repo.insertSlot(Slot(slotNumber = slotName, category = category))
                        withContext(Dispatchers.Main) {
                            populateGrid()
                            Toast.makeText(this@SlotMapActivity, getString(R.string.slot_added), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@SlotMapActivity, getString(R.string.slot_name_cannot_be_empty), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDeleteSlotDialog() {
        mainScope.launch {
            val slots = withContext(Dispatchers.IO) { repo.getAllSlots() }
            val slotNames = slots.map { it.slotNumber }.toTypedArray()

            AlertDialog.Builder(this@SlotMapActivity)
                .setTitle(getString(R.string.delete_slot))
                .setItems(slotNames) { _, which ->
                    val selectedSlot = slots[which]
                    showDeleteConfirmationDialog(selectedSlot)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun showDeleteConfirmationDialog(slot: Slot) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_slot_confirmation_title))
            .setMessage(getString(R.string.delete_slot_confirmation_message) + " (${slot.slotNumber})?")
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                mainScope.launch(Dispatchers.IO) {
                    repo.deleteSlot(slot)
                    withContext(Dispatchers.Main) {
                        populateGrid()
                        Toast.makeText(this@SlotMapActivity, getString(R.string.slot_deleted), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun populateGrid() {
        mainScope.launch {
            val slots = withContext(Dispatchers.IO) { repo.getAllSlots() }
            gridSlots.removeAllViews()
            for (s in slots) {
                val b = Button(this@SlotMapActivity)
                b.text = s.slotNumber
                b.setTypeface(null, Typeface.BOLD)
                b.textSize = 12f
                val params = GridLayout.LayoutParams()
                params.width = 0
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                params.setMargins(6, 6, 6, 6)
                b.layoutParams = params
                when (s.status) {
                    "Available" -> b.setBackgroundColor(getColor(R.color.green_available))
                    "Occupied" -> b.setBackgroundColor(getColor(R.color.red_occupied))
                    "Maintenance" -> b.setBackgroundColor(getColor(R.color.yellow_maintenance))
                }
                b.setOnClickListener {
                    onSlotClicked(s)
                }
                gridSlots.addView(b)
            }
        }
    }

    private fun onSlotClicked(slot: Slot) {
        mainScope.launch {
            when (slot.status) {
                "Occupied" -> {
                    val booking = withContext(Dispatchers.IO) { slot.currentBookingId?.let { repo.getBookingById(it) } }
                    if (booking != null) {
                        val formattedDate = DateFormat.format("dd/MM/yyyy hh:mm a", booking.startTime).toString()
                        val info = getString(R.string.vehicle_info, booking.vehicleNumber, booking.name, formattedDate, booking.price.toString())
                        AlertDialog.Builder(this@SlotMapActivity)
                            .setTitle(slot.slotNumber)
                            .setMessage(info)
                            .setPositiveButton(getString(R.string.extend)) { _, _ -> showExtendDialog(booking) }
                            .setNegativeButton(getString(R.string.delete_booking)) { _, _ ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    booking.status = "Cancelled"
                                    repo.updateBooking(booking)
                                    repo.freeSlot(slot.slotId)
                                    withContext(Dispatchers.Main) { populateGrid() }
                                }
                            }.show()
                    } else {
                        // Data inconsistency: slot is Occupied but has no valid booking.
                        Toast.makeText(this@SlotMapActivity, "Error: Booking data not found for this slot. Freeing slot.", Toast.LENGTH_LONG).show()
                        withContext(Dispatchers.IO) {
                            repo.freeSlot(slot.slotId)
                        }
                        populateGrid()
                    }
                }
                "Available" -> {
                    AlertDialog.Builder(this@SlotMapActivity)
                        .setTitle(slot.slotNumber)
                        .setMessage(getString(R.string.maintenance_mode))
                        .setPositiveButton(getString(R.string.set_to_maintenance)) { _, _ ->
                            mainScope.launch(Dispatchers.IO) {
                                slot.status = "Maintenance"
                                repo.updateSlot(slot)
                                withContext(Dispatchers.Main) { populateGrid() }
                            }
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
                "Maintenance" -> {
                    AlertDialog.Builder(this@SlotMapActivity)
                        .setTitle(slot.slotNumber)
                        .setMessage(getString(R.string.maintenance_mode))
                        .setPositiveButton(getString(R.string.set_to_available)) { _, _ ->
                            mainScope.launch(Dispatchers.IO) {
                                slot.status = "Available"
                                repo.updateSlot(slot)
                                withContext(Dispatchers.Main) { populateGrid() }
                            }
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
            }
        }
    }

    private fun showExtendDialog(booking: Booking) {
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        editText.hint = getString(R.string.enter_hours_to_extend)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.extend_booking))
            .setView(editText)
            .setPositiveButton(getString(R.string.extend)) { _, _ ->
                val hoursToExtend = editText.text.toString().toIntOrNull()
                if (hoursToExtend != null && hoursToExtend > 0) {
                    mainScope.launch {
                        val updatedBooking = withContext(Dispatchers.IO) {
                            val newTotalHours = booking.requiredHours + hoursToExtend
                            val newPrice = if (newTotalHours >= 24) {
                                val dailyRate = repo.getDailyRate(booking.vehicleType)
                                val days = newTotalHours / 24
                                val remainingHours = newTotalHours % 24
                                val hourlyRate = repo.getHourlyRate(booking.vehicleType)
                                (days * dailyRate) + (remainingHours * hourlyRate)
                            } else {
                                val hourlyRate = repo.getHourlyRate(booking.vehicleType)
                                hourlyRate * newTotalHours
                            }
                            booking.requiredHours = newTotalHours
                            booking.price = newPrice
                            repo.updateBooking(booking)
                            booking
                        }
                        Toast.makeText(this@SlotMapActivity, getString(R.string.booking_extended, hoursToExtend), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SlotMapActivity, getString(R.string.invalid_hours), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}
