package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.QueueDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class QueueRepository(private val dao: QueueDao) {

    val allBusinesses: Flow<List<BusinessEntity>> = dao.getAllBusinesses()
    val allBookings: Flow<List<QueueBookingEntity>> = dao.getAllBookings()

    fun getBusinessesByCategory(category: BusinessCategory): Flow<List<BusinessEntity>> {
        return dao.getBusinessesByCategory(category)
    }

    fun getBusinessById(businessId: String): Flow<BusinessEntity?> {
        return dao.getBusinessById(businessId)
    }

    suspend fun getBusinessByOwnerEmail(email: String): BusinessEntity? {
        return dao.getBusinessByOwnerEmail(email)
    }

    fun getServicesForBusiness(businessId: String): Flow<List<ServiceEntity>> {
        return dao.getServicesForBusiness(businessId)
    }

    fun getAllServicesForBusiness(businessId: String): Flow<List<ServiceEntity>> {
        return dao.getAllServicesForBusiness(businessId)
    }

    fun getBookingsForBusiness(businessId: String): Flow<List<QueueBookingEntity>> {
        return dao.getBookingsForBusiness(businessId)
    }

    fun getCustomerBookings(phone: String, email: String): Flow<List<QueueBookingEntity>> {
        return dao.getCustomerBookings(phone, email)
    }

    fun getBookingById(bookingId: String): Flow<QueueBookingEntity?> {
        return dao.getBookingById(bookingId)
    }

    /**
     * Seeds initial business, service, and queue data if database is empty.
     */
    suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getAllBusinesses().first()
        if (existing.isNotEmpty()) return

        val todayDate = getTodayDateString()

        // 1. Salon Business
        val salonBiz = BusinessEntity(
            id = "biz_salon_luxe",
            name = "Luxe & Glow Unisex Salon",
            ownerName = "Alex Rivera",
            ownerEmail = "alex.salon@nextin.app",
            ownerPhone = "+1 (555) 234-5678",
            category = BusinessCategory.SALON,
            address = "742 Evergreen Terrace, Downtown Plaza, Suite 4B",
            openTime = "09:00 AM",
            closeTime = "08:30 PM",
            isOpen = true,
            consultationFee = 0.0,
            averageSlotDurationMinutes = 25,
            rating = 4.9,
            totalReviews = 218,
            qrCodePayload = "https://nextin.app/download?firmId=biz_salon_luxe"
        )

        // 2. Doctor Clinic Business
        val clinicBiz = BusinessEntity(
            id = "biz_clinic_apex",
            name = "Apex Health & Dental Clinic",
            ownerName = "Dr. Anita Sharma, MD",
            ownerEmail = "dr.anita@nextin.app",
            ownerPhone = "+1 (555) 876-5432",
            category = BusinessCategory.DOCTOR_CLINIC,
            address = "120 Medical Boulevard, Healthcare Wing 2",
            openTime = "08:30 AM",
            closeTime = "06:00 PM",
            isOpen = true,
            consultationFee = 50.0,
            averageSlotDurationMinutes = 15,
            maxPatientsPerSlot = 3,
            rating = 4.8,
            totalReviews = 164,
            qrCodePayload = "https://nextin.app/download?firmId=biz_clinic_apex"
        )

        dao.insertBusinesses(listOf(salonBiz, clinicBiz))

        // Pre-configured Male Services for Salon
        val salonMaleServices = listOf(
            ServiceEntity("srv_m_1", salonBiz.id, "Classic Haircut & Styling", ServiceGender.MALE, 25.0, 25, "Hair", "Precision cut, wash & style", isPopular = true),
            ServiceEntity("srv_m_2", salonBiz.id, "Beard Trim & Hot Towel Shave", ServiceGender.MALE, 18.0, 20, "Beard", "Straight razor shaping with essential oils", isPopular = true),
            ServiceEntity("srv_m_3", salonBiz.id, "Hair Color & Highlights", ServiceGender.MALE, 45.0, 45, "Color", "Ammonia-free organic dye"),
            ServiceEntity("srv_m_4", salonBiz.id, "Charcoal Detox Facial", ServiceGender.MALE, 40.0, 35, "Face", "Deep pore cleansing and exfoliating scrub"),
            ServiceEntity("srv_m_5", salonBiz.id, "Herbal Head Massage", ServiceGender.MALE, 20.0, 20, "Relaxation", "Warm herbal oil acupressure therapy")
        )

        // Pre-configured Female Services for Salon
        val salonFemaleServices = listOf(
            ServiceEntity("srv_f_1", salonBiz.id, "Signature Haircut & Blowdry", ServiceGender.FEMALE, 35.0, 35, "Hair", "Layering, shampoo wash & blowdry finish", isPopular = true),
            ServiceEntity("srv_f_2", salonBiz.id, "Keratin Protein Hair Spa", ServiceGender.FEMALE, 55.0, 50, "Hair Spa", "Deep conditioning & steam revival treatment", isPopular = true),
            ServiceEntity("srv_f_3", salonBiz.id, "Eyebrow Threading & Waxing", ServiceGender.FEMALE, 15.0, 15, "Grooming", "Full face shaping & soothing gel"),
            ServiceEntity("srv_f_4", salonBiz.id, "Gold Radiance Facial", ServiceGender.FEMALE, 60.0, 45, "Face", "Brightening facial with collagen mask"),
            ServiceEntity("srv_f_5", salonBiz.id, "Fruit Glow Cleanup", ServiceGender.FEMALE, 30.0, 30, "Face", "Instant tan removal & glow polish"),
            ServiceEntity("srv_f_6", salonBiz.id, "Deluxe Manicure & Pedicure", ServiceGender.FEMALE, 45.0, 40, "Nails", "Exfoliation, cuticle care, massage & polish")
        )

        dao.insertServices(salonMaleServices + salonFemaleServices)

        // Seed initial active queue tokens for interactive demo
        val initialBookings = listOf(
            QueueBookingEntity(
                id = "book_sln_101",
                businessId = salonBiz.id,
                businessName = salonBiz.name,
                businessCategory = BusinessCategory.SALON,
                tokenNumber = "SLN-101",
                tokenSeq = 101,
                customerName = "David Miller",
                customerPhone = "+1 555-0101",
                customerEmail = "david@example.com",
                bookedServicesJson = "Classic Haircut & Styling",
                totalPrice = 25.0,
                totalDurationMinutes = 25,
                appointmentDate = todayDate,
                appointmentSlotTime = "Live Queue",
                status = BookingStatus.SERVING,
                createdAt = System.currentTimeMillis() - (15 * 60 * 1000),
                servingStartedAt = System.currentTimeMillis() - (10 * 60 * 1000)
            ),
            QueueBookingEntity(
                id = "book_sln_102",
                businessId = salonBiz.id,
                businessName = salonBiz.name,
                businessCategory = BusinessCategory.SALON,
                tokenNumber = "SLN-102",
                tokenSeq = 102,
                customerName = "Marcus Vance",
                customerPhone = "+1 555-0102",
                customerEmail = "marcus@example.com",
                bookedServicesJson = "Classic Haircut & Styling, Beard Trim & Hot Towel Shave",
                totalPrice = 43.0,
                totalDurationMinutes = 45,
                appointmentDate = todayDate,
                appointmentSlotTime = "Live Queue",
                status = BookingStatus.WAITING,
                createdAt = System.currentTimeMillis() - (8 * 60 * 1000)
            ),
            QueueBookingEntity(
                id = "book_sln_103",
                businessId = salonBiz.id,
                businessName = salonBiz.name,
                businessCategory = BusinessCategory.SALON,
                tokenNumber = "SLN-103",
                tokenSeq = 103,
                customerName = "Elena Rostova",
                customerPhone = "+1 555-0103",
                customerEmail = "elena@example.com",
                bookedServicesJson = "Signature Haircut & Blowdry, Deluxe Manicure & Pedicure",
                totalPrice = 80.0,
                totalDurationMinutes = 75,
                appointmentDate = todayDate,
                appointmentSlotTime = "Live Queue",
                status = BookingStatus.WAITING,
                createdAt = System.currentTimeMillis() - (5 * 60 * 1000)
            ),
            // Clinic Bookings
            QueueBookingEntity(
                id = "book_doc_201",
                businessId = clinicBiz.id,
                businessName = clinicBiz.name,
                businessCategory = BusinessCategory.DOCTOR_CLINIC,
                tokenNumber = "DOC-201",
                tokenSeq = 201,
                customerName = "Robert Chen",
                customerPhone = "+1 555-0201",
                customerEmail = "robert.c@example.com",
                bookedServicesJson = "General Health Consultation",
                totalPrice = 50.0,
                totalDurationMinutes = 15,
                appointmentDate = todayDate,
                appointmentSlotTime = "09:00 AM - 09:30 AM",
                status = BookingStatus.SERVING,
                createdAt = System.currentTimeMillis() - (20 * 60 * 1000),
                servingStartedAt = System.currentTimeMillis() - (6 * 60 * 1000),
                notes = "Seasonal allergy & sinus check"
            ),
            QueueBookingEntity(
                id = "book_doc_202",
                businessId = clinicBiz.id,
                businessName = clinicBiz.name,
                businessCategory = BusinessCategory.DOCTOR_CLINIC,
                tokenNumber = "DOC-202",
                tokenSeq = 202,
                customerName = "Sophia Martinez",
                customerPhone = "+1 555-0202",
                customerEmail = "sophia.m@example.com",
                bookedServicesJson = "Dental & Oral Checkup",
                totalPrice = 50.0,
                totalDurationMinutes = 15,
                appointmentDate = todayDate,
                appointmentSlotTime = "09:30 AM - 10:00 AM",
                status = BookingStatus.WAITING,
                createdAt = System.currentTimeMillis() - (12 * 60 * 1000),
                notes = "Toothache in upper molar"
            )
        )

        dao.insertBookings(initialBookings)
    }

    // --- Salon Customer Booking ---
    suspend fun createSalonBooking(
        business: BusinessEntity,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        selectedServices: List<ServiceEntity>,
        notes: String = ""
    ): QueueBookingEntity {
        val today = getTodayDateString()
        val currentMax = dao.getMaxTokenSeq(business.id, today) ?: 100
        val nextSeq = currentMax + 1
        val tokenPrefix = if (business.category == BusinessCategory.SALON) "SLN" else "DOC"
        val tokenNumber = "$tokenPrefix-$nextSeq"

        val totalPrice = selectedServices.sumOf { it.price }
        val totalDuration = selectedServices.sumOf { it.durationMinutes }.coerceAtLeast(15)
        val servicesList = selectedServices.joinToString(", ") { it.name }

        val booking = QueueBookingEntity(
            id = "book_${UUID.randomUUID().toString().take(8)}",
            businessId = business.id,
            businessName = business.name,
            businessCategory = business.category,
            tokenNumber = tokenNumber,
            tokenSeq = nextSeq,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            customerEmail = customerEmail.trim(),
            bookedServicesJson = servicesList,
            totalPrice = totalPrice,
            totalDurationMinutes = totalDuration,
            appointmentDate = today,
            appointmentSlotTime = "Live Queue",
            status = BookingStatus.WAITING,
            createdAt = System.currentTimeMillis(),
            notes = notes
        )

        dao.insertBooking(booking)
        return booking
    }

    // --- Clinic Doctor Appointment Booking ---
    suspend fun createClinicBooking(
        business: BusinessEntity,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        appointmentDate: String,
        appointmentSlotTime: String,
        notes: String = ""
    ): QueueBookingEntity {
        val currentMax = dao.getMaxTokenSeq(business.id, appointmentDate) ?: 200
        val nextSeq = currentMax + 1
        val tokenNumber = "DOC-$nextSeq"

        val booking = QueueBookingEntity(
            id = "book_${UUID.randomUUID().toString().take(8)}",
            businessId = business.id,
            businessName = business.name,
            businessCategory = BusinessCategory.DOCTOR_CLINIC,
            tokenNumber = tokenNumber,
            tokenSeq = nextSeq,
            customerName = customerName.trim(),
            customerPhone = customerPhone.trim(),
            customerEmail = customerEmail.trim(),
            bookedServicesJson = "Doctor Consultation",
            totalPrice = business.consultationFee,
            totalDurationMinutes = business.averageSlotDurationMinutes,
            appointmentDate = appointmentDate,
            appointmentSlotTime = appointmentSlotTime,
            status = BookingStatus.WAITING,
            createdAt = System.currentTimeMillis(),
            notes = notes
        )

        dao.insertBooking(booking)
        return booking
    }

    // --- Walk-in Creation for Owner ---
    suspend fun createWalkInToken(
        businessId: String,
        customerName: String,
        serviceNames: String,
        price: Double,
        durationMinutes: Int
    ): QueueBookingEntity {
        val biz = dao.getBusinessByIdOnce(businessId) ?: throw IllegalStateException("Business not found")
        val today = getTodayDateString()
        val maxSeq = dao.getMaxTokenSeq(biz.id, today) ?: (if (biz.category == BusinessCategory.SALON) 100 else 200)
        val nextSeq = maxSeq + 1
        val prefix = if (biz.category == BusinessCategory.SALON) "SLN" else "DOC"
        val tokenNum = "$prefix-$nextSeq"

        val booking = QueueBookingEntity(
            id = "walkin_${UUID.randomUUID().toString().take(8)}",
            businessId = biz.id,
            businessName = biz.name,
            businessCategory = biz.category,
            tokenNumber = tokenNum,
            tokenSeq = nextSeq,
            customerName = if (customerName.isBlank()) "Walk-in Guest" else customerName.trim(),
            customerPhone = "Walk-in",
            customerEmail = "",
            bookedServicesJson = serviceNames,
            totalPrice = price,
            totalDurationMinutes = durationMinutes,
            appointmentDate = today,
            appointmentSlotTime = "Live Queue",
            status = BookingStatus.WAITING,
            createdAt = System.currentTimeMillis(),
            isWalkIn = true
        )

        dao.insertBooking(booking)
        return booking
    }

    // --- Real-time Queue Actions for Owner ---
    suspend fun callNextToken(businessId: String) {
        val bookings = dao.getBookingsForBusiness(businessId).first()
        val currentServing = bookings.find { it.status == BookingStatus.SERVING }
        if (currentServing != null) {
            dao.updateBooking(
                currentServing.copy(
                    status = BookingStatus.COMPLETED,
                    completedAt = System.currentTimeMillis()
                )
            )
        }

        val nextWaiting = bookings.firstOrNull { it.status == BookingStatus.WAITING }
        if (nextWaiting != null) {
            dao.updateBooking(
                nextWaiting.copy(
                    status = BookingStatus.SERVING,
                    servingStartedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun completeCurrentServing(bookingId: String) {
        dao.updateBookingStatus(bookingId, BookingStatus.COMPLETED)
    }

    suspend fun markNoShow(bookingId: String) {
        dao.updateBookingStatus(bookingId, BookingStatus.NO_SHOW)
    }

    suspend fun cancelBooking(bookingId: String) {
        dao.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
    }

    // --- Service & Profile Management ---
    suspend fun saveService(service: ServiceEntity) {
        dao.insertService(service)
    }

    suspend fun deleteService(service: ServiceEntity) {
        dao.deleteService(service)
    }

    suspend fun updateBusiness(business: BusinessEntity) {
        dao.updateBusiness(business)
    }

    suspend fun broadcastDelayAlert(businessId: String, delayMinutes: Int, message: String) {
        val biz = dao.getBusinessByIdOnce(businessId) ?: return
        dao.updateBusiness(
            biz.copy(
                activeDelayMinutes = delayMinutes,
                delayAlertMessage = message
            )
        )
    }

    suspend fun clearDelayAlert(businessId: String) {
        val biz = dao.getBusinessByIdOnce(businessId) ?: return
        dao.updateBusiness(
            biz.copy(
                activeDelayMinutes = 0,
                delayAlertMessage = ""
            )
        )
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun formatDateDisplay(dateStr: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
                val d = parser.parse(dateStr)
                if (d != null) formatter.format(d) else dateStr
            } catch (e: Exception) {
                dateStr
            }
        }
    }
}
