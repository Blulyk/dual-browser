package com.blulyk.dualbrowser.platform

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import androidx.webkit.WebViewCompat

class DiagnosticsCollector(
    private val context: Context,
) {
    fun collect(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val webView = WebViewCompat.getCurrentWebViewPackage(context)?.versionName ?: "unavailable"
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.displays.map { display ->
            val metrics = context.createDisplayContext(display).resources.displayMetrics
            DisplaySnapshot(
                id = display.displayId,
                width = display.mode.physicalWidth,
                height = display.mode.physicalHeight,
                densityDpi = metrics.densityDpi,
                refreshRate = display.refreshRate,
            )
        }
        return buildString {
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            append(format(packageInfo.versionName.orEmpty(), webView, displays))
        }
    }

    companion object {
        fun format(
            appVersion: String,
            webViewVersion: String,
            displays: List<DisplaySnapshot>,
        ): String = buildString {
            appendLine("Dual Browser: $appVersion")
            appendLine("WebView: $webViewVersion")
            displays.sortedBy(DisplaySnapshot::id).forEach { display ->
                appendLine(
                    "Display ${display.id}: ${display.width}x${display.height}, " +
                        "${display.densityDpi} dpi, ${display.refreshRate} Hz",
                )
            }
        }
    }
}

