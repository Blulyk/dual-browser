package com.blulyk.dualbrowser.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class SecondaryDisplayTrackerTest {
    @Test
    fun reportsOnlyTheCurrentlyStartedSecondaryDisplay() {
        val tracker = SecondaryDisplayTracker()

        tracker.started(displayId = 2)
        assertEquals(2, tracker.activeDisplayId.value)

        tracker.stopped(displayId = 3)
        assertEquals(2, tracker.activeDisplayId.value)

        tracker.stopped(displayId = 2)
        assertEquals(null, tracker.activeDisplayId.value)
    }
}
