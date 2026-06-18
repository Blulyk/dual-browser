package com.blulyk.dualbrowser.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.blulyk.dualbrowser.DualBrowserApplication
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserSessionManager
import com.blulyk.dualbrowser.platform.ControllerAction
import com.blulyk.dualbrowser.data.BookmarkEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class BrowserViewModel(
    private val sessionManager: BrowserSessionManager = BrowserSessionManager(),
    private val engineActionBus: EngineActionBus = EngineActionBus(),
    private val previewStore: TabPreviewStore = TabPreviewStore(),
    val bookmarks: Flow<List<BookmarkEntity>> = emptyFlow(),
) : ViewModel() {
    val state = sessionManager.state
    val engineActions = engineActionBus.actions
    val previews = previewStore.previews

    fun dispatch(command: BrowserCommand) {
        sessionManager.dispatch(command)
        previewStore.retain(sessionManager.state.value.tabs.mapTo(mutableSetOf()) { it.id })
    }

    fun updatePreview(tabId: String, bitmap: Bitmap) {
        if (state.value.tabs.any { it.id == tabId }) previewStore.put(tabId, bitmap)
    }

    fun dispatchEngine(action: EngineAction) {
        engineActionBus.dispatch(action)
    }

    fun handleController(action: ControllerAction) {
        when (action) {
            ControllerAction.Back -> dispatchEngine(EngineAction.Back)
            ControllerAction.PreviousTab -> focusRelative(-1)
            ControllerAction.NextTab -> focusRelative(1)
        }
    }

    private fun focusRelative(offset: Int) {
        val current = state.value
        val index = current.tabs.indexOfFirst { it.id == current.focusedTabId }
        val nextIndex = (index + offset).mod(current.tabs.size)
        dispatch(BrowserCommand.Focus(current.tabs[nextIndex].id))
    }

    class Factory(
        private val application: DualBrowserApplication,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass.isAssignableFrom(BrowserViewModel::class.java))
            return BrowserViewModel(
                application.sessionManager,
                application.engineActionBus,
                application.tabPreviewStore,
                application.repository.bookmarks,
            ) as T
        }
    }
}
