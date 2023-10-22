package com.example.timetwist.presentation

import android.annotation.SuppressLint
import android.annotation.TargetApi
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
import androidx.compose.ui.text.style.TextAlign
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
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.rememberCoroutineScope
import com.example.timetwist.service.CountdownService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp("Android", this@MainActivity)
        }
    }
}


@Composable
fun WearApp(greetingName: String, context: Context) {
    TimeTwistTheme {
        var started by remember { mutableStateOf(false) }
        var durationMillis by remember { mutableStateOf(30000L) }
        var startTime by remember { mutableStateOf(0L) }
        var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
        var elapsedTime by remember { mutableStateOf(currentTime - startTime) }
        var timeRemaining by remember { mutableStateOf(durationMillis - elapsedTime) }
        var secondsRemaining by remember { mutableStateOf(durationMillis.toInt()) }

        fun updateTimes() {
            currentTime = System.currentTimeMillis()
            elapsedTime = currentTime - startTime
            timeRemaining = durationMillis - elapsedTime
            secondsRemaining = (timeRemaining / 1000L).toInt()
            // Log.e("updateTimes", "===========================")
            // Log.e("updateTimes", "startTime: $startTime")
            // Log.e("updateTimes", "currentTime: $currentTime")
            // Log.e("updateTimes", "elapsedTime: $elapsedTime")
            // Log.e("updateTimes", "timeRemaining: $timeRemaining")
            // Log.e("updateTimes", "secondsDisplay: $secondsDisplay")
            // Log.e("updateTimes", "===========================")
        }


        LaunchedEffect(started) {
            updateTimes()
            while (started) {
                updateTimes()
                // Log.e("DisplayLoop", "secondsDisplay: $secondsDisplay, $timeRemaining")
                if (timeRemaining <= 0L) {
                    started = false  // Wait for 1 second
                }
                delay(1000L)
            }
        }

        fun toggleService(coroutineScope: CoroutineScope) {
            started = !started
            if (started) {
                Log.e("TimeTwist::UI", "Starting service...")
                startTime = System.currentTimeMillis()
                coroutineScope.launch {
                    val intent = Intent(context, CountdownService::class.java)
                    updateTimes()
                    intent.putExtra("startTime", startTime)
                    intent.putExtra("durationMillis", durationMillis)
                    context.startService(intent)
                }
            } else {
                Log.e("TimeTwist::UI", "Stopping service...")
                val intent = Intent(context, CountdownService::class.java)
                context.stopService(intent)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val secondsDisplay = if (started) secondsRemaining else (durationMillis / 1000)
            Text(textAlign = TextAlign.Center, text = "secondsDisplay: $secondsDisplay")
            val coroutineScope = rememberCoroutineScope()
            Button(onClick = { toggleService(coroutineScope) }) {
                Text(text = "button")
            }
        }
    }
}
