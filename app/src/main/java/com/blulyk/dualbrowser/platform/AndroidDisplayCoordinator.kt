package com.blulyk.dualbrowser.platform

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display
import com.blulyk.dualbrowser.ui.SecondaryDisplayActivity

class AndroidDisplayCoordinator(
    private val activity: Activity,
    private val coordinator: DisplayCoordinator = DisplayCoordinator(),
) : DisplayManager.DisplayListener {
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
        val currentDisplayId = activity.display?.displayId ?: Display.DEFAULT_DISPLAY
        val lowerId = assignment.lowerId ?: return false
        if (!coordinator.shouldLaunchLower(currentDisplayId, assignment)) return false
        return launchLowerActivity(lowerId)
    }

    private fun launchLowerActivity(displayId: Int): Boolean = runCatching {
        val intent = Intent(activity, SecondaryDisplayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val options = ActivityOptions.makeBasic().setLaunchDisplayId(displayId)
        activity.startActivity(intent, options.toBundle())
    }.isSuccess

    override fun onDisplayAdded(displayId: Int) = notifyAssignment()

    override fun onDisplayRemoved(displayId: Int) = notifyAssignment()

    override fun onDisplayChanged(displayId: Int) = notifyAssignment()

    private fun notifyAssignment() {
        val snapshots = displayManager.displays
            .filter { it.state != Display.STATE_OFF }
            .map { display ->
                DisplaySnapshot(
                    id = display.displayId,
                    width = display.mode.physicalWidth,
                    height = display.mode.physicalHeight,
                    refreshRate = display.refreshRate,
                )
            }
        if (snapshots.isNotEmpty()) assignmentListener(coordinator.assign(snapshots))
    }
}
