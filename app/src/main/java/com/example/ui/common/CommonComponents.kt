package com.example.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingStatus
import com.example.data.model.BusinessCategory
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: BookingStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text, icon) = when (status) {
        BookingStatus.SERVING -> Quad(StatusServingGreenLight, StatusServingGreen, "Serving Now", Icons.Default.PlayArrow)
        BookingStatus.WAITING -> Quad(PrimaryIndigoLight, PrimaryIndigo, "In Queue", Icons.Default.HourglassTop)
        BookingStatus.COMPLETED -> Quad(StatusCompletedBlueLight, StatusCompletedBlue, "Completed", Icons.Default.CheckCircle)
        BookingStatus.NO_SHOW -> Quad(SurfaceVariantLight, OnSurfaceSubtleLight, "No Show", Icons.Default.PersonOff)
        BookingStatus.CANCELLED -> Quad(StatusCancelledRedLight, StatusCancelledRed, "Cancelled", Icons.Default.Cancel)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = modifier.border(
            width = if (status == BookingStatus.WAITING) 1.dp else 0.dp,
            color = if (status == BookingStatus.WAITING) PrimaryIndigo.copy(alpha = 0.3f) else Color.Transparent,
            shape = RoundedCornerShape(50)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (status == BookingStatus.SERVING) {
                LivePulseDot(color = textColor, sizeDp = 8)
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun LivePulseDot(
    color: Color = StatusServingGreen,
    sizeDp: Int = 10,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size((sizeDp * 2).dp)
    ) {
        Box(
            modifier = Modifier
                .size((sizeDp * 1.6).dp)
                .scale(scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha * 0.35f))
        )
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun CategoryChip(
    category: BusinessCategory,
    modifier: Modifier = Modifier
) {
    val isSalon = category == BusinessCategory.SALON
    val label = if (isSalon) "Salon & Spa" else "Doctor Clinic"
    val icon = if (isSalon) Icons.Default.ContentCut else Icons.Default.MedicalServices
    val containerColor = if (isSalon) PrimaryIndigoLight else SecondaryTealLight
    val contentColor = if (isSalon) PrimaryIndigo else SecondaryTeal

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(50),
        modifier = modifier.border(
            width = 0.8.dp,
            color = if (isSalon) PrimaryIndigo.copy(alpha = 0.25f) else SecondaryTeal.copy(alpha = 0.25f),
            shape = RoundedCornerShape(50)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MetricBadge(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String = "",
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FiveMinuteTurnAlertBanner(
    estimatedMinutes: Int,
    peopleAhead: Int,
    onSimulateAlert: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turn_alert_shimmer")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_shimmer"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryIndigoContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        PrimaryIndigoSky.copy(alpha = borderAlpha),
                        SilverMetallic,
                        PrimaryIndigoSky.copy(alpha = borderAlpha)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("five_minute_alert_banner")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigoVibrant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = "Urgent Alert",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "5-MINUTE TURN ALERT TRIGGERED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = SilverBright
                    )
                    Text(
                        text = "Your turn is coming up next! Please reach the counter. (Remaining: ~$estimatedMinutes mins)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📳 Device Vibration Active • $peopleAhead waiting in front",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SilverLight.copy(alpha = 0.9f)
                )

                TextButton(
                    onClick = onSimulateAlert,
                    colors = ButtonDefaults.textButtonColors(contentColor = SilverBright),
                    modifier = Modifier.testTag("test_vibrate_btn")
                ) {
                    Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-test Vibrate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
