package com.example.timetwist.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun valueToPosition(value: Long, center: Offset, radius: Float): Offset {
    // Scale back to [0, 2π] range
    val angle = ((value.toDouble() / 60.0) * 2 * Math.PI)

    // Rotate by -π/2 to orient like a clock
    val adjustedAngle = angle - (Math.PI / 2.0)

    // Compute the x and y coordinates
    val x = center.x + radius * cos(adjustedAngle)
    val y = center.y + radius * sin(adjustedAngle)

    return Offset(x.toFloat(), y.toFloat())
}


@Composable
fun CircularSlider(
    originalValue: Long,
    onValueChanged: (Long) -> Unit
) {
    val draggerDp = 16.dp
    val draggerPosition = remember { mutableStateOf(Offset(0f, 0f)) }
    val center = remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(0, 0)
                    center.value = Offset(placeable.width / 2f, placeable.height / 2f)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val local = change.position
                    val bounds = Offset(size.width / 2f, size.height / 2f)

                    // This is the angle between the X axis and the line from 0,0 to x,y
                    var originalAngle = atan2(local.y - bounds.y, local.x - bounds.x).toDouble()

                    // Rotate by +π to make the 0 value appear at the top (like a clock)
                    var adjustedAngle = originalAngle + (Math.PI / 2)

                    // Normalize angle (currently at [-π, π] range) to [0, 2π] range
                    val normalizedAngle = if (adjustedAngle < 0) adjustedAngle + 2 * Math.PI else adjustedAngle

                    // Scale the angle to [0, 59] range (to represent minutes/seconds)
                    val scaledValue = ((normalizedAngle / (2 * Math.PI)) * 60).toLong()

                    val halfDraggerRadius = draggerDp.toPx()
                    val radius = (size.width / 2f) - halfDraggerRadius
                    val x = bounds.x + radius * cos(originalAngle)
                    val y = bounds.y + radius * sin(originalAngle)
                    draggerPosition.value = Offset(x.toFloat(), y.toFloat())
                    onValueChanged(scaledValue)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.width / 2) - (draggerDp.toPx() / 2)

            drawCircle(
                center = Offset(size.width / 2, size.height / 2),
                radius = radius,
                color = Color.Gray,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            drawCircle(
                center = draggerPosition.value,
                radius = draggerDp.toPx(),
                color = Color.Blue
            )
        }
    }
}