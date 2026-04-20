package com.cgm.timetwist.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class RotaryInputTest {
    @Test
    fun `accumulates below threshold without change`() {
        val (next, rem) = applyRotaryDelta(current = 10L, accumulatorPx = 0f, scrollPx = 10f)
        assertEquals(10L, next)
        assertEquals(10f, rem, 0.0001f)
    }

    @Test
    fun `one unit emitted at threshold`() {
        val (next, rem) = applyRotaryDelta(current = 10L, accumulatorPx = 0f, scrollPx = ROTARY_PIXELS_PER_UNIT)
        assertEquals(11L, next)
        assertEquals(0f, rem, 0.0001f)
    }

    @Test
    fun `multiple units emitted from single large event`() {
        val (next, rem) = applyRotaryDelta(current = 0L, accumulatorPx = 0f, scrollPx = ROTARY_PIXELS_PER_UNIT * 3)
        assertEquals(3L, next)
        assertEquals(0f, rem, 0.0001f)
    }

    @Test
    fun `wraps past 60 forward`() {
        val (next, _) = applyRotaryDelta(current = 59L, accumulatorPx = 0f, scrollPx = ROTARY_PIXELS_PER_UNIT * 2)
        assertEquals(1L, next)
    }

    @Test
    fun `wraps past 0 backward`() {
        val (next, _) = applyRotaryDelta(current = 0L, accumulatorPx = 0f, scrollPx = -ROTARY_PIXELS_PER_UNIT)
        assertEquals(59L, next)
    }

    @Test
    fun `accumulator carries remainder across calls`() {
        val (n1, r1) = applyRotaryDelta(current = 0L, accumulatorPx = 0f, scrollPx = ROTARY_PIXELS_PER_UNIT * 0.6f)
        assertEquals(0L, n1)
        val (n2, _) = applyRotaryDelta(current = n1, accumulatorPx = r1, scrollPx = ROTARY_PIXELS_PER_UNIT * 0.6f)
        assertEquals(1L, n2)
    }
}
