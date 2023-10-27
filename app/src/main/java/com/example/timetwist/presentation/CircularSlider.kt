package com.example.timetwist.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularSlider(
    onValueChanged: (Float) -> Unit
) {
    val pointerPosition = remember { mutableStateOf(Offset(0f, 0f)) }
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
                    val angle = atan2(local.y - bounds.y, local.x - bounds.x)
                    val radius = size.width / 2f // Or compute differently as you need
                    val x = bounds.x + radius * cos(angle)
                    val y = bounds.y + radius * sin(angle)
                    pointerPosition.value = Offset(x, y)
                    onValueChanged(angle)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.width / 2 // Or compute differently as you need

            drawCircle(
                center = Offset(size.width / 2, size.height / 2),
                radius = radius,
                color = Color.Gray,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            drawCircle(
                center = pointerPosition.value,
                radius = 8.dp.toPx(),
                color = Color.Blue
            )
        }
    }
}



