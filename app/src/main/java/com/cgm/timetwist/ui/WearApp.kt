package com.cgm.timetwist.ui

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
import com.cgm.timetwist.presentation.TimerViewModel
import com.cgm.timetwist.presentation.buttonPadding
import com.cgm.timetwist.presentation.googleBlue
import com.cgm.timetwist.presentation.googleGreen
import com.cgm.timetwist.presentation.googleRed
import com.cgm.timetwist.presentation.googleYellow
import com.cgm.timetwist.presentation.mutedGoogleBlue
import com.cgm.timetwist.presentation.mutedGoogleGreen
import com.cgm.timetwist.presentation.mutedGoogleRed
import com.cgm.timetwist.presentation.mutedGoogleYellow
import com.cgm.timetwist.presentation.theme.TimeTwistTheme
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
                    editModeColor = mutedGoogleBlue,
                    color = googleBlue,
                    inEditMode = inEditMode,
                    navController = navController,
                    timerViewModel = timerViewModel,
                    timerState = timerViewModel.timer0,
                    timerId = "timer0",
                    context = context,
                    coroutineScope = coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(start = buttonPadding, bottom = buttonPadding)
                        .weight(1f),
                    textModifier = Modifier.offset(x = -buttonPadding, y = buttonPadding * 3),
                )
            }
            Row(
                // 2
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                TimerButton(
                    editModeColor = mutedGoogleYellow,
                    color = googleYellow,
                    inEditMode = inEditMode,
                    navController = navController,
                    timerViewModel = timerViewModel,
                    timerState = timerViewModel.timer1,
                    timerId = "timer1",
                    context = context,
                    coroutineScope = coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(end = buttonPadding, top = buttonPadding)
                        .weight(1f),
                    textModifier = Modifier.offset(x = buttonPadding, y = -buttonPadding * 3),
                )
                TimerButton(
                    editModeColor = mutedGoogleGreen,
                    color = googleGreen,
                    inEditMode = inEditMode,
                    navController = navController,
                    timerViewModel = timerViewModel,
                    timerState = timerViewModel.timer2,
                    timerId = "timer2",
                    context = context,
                    coroutineScope = coroutineScope,
                    buttonModifier = Modifier
                        .fillMaxSize()
                        .padding(start = buttonPadding, top = buttonPadding)
                        .weight(1f),
                    textModifier = Modifier.offset(x = -buttonPadding, y = -buttonPadding * 3),
                )
            }
        }
    }
}
