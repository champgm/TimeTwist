package com.example.timetwist.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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

fun timeValueToPosition(
    timeValue: Long,
    center: Offset,
    trackRadius: Float,
    draggerRadius: Float
): IntOffset {
    // Scale back to [0, 2π] range
    val angle = ((timeValue.toDouble() / 60.0) * 2 * Math.PI)

    // Rotate by -π/2 to orient like a clock
    val adjustedAngle = angle - (Math.PI / 2.0)

    // Compute the x and y coordinates
    val x = center.x + trackRadius * cos(adjustedAngle)
    val y = center.y + trackRadius * sin(adjustedAngle)

    return IntOffset(x.toInt(), y.toInt())
}

data class DraggerState(
    var contentSize: IntSize = IntSize(width = 0, height = 0),
    var containerSize: IntSize = IntSize(width = 0, height = 0),
    var offset: MutableState<IntOffset> = mutableStateOf(IntOffset(x = 10, y = 0))
//    var offset: IntOffset = IntOffset(x = 10, y = 0),
) {
    fun setOffset(newOffset: IntOffset) {
        offset.value = newOffset
    }
}

@Composable
fun CircularSlider(
    originalValue: Long,
    onValueChanged: (Long) -> Unit
) {
    val isInitialPositionSet = remember { mutableStateOf(false) }
    val center = remember { mutableStateOf(Offset(0f, 0f)) }
    val bounds = remember { mutableStateOf(IntSize(0, 0)) }
    val trackRadius = remember { mutableFloatStateOf(0f) }
    val draggerRadiusDp = 16.dp
    var draggerRadiusPx = 0f
    val draggerState = remember { mutableStateOf(DraggerState()) }

    Box( // The circle around the edge of the screen
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                bounds.value = it.size
            }
            .layout { measurable, constraints ->
                draggerRadiusPx = draggerRadiusDp.toPx()
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                    center.value = Offset(placeable.width / 2f, placeable.height / 2f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            trackRadius.value = (size.width / 2) - draggerRadiusPx// / 2)
            if (!isInitialPositionSet.value) {
                val onCircleOffset = timeValueToPosition(
                    originalValue, center.value, trackRadius.value, draggerRadiusPx
                )
                val draggerOffset = IntOffset((onCircleOffset.x - draggerRadiusPx).toInt(), (onCircleOffset.y - draggerRadiusPx).toInt())
                draggerState.value.setOffset(draggerOffset)
                isInitialPositionSet.value = true
            }
            drawCircle(
                center = Offset(size.width / 2, size.height / 2),
                radius = trackRadius.value,
                color = Color.Gray,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, Color.Magenta)
            .onGloballyPositioned {
                Log.e("CirclePointerInput", "onGloballyPositioned, size is: ${it.size}")
                draggerState.value.containerSize = it.size
            }
    ) {
        val boxSize = (draggerRadiusDp * 2) + 2.dp
        Box( // I don't remember why this is in 2 boxes.
            modifier = Modifier
                .size(boxSize)
                .offset { draggerState.value.offset.value }
                .border(2.dp, Color.Magenta)
                .onGloballyPositioned { draggerState.value.contentSize = it.size }
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        val calculatedX = draggerState.value.offset.value.x + dragAmount.x.roundToInt()
                        val calculatedY = draggerState.value.offset.value.y + dragAmount.y.roundToInt()

                        val originalAngle = atan2(calculatedX.toDouble(), calculatedY.toDouble())

                        // Rotate by +π to make the 0 value appear at the top (like a clock)
                        val adjustedAngle = originalAngle + (Math.PI / 2)

                        // Normalize angle (currently at [-π, π] range) to [0, 2π] range
                        val normalizedAngle = if (adjustedAngle < 0) adjustedAngle + 2 * Math.PI else adjustedAngle

                        // Scale the angle to [0, 59] range (to represent minutes/seconds)
                        val scaledValue = ((normalizedAngle / (2 * Math.PI)) * 60).toLong()
                        onValueChanged(scaledValue)

                        val x = bounds.value.width + trackRadius.value * cos(originalAngle)
                        val y = bounds.value.height + trackRadius.value * sin(originalAngle)

                        draggerState.value.setOffset(IntOffset(x.toInt(), y.toInt()))
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(radius = draggerRadiusPx, color = Color.Blue)
            }
        }
    }


//    Box( // The circle that the user drags around to input a value
//        modifier = Modifier
//            .size(draggerDp * 2)
//            .offset {
//                val draggerDpInt = draggerDp
//                    .toPx()
//                    .toInt() / 2
//                IntOffset(
//                    draggerPosition.value.x.toInt() - draggerDpInt,
//                    draggerPosition.value.y.toInt() - draggerDpInt
//                )
//            }
//            .pointerInput(Unit) {
//                detectDragGestures { change, _ ->
////                    val local = change.position
//                    val global = change.globalPosition
//                    // This is the angle between the X axis and the line from 0,0 to x,y
//                    val originalAngle = atan2(local.y - bounds.value.y, local.x - bounds.value.x).toDouble()
//
//                    // Rotate by +π to make the 0 value appear at the top (like a clock)
//                    val adjustedAngle = originalAngle + (Math.PI / 2)
//
//                    // Normalize angle (currently at [-π, π] range) to [0, 2π] range
//                    val normalizedAngle = if (adjustedAngle < 0) adjustedAngle + 2 * Math.PI else adjustedAngle
//
//                    // Scale the angle to [0, 59] range (to represent minutes/seconds)
//                    val scaledValue = ((normalizedAngle / (2 * Math.PI)) * 60).toLong()
//
//                    val x = bounds.value.x + radius.value * cos(originalAngle)
//                    val y = bounds.value.y + radius.value * sin(originalAngle)
//                    draggerPosition.value = Offset(x.toFloat(), y.toFloat())
//                    onValueChanged(scaledValue)
//                }
//            }
//    ) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            drawCircle(
//                radius = draggerDp.toPx(),
//                color = Color.Blue
//            )
//        }
//    }
}

//@Composable
//fun CircularSliderX(
//    originalValue: Long,
//    onValueChanged: (Long) -> Unit
//) {
//    val draggerDp = 16.dp
//    val draggerPosition = remember { mutableStateOf(Offset(0f, 0f)) }
//    val isInitialPositionSet = remember { mutableStateOf(false) }
//    val center = remember { mutableStateOf(Offset(0f, 0f)) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .layout { measurable, constraints ->
//                val placeable = measurable.measure(constraints)
//                layout(placeable.width, placeable.height) {
//                    placeable.placeRelative(0, 0)
//                    center.value = Offset(placeable.width / 2f, placeable.height / 2f)
//                }
//            }
//            .pointerInput(Unit) {
//                detectDragGestures { change, _ ->
//                    val local = change.position
//                    val bounds = Offset(size.width / 2f, size.height / 2f)
//
//                    // This is the angle between the X axis and the line from 0,0 to x,y
//                    val originalAngle = atan2(local.y - bounds.y, local.x - bounds.x).toDouble()
//
//                    // Rotate by +π to make the 0 value appear at the top (like a clock)
//                    val adjustedAngle = originalAngle + (Math.PI / 2)
//
//                    // Normalize angle (currently at [-π, π] range) to [0, 2π] range
//                    val normalizedAngle = if (adjustedAngle < 0) adjustedAngle + 2 * Math.PI else adjustedAngle
//
//                    // Scale the angle to [0, 59] range (to represent minutes/seconds)
//                    val scaledValue = ((normalizedAngle / (2 * Math.PI)) * 60).toLong()
//
//                    val halfDraggerRadius = draggerDp.toPx()
//                    val radius = (size.width / 2f) - halfDraggerRadius
//                    val x = bounds.x + radius * cos(originalAngle)
//                    val y = bounds.y + radius * sin(originalAngle)
//                    draggerPosition.value = Offset(x.toFloat(), y.toFloat())
//                    onValueChanged(scaledValue)
//                }
//            }
//    ) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val radius = (size.width / 2) - (draggerDp.toPx() / 2)
//            if (!isInitialPositionSet.value) {
//                draggerPosition.value = valueToPosition(originalValue, center.value, ((size.width / 2f) - (draggerDp.toPx() / 2)))
//                isInitialPositionSet.value = true
//            }
//            drawCircle(
//                center = Offset(size.width / 2, size.height / 2),
//                radius = radius,
//                color = Color.Gray,
//                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
//            )
//
//            drawCircle(
//                center = draggerPosition.value,
//                radius = draggerDp.toPx(),
//                color = Color.Blue
//            )
//        }
//    }
//}