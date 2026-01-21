package com.dev.parkingsystem

import androidx.room.*
import java.util.*

@Dao
interface ParkingDao {
    // Slots
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSlot(slot: Slot): Long

    @Delete
    fun deleteSlot(slot: Slot)

    @Query("SELECT * FROM slots ORDER BY slotId")
    fun getAllSlots(): List<Slot>

    @Query("SELECT COUNT(*) FROM slots")
    fun countSlots(): Int

    @Query("SELECT COUNT(*) FROM slots WHERE status = 'Available'")
    fun countAvailableSlots(): Int

    @Query("SELECT * FROM slots WHERE category = :category AND status = 'Available' ORDER BY slotId LIMIT 1")
    fun findFirstAvailableSlot(category: String): Slot?

    @Update
    fun updateSlot(slot: Slot)

    @Query("UPDATE slots SET status='Occupied', currentBookingId=:bookingId WHERE slotId=:slotId")
    fun occupySlot(slotId: Int, bookingId: Int)

    @Query("UPDATE slots SET status='Available', currentBookingId=NULL WHERE slotId=:slotId")
    fun freeSlot(slotId: Int)

    @Query("UPDATE slots SET status = 'Available', currentBookingId = NULL")
    fun freeAllSlots()

    // Bookings
    @Insert
    fun insertBooking(booking: Booking): Long

    @Query("SELECT * FROM bookings ORDER BY bookingId DESC")
    fun getAllBookings(): List<Booking>

    @Query("SELECT * FROM bookings WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY bookingId DESC")
    fun getBookingsForDay(startOfDay: Long, endOfDay: Long): List<Booking>

    @Query("SELECT * FROM bookings WHERE bookingId = :id LIMIT 1")
    fun getBookingById(id: Int): Booking?

    @Update
    fun updateBooking(booking: Booking)

    @Query("DELETE FROM bookings")
    fun deleteAllBookings()

    @Query("UPDATE bookings SET price = 0")
    fun resetAllBookingPrices()

    // Pricing
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPricing(p: Pricing)

    @Query("SELECT * FROM pricing WHERE vehicleType = :type LIMIT 1")
    fun getPricing(type: String): Pricing?

    @Query("SELECT hourlyRate FROM pricing WHERE vehicleType = :type LIMIT 1")
    fun getHourlyRate(type: String): Double?

    @Query("SELECT dailyRate FROM pricing WHERE vehicleType = :type LIMIT 1")
    fun getDailyRate(type: String): Double?

    // Revenue
    @Query("SELECT SUM(price) FROM bookings")
    fun getTotalRevenue(): Double?

    @Query("SELECT SUM(price) FROM bookings WHERE startTime >= :startOfDay AND startTime < :endOfDay")
    fun getRevenueForDay(startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT SUM(price) FROM bookings WHERE startTime >= :startOfDay")
    fun getRevenueSince(startOfDay: Long): Double?
}
