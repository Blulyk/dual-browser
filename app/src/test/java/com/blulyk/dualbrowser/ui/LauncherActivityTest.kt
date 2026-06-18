package com.blulyk.dualbrowser.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LauncherActivityTest {
    @Test
    fun launcherStartsThePrimaryActivityAndFinishes() {
        val activity = Robolectric.buildActivity(LauncherActivity::class.java).create().get()

        val startedIntent = shadowOf(activity).nextStartedActivity
        assertEquals(MainActivity::class.java.name, startedIntent.component?.className)
        assertTrue(activity.isFinishing)
    }
}
