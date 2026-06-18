package com.blulyk.dualbrowser.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecondaryDisplayTracker {
    private data class ActiveDisplay(val owner: Any, val displayId: Int)

    private var activeDisplay: ActiveDisplay? = null
    private val mutableActiveDisplayId = MutableStateFlow<Int?>(null)
    val activeDisplayId: StateFlow<Int?> = mutableActiveDisplayId.asStateFlow()

    fun started(owner: Any, displayId: Int) {
        activeDisplay = ActiveDisplay(owner, displayId)
        mutableActiveDisplayId.value = displayId
    }

    fun stopped(owner: Any) {
        if (activeDisplay?.owner === owner) {
            activeDisplay = null
            mutableActiveDisplayId.value = null
        }
    }
}
