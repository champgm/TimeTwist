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
import androidx.compose.runtime.rememberCoroutineScope
import com.example.timetwist.service.CountdownService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(this@MainActivity)
        }
    }
}

data class TimeDetails(
    var durationMillis: Long = 0,
    var startTime: Long = 0,
    var elapsedTime: Long = 0,
    var timeRemaining: Long = 0,
    var secondsRemaining: Int = 0,
    var started: Boolean = false,
) {
    fun updateTimes(): TimeDetails {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - startTime
        val timeRemaining = durationMillis - elapsedTime
        val secondsRemaining = if (started) (timeRemaining / 1000L).toInt() else (durationMillis / 1000).toInt()

        return this.copy(
            durationMillis = durationMillis,
            startTime = startTime,
            elapsedTime = elapsedTime,
            timeRemaining = timeRemaining,
            secondsRemaining = secondsRemaining,
            started = started,
        )
    }

    fun stop(): TimeDetails {
        return this.copy(
            durationMillis = durationMillis,
            startTime = startTime,
            elapsedTime = elapsedTime,
            timeRemaining = timeRemaining,
            secondsRemaining = secondsRemaining,
            started = false,
        )
    }

    fun start(): TimeDetails {
        return this.copy(
            durationMillis = durationMillis,
            startTime = startTime,
            elapsedTime = elapsedTime,
            timeRemaining = timeRemaining,
            secondsRemaining = secondsRemaining,
            started = true,
        )
    }
}


@Composable
fun WearApp(context: Context) {
    val sharedPreferences = context.getSharedPreferences("TimeTwist_Settings", Context.MODE_PRIVATE)

    TimeTwistTheme {
        var started by remember { mutableStateOf(false) }
        var timer0 by remember { mutableStateOf(TimeDetails(sharedPreferences.getLong("timer0_durationMillis", 30000L))) }
        var timer1 by remember { mutableStateOf(TimeDetails(sharedPreferences.getLong("timer1_durationMillis", 300000L))) }
        var timer2 by remember { mutableStateOf(TimeDetails(sharedPreferences.getLong("timer2_durationMillis", 60000L))) }

//        fun updateTimes() {
//            if (timer0.started) timer0 = timer0.updateTimes()
//            if (timer1.started) timer1 = timer1.updateTimes()
//            if (timer2.started) timer2 = timer2.updateTimes()
//        }

//        LaunchedEffect(started) {
//            updateTimes()
//            while (started) {
//                updateTimes()
//                if (timer0.started && timer0.timeRemaining <= 0L) timer0 = timer0.stop();
//                if (timer1.started && timer1.timeRemaining <= 0L) timer1 = timer1.stop();
//                if (timer2.started && timer2.timeRemaining <= 0L) timer2 = timer2.stop();
//                delay(1000L)
//            }
//        }

        fun updateTimer(timer: TimeDetails): TimeDetails {
            return if (timer.started) {
                timer.updateTimes()
            } else {
                timer
            }
        }

        LaunchedEffect(Unit) { // key is Unit to make sure this effect is only launched once
            while (true) {
                timer0 = updateTimer(timer0)
                timer1 = updateTimer(timer1)
                timer2 = updateTimer(timer2)

                if (timer0.started && timer0.timeRemaining <= 0L) timer0 = timer0.stop()
                if (timer1.started && timer1.timeRemaining <= 0L) timer1 = timer1.stop()
                if (timer2.started && timer2.timeRemaining <= 0L) timer2 = timer2.stop()

                delay(1000L)
            }
        }


        fun startService(coroutineScope: CoroutineScope, durationMillis: Long) {
            Log.e("TimeTwist::UI", "Starting service...")
            val startTime = System.currentTimeMillis()
            coroutineScope.launch {
                val intent = Intent(context, CountdownService::class.java)
                updateTimes()
                intent.putExtra("startTime", startTime)
                intent.putExtra("durationMillis", durationMillis)
                context.startService(intent)
            }
        }

        fun toggleService(coroutineScope: CoroutineScope) {
            Log.e("TimeTwist::UI", "Stopping countdown service if one is running...")
            val intent = Intent(context, CountdownService::class.java)
            context.stopService(intent)

            if (timer0.started) {
                startService(coroutineScope, timer0.durationMillis)
            } else if (timer1.started) {
                startService(coroutineScope, timer1.durationMillis)
            } else if (timer2.started) {
                startService(coroutineScope, timer2.durationMillis)
            }
        }

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
                Button(onClick = {
                    toggleService(coroutineScope)
                }) {
                    Text(text = "Edit")
                }
                Button(onClick = {
                    timer0.started = !timer0.started
                    timer1.started = false
                    timer2.started = false
                    toggleService(coroutineScope)
                }) {
                    val secondsDisplay = if (started) timer0.secondsRemaining else (timer0.durationMillis / 1000)
                    Text(text = "$secondsDisplay")
                }
            }
            Row( // 2
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    timer0.started = false
                    timer1.started = !timer1.started
                    timer2.started = false
                    toggleService(coroutineScope)
                }) {
                    val secondsDisplay = if (started) timer1.secondsRemaining else (timer1.durationMillis / 1000)
                    Text(text = "$secondsDisplay")
                }
                Button(onClick = {
                    timer0.started = false
                    timer1.started = false
                    timer2.started = !timer2.started
                    toggleService(coroutineScope)
                }) {
                    val secondsDisplay = if (started) timer2.secondsRemaining else (timer2.durationMillis / 1000)
                    Text(text = "$secondsDisplay")
                }
            }
        }
    }
}
