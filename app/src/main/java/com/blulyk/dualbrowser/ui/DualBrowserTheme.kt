package com.blulyk.dualbrowser.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object DualBrowserColors {
    val primary = Color(0xFF163A63)
    val primaryBright = Color(0xFF2F6EA8)
    val background = Color(0xFF0B111A)
    val surface = Color(0xFF111B27)
    val surfaceElevated = Color(0xFF192636)
    val outline = Color(0xFF30445C)
    val text = Color(0xFFEAF2FA)
    val textMuted = Color(0xFF9FB1C5)
}

private val DualBrowserColorScheme = darkColorScheme(
    primary = DualBrowserColors.primaryBright,
    onPrimary = Color.White,
    primaryContainer = DualBrowserColors.primary,
    onPrimaryContainer = DualBrowserColors.text,
    background = DualBrowserColors.background,
    onBackground = DualBrowserColors.text,
    surface = DualBrowserColors.surface,
    onSurface = DualBrowserColors.text,
    surfaceContainer = DualBrowserColors.surface,
    surfaceContainerHigh = DualBrowserColors.surfaceElevated,
    outline = DualBrowserColors.outline,
)

@Composable
fun DualBrowserTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DualBrowserColorScheme, content = content)
}
