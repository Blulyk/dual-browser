package com.blulyk.dualbrowser.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainActivityLaunchModeTest {
    @Test
    fun launcherReusesThePrimaryActivityAcrossDisplays() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val activityInfo = context.packageManager.getActivityInfo(
            ComponentName(context, MainActivity::class.java),
            0,
        )

        assertEquals(ActivityInfo.LAUNCH_SINGLE_TASK, activityInfo.launchMode)
        assertTrue(activityInfo.exported)

        val launcherActivities = context.packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(context.packageName)
            },
            0,
        )
        assertEquals(
            listOf(MainActivity::class.java.name),
            launcherActivities.map { it.activityInfo.name },
        )
    }
}
