package com.blulyk.dualbrowser.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserState
import com.blulyk.dualbrowser.domain.BrowserTab
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TabPreviewCarouselTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun previewSelectsAndClosesTab() {
        val first = BrowserTab("one", "https://one.example", "One")
        val second = BrowserTab("two", "https://two.example", "Two")
        val state = BrowserState(listOf(first, second), first.id)
        val commands = mutableListOf<BrowserCommand>()
        composeRule.setContent {
            DualBrowserTheme {
                TabPreviewCarousel(state, emptyMap(), commands::add)
            }
        }

        composeRule.onNodeWithTag("tab-preview-list").performScrollToIndex(1)
        composeRule.onNodeWithTag("tab-preview-two").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("close-preview-two").performClick()

        assertEquals(
            listOf(BrowserCommand.Focus("two"), BrowserCommand.Close("two")),
            commands,
        )
    }
}
