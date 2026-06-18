package com.blulyk.dualbrowser.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.blulyk.dualbrowser.domain.BrowserState
import com.blulyk.dualbrowser.domain.BrowserTab
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BrowserAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun singleDisplayShowsWebSurfaceAndControls() {
        val tab = BrowserTab(id = "tab-1", url = "about:blank")
        val state = BrowserState(tabs = listOf(tab), focusedTabId = tab.id)

        composeRule.setContent {
            BrowserApp(
                state = state,
                onCommand = {},
                webContent = { Box(Modifier.fillMaxSize().testTag("fake-web")) },
            )
        }

        composeRule.onNodeWithTag("fake-web").assertIsDisplayed()
        composeRule.onNodeWithTag("control-center").assertIsDisplayed()
    }
}
