package com.example.timetwist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Text

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditScreen(timerId: String?) {
    val timerViewModel: TimerViewModel = viewModel()

    // Get the timer based on timerId to edit. For now, just assuming timer0
    val timer = when (timerId) {
        "timer0" -> timerViewModel.timer0.value
        "timer1" -> timerViewModel.timer1.value
        "timer2" -> timerViewModel.timer2.value
        else -> throw IllegalArgumentException("Invalid timerId")
    }

    // The state for the minutes and seconds text fields
    var minutes by remember { mutableFloatStateOf((timer.durationMillis / 60000).toFloat()) }
    var seconds by remember { mutableFloatStateOf(((timer.durationMillis % 60000) / 1000).toFloat()) }

    // The state for focused field
    var focusedField by remember { mutableStateOf(FocusedField.NONE) }

    Column {
        Text("Edit Timer", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Box(
            modifier = Modifier
                .clickable { focusedField = FocusedField.MINUTES }
                .background(if (focusedField == FocusedField.MINUTES) Color.Gray else Color.Transparent)
                .padding(16.dp)
        ) {
            Text("$minutes : ", fontSize = 20.sp)
        }

        Box(
            modifier = Modifier
                .clickable { focusedField = FocusedField.SECONDS }
                .background(if (focusedField == FocusedField.SECONDS) Color.Gray else Color.Transparent)
                .padding(16.dp)
        ) {
            Text("$seconds", fontSize = 20.sp)
        }
    }

    if (focusedField != FocusedField.NONE) {
        CircularSlider() { newValue ->
            if (focusedField == FocusedField.SECONDS) {
                minutes = newValue
            } else if (focusedField == FocusedField.MINUTES) {
                seconds = newValue
            }
        }
    }
}
