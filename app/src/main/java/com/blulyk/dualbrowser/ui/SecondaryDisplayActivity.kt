package com.blulyk.dualbrowser.ui

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blulyk.dualbrowser.DualBrowserApplication
import com.blulyk.dualbrowser.platform.ControllerMapper
import android.os.Build
import android.view.Display

class SecondaryDisplayActivity : ComponentActivity() {
    private val displayTrackerOwner = Any()
    private val controllerMapper = ControllerMapper()
    private val viewModel by viewModels<BrowserViewModel> {
        BrowserViewModel.Factory(application as DualBrowserApplication)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            MaterialTheme {
                val lowerTabId = state.lowerTabId
                if (lowerTabId == null) {
                    ControlCenter(
                        state = state,
                        onCommand = viewModel::dispatch,
                        onEngineAction = viewModel::dispatchEngine,
                    )
                } else {
                    BrowserApp(
                        state = state.copy(focusedTabId = lowerTabId),
                        onCommand = viewModel::dispatch,
                        engineActions = viewModel.engineActions,
                        onEngineAction = viewModel::dispatchEngine,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (application as DualBrowserApplication).secondaryDisplayTracker.started(
            displayTrackerOwner,
            currentDisplayId(),
        )
    }

    override fun onStop() {
        (application as DualBrowserApplication).secondaryDisplayTracker.stopped(displayTrackerOwner)
        super.onStop()
    }

    private fun currentDisplayId(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display?.displayId ?: Display.DEFAULT_DISPLAY
    } else {
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.displayId
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        controllerMapper.map(keyCode)?.let {
            viewModel.handleController(it)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
