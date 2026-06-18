package com.blulyk.dualbrowser.platform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayCoordinatorTest {
    private val coordinator = DisplayCoordinator()

    @Test
    fun largestDisplayIsUpperAndOtherIsLower() {
        val assignment = coordinator.assign(
            listOf(
                DisplaySnapshot(id = 0, width = 1920, height = 1080),
                DisplaySnapshot(id = 2, width = 1240, height = 1080),
            ),
        )

        assertEquals(0, assignment.upperId)
        assertEquals(2, assignment.lowerId)
    }

    @Test
    fun oneDisplayHasNoLowerAssignment() {
        val assignment = coordinator.assign(
            listOf(DisplaySnapshot(id = 0, width = 1920, height = 1080)),
        )

        assertEquals(0, assignment.upperId)
        assertNull(assignment.lowerId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyDisplayListIsRejected() {
        coordinator.assign(emptyList())
    }

    @Test
    fun onlyUpperActivityShouldLaunchLowerDisplay() {
        val assignment = DisplayAssignment(upperId = 0, lowerId = 2)

        assertEquals(true, coordinator.shouldLaunchLower(currentDisplayId = 0, assignment))
        assertEquals(false, coordinator.shouldLaunchLower(currentDisplayId = 2, assignment))
    }
}
