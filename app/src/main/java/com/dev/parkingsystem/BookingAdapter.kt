package com.dev.parkingsystem

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.*

class BookingAdapter(context: Context, private val bookings: List<Booking>) :
    ArrayAdapter<Booking>(context, 0, bookings) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_booking, parent, false)
        }

        val currentBooking = bookings[position]

        val nameTextView = itemView!!.findViewById<TextView>(R.id.tvBookingName)
        val vehicleNumberTextView = itemView.findViewById<TextView>(R.id.tvBookingVehicleNumber)
        val entryTimeTextView = itemView.findViewById<TextView>(R.id.tvBookingEntryTime)
        val exitTimeTextView = itemView.findViewById<TextView>(R.id.tvBookingExitTime)
        val revenueTextView = itemView.findViewById<TextView>(R.id.tvBookingRevenue)
        val statusTextView = itemView.findViewById<TextView>(R.id.tvBookingStatus)

        nameTextView.text = currentBooking.name
        vehicleNumberTextView.text = currentBooking.vehicleNumber
        entryTimeTextView.text = "Entry: ${formatTimestamp(currentBooking.startTime)}"

        if (currentBooking.status == "Completed" || currentBooking.status == "Cancelled") {
            val exitTime = currentBooking.startTime + (currentBooking.requiredHours * 60 * 60 * 1000)
            exitTimeTextView.text = context.getString(R.string.exit_time, formatTimestamp(exitTime))
            exitTimeTextView.visibility = View.VISIBLE
        } else {
            exitTimeTextView.visibility = View.GONE
        }

        revenueTextView.text = context.getString(R.string.revenue, currentBooking.price.toString())
        statusTextView.text = "Status: ${currentBooking.status}"

        return itemView
    }

    private fun formatTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp
        return DateFormat.format("hh:mm a", calendar).toString()
    }
}
