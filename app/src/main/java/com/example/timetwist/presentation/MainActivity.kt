package com.example.timetwist.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.timetwist.presentation.theme.TimeTwistTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.wear.compose.material.Button
import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.wear.compose.material.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

val buttonPadding = 4.dp
val googleYellow = Color(0xFFFFEB3B)
val googleRed = Color(0xFFF44336)
val googleGreen = Color(0xFF4CAF50)
val googleBlue = Color(0xFF2196F3)
fun muteColor(originalColor: Color, factor: Float): Color {
    return lerp(originalColor, Color.Gray, factor)
}

const val muteFactor = 0.4f
val mutedGoogleYellow = muteColor(Color(0xFFFFEB3B), muteFactor)
val mutedGoogleRed = muteColor(Color(0xFFF44336), muteFactor)
val mutedGoogleGreen = muteColor(Color(0xFF4CAF50), muteFactor)
val mutedGoogleBlue = muteColor(Color(0xFF2196F3), muteFactor)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val timerViewModel: TimerViewModel = viewModel()
            val navController = rememberNavController()
            NavHost(navController, startDestination = "main") {
                composable("main") {
                    WearApp(this@MainActivity, navController, timerViewModel)
                }
                composable("edit/{timerId}") { backStackEntry ->
                    val timerId = backStackEntry.arguments?.getString("timerId")
                    EditScreen(timerId, navController, timerViewModel)
                }
            }
        }
    }
}

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
                        backgroundColor = if (inEditMode) mutedGoogleYellow else googleYellow
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
                Button(
                    onClick = {
                        if (inEditMode) {
                            navController.navigate("edit/timer0")
                        } else {
                            timerViewModel.startTimerStopOthers("timer0")
                            timerViewModel.toggleService(context, coroutineScope)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (inEditMode) mutedGoogleRed else googleRed
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = buttonPadding, bottom = buttonPadding)
                        .weight(1f),
                    shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
                ) {
                    val repeating = if(timerViewModel.timer0.value.repeating) "↻" else ""
                    val remaining =
                        if (timerViewModel.timer0.value.started) timerViewModel.timer0.value.timeRemaining
                        else (timerViewModel.timer0.value.durationMillis)
                    Text(
                        text = "${remaining.getTime()} $repeating",
                        modifier = Modifier.offset(x = -buttonPadding, y = buttonPadding * 3),
                        color = Color.Black,
                    )
                }
            }
            Row(
                // 2
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Button(
                    onClick = {
                        if (inEditMode) {
                            navController.navigate("edit/timer1")
                        } else {
                            timerViewModel.startTimerStopOthers("timer1")
                            timerViewModel.toggleService(context, coroutineScope)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (inEditMode) mutedGoogleGreen else googleGreen
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = buttonPadding, top = buttonPadding)
                        .weight(1f),
                    shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))

                ) {
                    val repeating = if(timerViewModel.timer1.value.repeating) "↻" else ""
                    val remaining =
                        if (timerViewModel.timer1.value.started) timerViewModel.timer1.value.timeRemaining
                        else (timerViewModel.timer1.value.durationMillis)
                    Text(
                        text = "${remaining.getTime()} $repeating",
                        modifier = Modifier.offset(x = buttonPadding, y = -buttonPadding * 3),
                        color = Color.Black,
                    )
                }
                Button(
                    onClick = {
                        if (inEditMode) {
                            navController.navigate("edit/timer2")
                        } else {
                            timerViewModel.startTimerStopOthers("timer2")
                            timerViewModel.toggleService(context, coroutineScope)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (inEditMode) mutedGoogleBlue else googleBlue
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = buttonPadding, top = buttonPadding)
                        .weight(1f),
                    shape = MaterialTheme.shapes.large.copy(CornerSize(0.dp))
                ) {
                    val repeating = if(timerViewModel.timer2.value.repeating) "↻" else ""
                    val remaining =
                        if (timerViewModel.timer2.value.started) timerViewModel.timer2.value.timeRemaining
                        else (timerViewModel.timer2.value.durationMillis)
                    Text(
                        text = "${remaining.getTime()} $repeating",
                        modifier = Modifier.offset(x = -buttonPadding, y = -buttonPadding * 3),
                        color = Color.Black,
                    )
                }
            }
        }
    }
}

