package com.blulyk.dualbrowser.platform

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display
import android.os.Build
import android.util.Log
import com.blulyk.dualbrowser.ui.SecondaryDisplayActivity

class AndroidDisplayCoordinator(
    private val activity: Activity,
    private val coordinator: DisplayCoordinator = DisplayCoordinator(),
) : DisplayManager.DisplayListener {
    private companion object {
        const val TAG = "DualBrowserDisplays"
    }
    private val displayManager = activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var assignmentListener: (DisplayAssignment) -> Unit = {}

    fun start(listener: (DisplayAssignment) -> Unit) {
        assignmentListener = listener
        displayManager.registerDisplayListener(this, null)
        notifyAssignment()
    }

    fun stop() {
        displayManager.unregisterDisplayListener(this)
    }

    fun launchLowerIfNeeded(assignment: DisplayAssignment): Boolean {
        val currentDisplayId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display?.displayId ?: Display.DEFAULT_DISPLAY
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay.displayId
        }
        val lowerId = assignment.lowerId ?: return false
        if (!coordinator.shouldLaunchLower(currentDisplayId, assignment)) return false
        return launchLowerActivity(lowerId)
    }

    fun isDualModeReady(
        assignment: DisplayAssignment,
        activeSecondaryDisplayId: Int?,
    ): Boolean = coordinator.isDualModeReady(assignment, activeSecondaryDisplayId)

    private fun launchLowerActivity(displayId: Int): Boolean = runCatching {
        val intent = Intent(activity, SecondaryDisplayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val options = ActivityOptions.makeBasic().setLaunchDisplayId(displayId)
        activity.startActivity(intent, options.toBundle())
    }.onFailure { error ->
        Log.e(TAG, "Unable to launch secondary activity on display $displayId", error)
    }.isSuccess

    override fun onDisplayAdded(displayId: Int) = notifyAssignment()

    override fun onDisplayRemoved(displayId: Int) = notifyAssignment()

    override fun onDisplayChanged(displayId: Int) = notifyAssignment()

    private fun notifyAssignment() {
        val snapshots = displayManager.displays.map { display ->
                DisplaySnapshot(
                    id = display.displayId,
                    width = display.mode.physicalWidth,
                    height = display.mode.physicalHeight,
                    refreshRate = display.refreshRate,
                    isPoweredOn = display.state != Display.STATE_OFF,
                )
            }
        Log.i(TAG, "Reported displays: $snapshots")
        if (snapshots.isNotEmpty()) assignmentListener(coordinator.assign(snapshots))
    }
}
