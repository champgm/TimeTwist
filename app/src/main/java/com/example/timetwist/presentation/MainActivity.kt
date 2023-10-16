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
        val maxCounter = 30
        var counter by remember { mutableStateOf(maxCounter) }
        var started by remember { mutableStateOf(false) }

        LaunchedEffect(started) {
            while (started) {
                if (counter > 0) {
                    delay(1000)  // Wait for 1 second
                    counter -= 1 // Decrement counter
//                    Log.e("Counter", "count is $counter")
                } else {
                    counter = maxCounter  // Reset the counter
                    started = false
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(textAlign = TextAlign.Center, text = "counter value: $counter")
            val coroutineScope = rememberCoroutineScope()
            Button(
                onClick = {
                    started = !started
                    if (started) {
                        Log.e("Button", "Starting service...")
                        coroutineScope.launch {
                            // Start your foreground service here
                            val intent = Intent(context, CountdownService::class.java)
                            intent.putExtra("durationMillis", 30000L) // 5 seconds
                            context.startService(intent)
                        }
                    } else {
                        // Stop the service?
                        Log.e("Button", "Stopping service...")
                        val intent = Intent(context, CountdownService::class.java)
                        context.stopService(intent)
                    }
                }) {
                Text(text = "button")
            }
        }
    }
}
