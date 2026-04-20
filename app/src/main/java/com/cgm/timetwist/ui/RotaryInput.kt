package com.cgm.timetwist.ui

const val ROTARY_PIXELS_PER_UNIT: Float = 24f

fun applyRotaryDelta(
    current: Long,
    accumulatorPx: Float,
    scrollPx: Float,
): Pair<Long, Float> {
    val total = accumulatorPx + scrollPx
    val delta = (total / ROTARY_PIXELS_PER_UNIT).toInt()
    val remainder = total - delta * ROTARY_PIXELS_PER_UNIT
    val next = ((current + delta) % 60 + 60) % 60
    return next to remainder
}
