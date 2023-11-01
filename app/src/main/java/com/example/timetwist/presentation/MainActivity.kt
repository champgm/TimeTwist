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
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.wear.compose.material.ButtonDefaults
import com.example.timetwist.service.CountdownService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "main") {
                composable("main") {
                    WearApp(this@MainActivity, navController)
                }
                composable("edit/{timerId}") { backStackEntry ->
                    val timerId = backStackEntry.arguments?.getString("timerId")
                    EditScreen(timerId, navController)
                }
            }
        }
    }
}

@Composable
fun WearApp(context: Context, navController: NavController) {
    val timerViewModel: TimerViewModel = viewModel()

    TimeTwistTheme {
        var inEditMode by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val coroutineScope = rememberCoroutineScope()
            Row( // 1
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        inEditMode = !inEditMode
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (inEditMode) Color.Gray else MaterialTheme.colors.primary
                    )
                ) {
                    Text(text = if (inEditMode) "Done" else "Edit")
                }
                Button(
                    onClick = {
                        if (inEditMode) {
                            navController.navigate("edit/timer0")
                        } else {
                            timerViewModel.startTimerStopOthers("timer0")
                            timerViewModel.toggleService(context,coroutineScope)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (inEditMode) Color.Gray else MaterialTheme.colors.primary
                    )
                ) {
                    val remaining =
                        if (timerViewModel.timer0.value.started) timerViewModel.timer0.value.timeRemaining
                        else (timerViewModel.timer0.value.durationMillis)
                    Text(text = "${remaining.getTime()}")
                }
            }
            Row( // 2
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (inEditMode) {
                            navController.navigate("edit/timer1")
                        } else {
                            timerViewModel.startTimerStopOthers("timer1")
                            timerViewModel.toggleService(context,coroutineScope)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (inEditMode) Color.Gray else MaterialTheme.colors.primary
                    )
                ) {
                    val remaining =
                        if (timerViewModel.timer1.value.started) timerViewModel.timer1.value.timeRemaining
                        else (timerViewModel.timer1.value.durationMillis)
                    Text(text = "${remaining.getTime()}")
                }
                Button(
                    onClick = {
                        if (inEditMode) {
                            navController.navigate("edit/timer2")
                        } else {
                            timerViewModel.startTimerStopOthers("timer2")
                            timerViewModel.toggleService(context,coroutineScope)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (inEditMode) Color.Gray else MaterialTheme.colors.primary
                    )
                ) {
                    val remaining =
                        if (timerViewModel.timer2.value.started) timerViewModel.timer2.value.timeRemaining
                        else (timerViewModel.timer2.value.durationMillis)
                    Text(text = "${remaining.getTime()}")
                }
            }
        }
    }
}

