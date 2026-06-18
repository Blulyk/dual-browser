package com.blulyk.dualbrowser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.blulyk.dualbrowser.DualBrowserApplication
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserSessionManager

class BrowserViewModel(
    private val sessionManager: BrowserSessionManager = BrowserSessionManager(),
    private val engineActionBus: EngineActionBus = EngineActionBus(),
) : ViewModel() {
    val state = sessionManager.state
    val engineActions = engineActionBus.actions

    fun dispatch(command: BrowserCommand) = sessionManager.dispatch(command)

    fun dispatchEngine(action: EngineAction) {
        engineActionBus.dispatch(action)
    }

    class Factory(
        private val application: DualBrowserApplication,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass.isAssignableFrom(BrowserViewModel::class.java))
            return BrowserViewModel(application.sessionManager, application.engineActionBus) as T
        }
    }
}

