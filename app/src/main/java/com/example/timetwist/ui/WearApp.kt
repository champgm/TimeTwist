package com.example.timetwist.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.timetwist.presentation.TimerViewModel
import com.example.timetwist.presentation.buttonPadding
import com.example.timetwist.presentation.getTime
import com.example.timetwist.presentation.googleBlue
import com.example.timetwist.presentation.googleGreen
import com.example.timetwist.presentation.googleRed
import com.example.timetwist.presentation.googleYellow
import com.example.timetwist.presentation.mutedGoogleBlue
import com.example.timetwist.presentation.mutedGoogleGreen
import com.example.timetwist.presentation.mutedGoogleRed
import com.example.timetwist.presentation.mutedGoogleYellow
import com.example.timetwist.presentation.theme.TimeTwistTheme
import androidx.compose.runtime.*

@Composable
fun WearApp(context: Context, navController: NavController, timerViewModel: TimerViewModel) {
    Log.d("WearApp", "TimerViewModel HashCode: ${timerViewModel.hashCode()}")

    TimeTwistTheme {
        var inEditMode by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val coroutineScope = rememberCoroutineScope()
            Row(
                // 1
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {

                Button(
                    onClick = {
                        timerViewModel.stopTimers(context)
                        inEditMode = !inEditMode
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (inEditMode) mutedGoogleRed else googleRed
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = buttonPadding, bottom = buttonPadding)
                        .weight(1f),
                    shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
                ) {
                    Text(
                        text = if (inEditMode) "Done" else "Edit",
                        modifier = Modifier.offset(x = buttonPadding, y = buttonPadding * 3),
                        color = Color.Black,
                    )
                }

                TimerButton(
                    inEditMode,
                    navController,
                    timerViewModel,
                    timerState = timerViewModel.timer0,
                    context,
                    coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(start = buttonPadding, bottom = buttonPadding)
                        .weight(1f),
                    textModifier = Modifier.offset(x = -buttonPadding, y = buttonPadding * 3),
                    editModeColor = mutedGoogleYellow,
                    color = googleYellow
                )
//                Button(
//                    onClick = {
//                        if (inEditMode) {
//                            navController.navigate("edit/timer0")
//                        } else if (timerViewModel.timer0.value.started) {
//                            timerViewModel.stopTimers(context)
//                        } else {
//                            timerViewModel.startTimer("timer0", context, coroutineScope)
//                        }
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = if (inEditMode) mutedGoogleRed else googleRed
//                    ),
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(start = buttonPadding, bottom = buttonPadding)
//                        .weight(1f),
//                    shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
//                ) {
//                    val repeating = if (timerViewModel.timer0.value.repeating) "↻" else ""
//                    val remaining =
//                        if (timerViewModel.timer0.value.started) timerViewModel.timer0.value.timeRemaining
//                        else (timerViewModel.timer0.value.durationMillis)
//                    Text(
//                        text = "${remaining.getTime()} $repeating",
//                        modifier = Modifier.offset(x = -buttonPadding, y = buttonPadding * 3),
//                        color = Color.Black,
//                    )
//                }
            }
            Row(
                // 2
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                TimerButton(
                    inEditMode,
                    navController,
                    timerViewModel,
                    timerState = timerViewModel.timer1,
                    context,
                    coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(end = buttonPadding, top = buttonPadding)
                        .weight(1f),
                    textModifier = Modifier.offset(x = buttonPadding, y = -buttonPadding * 3),
                    editModeColor = mutedGoogleGreen,
                    color = googleGreen
                )

//                Button(
//                    onClick = {
//                        if (inEditMode) {
//                            navController.navigate("edit/timer1")
//                        } else if (timerViewModel.timer1.value.started) {
//                            timerViewModel.stopTimers(context)
//                        } else {
//                            timerViewModel.startTimer("timer1", context, coroutineScope)
//                        }
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = if (inEditMode) mutedGoogleGreen else googleGreen
//                    ),
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(end = buttonPadding, top = buttonPadding)
//                        .weight(1f),
//                    shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
//
//                ) {
//                    val repeating = if (timerViewModel.timer1.value.repeating) "↻" else ""
//                    val remaining =
//                        if (timerViewModel.timer1.value.started) timerViewModel.timer1.value.timeRemaining
//                        else (timerViewModel.timer1.value.durationMillis)
//                    Text(
//                        text = "${remaining.getTime()} $repeating",
//                        modifier = Modifier.offset(x = buttonPadding, y = -buttonPadding * 3),
//                        color = Color.Black,
//                    )
//                }

                TimerButton(
                    inEditMode,
                    navController,
                    timerViewModel,
                    timerState = timerViewModel.timer2,
                    context,
                    coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(start = buttonPadding, top = buttonPadding)
                        .weight(1f),
                    textModifier =Modifier.offset(x = -buttonPadding, y = -buttonPadding * 3),
                    editModeColor = mutedGoogleBlue,
                    color = googleBlue
                )

//                Button(
//                    onClick = {
//                        if (inEditMode) {
//                            navController.navigate("edit/timer2")
//                        } else if (timerViewModel.timer2.value.started) {
//                            timerViewModel.stopTimers(context)
//                        } else {
//                            timerViewModel.startTimer("timer2", context, coroutineScope)
//                        }
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        backgroundColor = if (inEditMode) mutedGoogleBlue else googleBlue
//                    ),
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(start = buttonPadding, top = buttonPadding)
//                        .weight(1f),
//                    shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
//                ) {
//                    val repeating = if (timerViewModel.timer2.value.repeating) "↻" else ""
//                    val remaining =
//                        if (timerViewModel.timer2.value.started) timerViewModel.timer2.value.timeRemaining
//                        else (timerViewModel.timer2.value.durationMillis)
//                    Text(
//                        text = "${remaining.getTime()} $repeating",
//                        modifier = Modifier.offset(x = -buttonPadding, y = -buttonPadding * 3),
//                        color = Color.Black,
//                    )
//                }
            }
        }
    }
}