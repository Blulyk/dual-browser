package com.blulyk.dualbrowser.ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import com.blulyk.dualbrowser.platform.DisplayCoordinator
import com.blulyk.dualbrowser.platform.DisplaySnapshot

class LauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val snapshots = displayManager.displays.map { display ->
            DisplaySnapshot(
                id = display.displayId,
                width = display.mode.physicalWidth,
                height = display.mode.physicalHeight,
                refreshRate = display.refreshRate,
                isPoweredOn = display.state != Display.STATE_OFF,
            )
        }
        val upperDisplayId = snapshots.takeIf { it.isNotEmpty() }
            ?.let(DisplayCoordinator()::assign)
            ?.upperId
            ?: Display.DEFAULT_DISPLAY
        val primaryIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val options = ActivityOptions.makeBasic().setLaunchDisplayId(upperDisplayId)

        startActivity(primaryIntent, options.toBundle())
        finish()
    }
}
