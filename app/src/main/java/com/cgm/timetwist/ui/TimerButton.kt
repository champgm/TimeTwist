package com.cgm.timetwist.ui

import android.content.Context
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.cgm.timetwist.presentation.TimerViewModel
import com.cgm.timetwist.service.getTime
import com.cgm.timetwist.service.TimeDetails
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.*

@Composable
fun TimerButton(
    inEditMode: Boolean,
    navController: NavController,
    timerViewModel: TimerViewModel,
    timerState: MutableState<TimeDetails>,
    timerId: String,
    context: Context,
    coroutineScope: CoroutineScope,
    buttonModifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    color: Color,
    editModeColor: Color
) {
    Button(
        onClick = {
            if (inEditMode) {
                navController.navigate("edit/$timerId")
            } else if (timerState.value.started) {
                timerViewModel.stopTimers(context)
            } else {
                timerViewModel.startTimer(timerId, context, coroutineScope)
            }
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (inEditMode) editModeColor else color
        ),
        modifier = buttonModifier,
        shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
    ) {
        val repeating = if (timerState.value.repeating) "↻" else ""
        val remaining =
            if (timerState.value.started) timerState.value.timeRemaining
            else (timerState.value.durationMillis)
        Text(
            text = "${remaining.getTime()} $repeating",
            modifier = textModifier,
            color = Color.Black,
        )
    }

}
