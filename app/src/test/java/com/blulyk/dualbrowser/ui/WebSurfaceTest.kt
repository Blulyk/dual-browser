package com.blulyk.dualbrowser.ui

import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.blulyk.dualbrowser.domain.BrowserTab
import com.blulyk.dualbrowser.engine.WebViewBrowserEngine
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
}
