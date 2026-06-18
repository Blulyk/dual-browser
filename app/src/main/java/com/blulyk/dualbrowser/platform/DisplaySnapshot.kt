package com.blulyk.dualbrowser.platform

data class DisplaySnapshot(
    val id: Int,
    val width: Int,
    val height: Int,
    val densityDpi: Int = 0,
    val refreshRate: Float = 0f,
    val isPoweredOn: Boolean = true,
) {
    val area: Long = width.toLong() * height.toLong()
}

data class DisplayAssignment(
    val upperId: Int,
    val lowerId: Int?,
)

