package com.example.timetwist.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class DoubleOffset(val x: Double, val y: Double) {
    fun toInt(): IntOffset {
        return IntOffset(x.toInt(), y.toInt())
    }

    fun toFloat(): Offset {
        return Offset(x.toFloat(), y.toFloat())
    }
}

fun timeValueToPosition(
    timeValue: Double,
    center: DoubleOffset,
    trackRadius: Double,
    draggerRadius: Double
): DoubleOffset {
    // Scale back to [0, 2π] range
    val angle = ((timeValue / 60.0) * 2 * Math.PI)

    // Rotate by -π/2 to orient like a clock
    val rotatedAngle: Double = angle - (Math.PI / 2.0)

    // Compute the x and y coordinates
    val x = center.x + trackRadius * cos(rotatedAngle)
    val y = center.y + trackRadius * sin(rotatedAngle)

    // Adjust to compensate for dragger size
    val draggerOffset = DoubleOffset((x - draggerRadius), (y - draggerRadius))

    return draggerOffset
}

fun draggerPositionToAngle(
    draggerOffset: DoubleOffset,
    center: DoubleOffset,
    draggerRadius: Double
): Double {
    // Convert dragger offset to actual x, y coordinates
    val x: Double = draggerOffset.x + draggerRadius
    val y: Double = draggerOffset.y + draggerRadius

    // Calculate angle based on x, y coordinates
    val angle = atan2((y - center.y), (x - center.x))

    return angle
}

fun positionToRotatedAngle(
    draggerOffset: DoubleOffset,
    center: DoubleOffset,
    draggerRadius: Double
): Double {
    val angle = draggerPositionToAngle(draggerOffset, center, draggerRadius)

    // Rotate by +π/2 to adjust for clock-like orientation
    val rotatedAngle = angle + (Math.PI / 2.0)

    return rotatedAngle
}

fun rotatedAngleToTimeValue(
    rotatedAngle: Double
): Double {
    // Scale to a [0, 60) range
    val timeValue = ((rotatedAngle / (2 * Math.PI)) * 60)

    // Ensure timeValue stays within [0, 60)
    val wrappedTimeValue = if (timeValue < 0) (timeValue + 60) else timeValue

    return wrappedTimeValue
}

fun angleToPosition(
    angle: Double,
    center: DoubleOffset,
    trackRadius: Double,
): DoubleOffset {
    // Compute the x and y coordinates
    val x = center.x + trackRadius * cos(angle)
    val y = center.y + trackRadius * sin(angle)

    // Adjust to compensate for dragger size
    val draggerOffset = DoubleOffset(x, y)

    return draggerOffset
}


@Composable
fun CircularSlider(
    originalValue: Double,
    setNewTimeValue: (Double) -> Unit
) {
    val draggerRadiusDp = 16.dp

    val draggerRadiusInitialized = remember { mutableStateOf(false) }
    val draggerRadiusPx = remember { mutableDoubleStateOf(0.0) }
    val sliderState = remember { mutableStateOf(CircularSliderState(DoubleOffset(0.0, 0.0), 0.0)) }
    val draggerState = remember { mutableStateOf(DraggerState(isInitialized = false)) }


    Box( // The circle around the edge of the screen
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                if (!sliderState.value.isInitialized) {
                    val newRadius = (it.size.width.toDouble() / 2) - draggerRadiusPx.doubleValue
                    val newCenter = DoubleOffset(it.size.width.toDouble() / 2, it.size.height.toDouble() / 2f)
                    sliderState.value = sliderState.value.copy(
                        center = newCenter,
                        radius = newRadius,
                        isInitialized = true,
                    )
                }
                if (!draggerState.value.isInitialized) {
                    val draggerOffset = timeValueToPosition(
                        originalValue, sliderState.value.center, sliderState.value.radius, draggerRadiusPx.doubleValue
                    )
                    draggerState.value = DraggerState(draggerOffset, true)
                }
            }
            .layout { measurable, constraints ->
                if (!draggerRadiusInitialized.value) {
                    draggerRadiusPx.doubleValue = draggerRadiusDp
                        .toPx()
                        .toDouble()
                    draggerRadiusInitialized.value = true
                }
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                center = Offset(size.width / 2, size.height / 2),
                radius = sliderState.value.radius.toFloat(),
                color = Color.Gray,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
//            val centerFloat = sliderState.value.center.toFloat()
//            drawLine(color = Color.Magenta, start = centerFloat, end = positionLineEnd.value.toFloat())
//            drawLine(color = Color.Blue, start = centerFloat, end = angleLineEnd.value.toFloat())
//            drawLine(color = Color.Cyan, start = centerFloat, end = touchLineEnd.value.toFloat())
//            drawCircle(radius = 5f, color = Color.Cyan, center = touchLineEnd.value.toFloat())
        }
    }


    Box( // The box containing the dragger
        modifier = Modifier
            .fillMaxSize()
    ) {
        val boxSize = (draggerRadiusDp * 2) + 2.dp
        Box(
            modifier = Modifier
                .size(boxSize)
                .offset {
                    draggerState.value.offset.toInt()
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            val calculatedX = draggerState.value.offset.x + change.position.x.toDouble()
                            val calculatedY = draggerState.value.offset.y + change.position.y.toDouble()
//                            positionLineEnd.value = (DoubleOffset(calculatedX + draggerRadiusPx, calculatedY + draggerRadiusPx))

                            val position = DoubleOffset(calculatedX, calculatedY)
                            val rotatedAngle = positionToRotatedAngle(position, sliderState.value.center, draggerRadiusPx.doubleValue)

                            // Use that angle to set the time value
                            val newTimeValue = rotatedAngleToTimeValue(rotatedAngle)
                            setNewTimeValue(newTimeValue)

                            // Use that angle to position the dragger
                            val touchAngle = draggerPositionToAngle(position, sliderState.value.center, draggerRadiusPx.doubleValue)
                            val trackPosition = angleToPosition(
                                touchAngle,
                                sliderState.value.center,
                                sliderState.value.radius,
                            )
                            val newPosition = DoubleOffset(trackPosition.x - draggerRadiusPx.doubleValue, trackPosition.y - draggerRadiusPx.doubleValue)
//                            angleLineEnd.value = DoubleOffset(newPosition.x, newPosition.y)
                            draggerState.value = DraggerState(DoubleOffset(newPosition.x, newPosition.y), true)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(radius = draggerRadiusPx.doubleValue.toFloat(), color = Color.Blue)
            }
        }
    }
}
