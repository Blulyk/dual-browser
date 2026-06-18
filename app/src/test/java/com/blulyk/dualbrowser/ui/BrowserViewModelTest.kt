package com.blulyk.dualbrowser.ui

import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserSessionManager
import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserViewModelTest {
    @Test
    fun commandIsForwardedToSessionManager() {
        val manager = BrowserSessionManager()
        val viewModel = BrowserViewModel(manager)

        viewModel.dispatch(BrowserCommand.NewTab(isPrivate = false))

        assertEquals(2, viewModel.state.value.tabs.size)
    }
}

