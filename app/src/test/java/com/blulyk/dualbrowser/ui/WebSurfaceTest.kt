package com.blulyk.dualbrowser.ui

import android.webkit.WebView
import android.graphics.Bitmap
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.blulyk.dualbrowser.domain.BrowserTab
import com.blulyk.dualbrowser.engine.WebViewBrowserEngine
import com.blulyk.dualbrowser.engine.WebViewCallbacks
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WebSurfaceTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun changingTabCreatesANewWebEngine() {
        var tab by mutableStateOf(BrowserTab("tab-1", "https://one.example"))
        val createdForTabs = mutableListOf<String>()

        composeRule.setContent {
            WebSurface(
                tab = tab,
                engineActions = emptyFlow(),
                engineFactory = { context, _ ->
                    createdForTabs += tab.id
                    WebViewBrowserEngine(WebView(context))
                },
            )
        }

        tab = BrowserTab("tab-2", "https://two.example")
        composeRule.waitForIdle()

        assertEquals(listOf("tab-1", "tab-2"), createdForTabs)
    }

    @Test
    fun visibleCommitCapturesPreviewForActiveTab() {
        val tab = BrowserTab("tab-1", "https://one.example")
        lateinit var callbacks: WebViewCallbacks
        var captured: Pair<String, Bitmap>? = null

        composeRule.setContent {
            WebSurface(
                tab = tab,
                engineActions = emptyFlow(),
                engineFactory = { context, suppliedCallbacks ->
                    callbacks = suppliedCallbacks
                    WebViewBrowserEngine(WebView(context).apply { layout(0, 0, 480, 270) })
                },
                previewCapture = { Bitmap.createBitmap(480, 270, Bitmap.Config.ARGB_8888) },
                onPreviewCaptured = { tabId, bitmap -> captured = tabId to bitmap },
            )
        }

        callbacks.onPageCommitVisible(tab.url)
        shadowOf(Looper.getMainLooper()).idle()
        composeRule.waitForIdle()

        assertEquals(tab.id, captured?.first)
        assertEquals(480, captured?.second?.width)
    }
}
