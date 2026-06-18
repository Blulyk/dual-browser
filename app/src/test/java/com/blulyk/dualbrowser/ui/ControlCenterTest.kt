package com.blulyk.dualbrowser.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
class ControlCenterTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val tab = BrowserTab(id = "tab-1", url = "about:blank")
    private val state = BrowserState(tabs = listOf(tab), focusedTabId = tab.id)

    @Test
    fun submittingAddressNavigatesFocusedTab() {
        val commands = mutableListOf<BrowserCommand>()
        composeRule.setContent { ControlCenter(state = state, onCommand = commands::add) }

        composeRule.onNodeWithTag("address").performTextInput("example.com")
        composeRule.onNodeWithTag("address").performImeAction()

        assertEquals(BrowserCommand.Navigate(tab.id, "example.com"), commands.single())
    }

    @Test
    fun goButtonNavigatesFromGoogleNewTab() {
        val googleTab = BrowserTab(id = "tab-google", url = "https://www.google.com/")
        val googleState = BrowserState(listOf(googleTab), googleTab.id)
        val commands = mutableListOf<BrowserCommand>()
        composeRule.setContent { ControlCenter(state = googleState, onCommand = commands::add) }

        composeRule.onNodeWithTag("address").performTextClearance()
        composeRule.onNodeWithTag("address").performTextInput("dual browser")
        composeRule.onNodeWithTag("go").performClick()

        assertEquals(BrowserCommand.Navigate(googleTab.id, "dual browser"), commands.single())
    }

    @Test
    fun addressUpdatesWhenFocusedPageUrlChanges() {
        var currentState by mutableStateOf(state)
        composeRule.setContent { ControlCenter(state = currentState, onCommand = {}) }

        currentState = state.copy(
            tabs = listOf(tab.copy(url = "https://example.com/result")),
        )

        composeRule.onNodeWithTag("address").assertTextEquals("https://example.com/result")
    }

    @Test
    fun newTabButtonEmitsNormalTabCommand() {
        val commands = mutableListOf<BrowserCommand>()
        composeRule.setContent { ControlCenter(state = state, onCommand = commands::add) }

        composeRule.onNodeWithTag("control-center").assertIsDisplayed()
        composeRule.onNodeWithTag("browser-toolbar").assertIsDisplayed()
        composeRule.onNodeWithTag("new-tab").performClick()

        assertEquals(BrowserCommand.NewTab(isPrivate = false), commands.single())
    }

    @Test
    fun backButtonEmitsEngineAction() {
        val actions = mutableListOf<EngineAction>()
        composeRule.setContent {
            ControlCenter(
                state = state,
                onCommand = {},
                onEngineAction = actions::add,
            )
        }

        composeRule.onNodeWithTag("back").performClick()

        assertEquals(EngineAction.Back, actions.single())
    }

    @Test
    fun selectingTabMovesFocus() {
        val second = BrowserTab(id = "tab-2", url = "https://example.com", title = "Example")
        val commands = mutableListOf<BrowserCommand>()
        composeRule.setContent {
            ControlCenter(
                state = state.copy(tabs = listOf(tab, second)),
                onCommand = commands::add,
            )
        }

        composeRule.onNodeWithTag("tab-preview-list").performScrollToIndex(1)
        composeRule.onNodeWithTag("tab-preview-tab-2").performClick()

        assertEquals(BrowserCommand.Focus(second.id), commands.single())
    }

    @Test
    fun privateTabButtonEmitsPrivateTabCommand() {
        val commands = mutableListOf<BrowserCommand>()
        composeRule.setContent { ControlCenter(state = state, onCommand = commands::add) }

        composeRule.onNodeWithTag("private-tab").performClick()

        assertEquals(BrowserCommand.NewTab(isPrivate = true), commands.single())
    }

    @Test
    fun dualViewButtonPromotesFocusedTab() {
        val commands = mutableListOf<BrowserCommand>()
        composeRule.setContent { ControlCenter(state = state, onCommand = commands::add) }

        composeRule.onNodeWithTag("dual-view").performClick()

        assertEquals(BrowserCommand.PromoteToLower(tab.id), commands.single())
    }
}
