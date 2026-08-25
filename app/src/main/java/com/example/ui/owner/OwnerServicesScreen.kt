package com.example.ui.owner

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerServicesScreen(
    business: BusinessEntity,
    services: List<ServiceEntity>,
    onSaveService: (ServiceEntity) -> Unit,
    onDeleteService: (ServiceEntity) -> Unit,
    onUpdateBusinessConfig: (BusinessEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSalon = business.category == BusinessCategory.SALON
    var selectedGenderTab by remember { mutableStateOf(ServiceGender.MALE) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ServiceEntity?>(null) }

    // Clinic Config State
    var clinicFeeStr by remember { mutableStateOf("${business.consultationFee.toInt()}") }
    var slotDurationMinutes by remember { mutableStateOf(business.averageSlotDurationMinutes) }
    var maxPatientsPerSlot by remember { mutableStateOf(business.maxPatientsPerSlot) }
    var hasConfigSavedMessage by remember { mutableStateOf(false) }

    val filteredServices = remember(services, selectedGenderTab) {
        services.filter { it.gender == selectedGenderTab || it.gender == ServiceGender.UNISEX }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isSalon) "Service Catalog & Pricing" else "Clinic Slot Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${business.name} • Setup",
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
                actions = {
                    if (isSalon) {
                        IconButton(
                            onClick = {
                                serviceToEdit = null
                                showAddServiceDialog = true
                            },
                            modifier = Modifier.testTag("add_new_service_btn")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Service", tint = PrimaryPurple)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isSalon) {
            // Salon Service Management
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gender Filter Switcher
                item {
                    Card(
                        shape = RoundedCornerShape(50),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            FilterTabItem(
                                title = "👨 Men's Services",
                                isSelected = selectedGenderTab == ServiceGender.MALE,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedGenderTab = ServiceGender.MALE }
                            )
                            FilterTabItem(
                                title = "👩 Women's Services",
                                isSelected = selectedGenderTab == ServiceGender.FEMALE,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedGenderTab = ServiceGender.FEMALE }
                            )
                        }
                    }
                }

                // Info banner
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryPurple)
                            Text(
                                text = "Adjust prices and durations to calculate accurate live wait times for walk-in customers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryPurpleDark
                            )
                        }
                    }
                }

                // Services List
                items(filteredServices, key = { it.id }) { srv ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                            .testTag("owner_service_item_${srv.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = srv.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (srv.isPopular) {
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

                                if (srv.description.isNotBlank()) {
                                    Text(
                                        text = srv.description,
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
                                        Text(
                                            text = "${srv.durationMinutes} mins",
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "$${srv.price.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPurple,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            IconButton(onClick = {
                                serviceToEdit = srv
                                showAddServiceDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Service", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        } else {
            // Clinic Slot Configuration
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Doctor Consultation Fee",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = clinicFeeStr,
                                onValueChange = { clinicFeeStr = it },
                                label = { Text("Standard Fee ($)") },
                                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Divider()

                            Text(
                                text = "Appointment Slot Duration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(15, 20, 30, 45).forEach { dur ->
                                    FilterChip(
                                        selected = slotDurationMinutes == dur,
                                        onClick = { slotDurationMinutes = dur },
                                        label = { Text("$dur mins") },
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }

                            Divider()

                            Text(
                                text = "Max Patients per Time Slot",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                (1..5).forEach { count ->
                                    FilterChip(
                                        selected = maxPatientsPerSlot == count,
                                        onClick = { maxPatientsPerSlot = count },
                                        label = { Text("$count Patient${if (count > 1) "s" else ""}") },
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val fee = clinicFeeStr.toDoubleOrNull() ?: 50.0
                                    onUpdateBusinessConfig(
                                        business.copy(
                                            consultationFee = fee,
                                            averageSlotDurationMinutes = slotDurationMinutes,
                                            maxPatientsPerSlot = maxPatientsPerSlot
                                        )
                                    )
                                    hasConfigSavedMessage = true
                                },
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_clinic_config_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Clinic Settings", fontWeight = FontWeight.Bold)
                            }

                            if (hasConfigSavedMessage) {
                                Text(
                                    text = "✓ Clinic settings updated successfully!",
                                    color = StatusServingGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add/Edit Service Dialog
        if (showAddServiceDialog) {
            ServiceEditDialog(
                initialService = serviceToEdit,
                defaultGender = selectedGenderTab,
                businessId = business.id,
                onDismiss = { showAddServiceDialog = false },
                onSave = { srv ->
                    onSaveService(srv)
                    showAddServiceDialog = false
                },
                onDelete = { srv ->
                    onDeleteService(srv)
                    showAddServiceDialog = false
                }
            )
        }
    }
}

@Composable
private fun FilterTabItem(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 1.dp else 0.dp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceEditDialog(
    initialService: ServiceEntity?,
    defaultGender: ServiceGender,
    businessId: String,
    onDismiss: () -> Unit,
    onSave: (ServiceEntity) -> Unit,
    onDelete: (ServiceEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialService?.name ?: "") }
    var priceStr by remember { mutableStateOf(initialService?.price?.toInt()?.toString() ?: "30") }
    var durationStr by remember { mutableStateOf(initialService?.durationMinutes?.toString() ?: "25") }
    var gender by remember { mutableStateOf(initialService?.gender ?: defaultGender) }
    var description by remember { mutableStateOf(initialService?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialService == null) "Add New Service" else "Edit Service",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    placeholder = { Text("e.g. Beard Styling & Wash") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("Duration (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Short details of what's included") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = gender == ServiceGender.MALE,
                        onClick = { gender = ServiceGender.MALE },
                        label = { Text("Men") },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = gender == ServiceGender.FEMALE,
                        onClick = { gender = ServiceGender.FEMALE },
                        label = { Text("Women") },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = gender == ServiceGender.UNISEX,
                        onClick = { gender = ServiceGender.UNISEX },
                        label = { Text("Unisex") },
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 25.0
                    val duration = durationStr.toIntOrNull() ?: 25
                    val srv = ServiceEntity(
                        id = initialService?.id ?: "srv_${UUID.randomUUID().toString().take(8)}",
                        businessId = businessId,
                        name = name.ifBlank { "Custom Service" },
                        gender = gender,
                        price = price,
                        durationMinutes = duration,
                        description = description
                    )
                    onSave(srv)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Save Service", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (initialService != null) {
                    TextButton(
                        onClick = { onDelete(initialService) },
                        colors = ButtonDefaults.textButtonColors(contentColor = StatusCancelledRed)
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
