package com.cgm.timetwist.service

import com.cgm.timetwist.ui.DoubleOffset
import com.cgm.timetwist.ui.angleToPosition
import com.cgm.timetwist.ui.draggerPositionToAngle
import com.cgm.timetwist.ui.positionToRotatedAngle
import com.cgm.timetwist.ui.rotatedAngleToTimeValue
import com.cgm.timetwist.ui.timeValueToPosition
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.offset
import org.junit.Test

class CircularSliderTest {

    @Test
    fun `timeValueToPosition should calculate correct position`() {
        val center = DoubleOffset(50.0, 50.0)
        val trackRadius = 50.0
        val draggerRadius = 0.0 // Set draggerRadius to 0 to simplify the test

        var timeValue = 15.0
        var position = timeValueToPosition(timeValue, center, trackRadius, draggerRadius)
        assertThat(position.x).isCloseTo(100.0, offset(0.1))
        assertThat(position.y).isCloseTo(50.0, offset(0.1))
        timeValue = 30.0
        position = timeValueToPosition(timeValue, center, trackRadius, draggerRadius)
        assertThat(position.x).isCloseTo(50.0, offset(0.1))
        assertThat(position.y).isCloseTo(100.0, offset(0.1))
        timeValue = 45.0
        position = timeValueToPosition(timeValue, center, trackRadius, draggerRadius)
        assertThat(position.x).isCloseTo(0.0, offset(0.1))
        assertThat(position.y).isCloseTo(50.0, offset(0.1))
    }

    @Test
    fun `draggerPositionToAngle should calculate correct angle`() {
        val center = DoubleOffset(50.0, 50.0)
        val draggerRadius = 0.0

        val draggerOffset1 = DoubleOffset(100.0, 50.0)
        val angle1 = draggerPositionToAngle(draggerOffset1, center, draggerRadius)
        assertThat(angle1).isCloseTo(0.0, offset(0.1))

        val draggerOffset2 = DoubleOffset(50.0, 100.0)
        val angle2 = draggerPositionToAngle(draggerOffset2, center, draggerRadius)
        assertThat(angle2).isCloseTo(Math.PI / 2, offset(0.1))

        val draggerOffset3 = DoubleOffset(-100.0, 50.0)
        val angle3 = draggerPositionToAngle(draggerOffset3, center, draggerRadius)
        assertThat(angle3).isCloseTo(Math.PI, offset(0.1))

        val draggerOffset4 = DoubleOffset(50.0, -100.0)
        val angle4 = draggerPositionToAngle(draggerOffset4, center, draggerRadius)
        assertThat(angle4).isCloseTo(-Math.PI / 2, offset(0.1))
    }

    @Test
    fun `positionToRotatedAngle should calculate correct rotated angle`() {
        val center = DoubleOffset(50.0, 50.0)
        val draggerRadius = 0.0

        var draggerOffset = DoubleOffset(50.0, 0.0)
        var rotatedAngle = positionToRotatedAngle(draggerOffset, center, draggerRadius)
        assertThat(rotatedAngle).isCloseTo(0.0, offset(0.1))

        draggerOffset = DoubleOffset(0.0, 50.0)
        rotatedAngle = positionToRotatedAngle(draggerOffset, center, draggerRadius)
        assertThat(rotatedAngle).isCloseTo(1.5 * Math.PI, offset(0.1))

        draggerOffset = DoubleOffset(100.0, 50.0)
        rotatedAngle = positionToRotatedAngle(draggerOffset, center, draggerRadius)
        assertThat(rotatedAngle).isCloseTo(Math.PI / 2, offset(0.1))

        draggerOffset = DoubleOffset(50.0, 100.0)
        rotatedAngle = positionToRotatedAngle(draggerOffset, center, draggerRadius)
        assertThat(rotatedAngle).isCloseTo(Math.PI, offset(0.1))
    }

    @Test
    fun `rotatedAngleToTimeValue should calculate correct time value`() {
        val timeValue1 = rotatedAngleToTimeValue((Math.PI / 2.0))
        assertThat(timeValue1).isCloseTo(15.0, offset(0.1))
        val timeValue2 = rotatedAngleToTimeValue(Math.PI)
        assertThat(timeValue2).isCloseTo(30.0, offset(0.1))
        val timeValue3 = rotatedAngleToTimeValue(Math.PI * 1.5)
        assertThat(timeValue3).isCloseTo(45.0, offset(0.1))
    }

    @Test
    fun `angleToPosition should calculate correct position`() {
        val angle = Math.PI / 2
        val center = DoubleOffset(50.0, 50.0)
        val trackRadius = 40.0

        val position = angleToPosition(angle, center, trackRadius)

        assertThat(position.x).isCloseTo(50.0, offset(0.1))
        assertThat(position.y).isCloseTo(90.0, offset(0.1))
    }
}