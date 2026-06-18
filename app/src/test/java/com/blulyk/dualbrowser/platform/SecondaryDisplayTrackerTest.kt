package com.blulyk.dualbrowser.platform

import org.junit.Assert.assertEquals
import org.junit.Test

class SecondaryDisplayTrackerTest {
    @Test
    fun reportsOnlyTheCurrentlyStartedSecondaryDisplay() {
        val tracker = SecondaryDisplayTracker()
        val owner = Any()

        tracker.started(owner, displayId = 2)
        assertEquals(2, tracker.activeDisplayId.value)

        tracker.stopped(Any())
        assertEquals(2, tracker.activeDisplayId.value)

        tracker.stopped(owner)
        assertEquals(null, tracker.activeDisplayId.value)
    }

    @Test
    fun oldOwnerCannotClearNewOwnerOnSameDisplay() {
        val tracker = SecondaryDisplayTracker()
        val oldOwner = Any()
        val newOwner = Any()

        tracker.started(oldOwner, displayId = 2)
        tracker.started(newOwner, displayId = 2)
        tracker.stopped(oldOwner)

        assertEquals(2, tracker.activeDisplayId.value)
    }
}
