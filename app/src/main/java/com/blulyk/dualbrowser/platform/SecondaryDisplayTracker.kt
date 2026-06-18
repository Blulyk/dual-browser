package com.blulyk.dualbrowser.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecondaryDisplayTracker {
    private val mutableActiveDisplayId = MutableStateFlow<Int?>(null)
    val activeDisplayId: StateFlow<Int?> = mutableActiveDisplayId.asStateFlow()

    fun started(displayId: Int) {
        mutableActiveDisplayId.value = displayId
    }

    fun stopped(displayId: Int) {
        if (mutableActiveDisplayId.value == displayId) {
            mutableActiveDisplayId.value = null
        }
    }
}
