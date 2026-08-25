package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingStatus
import com.example.data.model.BusinessCategory
import com.example.data.model.BusinessEntity
import com.example.data.model.QueueBookingEntity
import com.example.ui.common.CategoryChip
import com.example.ui.common.LivePulseDot
import com.example.ui.common.LogoVariant
import com.example.ui.common.NextInBrandLogo
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerExploreScreen(
    businesses: List<BusinessEntity>,
    allBookings: List<QueueBookingEntity>,
    activeCustomerBooking: QueueBookingEntity?,
    onSelectBusiness: (BusinessEntity) -> Unit,
    onViewLiveToken: (QueueBookingEntity) -> Unit,
    onSwitchToOwner: () -> Unit,
    onLogout: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<BusinessCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredBusinesses = remember(businesses, selectedCategoryFilter, searchQuery) {
        businesses.filter { biz ->
            val matchCategory = selectedCategoryFilter == null || biz.category == selectedCategoryFilter
            val matchQuery = searchQuery.isBlank() || biz.name.contains(searchQuery, ignoreCase = true) || biz.address.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    NextInBrandLogo(variant = LogoVariant.APP_BAR)
                },
                actions = {
                    IconButton(
                        onClick = onOpenPrivacyPolicy,
                        modifier = Modifier.testTag("privacy_policy_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Privacy Policy",
                            tint = PrimaryIndigoVibrant
                        )
                    }
                    IconButton(
                        onClick = onSwitchToOwner,
                        modifier = Modifier.testTag("switch_to_owner_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Switch to Owner View",
                            tint = PrimaryIndigoVibrant
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            // Active Customer Token Alert Card (if any active waiting/serving)
            if (activeCustomerBooking != null) {
                item {
                    ActiveTokenBanner(
                        booking = activeCustomerBooking,
                        onClick = { onViewLiveToken(activeCustomerBooking) }
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search salons, clinics, doctors...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_business_input")
                )
            }

            // Category Filter Pills
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All Places (${businesses.size})") },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == BusinessCategory.SALON,
                        onClick = { selectedCategoryFilter = BusinessCategory.SALON },
                        label = { Text("✂️ Salons") },
                        shape = RoundedCornerShape(50)
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == BusinessCategory.DOCTOR_CLINIC,
                        onClick = { selectedCategoryFilter = BusinessCategory.DOCTOR_CLINIC },
                        label = { Text("🩺 Doctor Clinics") },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // Business Cards List
            if (filteredBusinesses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No places found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search filter",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredBusinesses, key = { it.id }) { business ->
                    val bizBookings = allBookings.filter { it.businessId == business.id }
                    val waitingCount = bizBookings.count { it.status == BookingStatus.WAITING }
                    val servingBooking = bizBookings.firstOrNull { it.status == BookingStatus.SERVING }
                    val estWait = waitingCount * business.averageSlotDurationMinutes

                    BusinessCard(
                        business = business,
                        waitingCount = waitingCount,
                        currentlyServingToken = servingBooking?.tokenNumber,
                        estWaitMinutes = estWait,
                        onClick = { onSelectBusiness(business) }
                    )
                }
            }

            // Privacy Policy & Trust Footer
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onOpenPrivacyPolicy,
                        modifier = Modifier.testTag("explore_privacy_policy_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NextIn Privacy Policy & Data Protection",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveTokenBanner(
    booking: QueueBookingEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryPurpleDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_token_banner")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = booking.tokenNumber,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Live Token: #${booking.tokenNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (booking.status == BookingStatus.SERVING) {
                        LivePulseDot(color = StatusServingGreen, sizeDp = 8)
                    }
                }
                Text(
                    text = "${booking.businessName} • Status: ${booking.status.name}",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryPurpleDark),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Track Live", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BusinessCard(
    business: BusinessEntity,
    waitingCount: Int,
    currentlyServingToken: String?,
    estWaitMinutes: Int,
    onClick: () -> Unit
) {
    val isSalon = business.category == BusinessCategory.SALON

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .testTag("business_card_${business.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChip(category = business.category)
                    if (business.activeDelayMinutes > 0) {
                        Surface(
                            color = AccentAmberLight,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(0.5.dp, AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "+${business.activeDelayMinutes}m delay",
                                color = AccentAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${business.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "(${business.totalReviews})",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = business.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = business.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Queue Status Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LivePulseDot(color = StatusServingGreen, sizeDp = 7)
                            Text(
                                text = "Queue: $waitingCount waiting",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (currentlyServingToken != null) {
                            Text(
                                text = "Serving: #$currentlyServingToken",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (waitingCount > 0) "~$estWaitMinutes mins wait" else "No wait time",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (waitingCount > 0) AccentAmber else StatusServingGreen
                        )
                        Text(
                            text = if (isSalon) "Walk-in Queue" else "Slot Booking",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSalon) "Open: ${business.openTime} - ${business.closeTime}" else "Fee: $${business.consultationFee.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSalon) PrimaryPurple else SecondaryTeal
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isSalon) "Book Token" else "Select Slot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
