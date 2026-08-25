package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessCategory
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.ui.common.LogoVariant
import com.example.ui.common.NextInBrandLogo
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (UserSession) -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var authMethod by remember { mutableStateOf("PHONE") } // "PHONE" or "EMAIL"
    
    var phoneInput by remember { mutableStateOf("+1 555-0199") }
    var emailInput by remember { mutableStateOf("customer@example.com") }
    var nameInput by remember { mutableStateOf("Sarah Jenkins") }
    
    var showOtpSheet by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("849201") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Logo Hero - Metallic Gold & Wine Emblem
            NextInBrandLogo(
                variant = LogoVariant.HERO,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Live queue tracking & appointments for salons and doctor clinics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Role Selector Switcher
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    RoleTab(
                        title = "Customer",
                        subtitle = "Book tokens & slots",
                        icon = Icons.Default.Person,
                        isSelected = selectedRole == UserRole.CUSTOMER,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedRole = UserRole.CUSTOMER
                            nameInput = "Sarah Jenkins"
                            emailInput = "sarah.j@example.com"
                            phoneInput = "+1 555-0199"
                        }
                    )

                    RoleTab(
                        title = "Business Owner",
                        subtitle = "Manage live queue",
                        icon = Icons.Default.Storefront,
                        isSelected = selectedRole == UserRole.BUSINESS_OWNER,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedRole = UserRole.BUSINESS_OWNER
                            nameInput = "Alex Rivera (Owner)"
                            emailInput = "alex.salon@queuebook.app"
                            phoneInput = "+1 555-2345"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Authentication Input Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedRole == UserRole.CUSTOMER) "Customer Login" else "Partner Portal Login",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Toggle Phone vs Email
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = authMethod == "PHONE",
                                onClick = { authMethod = "PHONE" },
                                label = { Text("Mobile OTP", fontSize = 11.sp) },
                                shape = RoundedCornerShape(50),
                                leadingIcon = {
                                    Icon(Icons.Default.PhoneIphone, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            )
                            FilterChip(
                                selected = authMethod == "EMAIL",
                                onClick = { authMethod = "EMAIL" },
                                label = { Text("Email", fontSize = 11.sp) },
                                shape = RoundedCornerShape(50),
                                leadingIcon = {
                                    Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input")
                    )

                    if (authMethod == "PHONE") {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Mobile Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            placeholder = { Text("+1 (555) 000-0000") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )
                    } else {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            placeholder = { Text("name@example.com") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input")
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            if (nameInput.isBlank()) {
                                errorMessage = "Please enter your name"
                                return@Button
                            }
                            errorMessage = null
                            showOtpSheet = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_btn"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRole == UserRole.CUSTOMER) PrimaryPurple else SecondaryTeal
                        )
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (authMethod == "PHONE") "Send 6-Digit OTP" else "Continue with Email",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Demo Presets
            Text(
                text = "⚡ Instant Demo Profiles",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DemoLoginButton(
                    title = "Luxe & Glow Salon (Owner)",
                    subtitle = "Manage live haircut & spa queue",
                    icon = Icons.Default.ContentCut,
                    accentColor = PrimaryPurple,
                    onClick = {
                        onLoginSuccess(
                            UserSession(
                                role = UserRole.BUSINESS_OWNER,
                                userId = "owner_salon_1",
                                name = "Alex Rivera",
                                phone = "+1 (555) 234-5678",
                                email = "alex.salon@queuebook.app",
                                businessId = "biz_salon_luxe"
                            )
                        )
                    }
                )

                DemoLoginButton(
                    title = "Apex Health Clinic (Owner)",
                    subtitle = "Manage doctor consultation queue & slots",
                    icon = Icons.Default.MedicalServices,
                    accentColor = SecondaryTeal,
                    onClick = {
                        onLoginSuccess(
                            UserSession(
                                role = UserRole.BUSINESS_OWNER,
                                userId = "owner_clinic_1",
                                name = "Dr. Anita Sharma",
                                phone = "+1 (555) 876-5432",
                                email = "dr.anita@queuebook.app",
                                businessId = "biz_clinic_apex"
                            )
                        )
                    }
                )

                DemoLoginButton(
                    title = "Customer Profile (Sarah)",
                    subtitle = "Book slots & track live queue position",
                    icon = Icons.Default.Person,
                    accentColor = AccentAmber,
                    onClick = {
                        onLoginSuccess(
                            UserSession(
                                role = UserRole.CUSTOMER,
                                userId = "cust_sarah_1",
                                name = "Sarah Jenkins",
                                phone = "+1 555-0199",
                                email = "sarah.j@example.com"
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy Policy & Terms Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "By continuing, you agree to our ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onOpenPrivacyPolicy,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.testTag("auth_privacy_policy_btn")
                ) {
                    Text(
                        text = "Privacy Policy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigoVibrant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // OTP Bottom Sheet Modal
        if (showOtpSheet) {
            ModalBottomSheet(
                onDismissRequest = { showOtpSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Verify 6-Digit Code",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "We sent a simulated one-time passcode to ${if (authMethod == "PHONE") phoneInput else emailInput}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6) otpInput = it },
                        label = { Text("Enter 6-Digit OTP") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input")
                    )

                    Button(
                        onClick = {
                            showOtpSheet = false
                            val assignedBizId = if (selectedRole == UserRole.BUSINESS_OWNER) "biz_salon_luxe" else null
                            onLoginSuccess(
                                UserSession(
                                    role = selectedRole,
                                    userId = "usr_${System.currentTimeMillis() % 10000}",
                                    name = nameInput.ifBlank { "User" },
                                    phone = phoneInput,
                                    email = emailInput,
                                    businessId = assignedBizId
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_otp_btn"),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Verify & Enter", fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = { otpInput = "849201" }) {
                        Text("Auto-Fill Demo Code (849201)", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RoleTab(
    title: String,
    subtitle: String,
    icon: ImageVector,
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
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun DemoLoginButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
