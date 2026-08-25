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
import com.example.ui.common.CategoryChip
import com.example.ui.common.LivePulseDot
import com.example.ui.common.LogoVariant
import com.example.ui.common.NextInBrandLogo
import com.example.ui.common.StatCard
import com.example.ui.common.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    business: BusinessEntity,
    bookings: List<QueueBookingEntity>,
    onCallNext: () -> Unit,
    onCompleteBooking: (String) -> Unit,
    onMarkNoShow: (String) -> Unit,
    onCancelBooking: (String) -> Unit,
    onAddWalkIn: (String, String, Double, Int) -> Unit,
    onBroadcastDelay: (Int, String) -> Unit = { _, _ -> },
    onClearDelay: () -> Unit = {},
    onSwitchToCustomer: () -> Unit,
    onManageServices: () -> Unit,
    onViewProfileQr: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("WAITING") } // "WAITING", "ALL", "COMPLETED"
    var showWalkInDialog by remember { mutableStateOf(false) }
    var showDelayDialog by remember { mutableStateOf(false) }

    val servingBooking = remember(bookings) {
        bookings.firstOrNull { it.status == BookingStatus.SERVING }
    }

    val waitingBookings = remember(bookings) {
        bookings.filter { it.status == BookingStatus.WAITING }.sortedBy { it.tokenSeq }
    }

    val completedBookings = remember(bookings) {
        bookings.filter { it.status == BookingStatus.COMPLETED }.sortedByDescending { it.completedAt ?: it.createdAt }
    }

    val filteredList = remember(bookings, selectedTab) {
        when (selectedTab) {
            "WAITING" -> waitingBookings
            "COMPLETED" -> completedBookings
            else -> bookings.sortedBy { it.tokenSeq }
        }
    }

    val isSalon = business.category == BusinessCategory.SALON
    val avgDuration = business.averageSlotDurationMinutes
    val totalEstQueueWait = waitingBookings.size * avgDuration

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NextInBrandLogo(variant = LogoVariant.APP_BAR)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDelayDialog = true },
                        modifier = Modifier.testTag("broadcast_delay_btn")
                    ) {
                        Icon(
                            Icons.Default.NotificationImportant,
                            contentDescription = "Broadcast Delay Alert",
                            tint = if (business.activeDelayMinutes > 0) AccentAmber else GoldMetallic
                        )
                    }
                    IconButton(
                        onClick = onViewProfileQr,
                        modifier = Modifier.testTag("owner_qr_profile_btn")
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "Business QR", tint = GoldMetallic)
                    }
                    IconButton(
                        onClick = onManageServices,
                        modifier = Modifier.testTag("owner_services_menu_btn")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Manage Services/Slots", tint = GoldMetallic)
                    }
                    IconButton(
                        onClick = onSwitchToCustomer,
                        modifier = Modifier.testTag("switch_to_customer_btn")
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Switch to Customer", tint = SecondaryTeal)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showWalkInDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Walk-in Token", fontWeight = FontWeight.Bold) },
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("add_walkin_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Currently Serving Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (servingBooking != null) StatusServingGreenLight else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (servingBooking != null) StatusServingGreen.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (servingBooking != null) {
                                    LivePulseDot(color = StatusServingGreen, sizeDp = 8)
                                }
                                Text(
                                    text = if (servingBooking != null) "NOW SERVING AT COUNTER" else "READY FOR NEXT CUSTOMER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (servingBooking != null) StatusServingGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                            }

                            if (servingBooking != null) {
                                StatusBadge(status = BookingStatus.SERVING)
                            }
                        }

                        if (servingBooking != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Token #${servingBooking.tokenNumber}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StatusServingGreen
                                    )
                                    Text(
                                        text = "${servingBooking.customerName} • ${servingBooking.customerPhone}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = servingBooking.bookedServicesJson.ifBlank { "Consultation" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onCompleteBooking(servingBooking.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusCompletedBlue),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("complete_current_btn")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mark Complete", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { onMarkNoShow(servingBooking.id) },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("no_show_btn")
                                ) {
                                    Icon(Icons.Default.PersonOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("No Show", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "No active customer in service",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Call Next Button
                        Button(
                            onClick = onCallNext,
                            enabled = waitingBookings.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("call_next_token_btn")
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (waitingBookings.isNotEmpty()) "Call Next: #${waitingBookings.first().tokenNumber} (${waitingBookings.first().customerName})" else "No Waiting Tokens",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Live Delay Broadcast Banner (if active) or Broadcast trigger
            item {
                if (business.activeDelayMinutes > 0) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentAmberLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .testTag("owner_active_delay_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AccentAmber)
                                Column {
                                    Text(
                                        text = "LIVE DELAY BROADCAST: +${business.activeDelayMinutes} MINS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AccentAmber
                                    )
                                    if (business.delayAlertMessage.isNotBlank()) {
                                        Text(
                                            text = business.delayAlertMessage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            TextButton(
                                onClick = onClearDelay,
                                colors = ButtonDefaults.textButtonColors(contentColor = AccentAmber),
                                modifier = Modifier.testTag("clear_delay_btn")
                            ) {
                                Text("Clear", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showDelayDialog = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_broadcast_delay_btn")
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Broadcast Live Delay Notice (+ mins)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Stat Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "In Queue",
                        value = "${waitingBookings.size}",
                        subtitle = "~$totalEstQueueWait mins est.",
                        icon = Icons.Default.HourglassTop,
                        accentColor = PrimaryPurple,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Served Today",
                        value = "${completedBookings.size}",
                        subtitle = "Total: ${bookings.size}",
                        icon = Icons.Default.DoneAll,
                        accentColor = StatusCompletedBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Tab Filter Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == "WAITING",
                        onClick = { selectedTab = "WAITING" },
                        label = { Text("Waiting Queue (${waitingBookings.size})") },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = selectedTab == "COMPLETED",
                        onClick = { selectedTab = "COMPLETED" },
                        label = { Text("Completed (${completedBookings.size})") },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = selectedTab == "ALL",
                        onClick = { selectedTab = "ALL" },
                        label = { Text("All Bookings (${bookings.size})") },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // Queue List
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ${selectedTab.lowercase()} bookings found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { bookingItem ->
                    OwnerQueueItemCard(
                        booking = bookingItem,
                        onServe = {
                            // If user clicks serve directly on an item
                            onCompleteBooking(bookingItem.id)
                        },
                        onMarkNoShow = { onMarkNoShow(bookingItem.id) },
                        onCancel = { onCancelBooking(bookingItem.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // Add Walk-In Customer Dialog
        if (showWalkInDialog) {
            AddWalkInDialog(
                business = business,
                onDismiss = { showWalkInDialog = false },
                onAdd = { name, services, price, duration ->
                    onAddWalkIn(name, services, price, duration)
                    showWalkInDialog = false
                }
            )
        }

        // Broadcast Delay Dialog
        if (showDelayDialog) {
            BroadcastDelayDialog(
                currentDelay = business.activeDelayMinutes,
                currentMessage = business.delayAlertMessage,
                onDismiss = { showDelayDialog = false },
                onBroadcast = { minutes, message ->
                    onBroadcastDelay(minutes, message)
                    showDelayDialog = false
                },
                onClear = {
                    onClearDelay()
                    showDelayDialog = false
                }
            )
        }
    }
}

@Composable
private fun OwnerQueueItemCard(
    booking: QueueBookingEntity,
    onServe: () -> Unit,
    onMarkNoShow: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .testTag("owner_queue_item_${booking.tokenNumber}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = PrimaryPurpleContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "#${booking.tokenNumber}",
                            color = PrimaryPurpleDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Column {
                        Text(
                            text = booking.customerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${booking.customerPhone} • ${booking.appointmentSlotTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = booking.status)
            }

            Text(
                text = "Services: ${booking.bookedServicesJson.ifBlank { "General Visit" }} (~${booking.totalDurationMinutes}m | $${booking.totalPrice.toInt()})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (booking.status == BookingStatus.WAITING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onMarkNoShow) {
                        Text("No Show", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onCancel) {
                        Text("Cancel", fontSize = 12.sp, color = StatusCancelledRed)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWalkInDialog(
    business: BusinessEntity,
    onDismiss: () -> Unit,
    onAdd: (name: String, services: String, price: Double, duration: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var serviceName by remember { mutableStateOf(if (business.category == BusinessCategory.SALON) "Haircut + Styling" else "General Consultation") }
    var priceStr by remember { mutableStateOf(if (business.category == BusinessCategory.SALON) "25" else "50") }
    var durationStr by remember { mutableStateOf("${business.averageSlotDurationMinutes}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Generate Walk-In Token",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Guest / Customer Name") },
                    placeholder = { Text("e.g. John Doe (Walk-in)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = { Text("Service Description") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 20.0
                    val duration = durationStr.toIntOrNull() ?: 20
                    onAdd(name.ifBlank { "Walk-in Guest" }, serviceName, price, duration)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Issue Token", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BroadcastDelayDialog(
    currentDelay: Int,
    currentMessage: String,
    onDismiss: () -> Unit,
    onBroadcast: (minutes: Int, message: String) -> Unit,
    onClear: () -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(if (currentDelay > 0) currentDelay else 15) }
    var reasonNote by remember {
        mutableStateOf(
            if (currentMessage.isNotBlank()) currentMessage
            else "High customer rush in store"
        )
    }

    val presetOptions = listOf(5, 10, 15, 20, 30, 45)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = AccentAmber)
                Text(
                    text = "Broadcast Delay Alert",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Broadcast dynamic delay notices to all customers waiting in queue and viewing store profiles in real-time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Select Delay Duration:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetOptions.take(3).forEach { mins ->
                        FilterChip(
                            selected = selectedMinutes == mins,
                            onClick = { selectedMinutes = mins },
                            label = { Text("+$mins m") },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetOptions.drop(3).forEach { mins ->
                        FilterChip(
                            selected = selectedMinutes == mins,
                            onClick = { selectedMinutes = mins },
                            label = { Text("+$mins m") },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = reasonNote,
                    onValueChange = { reasonNote = it },
                    label = { Text("Delay Reason / Customer Notice") },
                    placeholder = { Text("e.g. Doctor in emergency procedure") },
                    singleLine = false,
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onBroadcast(selectedMinutes, reasonNote.trim())
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                modifier = Modifier.testTag("submit_delay_broadcast_btn")
            ) {
                Text("Broadcast Notice", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (currentDelay > 0) {
                    TextButton(onClick = onClear) {
                        Text("Clear Alert", color = StatusCancelledRed)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
