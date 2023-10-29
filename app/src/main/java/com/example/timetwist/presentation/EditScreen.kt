package com.example.timetwist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditScreen(timerId: String?, navController: NavController) {
    val fontSize = 20.sp;
    val timerViewModel: TimerViewModel = viewModel()

    // Get the timer based on timerId to edit. For now, just assuming timer0
    val timer = when (timerId) {
        "timer0" -> timerViewModel.timer0.value
        "timer1" -> timerViewModel.timer1.value
        "timer2" -> timerViewModel.timer2.value
        else -> throw IllegalArgumentException("Invalid timerId")
    }

    // The state for the minutes and seconds text fields
    var minutes by remember { mutableLongStateOf((timer.durationMillis / 60000L)) }
    var seconds by remember { mutableLongStateOf(((timer.durationMillis % 60000L) / 1000L)) }

    // The state for focused field
    var focusedField by remember { mutableStateOf(FocusedField.NONE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Edit Timer", fontSize = fontSize, fontWeight = FontWeight.Bold)
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clickable { focusedField = FocusedField.MINUTES }
                    .background(if (focusedField == FocusedField.MINUTES) Color.Gray else Color.Transparent)
                    .padding(16.dp)
            ) {
                Text("$minutes", fontSize = fontSize)
            }
            Text(":", fontSize = 20.sp)
            Box(
                modifier = Modifier
                    .clickable { focusedField = FocusedField.SECONDS }
                    .background(if (focusedField == FocusedField.SECONDS) Color.Gray else Color.Transparent)
                    .padding(16.dp)
            ) {
                Text("$seconds", fontSize = fontSize)
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text(text = "Done")
            }
        }
    }

    if (focusedField == FocusedField.SECONDS) {
        CircularSlider(seconds) { newValue ->
            seconds = newValue
        }
    } else if (focusedField == FocusedField.MINUTES) {
        CircularSlider(minutes) { newValue ->
            minutes = newValue
        }
    }
}
