package com.blulyk.dualbrowser.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class SecondaryLaunchReconcilerTest {
    @Test
    fun launchesWhenStoppedSecondaryReportsAfterAssignment() {
        val calls = mutableListOf<Pair<DisplayAssignment, Int?>>()
        val assignment = DisplayAssignment(upperId = 0, lowerId = 4)
        val reconciler = SecondaryLaunchReconciler { currentAssignment, activeDisplayId ->
            calls += currentAssignment to activeDisplayId
        }

        reconciler.secondaryChanged(4)
        reconciler.assignmentChanged(assignment)
        reconciler.secondaryChanged(null)
        reconciler.secondaryChanged(null)

        assertEquals(
            listOf(assignment to 4, assignment to null),
            calls,
        )
    }

    @Test
    fun resetAllowsTheSameStateToReconcileOnANewLifecycle() {
        var calls = 0
        val assignment = DisplayAssignment(upperId = 0, lowerId = 4)
        val reconciler = SecondaryLaunchReconciler { _, _ -> calls++ }
        reconciler.secondaryChanged(null)
        reconciler.assignmentChanged(assignment)

        reconciler.reset()
        reconciler.secondaryChanged(null)

        assertEquals(2, calls)
    }
}
