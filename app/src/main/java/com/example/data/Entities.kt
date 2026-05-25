package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wash_providers")
data class WashProvider(
    @PrimaryKey val id: Int,
    val name: String,
    val locationName: String,
    val district: String, // e.g. Kampala Central, Rubaga, Nakawa
    val latitude: Double,
    val longitude: Double,
    val rating: Float,
    val isVerified: Boolean,
    val verificationReason: String, // e.g., "KCCA Certified", "Airtel Partner"
    val phone: String,
    val servicesString: String, // e.g., "Normal Wash:15000|Steam Detailing:50000|Under Wash:20000"
    val imageUrl: String = ""
) {
    fun parseServices(): List<WashService> {
        return servicesString.split("|").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                WashService(parts[0], parts[1].toIntOrNull() ?: 0)
            } else null
        }
    }
}

data class WashService(
    val name: String,
    val priceUGX: Int
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerId: Int,
    val providerName: String,
    val providerLocation: String,
    val serviceName: String,
    val priceUGX: Int,
    val scheduledDate: String,
    val scheduledTime: String,
    val carPlate: String,
    val carModel: String,
    val mobileMoneyNumber: String,
    val paymentProvider: String, // MTN or Airtel
    val paymentStatus: String, // PENDING, PAID, FAILED
    val verificationCode: String, // UG-XXXX
    val timestamp: Long = System.currentTimeMillis()
)
