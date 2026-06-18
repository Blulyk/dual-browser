package com.blulyk.dualbrowser.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class DualBrowserThemeTest {
    @Test
    fun darkPaletteUsesNavyPrimary() {
        assertEquals(Color(0xFF163A63), DualBrowserColors.primary)
        assertEquals(Color(0xFF0B111A), DualBrowserColors.background)
    }
}
