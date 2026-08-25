package com.example.ui.customer

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.BusinessEntity
import com.example.data.model.QueueBookingEntity
import com.example.data.model.ServiceEntity
import com.example.data.model.ServiceGender
import com.example.data.model.UserSession
import com.example.ui.common.LivePulseDot
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonBookingScreen(
    business: BusinessEntity,
    services: List<ServiceEntity>,
    userSession: UserSession,
    onBack: () -> Unit,
    onBookingConfirmed: (QueueBookingEntity) -> Unit,
    onConfirmBooking: suspend (BusinessEntity, String, String, String, List<ServiceEntity>, String) -> QueueBookingEntity,
    modifier: Modifier = Modifier
) {
    var selectedGenderFilter by remember { mutableStateOf<ServiceGender?>(null) }
    val selectedServices = remember { mutableStateListOf<ServiceEntity>() }
    
    var customerName by remember { mutableStateOf(userSession.name) }
    var customerPhone by remember { mutableStateOf(userSession.phone) }
    var customerNotes by remember { mutableStateOf("") }
    
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Pre-select first popular service on first load
    LaunchedEffect(services) {
        if (selectedServices.isEmpty() && services.isNotEmpty()) {
            services.firstOrNull { it.isPopular }?.let { selectedServices.add(it) }
        }
    }

    val filteredServices = remember(services, selectedGenderFilter) {
        if (selectedGenderFilter == null) services
        else services.filter { it.gender == selectedGenderFilter || it.gender == ServiceGender.UNISEX }
    }

    val totalPrice = remember(selectedServices.size) {
        selectedServices.sumOf { it.price }
    }
    val totalDuration = remember(selectedServices.size) {
        selectedServices.sumOf { it.durationMinutes }
    }

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
                            text = "Salon & Spa • Live Queue Token",
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
            // Sticky Bottom Summary Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedServices.size} Service(s) Selected",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$${totalPrice.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                                Text(
                                    text = "• ~$totalDuration mins",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedServices.isEmpty()) {
                                    errorMessage = "Please select at least one service."
                                    return@Button
                                }
                                if (customerName.isBlank()) {
                                    errorMessage = "Please enter your name."
                                    return@Button
                                }
                                errorMessage = null
                                isSubmitting = true
                            },
                            enabled = selectedServices.isNotEmpty() && !isSubmitting,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier.testTag("confirm_salon_booking_btn")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Get Live Token", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        // Handle trigger with CoroutineScope inside Compose
        LaunchedEffect(isSubmitting) {
            if (isSubmitting) {
                try {
                    val booking = onConfirmBooking(
                        business,
                        customerName,
                        customerPhone,
                        userSession.email,
                        selectedServices.toList(),
                        customerNotes
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
            // Live Status Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurpleContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.border(1.dp, PrimaryPurple.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LivePulseDot(color = PrimaryPurple, sizeDp = 8)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Live Walk-in Queue Open",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurpleDark,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Hours: ${business.openTime} - ${business.closeTime} • Tokens served in sequential order",
                                color = PrimaryPurpleDark.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Customer Details Section
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
                            text = "Your Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Your Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Phone Number (for SMS token updates)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Gender Filter Tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Services & Grooming",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedGenderFilter == null,
                            onClick = { selectedGenderFilter = null },
                            label = { Text("All Menu (${services.size})") },
                            shape = RoundedCornerShape(50)
                        )
                        FilterChip(
                            selected = selectedGenderFilter == ServiceGender.MALE,
                            onClick = { selectedGenderFilter = ServiceGender.MALE },
                            label = { Text("👨 Men's Services") },
                            shape = RoundedCornerShape(50)
                        )
                        FilterChip(
                            selected = selectedGenderFilter == ServiceGender.FEMALE,
                            onClick = { selectedGenderFilter = ServiceGender.FEMALE },
                            label = { Text("👩 Women's Services") },
                            shape = RoundedCornerShape(50)
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

            // Service Cards List
            items(filteredServices, key = { it.id }) { service ->
                val isSelected = selectedServices.any { it.id == service.id }

                ServiceSelectionCard(
                    service = service,
                    isSelected = isSelected,
                    onToggle = {
                        if (isSelected) {
                            selectedServices.removeAll { it.id == service.id }
                        } else {
                            selectedServices.add(service)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun ServiceSelectionCard(
    service: ServiceEntity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryPurpleContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(20.dp)
            )
            .testTag("service_item_${service.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (service.isPopular) {
                        Surface(
                            color = AccentAmberLight,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "POPULAR",
                                color = AccentAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (service.description.isNotBlank()) {
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${service.durationMinutes} mins",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = if (service.gender == ServiceGender.MALE) "Men" else if (service.gender == ServiceGender.FEMALE) "Women" else "Unisex",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "$${service.price.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
