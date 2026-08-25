package com.example.ui.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingStatus
import com.example.data.model.BusinessCategory
import com.example.data.model.BusinessEntity
import com.example.data.model.QueueBookingEntity
import com.example.data.repository.QueueRepository
import com.example.ui.common.CategoryChip
import com.example.ui.common.FiveMinuteTurnAlertBanner
import com.example.ui.common.LivePulseDot
import com.example.ui.common.LogoVariant
import com.example.ui.common.NextInBrandLogo
import com.example.ui.common.StatCard
import com.example.ui.common.StatusBadge
import com.example.ui.theme.*
import com.example.util.QrCodeView
import com.example.util.QueueAlertManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLiveTokenScreen(
    booking: QueueBookingEntity,
    business: BusinessEntity?,
    allBookingsForBusiness: List<QueueBookingEntity>,
    onBack: () -> Unit,
    onCancelBooking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showQrDialog by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Real-Time Queue Calculations
    val activeWaitingAhead = remember(allBookingsForBusiness, booking) {
        if (booking.status != BookingStatus.WAITING) 0
        else {
            allBookingsForBusiness.count {
                it.status == BookingStatus.WAITING && it.tokenSeq < booking.tokenSeq
            }
        }
    }

    val currentlyServingBooking = remember(allBookingsForBusiness) {
        allBookingsForBusiness.firstOrNull { it.status == BookingStatus.SERVING }
    }

    val avgDuration = business?.averageSlotDurationMinutes ?: 20
    val activeDelay = business?.activeDelayMinutes ?: 0
    val estimatedWaitMinutes = remember(activeWaitingAhead, avgDuration, booking.status, activeDelay) {
        if (booking.status == BookingStatus.SERVING) 0
        else if (booking.status == BookingStatus.COMPLETED || booking.status == BookingStatus.CANCELLED) 0
        else (activeWaitingAhead * avgDuration) + (if (currentlyServingBooking != null) 10 else 0) + activeDelay
    }

    val isSalon = booking.businessCategory == BusinessCategory.SALON

    // Smart 5-Minute Alert Engine Trigger:
    // When estimated wait drops to <= 5 mins OR people ahead <= 2, automatically trigger hardware vibration & push notification
    LaunchedEffect(booking.id, estimatedWaitMinutes, activeWaitingAhead, booking.status) {
        if (booking.status == BookingStatus.WAITING) {
            QueueAlertManager.checkAndTriggerQueueAlert(
                context = context,
                bookingId = booking.id,
                firmName = business?.name ?: booking.businessName,
                estimatedMinutes = estimatedWaitMinutes,
                peopleAhead = activeWaitingAhead
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NextInBrandLogo(variant = LogoVariant.APP_BAR)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            QueueAlertManager.triggerAlert(
                                context = context,
                                firmName = business?.name ?: booking.businessName,
                                estimatedMinutes = if (estimatedWaitMinutes > 0) estimatedWaitMinutes else 5,
                                peopleAhead = activeWaitingAhead
                            )
                        },
                        modifier = Modifier.testTag("test_alert_header_btn")
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Test 5-min Alert & Vibrate",
                            tint = GoldMetallic
                        )
                    }
                    IconButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier.testTag("open_qr_pass_btn")
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Digital QR Pass", tint = GoldMetallic)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 5-Minute Alert System Banner (Triggers when <= 5 mins or <= 2 people ahead)
            if (booking.status == BookingStatus.WAITING && (estimatedWaitMinutes in 1..5 || activeWaitingAhead <= 2)) {
                FiveMinuteTurnAlertBanner(
                    estimatedMinutes = estimatedWaitMinutes,
                    peopleAhead = activeWaitingAhead,
                    onSimulateAlert = {
                        QueueAlertManager.triggerAlert(
                            context = context,
                            firmName = business?.name ?: booking.businessName,
                            estimatedMinutes = estimatedWaitMinutes,
                            peopleAhead = activeWaitingAhead
                        )
                    }
                )
            }

            // Hero Live Token Card (Luxury Red & Gold Styling)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (booking.status == BookingStatus.SERVING) StatusServingGreenLight
                    else if (booking.status == BookingStatus.WAITING) PrimaryWineContainer
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (booking.status == BookingStatus.SERVING) StatusServingGreen.copy(alpha = 0.5f)
                        else if (booking.status == BookingStatus.WAITING) GoldMetallic
                        else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("live_token_hero_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryChip(category = booking.businessCategory)
                        StatusBadge(status = booking.status)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "YOUR TOKEN NUMBER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = if (booking.status == BookingStatus.WAITING) GoldLight else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Big Display Token with Gold Glow
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .border(
                                1.5.dp,
                                if (booking.status == BookingStatus.WAITING) GoldMetallic else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "#${booking.tokenNumber}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = if (booking.status == BookingStatus.SERVING) StatusServingGreen else PrimaryWine,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    if (booking.status == BookingStatus.SERVING) {
                        Surface(
                            color = StatusServingGreen,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LivePulseDot(color = Color.White, sizeDp = 8)
                                Text(
                                    text = "IT'S YOUR TURN! PLEASE PROCEED TO COUNTER",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (booking.status == BookingStatus.WAITING) {
                        Surface(
                            color = GoldBadgeBg,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(1.dp, GoldMetallic.copy(alpha = 0.7f), RoundedCornerShape(50))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LivePulseDot(color = GoldBright, sizeDp = 7)
                                Text(
                                    text = "Live updates active • Smart 5-min alert enabled",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldBright
                                )
                            }
                        }
                    }
                }
            }

            // Live Broadcast Delay Alert (if store broadcasted delay)
            if (activeDelay > 0 || !business?.delayAlertMessage.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentAmberLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .testTag("customer_delay_alert_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AccentAmber)
                        Column {
                            Text(
                                text = if (activeDelay > 0) "LIVE DELAY NOTICE (+${activeDelay} MINS)" else "LIVE NOTICE FROM STORE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AccentAmber
                            )
                            Text(
                                text = business?.delayAlertMessage?.ifBlank { "Wait times have been dynamically adjusted." }
                                    ?: "Wait times have been dynamically adjusted.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Real-Time Stats Grid (People Ahead & Estimated Wait Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "People Ahead",
                    value = if (booking.status == BookingStatus.WAITING) "$activeWaitingAhead Ahead" else "0",
                    subtitle = if (activeWaitingAhead == 0 && booking.status == BookingStatus.WAITING) "You're up next!" else "In line before you",
                    icon = Icons.Default.PeopleAlt,
                    accentColor = AccentAmber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("people_ahead_stat")
                )

                StatCard(
                    title = "Est. Wait Time",
                    value = if (booking.status == BookingStatus.SERVING) "Serving Now"
                    else if (booking.status == BookingStatus.WAITING) "~$estimatedWaitMinutes mins"
                    else "0 mins",
                    subtitle = "Updates dynamically",
                    icon = Icons.Default.Timer,
                    accentColor = PrimaryIndigo,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("est_wait_time_stat")
                )
            }

            // Currently Serving Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(StatusServingGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = StatusServingGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Currently Serving At Counter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (currentlyServingBooking != null) "Token #${currentlyServingBooking.tokenNumber} (${currentlyServingBooking.customerName})" else "No active customer serving",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Live Queue Progression Timeline
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Queue Progress Timeline",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    val isBooked = true
                    val isInQueue = booking.status == BookingStatus.WAITING || booking.status == BookingStatus.SERVING || booking.status == BookingStatus.COMPLETED
                    val isUpNext = (activeWaitingAhead <= 1 && booking.status == BookingStatus.WAITING) || booking.status == BookingStatus.SERVING || booking.status == BookingStatus.COMPLETED
                    val isServing = booking.status == BookingStatus.SERVING || booking.status == BookingStatus.COMPLETED
                    val isCompleted = booking.status == BookingStatus.COMPLETED

                    TimelineStep(title = "Token Issued", subtitle = "Confirmed for ${booking.customerName}", isDone = isBooked, isActive = false)
                    TimelineStep(title = "Waiting in Queue", subtitle = "Position #${booking.tokenSeq}", isDone = isInQueue && !isUpNext, isActive = booking.status == BookingStatus.WAITING && !isUpNext)
                    TimelineStep(title = "Up Next", subtitle = "Prepare to proceed", isDone = isUpNext && !isServing, isActive = isUpNext && booking.status == BookingStatus.WAITING)
                    TimelineStep(title = "Serving in Progress", subtitle = "Your appointment is live", isDone = isCompleted, isActive = booking.status == BookingStatus.SERVING)
                    TimelineStep(title = "Completed", subtitle = "Service finalized", isDone = isCompleted, isActive = false, isLast = true)
                }
            }

            // Booking Details Summary
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Appointment Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow(label = "Business", value = booking.businessName)
                    DetailRow(label = "Date & Slot", value = "${QueueRepository.formatDateDisplay(booking.appointmentDate)} • ${booking.appointmentSlotTime}")
                    DetailRow(label = "Services / Visit", value = booking.bookedServicesJson.ifBlank { "General Consultation" })
                    DetailRow(label = "Total Amount", value = "$${booking.totalPrice.toInt()}")
                    if (business != null) {
                        DetailRow(label = "Location", value = business.address)
                    }
                }
            }

            // Action Buttons (Directions, Call, Cancel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(business?.address ?: booking.businessName)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                        try { context.startActivity(mapIntent) } catch (_: Exception) {}
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Directions")
                }

                OutlinedButton(
                    onClick = {
                        val phone = business?.ownerPhone ?: "+15550000"
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        try { context.startActivity(callIntent) } catch (_: Exception) {}
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Store")
                }
            }

            if (booking.status == BookingStatus.WAITING) {
                TextButton(
                    onClick = { showCancelConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusCancelledRed),
                    modifier = Modifier.testTag("cancel_booking_btn")
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel this Token", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Digital QR Pass Modal
        if (showQrDialog) {
            AlertDialog(
                onDismissRequest = { showQrDialog = false },
                title = {
                    Text(
                        text = "Digital Queue Pass",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Scan at reception counter to verify your token",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        QrCodeView(
                            data = "queuebook://ticket/${booking.id}?token=${booking.tokenNumber}",
                            sizeDp = 180.dp,
                            qrColor = PrimaryIndigoDark
                        )

                        Surface(
                            color = PrimaryIndigoLight,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "TOKEN #${booking.tokenNumber} • ${booking.customerName}",
                                color = PrimaryIndigoDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showQrDialog = false },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Pass")
                    }
                }
            )
        }

        // Cancel Confirmation Dialog
        if (showCancelConfirm) {
            AlertDialog(
                onDismissRequest = { showCancelConfirm = false },
                title = { Text("Cancel Token #${booking.tokenNumber}?") },
                text = { Text("Are you sure you want to release your queue position? You will need to take a new token to rejoin.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showCancelConfirm = false
                            onCancelBooking(booking.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusCancelledRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Yes, Cancel Token")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelConfirm = false }) {
                        Text("Keep Token")
                    }
                }
            )
        }
    }
}

@Composable
private fun TimelineStep(
    title: String,
    subtitle: String,
    isDone: Boolean,
    isActive: Boolean,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDone) StatusCompletedBlue
                        else if (isActive) PrimaryIndigo
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else if (isActive) {
                    LivePulseDot(color = Color.White, sizeDp = 6)
                } else {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(if (isDone) StatusCompletedBlue else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
