package com.blulyk.dualbrowser.platform

class SecondaryLaunchReconciler(
    private val onReconcile: (DisplayAssignment, Int?) -> Unit,
) {
    private var assignment: DisplayAssignment? = null
    private var activeSecondaryDisplayId: Int? = null
    private var hasSecondaryState = false
    private var lastReconciled: Pair<DisplayAssignment, Int?>? = null

    fun assignmentChanged(value: DisplayAssignment) {
        assignment = value
        reconcile()
    }

    fun secondaryChanged(displayId: Int?) {
        activeSecondaryDisplayId = displayId
        hasSecondaryState = true
        reconcile()
    }

    fun reset() {
        lastReconciled = null
    }

    private fun reconcile() {
        val currentAssignment = assignment ?: return
        if (!hasSecondaryState) return
        val current = currentAssignment to activeSecondaryDisplayId
        if (current == lastReconciled) return
        lastReconciled = current
        onReconcile(current.first, current.second)
    }
}
