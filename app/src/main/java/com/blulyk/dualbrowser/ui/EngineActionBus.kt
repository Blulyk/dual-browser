package com.blulyk.dualbrowser.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class EngineActionBus {
    private val mutableActions = MutableSharedFlow<EngineAction>(extraBufferCapacity = 16)
    val actions = mutableActions.asSharedFlow()

    fun dispatch(action: EngineAction) {
        mutableActions.tryEmit(action)
    }
}

