package com.otofarma.telehealth.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF0F4C81)
val Ink = Color(0xFF101828)
val Muted = Color(0xFF475467)
val Line = Color(0xFFDEE3EB)
val Canvas = Color(0xFFF8FAFC)
val InstalledBg = Color(0xFFE5FAF2)
val InstalledFg = Color(0xFF16A34A)
val MissingBg = Color(0xFFF0F2F7)

private val Colors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7F0F8),
    onPrimaryContainer = Navy,
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    outline = Line,
)

@Composable
fun TelehealthTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
