package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    BUSINESS_OWNER
}

enum class BusinessCategory {
    SALON,
    DOCTOR_CLINIC
}

enum class ServiceGender {
    MALE,
    FEMALE,
    UNISEX
}

enum class BookingStatus {
    WAITING,
    SERVING,
    COMPLETED,
    NO_SHOW,
    CANCELLED
}

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ownerName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val category: BusinessCategory,
    val address: String,
    val openTime: String = "09:00 AM",
    val closeTime: String = "08:00 PM",
    val isOpen: Boolean = true,
    val consultationFee: Double = 500.0,
    val averageSlotDurationMinutes: Int = 20,
    val maxPatientsPerSlot: Int = 3,
    val rating: Double = 4.8,
    val totalReviews: Int = 142,
    val imageUrl: String = "",
    val qrCodePayload: String = "",
    val activeDelayMinutes: Int = 0,
    val delayAlertMessage: String = ""
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val name: String,
    val gender: ServiceGender,
    val price: Double,
    val durationMinutes: Int,
    val category: String = "General",
    val description: String = "",
    val isPopular: Boolean = false,
    val isEnabled: Boolean = true
)

@Entity(tableName = "queue_bookings")
data class QueueBookingEntity(
    @PrimaryKey val id: String,
    val businessId: String,
    val businessName: String,
    val businessCategory: BusinessCategory,
    val tokenNumber: String,
    val tokenSeq: Int,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String = "",
    val bookedServicesJson: String = "", // Comma-separated or service names
    val totalPrice: Double,
    val totalDurationMinutes: Int,
    val appointmentDate: String, // e.g. "2026-08-24"
    val appointmentSlotTime: String = "Live Queue", // e.g. "10:00 AM - 10:30 AM" or "Live Queue"
    val status: BookingStatus = BookingStatus.WAITING,
    val createdAt: Long = System.currentTimeMillis(),
    val servingStartedAt: Long? = null,
    val completedAt: Long? = null,
    val notes: String = "",
    val isWalkIn: Boolean = false
)

data class TimeSlot(
    val timeLabel: String,
    val period: String, // "Morning", "Afternoon", "Evening"
    val maxCapacity: Int,
    val bookedCount: Int
) {
    val isAvailable: Boolean get() = bookedCount < maxCapacity
    val remainingCapacity: Int get() = (maxCapacity - bookedCount).coerceAtLeast(0)
}

data class UserSession(
    val role: UserRole,
    val userId: String,
    val name: String,
    val phone: String,
    val email: String,
    val businessId: String? = null
)
