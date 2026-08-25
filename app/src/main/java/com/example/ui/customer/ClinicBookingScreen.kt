package com.example.ui.customer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.QueueRepository
import com.example.ui.common.LivePulseDot
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicBookingScreen(
    business: BusinessEntity,
    existingBookings: List<QueueBookingEntity>,
    userSession: UserSession,
    onBack: () -> Unit,
    onBookingConfirmed: (QueueBookingEntity) -> Unit,
    onConfirmBooking: suspend (BusinessEntity, String, String, String, String, String, String) -> QueueBookingEntity,
    modifier: Modifier = Modifier
) {
    // Generate dates: Today, Tomorrow, +2 days, +3 days
    val dateList = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        (0..4).map { offset ->
            if (offset > 0) cal.add(Calendar.DAY_OF_YEAR, 1)
            val dateStr = sdf.format(cal.time)
            val displaySdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            val displayLabel = if (offset == 0) "Today" else if (offset == 1) "Tomorrow" else displaySdf.format(cal.time)
            Pair(dateStr, displayLabel)
        }
    }

    var selectedDate by remember { mutableStateOf(dateList.first().first) }

    // Pre-configured clinic slot blocks
    val predefinedSlots = listOf(
        "09:00 AM - 09:30 AM",
        "09:30 AM - 10:00 AM",
        "10:00 AM - 10:30 AM",
        "10:30 AM - 11:00 AM",
        "11:00 AM - 11:30 AM",
        "02:00 PM - 02:30 PM",
        "02:30 PM - 03:00 PM",
        "03:00 PM - 03:30 PM",
        "03:30 PM - 04:00 PM",
        "04:00 PM - 04:30 PM"
    )

    var selectedSlot by remember { mutableStateOf<String?>(predefinedSlots.first()) }
    var patientName by remember { mutableStateOf(userSession.name) }
    var patientPhone by remember { mutableStateOf(userSession.phone) }
    var symptomsNotes by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = business.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${business.ownerName} • Doctor Consultation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Consultation Fee",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${business.consultationFee.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTeal
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedSlot == null) {
                                errorMessage = "Please select an appointment time slot"
                                return@Button
                            }
                            if (patientName.isBlank()) {
                                errorMessage = "Please enter patient name"
                                return@Button
                            }
                            errorMessage = null
                            isSubmitting = true
                        },
                        enabled = selectedSlot != null && !isSubmitting,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                        modifier = Modifier.testTag("confirm_clinic_slot_btn")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.EventAvailable, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm Slot", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LaunchedEffect(isSubmitting) {
            if (isSubmitting && selectedSlot != null) {
                try {
                    val booking = onConfirmBooking(
                        business,
                        patientName,
                        patientPhone,
                        userSession.email,
                        selectedDate,
                        selectedSlot!!,
                        symptomsNotes
                    )
                    isSubmitting = false
                    onBookingConfirmed(booking)
                } catch (e: Exception) {
                    isSubmitting = false
                    errorMessage = e.message ?: "Booking failed"
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Clinic Doctor Info Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(SecondaryTealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalInformation,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = business.ownerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "General Physician & Specialist",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "${business.averageSlotDurationMinutes} min slots",
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "Max ${business.maxPatientsPerSlot} patients/slot",
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Date Selector Tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Select Appointment Date",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dateList.take(3).forEach { (dateStr, displayLabel) ->
                            val isSelected = selectedDate == dateStr
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDate = dateStr },
                                label = { Text(displayLabel) },
                                shape = RoundedCornerShape(50)
                            )
                        }
                    }
                }
            }

            // Slot Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "2. Available Consultation Slots",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select a preferred time window",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Grid of Slots
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    predefinedSlots.chunked(2).forEach { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSlots.forEach { slotTime ->
                                val bookedInSlot = existingBookings.count {
                                    it.businessId == business.id &&
                                            it.appointmentDate == selectedDate &&
                                            it.appointmentSlotTime == slotTime &&
                                            it.status != BookingStatus.CANCELLED
                                }
                                val maxCap = business.maxPatientsPerSlot
                                val remaining = (maxCap - bookedInSlot).coerceAtLeast(0)
                                val isAvailable = remaining > 0
                                val isSelected = selectedSlot == slotTime

                                Card(
                                    onClick = {
                                        if (isAvailable) selectedSlot = slotTime
                                    },
                                    enabled = isAvailable,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) SecondaryTealLight
                                        else if (!isAvailable) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (isSelected) SecondaryTeal else MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .testTag("slot_${slotTime.replace(" ", "_")}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = slotTime,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) SecondaryTeal else if (!isAvailable) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp
                                        )

                                        Surface(
                                            color = if (isAvailable) StatusServingGreenLight else StatusCancelledRedLight,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = if (isAvailable) "$remaining open" else "Full",
                                                color = if (isAvailable) StatusServingGreen else StatusCancelledRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Patient Info Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "3. Patient Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Patient Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = patientPhone,
                            onValueChange = { patientPhone = it },
                            label = { Text("Mobile Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = symptomsNotes,
                            onValueChange = { symptomsNotes = it },
                            label = { Text("Reason for Visit / Symptoms (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                            placeholder = { Text("e.g., Routine dental checkup, sore throat") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
