package com.example.data.local

import androidx.room.*
import com.example.data.model.BookingStatus
import com.example.data.model.BusinessCategory
import com.example.data.model.BusinessEntity
import com.example.data.model.QueueBookingEntity
import com.example.data.model.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {

    // --- Businesses ---
    @Query("SELECT * FROM businesses")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE category = :category")
    fun getBusinessesByCategory(category: BusinessCategory): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :businessId")
    fun getBusinessById(businessId: String): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses WHERE id = :businessId")
    suspend fun getBusinessByIdOnce(businessId: String): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE ownerEmail = :email LIMIT 1")
    suspend fun getBusinessByOwnerEmail(email: String): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<BusinessEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    // --- Services ---
    @Query("SELECT * FROM services WHERE businessId = :businessId AND isEnabled = 1")
    fun getServicesForBusiness(businessId: String): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE businessId = :businessId")
    fun getAllServicesForBusiness(businessId: String): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity)

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    // --- Bookings & Queue ---
    @Query("SELECT * FROM queue_bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<QueueBookingEntity>>

    @Query("SELECT * FROM queue_bookings WHERE businessId = :businessId ORDER BY tokenSeq ASC")
    fun getBookingsForBusiness(businessId: String): Flow<List<QueueBookingEntity>>

    @Query("SELECT * FROM queue_bookings WHERE businessId = :businessId AND status = :status ORDER BY tokenSeq ASC")
    fun getBookingsByStatus(businessId: String, status: BookingStatus): Flow<List<QueueBookingEntity>>

    @Query("SELECT * FROM queue_bookings WHERE id = :bookingId")
    fun getBookingById(bookingId: String): Flow<QueueBookingEntity?>

    @Query("SELECT * FROM queue_bookings WHERE customerPhone = :phone OR customerEmail = :email ORDER BY createdAt DESC")
    fun getCustomerBookings(phone: String, email: String): Flow<List<QueueBookingEntity>>

    @Query("SELECT COUNT(*) FROM queue_bookings WHERE businessId = :businessId AND appointmentDate = :date")
    suspend fun getDailyBookingCount(businessId: String, date: String): Int

    @Query("SELECT MAX(tokenSeq) FROM queue_bookings WHERE businessId = :businessId AND appointmentDate = :date")
    suspend fun getMaxTokenSeq(businessId: String, date: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: QueueBookingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<QueueBookingEntity>)

    @Update
    suspend fun updateBooking(booking: QueueBookingEntity)

    @Query("UPDATE queue_bookings SET status = :newStatus WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: String, newStatus: BookingStatus)

    @Query("DELETE FROM queue_bookings WHERE id = :bookingId")
    suspend fun deleteBooking(bookingId: String)
}
