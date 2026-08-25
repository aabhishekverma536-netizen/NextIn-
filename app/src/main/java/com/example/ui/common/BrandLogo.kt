package com.example.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class LogoVariant {
    HERO,
    APP_BAR,
    ICON_ONLY
}

@Composable
fun NextInBrandLogo(
    modifier: Modifier = Modifier,
    variant: LogoVariant = LogoVariant.HERO,
    isDarkBg: Boolean = false
) {
    when (variant) {
        LogoVariant.HERO -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Luxury Indigo Blue & Metallic Silver Crest Badge
                NextInEmblemBadge(size = 96.dp)

                Spacer(modifier = Modifier.height(14.dp))

                // Bold NextIn Wordmark in Indigo Blue & Silver
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Next",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = if (isDarkBg) Color.White else PrimaryIndigo
                    )
                    Text(
                        text = "In",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = PrimaryIndigoVibrant
                    )
                }

                // Tagline badge with silver shimmer border
                Surface(
                    color = if (isDarkBg) PrimaryIndigoContainer else PrimaryIndigoLight,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .border(1.dp, SilverBorder, RoundedCornerShape(50))
                ) {
                    Text(
                        text = "REAL-TIME QUEUE & SLOT SYSTEM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = if (isDarkBg) SilverLight else PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }
        }
        LogoVariant.APP_BAR -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NextInEmblemBadge(size = 36.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryIndigo,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "In",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryIndigoVibrant,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        LogoVariant.ICON_ONLY -> {
            NextInEmblemBadge(size = 48.dp, modifier = modifier)
        }
    }
}

@Composable
fun NextInEmblemBadge(
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "silver_glow")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape((size.value * 0.32f).dp),
                spotColor = PrimaryIndigo.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape((size.value * 0.32f).dp))
            .background(IndigoSilverGradientBrush)
            .border(
                width = (size.value * 0.035f).coerceAtLeast(1.5f).dp,
                brush = SilverGradientBrush,
                shape = RoundedCornerShape((size.value * 0.32f).dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding((size.value * 0.16f).dp)) {
            val w = this.size.width
            val h = this.size.height

            // Stylized Modern "N" / Fast-Forward Double Chevron in Metallic Silver/White
            val path = Path().apply {
                // Left chevron stem
                moveTo(w * 0.18f, h * 0.82f)
                lineTo(w * 0.18f, h * 0.18f)
                lineTo(w * 0.40f, h * 0.18f)
                lineTo(w * 0.62f, h * 0.62f)
                lineTo(w * 0.62f, h * 0.18f)
                lineTo(w * 0.82f, h * 0.18f)
                lineTo(w * 0.82f, h * 0.82f)
                lineTo(w * 0.60f, h * 0.82f)
                lineTo(w * 0.38f, h * 0.38f)
                lineTo(w * 0.38f, h * 0.82f)
                close()
            }

            // Draw Silver gradient path
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(SilverBright, SilverMetallic, SilverDark)
                )
            )

            // Inner fast-forward queue accent dot / arrow highlight
            drawCircle(
                color = Color.White.copy(alpha = shimmerAlpha * 0.95f),
                radius = w * 0.065f,
                center = Offset(w * 0.82f, h * 0.18f)
            )
        }
    }
}
