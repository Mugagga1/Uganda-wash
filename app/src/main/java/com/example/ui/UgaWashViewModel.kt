package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Booking
import com.example.data.BookingRepository
import com.example.data.UgaWashDatabase
import com.example.data.WashProvider
import com.example.data.WashService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class UgaWashViewModel(
    application: Application,
    private val repository: BookingRepository
) : AndroidViewModel(application) {

    // Simulated user location (e.g. Makerere Hill, Kampala, Uganda)
    val userLatitude = 0.3292
    val userLongitude = 32.5701

    private val _selectedDistrict = MutableStateFlow("All")
    val selectedDistrict: StateFlow<String> = _selectedDistrict

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Reactive list of providers filtered by selected district and search query
    val providers: StateFlow<List<WashProvider>> = combine(
        repository.allProviders,
        _selectedDistrict,
        _searchQuery
    ) { providersList, district, query ->
        providersList.filter { provider ->
            val matchesDistrict = district == "All" || provider.district.equals(district, ignoreCase = true)
            val matchesQuery = query.isEmpty() || 
                    provider.name.contains(query, ignoreCase = true) || 
                    provider.locationName.contains(query, ignoreCase = true)
            matchesDistrict && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bookings: StateFlow<List<Booking>> = repository.allBookings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Flow for current Mobile Money Payment Initiation state
    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    // Selected Provider and Service details for the active booking checkout
    private val _activeBookingDetails = MutableStateFlow<ActiveBooking?>(null)
    val activeBookingDetails: StateFlow<ActiveBooking?> = _activeBookingDetails

    fun selectDistrict(district: String) {
        _selectedDistrict.value = district
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startBooking(provider: WashProvider, service: WashService) {
        _activeBookingDetails.value = ActiveBooking(provider, service)
        _paymentState.value = PaymentState.Idle
    }

    fun clearActiveBooking() {
        _activeBookingDetails.value = null
        _paymentState.value = PaymentState.Idle
    }

    /**
     * Triggers a highly realistic Uganda Mobile Money transaction sequence
     * (Simulates the USSD push prompt standard on MTN/Airtel in Kampala)
     */
    fun initiateMobileMoneyPayment(
        carPlate: String,
        carModel: String,
        mobileNumber: String,
        paymentProvider: String, // MTN or Airtel
        scheduledDate: String,
        scheduledTime: String
    ) {
        val active = _activeBookingDetails.value ?: return
        
        viewModelScope.launch {
            _paymentState.value = PaymentState.Initiating
            kotlinx.coroutines.delay(1500) // Simulating network connection to telecom gateway
            
            _paymentState.value = PaymentState.PromptSent(mobileNumber, paymentProvider)
            
            // Simulates user entering Pin on their phone after receiving telecom pop-up prompt
            kotlinx.coroutines.delay(4000) 
            
            _paymentState.value = PaymentState.Processing
            kotlinx.coroutines.delay(2000)
            
            // Generate Ugandan stylized verification code e.g. UW-5382
            val randomPin = (1000..9999).random()
            val code = "UG-$randomPin"
            
            val newBooking = Booking(
                providerId = active.provider.id,
                providerName = active.provider.name,
                providerLocation = active.provider.locationName,
                serviceName = active.service.name,
                priceUGX = active.service.priceUGX,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                carPlate = carPlate.uppercase(),
                carModel = carModel,
                mobileMoneyNumber = mobileNumber,
                paymentProvider = paymentProvider.uppercase(),
                paymentStatus = "PAID", // Successful payment!
                verificationCode = code
            )
            
            val bookingId = repository.bookWash(newBooking)
            
            _paymentState.value = PaymentState.Success(code, bookingId.toInt())
        }
    }

    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId)
        }
    }

    /**
     * Verifies a booking code at the local wash bay
     * This is the "Local Service Provider Verification" feature requested by the user
     */
    fun verifyBayBooking(booking: Booking, inputPin: String): Boolean {
        if (booking.verificationCode.equals(inputPin, ignoreCase = true) || inputPin == booking.verificationCode.substringAfter("-")) {
            viewModelScope.launch {
                repository.updatePaymentStatus(booking.id, "USED/VERIFIED")
            }
            return true
        }
        return false
    }

    // Distance calculation helper (Haversine formula in Km)
    fun calculateDistance(lat1: Double, lon1: Double): Float {
        val dLat = Math.toRadians(lat1 - userLatitude)
        val dLon = Math.toRadians(lon1 - userLongitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLatitude)) * cos(Math.toRadians(lat1)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val r = 6371 // Radius of earth inside km
        return (r * c).toFloat()
    }
}

data class ActiveBooking(
    val provider: WashProvider,
    val service: WashService
)

sealed interface PaymentState {
    object Idle : PaymentState
    object Initiating : PaymentState
    data class PromptSent(val number: String, val telecom: String) : PaymentState
    object Processing : PaymentState
    data class Success(val pin: String, val bookingId: Int) : PaymentState
    data class Failed(val error: String) : PaymentState
}

class UgaWashViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UgaWashViewModel::class.java)) {
            val db = UgaWashDatabase.getDatabase(application)
            val repository = BookingRepository(db.dao())
            @Suppress("UNCHECKED_CAST")
            return UgaWashViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
