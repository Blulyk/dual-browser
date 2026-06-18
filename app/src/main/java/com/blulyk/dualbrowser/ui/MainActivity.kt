package com.blulyk.dualbrowser.ui

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blulyk.dualbrowser.DualBrowserApplication
import com.blulyk.dualbrowser.domain.BrowserCommand
import com.blulyk.dualbrowser.platform.AndroidDisplayCoordinator
import com.blulyk.dualbrowser.platform.ControllerMapper
import com.blulyk.dualbrowser.platform.DisplayAssignment
import com.blulyk.dualbrowser.platform.SecondaryLaunchReconciler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<BrowserViewModel> {
        BrowserViewModel.Factory(application as DualBrowserApplication)
    }
    private lateinit var displayCoordinator: AndroidDisplayCoordinator
    private lateinit var secondaryLaunchReconciler: SecondaryLaunchReconciler
    private val controllerMapper = ControllerMapper()
    private var displayAssignment by mutableStateOf<DisplayAssignment?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayCoordinator = AndroidDisplayCoordinator(this)
        secondaryLaunchReconciler = SecondaryLaunchReconciler { assignment, activeDisplayId ->
            displayCoordinator.launchLowerIfNeeded(assignment, activeDisplayId)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                (application as DualBrowserApplication).secondaryDisplayTracker.activeDisplayId
                    .collect(secondaryLaunchReconciler::secondaryChanged)
            }
        }
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val previews by viewModel.previews.collectAsStateWithLifecycle()
            val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle(initialValue = emptyList())
            val activeSecondaryDisplayId by (application as DualBrowserApplication)
                .secondaryDisplayTracker.activeDisplayId.collectAsStateWithLifecycle()
            val dualDisplayActive = displayAssignment?.let { assignment ->
                displayCoordinator.isDualModeReady(assignment, activeSecondaryDisplayId)
            } == true
            DualBrowserTheme {
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
                            onPreviewCaptured = viewModel::updatePreview,
                        )
                    }
                } else {
                    BrowserApp(
                        state = state,
                        onCommand = viewModel::dispatch,
                        engineActions = viewModel.engineActions,
                        onEngineAction = viewModel::dispatchEngine,
                        onPreviewCaptured = viewModel::updatePreview,
                        previews = previews,
                        bookmarks = bookmarks,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val tracker = (application as DualBrowserApplication).secondaryDisplayTracker
        secondaryLaunchReconciler.secondaryChanged(tracker.activeDisplayId.value)
        displayCoordinator.start { assignment ->
            displayAssignment = assignment
            secondaryLaunchReconciler.assignmentChanged(assignment)
        }
    }

    override fun onStop() {
        displayCoordinator.stop()
        secondaryLaunchReconciler.reset()
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
