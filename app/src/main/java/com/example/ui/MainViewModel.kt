package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.QueueRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    object CustomerExplore : Screen()
    object CustomerSalonBooking : Screen()
    object CustomerClinicBooking : Screen()
    object CustomerLiveToken : Screen()
    object OwnerDashboard : Screen()
    object OwnerServices : Screen()
    object OwnerProfileQr : Screen()
    data class PrivacyPolicy(val returnScreen: Screen = Screen.Auth) : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QueueRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = QueueRepository(db.queueDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun finishSplash() {
        if (_currentScreen.value is Screen.Splash) {
            _currentScreen.value = if (_userSession.value != null) {
                if (_userSession.value?.role == UserRole.BUSINESS_OWNER) Screen.OwnerDashboard else Screen.CustomerExplore
            } else {
                Screen.Auth
            }
        }
    }

    val allBusinesses: StateFlow<List<BusinessEntity>> = repository.allBusinesses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<QueueBookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedBusinessId = MutableStateFlow<String?>("biz_salon_luxe")
    val selectedBusinessId: StateFlow<String?> = _selectedBusinessId.asStateFlow()

    val selectedBusiness: StateFlow<BusinessEntity?> = combine(allBusinesses, _selectedBusinessId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentBusinessServices: StateFlow<List<ServiceEntity>> = _selectedBusinessId
        .flatMapLatest { id ->
            if (id != null) repository.getAllServicesForBusiness(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeLiveBookingId = MutableStateFlow<String?>(null)
    val activeLiveBookingId: StateFlow<String?> = _activeLiveBookingId.asStateFlow()

    val activeLiveBooking: StateFlow<QueueBookingEntity?> = combine(allBookings, _activeLiveBookingId) { bookings, id ->
        if (id != null) bookings.find { it.id == id } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Find active customer booking for current user
    val customerActiveBooking: StateFlow<QueueBookingEntity?> = combine(allBookings, _userSession) { bookings, session ->
        if (session != null && session.role == UserRole.CUSTOMER) {
            bookings.firstOrNull {
                (it.customerPhone == session.phone || it.customerEmail == session.email || it.customerName == session.name) &&
                        (it.status == BookingStatus.WAITING || it.status == BookingStatus.SERVING)
            }
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun login(session: UserSession) {
        _userSession.value = session
        if (session.role == UserRole.BUSINESS_OWNER) {
            val bizId = session.businessId ?: "biz_salon_luxe"
            _selectedBusinessId.value = bizId
            _currentScreen.value = Screen.OwnerDashboard
        } else {
            _currentScreen.value = Screen.CustomerExplore
        }
    }

    fun logout() {
        _userSession.value = null
        _activeLiveBookingId.value = null
        _currentScreen.value = Screen.Auth
    }

    fun switchRole() {
        val current = _userSession.value ?: return
        if (current.role == UserRole.CUSTOMER) {
            val ownerSession = UserSession(
                role = UserRole.BUSINESS_OWNER,
                userId = "owner_salon_1",
                name = "Alex Rivera",
                phone = "+1 555-2345",
                email = "alex.salon@queuebook.app",
                businessId = "biz_salon_luxe"
            )
            login(ownerSession)
        } else {
            val customerSession = UserSession(
                role = UserRole.CUSTOMER,
                userId = "cust_1",
                name = "Sarah Jenkins",
                phone = "+1 555-0199",
                email = "sarah.j@example.com"
            )
            login(customerSession)
        }
    }

    fun selectBusiness(biz: BusinessEntity) {
        _selectedBusinessId.value = biz.id
        if (biz.category == BusinessCategory.SALON) {
            _currentScreen.value = Screen.CustomerSalonBooking
        } else {
            _currentScreen.value = Screen.CustomerClinicBooking
        }
    }

    fun selectLiveBooking(booking: QueueBookingEntity) {
        _activeLiveBookingId.value = booking.id
        _selectedBusinessId.value = booking.businessId
        _currentScreen.value = Screen.CustomerLiveToken
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    private var previousScreenBeforePrivacy: Screen = Screen.Auth

    fun openPrivacyPolicy(fromScreen: Screen = _currentScreen.value) {
        previousScreenBeforePrivacy = fromScreen
        _currentScreen.value = Screen.PrivacyPolicy(fromScreen)
    }

    fun closePrivacyPolicy() {
        _currentScreen.value = previousScreenBeforePrivacy
    }

    suspend fun bookSalon(
        business: BusinessEntity,
        name: String,
        phone: String,
        email: String,
        services: List<ServiceEntity>,
        notes: String
    ): QueueBookingEntity {
        val booking = repository.createSalonBooking(business, name, phone, email, services, notes)
        _activeLiveBookingId.value = booking.id
        return booking
    }

    suspend fun bookClinic(
        business: BusinessEntity,
        name: String,
        phone: String,
        email: String,
        date: String,
        slot: String,
        notes: String
    ): QueueBookingEntity {
        val booking = repository.createClinicBooking(business, name, phone, email, date, slot, notes)
        _activeLiveBookingId.value = booking.id
        return booking
    }

    fun callNextToken(businessId: String) {
        viewModelScope.launch {
            repository.callNextToken(businessId)
        }
    }

    fun completeBooking(bookingId: String) {
        viewModelScope.launch {
            repository.completeCurrentServing(bookingId)
        }
    }

    fun markNoShow(bookingId: String) {
        viewModelScope.launch {
            repository.markNoShow(bookingId)
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId)
        }
    }

    fun addWalkIn(businessId: String, name: String, services: String, price: Double, duration: Int) {
        viewModelScope.launch {
            repository.createWalkInToken(businessId, name, services, price, duration)
        }
    }

    fun saveService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.saveService(service)
        }
    }

    fun deleteService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }

    fun updateBusinessProfile(business: BusinessEntity) {
        viewModelScope.launch {
            repository.updateBusiness(business)
        }
    }

    fun broadcastDelayAlert(businessId: String, delayMinutes: Int, message: String) {
        viewModelScope.launch {
            repository.broadcastDelayAlert(businessId, delayMinutes, message)
        }
    }

    fun clearDelayAlert(businessId: String) {
        viewModelScope.launch {
            repository.clearDelayAlert(businessId)
        }
    }
}
