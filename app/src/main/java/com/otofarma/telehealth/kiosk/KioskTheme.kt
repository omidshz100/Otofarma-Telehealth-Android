package com.otofarma.telehealth.kiosk

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val KioskInk = Color(0xFF13241C)
internal val KioskMuted = Color(0xFF5E7068)
internal val KioskMist = Color(0xFFF3F7F4)
internal val KioskGreen = Color(0xFF0E7A52)
internal val KioskGreenDeep = Color(0xFF084734)
internal val KioskGreenSoft = Color(0xFFE7F5EE)
internal val KioskYellow = Color(0xFFF0C400)
internal val KioskWhite = Color(0xFFFFFFFF)

private val KioskColors = lightColorScheme(
    primary = KioskGreen,
    onPrimary = KioskWhite,
    primaryContainer = KioskGreenSoft,
    onPrimaryContainer = KioskGreenDeep,
    background = KioskMist,
    onBackground = KioskInk,
    surface = KioskWhite,
    onSurface = KioskInk,
)

@Composable
internal fun KioskTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = KioskColors, content = content)
}
