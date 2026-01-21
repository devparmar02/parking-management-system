package com.dev.parkingsystem

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class Repository private constructor(context: Context) {
    private val dao = AppDatabase.getDatabase(context).parkingDao()

    // Slots helpers
    suspend fun insertSlot(slot: Slot) = withContext(Dispatchers.IO) { dao.insertSlot(slot) }
    suspend fun deleteSlot(slot: Slot) = withContext(Dispatchers.IO) { dao.deleteSlot(slot) }
    suspend fun updateSlot(slot: Slot) = withContext(Dispatchers.IO) { dao.updateSlot(slot) }
    suspend fun getAllSlots() = withContext(Dispatchers.IO) { dao.getAllSlots() }
    suspend fun countSlots() = withContext(Dispatchers.IO) { dao.countSlots() }
    suspend fun countAvailableSlots() = withContext(Dispatchers.IO) { dao.countAvailableSlots() }
    suspend fun findFirstAvailableSlot(category: String) = withContext(Dispatchers.IO) { dao.findFirstAvailableSlot(category) }
    suspend fun occupySlot(slotId: Int, bookingId: Int) = withContext(Dispatchers.IO) { dao.occupySlot(slotId, bookingId) }
    suspend fun freeSlot(slotId: Int) = withContext(Dispatchers.IO) { dao.freeSlot(slotId) }
    suspend fun freeAllSlots() = withContext(Dispatchers.IO) { dao.freeAllSlots() }

    // bookings
    suspend fun insertBooking(b: Booking) = withContext(Dispatchers.IO) { dao.insertBooking(b) }
    suspend fun getAllBookings() = withContext(Dispatchers.IO) { dao.getAllBookings() }
    suspend fun getTodaysBookings(): List<Booking> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return withContext(Dispatchers.IO) { dao.getBookingsForDay(cal.timeInMillis, cal.timeInMillis + 24 * 60 * 60 * 1000) }
    }
    suspend fun getBookingsForDay(date: Long): List<Booking> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = date
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000
        return withContext(Dispatchers.IO) { dao.getBookingsForDay(startOfDay, endOfDay) }
    }
    suspend fun getBookingById(id: Int) = withContext(Dispatchers.IO) { dao.getBookingById(id) }
    suspend fun updateBooking(b: Booking) = withContext(Dispatchers.IO) { dao.updateBooking(b) }
    suspend fun deleteAllBookings() = withContext(Dispatchers.IO) { dao.deleteAllBookings() }
    suspend fun resetAllBookingPrices() = withContext(Dispatchers.IO) { dao.resetAllBookingPrices() }

    // pricing
    suspend fun insertOrUpdatePricing(p: Pricing) = withContext(Dispatchers.IO) { dao.insertPricing(p) }
    suspend fun getPricing(type: String) = withContext(Dispatchers.IO) { dao.getPricing(type) }
    suspend fun getHourlyRate(type: String): Double {
        val v = withContext(Dispatchers.IO) { dao.getHourlyRate(type) }
        return v ?: when(type) {
            "TwoWheeler" -> 10.0
            "LMV" -> 20.0
            "HMV" -> 40.0
            else -> 20.0
        }
    }
    suspend fun getDailyRate(type: String): Double {
        val v = withContext(Dispatchers.IO) { dao.getDailyRate(type) }
        return v ?: when(type) {
            "TwoWheeler" -> 100.0
            "LMV" -> 200.0
            "HMV" -> 400.0
            else -> 200.0
        }
    }

    // revenue
    suspend fun getTotalRevenue(): Double = withContext(Dispatchers.IO) { dao.getTotalRevenue() } ?: 0.0
    suspend fun getTodayRevenue(): Double {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return withContext(Dispatchers.IO) { dao.getRevenueSince(cal.timeInMillis) } ?: 0.0
    }

    suspend fun getRevenueForDay(date: Long): Double {
        val cal = Calendar.getInstance()
        cal.timeInMillis = date
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000
        return withContext(Dispatchers.IO) { dao.getRevenueForDay(startOfDay, endOfDay) } ?: 0.0
    }

    suspend fun getThisWeekRevenue(): Double {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return withContext(Dispatchers.IO) { dao.getRevenueSince(cal.timeInMillis) } ?: 0.0
    }

    suspend fun getThisMonthRevenue(): Double {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return withContext(Dispatchers.IO) { dao.getRevenueSince(cal.timeInMillis) } ?: 0.0
    }

    companion object {
        @Volatile private var instance: Repository? = null
        fun getInstance(context: Context): Repository =
            instance ?: synchronized(this) {
                val repo = Repository(context)
                instance = repo
                repo
            }
    }
}
