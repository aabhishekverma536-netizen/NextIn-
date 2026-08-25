package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Luxury Indigo Blue & Metallic Silver/White Palette
val PrimaryIndigo = Color(0xFF1E3A8A) // Rich Royal Indigo Blue
val PrimaryIndigoDark = Color(0xFF0F172A) // Deep Navy Midnight Slate
val PrimaryIndigoDarker = Color(0xFF020617) // Obsidian Midnight Navy
val PrimaryIndigoLight = Color(0xFFEFF6FF) // Soft Ice Blue Mist
val PrimaryIndigoContainer = Color(0xFF1E293B) // Rich Slate Indigo Container
val PrimaryIndigoVibrant = Color(0xFF2563EB) // Electric Indigo Blue
val PrimaryIndigoSky = Color(0xFF3B82F6) // Clear Sapphire Blue

// Metallic Silver & Pure White Highlights
val SilverMetallic = Color(0xFFE2E8F0) // Clean Metallic Silver
val SilverBright = Color(0xFFFFFFFF) // Pure Radiant White
val SilverLight = Color(0xFFF8FAFC) // Platinum Silver Shimmer
val SilverDark = Color(0xFF94A3B8) // Polished Steel Slate
val SilverSlate = Color(0xFF64748B) // Refined Slate Accent
val SilverBadgeBg = Color(0xFF0F172A) // Deep Indigo Slate Container
val SilverBorder = Color(0xFFCBD5E1) // Crisp Silver Border

// Gradient Brushes - Indigo & Silver Luxury Aesthetics
val SilverGradientBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFE2E8F0),
        Color(0xFFCBD5E1),
        Color(0xFFFFFFFF)
    )
)

val SilverVerticalGradientBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFE2E8F0),
        Color(0xFF94A3B8)
    )
)

val IndigoSilverGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E3A8A),
        Color(0xFF1E293B),
        Color(0xFF0F172A)
    )
)

val IndigoSkyGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2563EB),
        Color(0xFF1E3A8A),
        Color(0xFF0F172A)
    )
)

// Legacy Aliases mapped to Luxury Indigo Blue & Silver for consistency across existing composables
val PrimaryWine = PrimaryIndigo
val PrimaryWineDark = PrimaryIndigoDark
val PrimaryWineDarker = PrimaryIndigoDarker
val PrimaryWineLight = PrimaryIndigoLight
val PrimaryWineContainer = PrimaryIndigoContainer

val GoldMetallic = SilverMetallic
val GoldBright = SilverBright
val GoldLight = SilverLight
val GoldDark = SilverDark
val GoldBadgeBg = SilverBadgeBg
val GoldBorder = SilverBorder

val GoldGradientBrush = SilverGradientBrush
val GoldVerticalGradientBrush = SilverVerticalGradientBrush
val WineGoldGradientBrush = IndigoSilverGradientBrush

val PrimaryPurple = PrimaryIndigo
val PrimaryPurpleDark = PrimaryIndigoDark
val PrimaryPurpleLight = PrimaryIndigoLight
val PrimaryPurpleContainer = PrimaryIndigoContainer
val PrimaryPurpleBadge = Color(0xFFDBEAFE)

val AccentAmber = Color(0xFF38BDF8) // Crisp Sky Cyan Accent
val AccentAmberLight = Color(0xFFE0F2FE)

// Secondary & Neutral Colors
val SecondarySlate = Color(0xFF475569)
val SecondarySlateLight = Color(0xFFF1F5F9)
val SecondaryTeal = Color(0xFF0284C7)
val SecondaryTealLight = Color(0xFFE0F2FE)

// Alert & Notice Colors
val AlertCoralLight = Color(0xFFFFDAD6)
val AlertCoral = Color(0xFFBA1A1A)
val AlertCoralDark = Color(0xFF410002)

// Status Colors
val StatusServingGreen = Color(0xFF10B981)
val StatusServingGreenLight = Color(0xFFD1FAE5)
val StatusWaitingAmber = Color(0xFF0284C7)
val StatusWaitingAmberLight = Color(0xFFE0F2FE)
val StatusCompletedBlue = Color(0xFF2563EB)
val StatusCompletedBlueLight = Color(0xFFDBEAFE)
val StatusCancelledRed = Color(0xFFEF4444)
val StatusCancelledRedLight = Color(0xFFFEE2E2)

// Canvas & Surfaces - Light Mode (Clean Slate White & Deep Indigo/Navy Text)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceLight = Color(0xFF0F172A)
val OnSurfaceSubtleLight = Color(0xFF475569)
val OutlineLight = Color(0xFFCBD5E1)
val OutlineVariantLight = Color(0xFFE2E8F0)

// Dark Theme Palette - Deep Obsidian Navy & Glowing Metallic Silver/White
val BackgroundDark = Color(0xFF020617)
val SurfaceDark = Color(0xFF0F172A)
val SurfaceVariantDark = Color(0xFF1E293B)
val OnSurfaceDark = Color(0xFFF8FAFC)
val OnSurfaceSubtleDark = Color(0xFF94A3B8)
val PrimaryPurpleNight = Color(0xFF93C5FD)
val SecondarySlateNight = Color(0xFFCBD5E1)
val PrimaryIndigoNight = Color(0xFF93C5FD)
val SecondaryTealNight = Color(0xFF38BDF8)
