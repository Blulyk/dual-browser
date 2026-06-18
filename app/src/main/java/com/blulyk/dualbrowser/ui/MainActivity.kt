package com.blulyk.dualbrowser.ui

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blulyk.dualbrowser.DualBrowserApplication
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.platform.AndroidDisplayCoordinator
import com.blulyk.dualbrowser.platform.ControllerMapper

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<BrowserViewModel> {
        BrowserViewModel.Factory(application as DualBrowserApplication)
    }
    private lateinit var displayCoordinator: AndroidDisplayCoordinator
    private val controllerMapper = ControllerMapper()
    private var dualDisplayActive by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayCoordinator = AndroidDisplayCoordinator(this)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            MaterialTheme {
                if (dualDisplayActive) {
                    if (state.focusedTab.needsRecovery) {
                        RecoveryView(state.focusedTab, viewModel::dispatch)
                    } else {
                        WebSurface(
                            tab = state.focusedTab,
                            engineActions = viewModel.engineActions,
                            onRendererGone = {
                                viewModel.dispatch(BrowserCommand.RendererGone(state.focusedTabId))
                            },
                            onPageFinished = { url, title ->
                                viewModel.dispatch(
                                    BrowserCommand.UpdatePage(state.focusedTabId, url, title),
                                )
                            },
                            onPopupRequested = { url ->
                                viewModel.dispatch(
                                    BrowserCommand.OpenTab(
                                        input = url,
                                        isPrivate = state.focusedTab.isPrivate,
                                    ),
                                )
                            },
                        )
                    }
                } else {
                    BrowserApp(
                        state = state,
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
        displayCoordinator.start { assignment ->
            dualDisplayActive = displayCoordinator.launchLowerIfNeeded(assignment)
        }
    }

    override fun onStop() {
        displayCoordinator.stop()
        super.onStop()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        controllerMapper.map(keyCode)?.let {
            viewModel.handleController(it)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
