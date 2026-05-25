package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UgaWashDao {
    @Query("SELECT * FROM wash_providers ORDER BY rating DESC")
    fun getAllProviders(): Flow<List<WashProvider>>

    @Query("SELECT * FROM wash_providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: Int): WashProvider?

    @Query("SELECT COUNT(*) FROM wash_providers")
    suspend fun getProvidersCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<WashProvider>)

    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Query("UPDATE bookings SET paymentStatus = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Int, status: String)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBooking(id: Int)
}
