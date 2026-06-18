package com.blulyk.dualbrowser.ui

import androidx.lifecycle.ViewModel
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.domain.BrowserSessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BrowserViewModel(
    private val sessionManager: BrowserSessionManager = BrowserSessionManager(),
) : ViewModel() {
    val state = sessionManager.state

    private val mutableEngineActions = MutableSharedFlow<EngineAction>(extraBufferCapacity = 16)
    val engineActions = mutableEngineActions.asSharedFlow()

    fun dispatch(command: BrowserCommand) = sessionManager.dispatch(command)

    fun dispatchEngine(action: EngineAction) {
        mutableEngineActions.tryEmit(action)
    }
}

