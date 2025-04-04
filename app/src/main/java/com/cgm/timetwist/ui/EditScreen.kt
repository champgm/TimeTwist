package com.cgm.timetwist.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.cgm.timetwist.presentation.TimerViewModel
import com.cgm.timetwist.presentation.googleBlue
import com.cgm.timetwist.presentation.googleGreen
import com.cgm.timetwist.presentation.googleYellow

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditScreen(timerId: String, navController: NavController, timerViewModel: TimerViewModel) {
    val fontSize = 20.sp;

    val timer = timerViewModel.getTimer(timerId)
    val trackColor = when (timerId) {
        "timer0" -> googleBlue
        "timer1" -> googleYellow
        "timer2" -> googleGreen
        else -> throw IllegalArgumentException("Invalid timerId")
    }

    // The state for the minutes and seconds text fields
    var minutes by remember { mutableLongStateOf((timer.durationMillis / 60000L)) }
    val minutesString = if (minutes < 10) "0$minutes" else "$minutes"
    var seconds by remember { mutableLongStateOf(((timer.durationMillis % 60000L) / 1000L)) }
    val secondsString = if (seconds < 10) "0$seconds" else "$seconds"
    var repeating by remember { mutableStateOf(timer.repeating) }
    var vibration by remember { mutableStateOf(timer.vibration) }
    var intervalStuff by remember { mutableStateOf(timer.intervalStuff) }
    var sound by remember { mutableStateOf(timer.sound) }

    // The state for focused field
    var focusedField by remember { mutableStateOf(FocusedField.SECONDS) }
    val focusRequester: FocusRequester = remember { FocusRequester() }
    val selectedColumn by remember { mutableIntStateOf(0) }
    LaunchedEffect(selectedColumn) {
        listOf(focusRequester)[selectedColumn]
            .requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Top row with buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Button(
                    onClick = { repeating = !repeating },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = "↻",
                        color = if (repeating) Color.Green else Color.White,
                        fontSize = 20.sp
                    )
                }
                Button(
                    onClick = { sound = !sound },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = if (sound) "🔊" else "🔇",
                        fontSize = 20.sp
                    )
                }
                Button(
                    onClick = { vibration = !vibration },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = if (vibration) "📳" else "📴",
                        fontSize = 20.sp
                    )
                }
                Button(
                    onClick = { intervalStuff = !intervalStuff },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black),
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = "∴",
                        color = if (intervalStuff) Color.Green else Color.Gray,
                        fontSize = 20.sp
                    )
                }
            }

            // Center row with time
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clickable { focusedField = FocusedField.MINUTES }
                        .background(
                            color = if (focusedField == FocusedField.MINUTES) Color.DarkGray else Color.Transparent,
                            shape = RoundedCornerShape(24.dp),
                        )
                        .padding(12.dp)
                ) {
                    Text(minutesString, fontSize = fontSize)
                }
                Text(" ∶ ", fontSize = 20.sp)
                Box(
                    modifier = Modifier
                        .clickable { focusedField = FocusedField.SECONDS }
                        .background(
                            color = if (focusedField == FocusedField.SECONDS) Color.DarkGray else Color.Transparent,
                            shape = RoundedCornerShape(24.dp),
                        )
                        .focusRequester(focusRequester)
                        .focusable()
                        .padding(12.dp)
                        .onRotaryScrollEvent {
                            Log.e("Rotary", "Got rotary scroll ${it.verticalScrollPixels}")
                            if (focusedField == FocusedField.SECONDS) {
                                seconds += it.verticalScrollPixels.toLong()
                            } else if (focusedField == FocusedField.MINUTES) {
                                minutes += it.verticalScrollPixels.toLong()
                            }
                            true
                        },
                ) {
                    Text(secondsString, fontSize = fontSize)
                }
            }

            // Bottom row with action buttons
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .size(width = 36.dp, height = 36.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                bottomStart = 24.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp,
                            )
                        )
                ) {
                    Text(text = " ✗", color = Color.Black)
                }
                Button(
                    onClick = {
                        val newDurationMillis = minutes * 60000L + seconds * 1000L
                        timerViewModel.updateTimerDuration(
                            timerId,
                            newDurationMillis,
                            repeating,
                            sound,
                            vibration,
                            intervalStuff
                        )
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = trackColor),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .size(width = 36.dp, height = 36.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                bottomStart = 0.dp,
                                topEnd = 24.dp,
                                bottomEnd = 24.dp,
                            )
                        )
                ) {
                    Text(text = "✓", color = Color.Black)
                }
            }
        }
    }

    if (focusedField == FocusedField.SECONDS) {
        CircularSlider(seconds.toDouble(), trackColor) { newValue ->
            seconds = newValue.toLong()
        }
    } else if (focusedField == FocusedField.MINUTES) {
        CircularSlider(minutes.toDouble(), trackColor) { newValue ->
            minutes = newValue.toLong()
        }
    }
}
