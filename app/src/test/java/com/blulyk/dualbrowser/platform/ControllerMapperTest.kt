package com.blulyk.dualbrowser.platform

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class ControllerMapperTest {
    private val mapper = ControllerMapper()

    @Test
    fun shoulderButtonsSwitchTabs() {
        assertEquals(ControllerAction.PreviousTab, mapper.map(KeyEvent.KEYCODE_BUTTON_L1))
        assertEquals(ControllerAction.NextTab, mapper.map(KeyEvent.KEYCODE_BUTTON_R1))
    }

    @Test
    fun buttonBMapsToBack() {
        assertEquals(ControllerAction.Back, mapper.map(KeyEvent.KEYCODE_BUTTON_B))
    }
}

