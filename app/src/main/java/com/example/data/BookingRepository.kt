package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookingRepository(private val dao: UgaWashDao) {

    val allProviders: Flow<List<WashProvider>> = dao.getAllProviders()
    val allBookings: Flow<List<Booking>> = dao.getAllBookings()

    init {
        // Prepopulate database with Ugandan providers if it's currently empty
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (dao.getProvidersCount() == 0) {
                    dao.insertProviders(getInitialProviders())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getProviderById(id: Int): WashProvider? {
        return dao.getProviderById(id)
    }

    suspend fun bookWash(booking: Booking): Long {
        return dao.insertBooking(booking)
    }

    suspend fun updatePaymentStatus(bookingId: Int, status: String) {
        dao.updateBookingStatus(bookingId, status)
    }

    suspend fun cancelBooking(bookingId: Int) {
        dao.deleteBooking(bookingId)
    }

    private fun getInitialProviders(): List<WashProvider> {
        return listOf(
            WashProvider(
                id = 1,
                name = "Kampala Deluxe Detailing",
                locationName = "Shell Bugolobi, Luthuli Ave, Kampala",
                district = "Kampala Central",
                latitude = 0.3162,
                longitude = 32.6105,
                rating = 4.9f,
                isVerified = true,
                verificationReason = "Certified Detailing Academy partner • KCCA Licensed",
                phone = "+256 772 120 345",
                servicesString = "Silver Wash & Clear:15000|Steam Detailing & Wax:50000|Premium Full Polish:120000",
                imageUrl = "https://images.unsplash.com/photo-1520340356584-f9917d1ecc6f?w=400&auto=format&fit=crop&q=60"
            ),
            WashProvider(
                id = 2,
                name = "Wandegeya Sparkle Bay",
                locationName = "Bombo Road near Shell, Kampala",
                district = "Kawempe / Makerere",
                latitude = 0.3345,
                longitude = 32.5714,
                rating = 4.7f,
                isVerified = true,
                verificationReason = "KCCA Environmental Board Green Wash License",
                phone = "+256 756 991 234",
                servicesString = "Standard Wash & Vacuum:12000|Under Wash & Engine Blast:25000|Premium Gold Shine:45000",
                imageUrl = "https://images.unsplash.com/photo-1605691918993-41c1b9c8ceb0?w=400&auto=format&fit=crop&q=60"
            ),
            WashProvider(
                id = 3,
                name = "Entebbe Eco Express Wash",
                locationName = "Victoria Mall carpark, Entebbe Road",
                district = "Entebbe",
                latitude = 0.0512,
                longitude = 32.4637,
                rating = 4.6f,
                isVerified = true,
                verificationReason = "Eco-Friendly Rated • National Environment Management (NEMA) Certified",
                phone = "+256 701 445 009",
                servicesString = "Eco Foam Body Spray:18000|Waterless Clay Wax:35000|Eco Wash & Gloss Guard:55000",
                imageUrl = "https://images.unsplash.com/photo-1552930294-6b595f4c2974?w=400&auto=format&fit=crop&q=60"
            ),
            WashProvider(
                id = 4,
                name = "Kireka Junction Auto Spar",
                locationName = "Jinja Road opposite Kireka Market",
                district = "Nakawa / Wakiso",
                latitude = 0.3456,
                longitude = 32.6512,
                rating = 4.5f,
                isVerified = true,
                verificationReason = "Uganda Road Safety Association partner • Verified",
                phone = "+256 777 558 911",
                servicesString = "Quick Body Dust:8000|Silver Wash & Carpet Wash:15000|Harsh Stain Deep Clean:60000",
                imageUrl = "https://images.unsplash.com/photo-1607860108855-64cac207c742?w=400&auto=format&fit=crop&q=60"
            ),
            WashProvider(
                id = 5,
                name = "Jinja Source-View Detailing",
                locationName = "Nile Avenue near Jinja Bridge",
                district = "Jinja",
                latitude = 0.4244,
                longitude = 33.2045,
                rating = 4.8f,
                isVerified = true,
                verificationReason = "Tourism Board Approved Eco detailing center",
                phone = "+256 781 123 456",
                servicesString = "Nile Mist Wash:15000|Engine Steam Deep Sparkle:35000|Lake View Luxury Detail:80000",
                imageUrl = "https://images.unsplash.com/photo-1517524206127-48bbd363f3d7?w=400&auto=format&fit=crop&q=60"
            ),
            WashProvider(
                id = 6,
                name = "Rubaga Elite Auto Spa",
                locationName = "Rubaga Cathedral road, Kampala",
                district = "Rubaga",
                latitude = 0.3012,
                longitude = 32.5541,
                rating = 4.4f,
                isVerified = false,
                verificationReason = "Verification Pending: Self-regulatory detailing code #1024",
                phone = "+256 702 331 445",
                servicesString = "Body Wash:12000|Interior Vacuum & Air Scent:18000|Hand Polish Wax:40000",
                imageUrl = ""
            )
        )
    }
}
