package com.example.ui.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.LogoVariant
import com.example.ui.common.NextInBrandLogo
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("privacy_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryIndigoContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryIndigoVibrant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Privacy Policy & Data Rights",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "NextIn Live Queue & Appointment Platform",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverLight.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Text(
                            text = "NextIn is committed to upholding the highest standards of data security, user transparency, and privacy protection for all customers and business owners.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SilverMetallic
                        )

                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(1.dp, SilverDark.copy(alpha = 0.4f), RoundedCornerShape(50))
                        ) {
                            Text(
                                text = "Last Updated: August 2026 • Policy Version 2.4",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SilverLight,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Section 1: Information We Collect
            item {
                PolicySectionCard(
                    icon = Icons.Default.PersonSearch,
                    title = "1. Information We Collect",
                    accentColor = PrimaryIndigoVibrant
                ) {
                    PolicyBulletItem(
                        title = "Queue & Token Booking",
                        description = "When you join a queue or book an appointment slot, we collect your name, phone number, and optional email to identify your token in real-time."
                    )
                    PolicyBulletItem(
                        title = "Business Profiles",
                        description = "For Salons and Doctor Clinics, we store business operational hours, consultation rates, service catalogs, and staff profiles."
                    )
                    PolicyBulletItem(
                        title = "Device Telemetry & Permissions",
                        description = "We request notification and vibration permissions solely to dispatch high-priority 5-minute turn alerts. No background tracking or continuous location surveillance is performed."
                    )
                }
            }

            // Section 2: How We Use Your Data
            item {
                PolicySectionCard(
                    icon = Icons.Default.Tune,
                    title = "2. How We Use Queue Data",
                    accentColor = SecondaryTeal
                ) {
                    PolicyBulletItem(
                        title = "Dynamic Wait-Time Calculations",
                        description = "Active token positions and service durations are processed to deliver accurate live wait times and count of people ahead."
                    )
                    PolicyBulletItem(
                        title = "5-Minute Turn Alerts",
                        description = "When your position approaches the counter (<= 5 minutes or <= 2 people ahead), local device notifications and haptic alerts ensure you never miss your turn."
                    )
                    PolicyBulletItem(
                        title = "Instant Digital QR Passes",
                        description = "Encrypted digital QR tickets facilitate zero-contact check-in at physical salon counters and doctor clinic receptions."
                    )
                }
            }

            // Section 3: Data Security & Zero Third-Party Selling
            item {
                PolicySectionCard(
                    icon = Icons.Default.Lock,
                    title = "3. Security & Zero Third-Party Selling",
                    accentColor = StatusServingGreen
                ) {
                    PolicyBulletItem(
                        title = "Local-First Encrypted Storage",
                        description = "All active queue tokens and customer profiles are stored securely in local database structures with end-to-end data integrity."
                    )
                    PolicyBulletItem(
                        title = "No Commercial Ad Brokers",
                        description = "We NEVER sell, monetize, rent, or trade your personal booking details or medical/salon notes to advertisers or third-party data brokers."
                    )
                }
            }

            // Section 4: User Rights & Data Control
            item {
                PolicySectionCard(
                    icon = Icons.Default.ManageAccounts,
                    title = "4. Your Rights & Data Controls",
                    accentColor = PrimaryIndigoSky
                ) {
                    PolicyBulletItem(
                        title = "Token Cancellation & Modification",
                        description = "You can cancel or reschedule any active queue token at any time directly with a single tap in the app."
                    )
                    PolicyBulletItem(
                        title = "Notification Preferences",
                        description = "You can reconfigure or disable turn alert notifications and haptic feedback in your system device settings."
                    )
                    PolicyBulletItem(
                        title = "Complete Account Erasure",
                        description = "You may wipe local token histories and sign out immediately from the app settings menu."
                    )
                }
            }

            // Section 5: Contact & Privacy Officer
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.ContactSupport,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                            Text(
                                text = "5. Contact & Privacy Inquiries",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "If you have questions regarding this Privacy Policy or your data rights, please reach out to our dedicated privacy desk:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "📧 Email: privacy@nextin.app\n🌐 Website: https://nextin.app/privacy\n📍 Support: support@nextin.app",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryIndigo
                        )
                    }
                }
            }

            // Bottom Return Button
            item {
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("privacy_accept_return_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Understood & Return",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicySectionCard(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }
    }
}

@Composable
private fun PolicyBulletItem(
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "•",
            fontWeight = FontWeight.Bold,
            color = PrimaryIndigo,
            fontSize = 16.sp
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
