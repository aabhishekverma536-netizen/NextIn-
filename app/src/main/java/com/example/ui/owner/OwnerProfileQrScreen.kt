package com.example.ui.owner

import android.content.Intent
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
import com.example.data.model.BusinessCategory
import com.example.data.model.BusinessEntity
import com.example.ui.common.CategoryChip
import com.example.ui.theme.*
import com.example.util.QrCodeView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProfileQrScreen(
    business: BusinessEntity,
    onUpdateProfile: (BusinessEntity) -> Unit,
    onBack: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var firmName by remember { mutableStateOf(business.name) }
    var ownerName by remember { mutableStateOf(business.ownerName) }
    var phone by remember { mutableStateOf(business.ownerPhone) }
    var address by remember { mutableStateOf(business.address) }
    var openTime by remember { mutableStateOf(business.openTime) }
    var closeTime by remember { mutableStateOf(business.closeTime) }
    var selectedCategory by remember { mutableStateOf(business.category) }
    var showSavedMessage by remember { mutableStateOf(false) }

    val qrLink = "https://nextin.app/download?firmId=${business.id}"
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Business Profile & Store QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "In-App QR Generator & Store Settings",
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
            // QR Code Store Poster Badge Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .testTag("store_qr_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryPurple, SecondaryTeal)
                                )
                            )
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCAN TO JOIN LIVE QUEUE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Download App & Track Wait Times",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // QR Code View
                    QrCodeView(
                        data = qrLink,
                        sizeDp = 190.dp,
                        qrColor = PrimaryPurpleDark
                    )

                    Text(
                        text = business.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    CategoryChip(category = business.category)

                    Text(
                        text = "Store ID: ${business.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Book appointments & join the live queue at ${business.name}! Click to open: $qrLink"
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Business QR Link")
                                context.startActivity(shareIntent)
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_qr_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share QR Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Edit Profile Form
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Firm / Business Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = firmName,
                        onValueChange = { firmName = it },
                        label = { Text("Firm / Business Name") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("owner_firm_name_input")
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner / Lead Specialist Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Physical Store / Clinic Address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = openTime,
                            onValueChange = { openTime = it },
                            label = { Text("Open Time") },
                            placeholder = { Text("09:00 AM") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = closeTime,
                            onValueChange = { closeTime = it },
                            label = { Text("Close Time") },
                            placeholder = { Text("08:30 PM") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Business Category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == BusinessCategory.SALON,
                            onClick = { selectedCategory = BusinessCategory.SALON },
                            label = { Text("✂️ Salon & Spa") },
                            shape = RoundedCornerShape(50)
                        )
                        FilterChip(
                            selected = selectedCategory == BusinessCategory.DOCTOR_CLINIC,
                            onClick = { selectedCategory = BusinessCategory.DOCTOR_CLINIC },
                            label = { Text("🩺 Doctor Clinic") },
                            shape = RoundedCornerShape(50)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            onUpdateProfile(
                                business.copy(
                                    name = firmName.ifBlank { business.name },
                                    ownerName = ownerName.ifBlank { business.ownerName },
                                    ownerPhone = phone.ifBlank { business.ownerPhone },
                                    address = address.ifBlank { business.address },
                                    openTime = openTime.ifBlank { business.openTime },
                                    closeTime = closeTime.ifBlank { business.closeTime },
                                    category = selectedCategory
                                )
                            )
                            showSavedMessage = true
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_business_profile_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }

                    if (showSavedMessage) {
                        Text(
                            text = "✓ Business profile updated successfully!",
                            color = StatusServingGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Privacy Policy Link
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryIndigoVibrant)
                        Column {
                            Text("Data & Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Standard data privacy and queue governance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    TextButton(
                        onClick = onOpenPrivacyPolicy,
                        modifier = Modifier.testTag("owner_privacy_policy_btn")
                    ) {
                        Text("View", fontWeight = FontWeight.Bold, color = PrimaryIndigoVibrant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
