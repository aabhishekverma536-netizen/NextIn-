package com.example.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.LogoVariant
import com.example.ui.common.NextInBrandLogo
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            )
        )
        alpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 600,
                easing = LinearEasing
            )
        )
        delay(900)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryIndigoDark,
                        PrimaryIndigo,
                        PrimaryIndigoDarker
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(24.dp)
        ) {
            NextInBrandLogo(
                variant = LogoVariant.HERO,
                isDarkBg = true,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "REAL-TIME QUEUE & TOKEN ENGINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = SilverLight.copy(alpha = 0.9f)
            )
        }
    }
}
