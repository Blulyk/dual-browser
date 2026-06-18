package com.blulyk.dualbrowser.platform

import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsCollectorTest {
    @Test
    fun reportContainsWebViewAndBothDisplays() {
        val report = DiagnosticsCollector.format(
            appVersion = "0.1.0-beta.1",
            webViewVersion = "136.0",
            displays = listOf(
                DisplaySnapshot(0, 1920, 1080, densityDpi = 480, refreshRate = 60f),
                DisplaySnapshot(2, 1240, 1080, densityDpi = 420, refreshRate = 60f),
            ),
        )

        assertTrue(report.contains("WebView: 136.0"))
        assertTrue(report.contains("Display 0: 1920x1080"))
        assertTrue(report.contains("Display 2: 1240x1080"))
    }
}

