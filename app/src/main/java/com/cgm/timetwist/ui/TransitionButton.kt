package com.cgm.timetwist.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import com.cgm.timetwist.presentation.TransitionState0To2
import com.cgm.timetwist.presentation.TransitionState1To2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TransitionButton0To2(
    state: TransitionState0To2,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp,
    backgroundColor: Color,
    lineColor: Color,
    borderColor: Color = lineColor,
    lineWidth: Dp,
) {
    TransitionButtonFrame(
        modifier = modifier,
        buttonSize = buttonSize,
        backgroundColor = backgroundColor,
        onClick = onClick,
    ) {
        val strokeWidth = lineWidth.toPx()
        drawCircle(
            color = borderColor,
            radius = size.minDimension / 2f - strokeWidth / 2f,
            style = Stroke(width = strokeWidth),
        )
        when (state) {
            TransitionState0To2.DEFAULT -> drawLine(
                color = lineColor,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            TransitionState0To2.ZERO_TO_TWO -> drawVerticalArrow(lineColor, strokeWidth, ArrowDirection.DOWN)
            TransitionState0To2.TWO_TO_ZERO -> drawVerticalArrow(lineColor, strokeWidth, ArrowDirection.UP)
            TransitionState0To2.ZERO_TWO_REPEAT -> {
                drawVerticalArrow(lineColor, strokeWidth, ArrowDirection.UP)
                drawVerticalArrow(lineColor, strokeWidth, ArrowDirection.DOWN)
            }
        }
    }
}

@Composable
fun TransitionButton1To2(
    state: TransitionState1To2,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp,
    backgroundColor: Color,
    lineColor: Color,
    borderColor: Color = lineColor,
    lineWidth: Dp,
) {
    TransitionButtonFrame(
        modifier = modifier,
        buttonSize = buttonSize,
        backgroundColor = backgroundColor,
        onClick = onClick,
    ) {
        val strokeWidth = lineWidth.toPx()
        drawCircle(
            color = borderColor,
            radius = size.minDimension / 2f - strokeWidth / 2f,
            style = Stroke(width = strokeWidth),
        )
        when (state) {
            TransitionState1To2.DEFAULT -> drawLine(
                color = lineColor,
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
            TransitionState1To2.ONE_TO_TWO -> drawHorizontalArrow(lineColor, strokeWidth, ArrowDirection.LEFT)
            TransitionState1To2.TWO_TO_ONE -> drawHorizontalArrow(lineColor, strokeWidth, ArrowDirection.RIGHT)
            TransitionState1To2.ONE_TWO_REPEAT -> {
                drawHorizontalArrow(lineColor, strokeWidth, ArrowDirection.LEFT)
                drawHorizontalArrow(lineColor, strokeWidth, ArrowDirection.RIGHT)
            }
        }
    }
}

@Composable
private fun TransitionButtonFrame(
    modifier: Modifier,
    buttonSize: Dp,
    backgroundColor: Color,
    onClick: () -> Unit,
    content: DrawScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(backgroundColor = backgroundColor),
    ) {
        Canvas(modifier = Modifier.size(buttonSize)) {
            content()
        }
    }
}

private enum class ArrowDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
}

private fun DrawScope.drawVerticalArrow(
    color: Color,
    strokeWidth: Float,
    direction: ArrowDirection,
) {
    val start = Offset(center.x, size.height * 0.2f)
    val end = Offset(center.x, size.height * 0.8f)
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    val tip = if (direction == ArrowDirection.UP) start else end
    val angle = if (direction == ArrowDirection.UP) -PI / 2 else PI / 2
    drawArrowHead(color = color, tip = tip, angleRadians = angle, strokeWidth = strokeWidth)
}

private fun DrawScope.drawHorizontalArrow(
    color: Color,
    strokeWidth: Float,
    direction: ArrowDirection,
) {
    val start = Offset(size.width * 0.2f, center.y)
    val end = Offset(size.width * 0.8f, center.y)
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    val tip = if (direction == ArrowDirection.LEFT) start else end
    val angle = if (direction == ArrowDirection.LEFT) PI else 0.0
    drawArrowHead(color = color, tip = tip, angleRadians = angle, strokeWidth = strokeWidth)
}

private fun DrawScope.drawArrowHead(
    color: Color,
    tip: Offset,
    angleRadians: Double,
    strokeWidth: Float,
) {
    val arrowLength = size.minDimension * 0.16f
    val spread = PI / 6
    val branchA = tip - polarOffset(arrowLength, angleRadians - spread)
    val branchB = tip - polarOffset(arrowLength, angleRadians + spread)
    drawLine(color = color, start = tip, end = branchA, strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color = color, start = tip, end = branchB, strokeWidth = strokeWidth, cap = StrokeCap.Round)
}

private fun polarOffset(length: Float, angleRadians: Double): Offset {
    return Offset(
        x = (cos(angleRadians) * length).toFloat(),
        y = (sin(angleRadians) * length).toFloat(),
    )
}
