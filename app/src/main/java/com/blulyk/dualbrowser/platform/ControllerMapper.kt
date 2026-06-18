package com.blulyk.dualbrowser.platform

import android.view.KeyEvent

enum class ControllerAction {
    Back,
    PreviousTab,
    NextTab,
}

class ControllerMapper {
    fun map(keyCode: Int): ControllerAction? = when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_B -> ControllerAction.Back
        KeyEvent.KEYCODE_BUTTON_L1 -> ControllerAction.PreviousTab
        KeyEvent.KEYCODE_BUTTON_R1 -> ControllerAction.NextTab
        else -> null
    }
}

