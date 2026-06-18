package com.blulyk.dualbrowser.platform

class DisplayCoordinator {
    fun assign(displays: List<DisplaySnapshot>): DisplayAssignment {
        require(displays.isNotEmpty()) { "At least one display is required" }
        val ordered = displays.sortedWith(
            compareByDescending<DisplaySnapshot> { it.area }.thenBy { it.id },
        )
        return DisplayAssignment(
            upperId = ordered.first().id,
            lowerId = ordered.getOrNull(1)?.id,
        )
    }

    fun shouldLaunchLower(
        currentDisplayId: Int,
        assignment: DisplayAssignment,
        activeSecondaryDisplayId: Int?,
    ): Boolean = assignment.lowerId != null &&
        currentDisplayId == assignment.upperId &&
        activeSecondaryDisplayId != assignment.lowerId

    fun isDualModeReady(
        assignment: DisplayAssignment,
        activeSecondaryDisplayId: Int?,
    ): Boolean = assignment.lowerId != null && assignment.lowerId == activeSecondaryDisplayId
}
