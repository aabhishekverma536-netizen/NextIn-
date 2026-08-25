package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigoVibrant,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoContainer,
    onPrimaryContainer = SilverBright,
    secondary = SilverMetallic,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = SilverLight,
    tertiary = SecondaryTealNight,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceSubtleDark,
    outline = SilverDark.copy(alpha = 0.5f),
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoLight,
    onPrimaryContainer = PrimaryIndigoDark,
    secondary = PrimaryIndigoVibrant,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = PrimaryIndigoDark,
    tertiary = SecondaryTeal,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceSubtleLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve our signature Luxury Indigo Blue & Metallic Silver aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
